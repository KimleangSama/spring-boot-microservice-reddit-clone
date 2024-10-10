package com.kkimleang.authservice.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkimleang.authservice.config.security.TokenProvider;
import com.kkimleang.authservice.dto.AuthResponse;
import com.kkimleang.authservice.dto.LoginRequest;
import com.kkimleang.authservice.dto.SignUpRequest;
import com.kkimleang.authservice.enumeration.AuthProvider;
import com.kkimleang.authservice.exception.ResourceNotFoundException;
import com.kkimleang.authservice.model.Role;
import com.kkimleang.authservice.model.Token;
import com.kkimleang.authservice.model.User;
import com.kkimleang.authservice.qpayload.RegisterVerifyEmailDetails;
import com.kkimleang.authservice.repository.TokenRepository;
import com.kkimleang.authservice.repository.UserRepository;
import com.kkimleang.authservice.util.RandomString;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final TokenRepository tokenRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redis;

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.email.name}")
    private String emailExchange;

    @Value("${rabbitmq.binding.email.name}")
    private String emailRoutingKey;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public boolean existsByEmail(@NotBlank @Email String email) {
        return userRepository.existsByEmail(email);
    }

    public User createUser(SignUpRequest signUpRequest) {
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());

        // Setup roles following the request, if empty role, set default role to ROLE_USER
        if (signUpRequest.getRoles().isEmpty()) {
            Role userRole = roleService.findByName("ROLE_USER");
            user.getRoles().add(userRole);
        } else {
            signUpRequest.getRoles().forEach(role -> {
                try {
                    List<Role> roles = roleService.findByNames(List.of(role));
                    user.getRoles().addAll(roles);
                } catch (ResourceNotFoundException e) {
                    System.out.println("Role not found: " + role + " with message: " + e.getMessage());
                }
            });
        }

        user.setProvider(AuthProvider.local);
        user.setIsEnabled(true);
        user.setIsVerified(false);
        String randomCode = RandomString.make(24);
        user.setVerificationCode(randomCode);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        RegisterVerifyEmailDetails verified = new RegisterVerifyEmailDetails(
                user.getEmail(),
                user.getUsername(),
                user.getVerificationCode()
        );
        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, verified);
        return userRepository.save(user);
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) throws IOException {
        String cachedAccessToken = redis.opsForValue().get("accessToken:" + loginRequest.getEmail());
        String cachedRefreshToken = redis.opsForValue().get("refreshToken:" + loginRequest.getEmail());
        if (cachedAccessToken != null && cachedRefreshToken != null && tokenProvider.validateToken(cachedAccessToken)) {
            return new AuthResponse(
                    cachedAccessToken,
                    cachedRefreshToken,
                    loginRequest.getEmail(),
                    tokenProvider.getExpirationDateFromToken(cachedAccessToken)
            );
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);
        User user = findByEmail(loginRequest.getEmail());
        this.revokeAllUserTokens(user);
        this.saveUserToken(user, accessToken);
        redis.opsForValue().set("accessToken:" + user.getEmail(), accessToken);
        redis.opsForValue().set("refreshToken:" + user.getEmail(), refreshToken);
        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                tokenProvider.getExpirationDateFromToken(accessToken)
        );
    }

    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(Math.toIntExact(user.getId()));
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = tokenProvider.getUserEmailFromToken(refreshToken);
        if (userEmail != null) {
            User user = this.findByEmail(userEmail);
            if (tokenProvider.isTokenValid(refreshToken, user)) {
                var accessToken = tokenProvider.createAccessToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = new AuthResponse(
                        accessToken,
                        refreshToken,
                        user.getUsername(),
                        tokenProvider.getExpirationDateFromToken(accessToken)
                );
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public boolean verifyToken(String token) {
        tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "token", token));
        User user = findByEmail(tokenProvider.getUserEmailFromToken(token));
        return tokenProvider.isTokenValid(token, user);
    }

    public boolean verifyUser(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);
        if (user.getVerificationCode().equals(verificationCode)) {
            user.setIsVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
