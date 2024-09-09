package com.kkimleang.gatewayservice.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Set<RoleResponse> roles;
    private Set<PermissionResponse> permissions;
}
