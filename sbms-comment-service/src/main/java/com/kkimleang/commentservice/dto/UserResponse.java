package com.kkimleang.commentservice.dto;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private Set<RoleResponse> roles;
    private Set<PermissionResponse> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        this.roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        this.permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }
}
