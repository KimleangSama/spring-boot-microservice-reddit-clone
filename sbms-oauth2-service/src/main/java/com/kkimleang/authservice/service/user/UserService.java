package com.kkimleang.authservice.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkimleang.authservice.util.TokenProvider;
import com.kkimleang.authservice.dto.auth.AuthDto;
import com.kkimleang.authservice.dto.auth.LoginRequest;
import com.kkimleang.authservice.dto.auth.SignUpRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final TokenRepository tokenRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.email.name}")
    private String emailExchange;

    @Value("${rabbitmq.binding.email.name}")
    private String emailRoutingKey;

    @Cacheable(value = "findByEmail", key = "#email")
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with " + email + " not found."));
    }

    @Cacheable(value = "existsByEmail", key = "#email")
    public boolean existsByEmail(@NotBlank @Email String email) {
        return userRepository.existsByEmail(email);
    }

    @Cacheable(value = "createUser", key = "#signUpRequest.email")
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

    @CacheEvict(value = "authenticate_user", key = "#loginRequest.email", allEntries = true)
    @Scheduled(fixedRateString = "3000")
    public AuthDto authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            if(authentication == null) {
                throw new BadCredentialsException("Username or password is incorrect.");
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.createAccessToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail());
            }
            this.revokeAllUserTokens(user);
            this.saveUserToken(user, accessToken);
            return new AuthDto(
                    accessToken,
                    refreshToken,
                    user.getUsername(),
                    tokenProvider.getExpirationDateFromToken(accessToken)
            );
        } catch (Exception e) {
            String message = "We cannot authenticate user. Please check email and password.";
            log.error(message);
            throw new BadCredentialsException(message);
        }
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
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
            if (tokenProvider.isTokenValid(refreshToken, user)) {
                var accessToken = tokenProvider.createAccessToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = new AuthDto(
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
        String username = tokenProvider.getUserEmailFromToken(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));
        return tokenProvider.isTokenValid(token, user);
    }

    public User verifyUser(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);
        if (user.getVerificationCode().equals(verificationCode)) {
            user.setIsVerified(true);
            userRepository.save(user);
            return user;
        }
        return null;
    }
}
