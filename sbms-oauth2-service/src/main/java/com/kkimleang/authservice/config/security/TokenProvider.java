package com.kkimleang.authservice.config.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.kkimleang.authservice.config.properties.TokenProperties;
import com.kkimleang.authservice.model.Permission;
import com.kkimleang.authservice.model.User;
import com.kkimleang.authservice.service.user.CustomUserDetails;
import com.kkimleang.authservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${token.issuer}")
    private String issuer;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private final TokenProperties tokenProperties;

    public String createAccessToken(Authentication authentication) {
        return buildToken(authentication, tokenProperties.getAccessTokenExpiresHours());
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        var jwtClaimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plus(tokenProperties.getAccessTokenExpiresHours(), ChronoUnit.HOURS))
                .claim(JwtUtils.EMAIL.getProperty(), user.getEmail())
                .claim(JwtUtils.SCOPE.getProperty(), user.getRoles());
        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet.build())).getTokenValue();
    }

    public String createRefreshToken(Authentication authentication) {
        return buildToken(authentication, tokenProperties.getRefreshTokenExpiresHours());
    }

    private String buildToken(Authentication authentication, Integer expiresHours) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Instant now = Instant.now();
        var jwtClaimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(expiresHours, ChronoUnit.HOURS))
                .claim(JwtUtils.EMAIL.getProperty(), userDetails.getEmail())
                .claim(JwtUtils.SCOPE.getProperty(), authentication.getAuthorities());
        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet.build())).getTokenValue();
    }

    public String getUserEmailFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaim(JwtUtils.EMAIL.getProperty());
    }

    public boolean validateToken(String authToken) {
        if (authToken == null) {
            return false;
        }
        return isTokenExpired(authToken);
    }

    public boolean isTokenValid(String token, User userDetails) {
        final String username = getUserEmailFromToken(token);
        return (username.equals(userDetails.getEmail())) && isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Instant exp = jwtDecoder.decode(token).getExpiresAt();
        if (exp != null) {
            return !exp.isBefore(Instant.now());
        } else {
            return false;
        }
    }
}
