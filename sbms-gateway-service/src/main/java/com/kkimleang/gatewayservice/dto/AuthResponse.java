package com.kkimleang.gatewayservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class AuthResponse {
    private Boolean status;
    private String statusCode;
    private String message;
}
