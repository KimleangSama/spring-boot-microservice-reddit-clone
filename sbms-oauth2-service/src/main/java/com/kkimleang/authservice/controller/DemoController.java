package com.kkimleang.authservice.controller;

import com.kkimleang.authservice.annotation.CurrentUser;
import com.kkimleang.authservice.dto.UserResponse;
import com.kkimleang.authservice.model.Permission;
import com.kkimleang.authservice.model.Role;
import com.kkimleang.authservice.model.User;
import com.kkimleang.authservice.service.user.CustomUserDetails;
import com.kkimleang.authservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    public String demo() {
        return "Hello World!";
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserResponse getCurrent(@CurrentUser CustomUserDetails currentUser) {
        User user = userService.findByEmail(currentUser.getEmail());
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setUsername(user.getUsername());
        userResponse.setImageUrl(user.getImageUrl());
        userResponse.setRoles(user.getRoles());
        Optional<Set<Permission>> permissions = user.getRoles().stream().map(Role::getPermissions).findFirst();
        permissions.ifPresent(userResponse::setPermissions);
        return userResponse;
    }
}
