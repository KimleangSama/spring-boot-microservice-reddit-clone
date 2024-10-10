package com.kkimleang.authservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
public class AuthResponse implements Serializable {
    private final String accessToken;
    private final String refreshToken;
    private String tokenType = "Bearer";
    private final String username;
    private final Instant expiredAt;
}