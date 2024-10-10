package com.kkimleang.authservice.dto;

import com.kkimleang.authservice.enumeration.AuthProvider;
import com.kkimleang.authservice.model.Permission;
import com.kkimleang.authservice.model.Role;
import lombok.*;

import java.io.Serializable;
import java.util.Set;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String imageUrl;
    private AuthProvider provider;
    private Set<Role> roles;
    private Set<Permission> permissions;
}
