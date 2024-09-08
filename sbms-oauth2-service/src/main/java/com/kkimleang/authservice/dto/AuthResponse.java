package com.kkimleang.authservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private String tokenType = "Bearer";
}