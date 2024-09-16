package com.kkimleang.authservice.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkimleang.authservice.config.security.TokenProvider;
import com.kkimleang.authservice.dto.AuthResponse;
import com.kkimleang.authservice.dto.LoginRequest;
import com.kkimleang.authservice.dto.SignUpRequest;
import com.kkimleang.authservice.enumeration.AuthProvider;
import com.kkimleang.authservice.event.AuthVerifiedEvent;
import com.kkimleang.authservice.exception.ResourceNotFoundException;
import com.kkimleang.authservice.model.Role;
import com.kkimleang.authservice.model.Token;
import com.kkimleang.authservice.model.User;
import com.kkimleang.authservice.repository.TokenRepository;
import com.kkimleang.authservice.repository.UserRepository;
import com.kkimleang.authservice.util.RandomString;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final TokenRepository tokenRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, AuthVerifiedEvent> kafkaTemplate;

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
        Role userRole = roleService.findByName("ROLE_USER");
        user.getRoles().add(userRole);
        user.setProvider(AuthProvider.local);
        user.setIsEnabled(true);
        user.setIsVerified(false);
        String randomCode = RandomString.make(24);
        user.setVerificationCode(randomCode);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        AuthVerifiedEvent verified = new AuthVerifiedEvent(
                user.getEmail(),
                user.getUsername(),
                user.getVerificationCode()
        );
        kafkaTemplate.send("auth-verified", verified);

        return userRepository.save(user);
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) throws IOException {
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
