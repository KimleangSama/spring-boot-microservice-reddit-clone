package com.kkimleang.authservice.controller;

import com.kkimleang.authservice.config.security.TokenProvider;
import com.kkimleang.authservice.dto.ApiResponse;
import com.kkimleang.authservice.dto.AuthResponse;
import com.kkimleang.authservice.dto.LoginRequest;
import com.kkimleang.authservice.dto.SignUpRequest;
import com.kkimleang.authservice.model.User;
import com.kkimleang.authservice.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        try {
            AuthResponse authResponse = userService.authenticateUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Bad credentials: There is no match with those information."));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            if (userService.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Email address already in use."));
            }
            User result = userService.createUser(signUpRequest);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/user/me")
                    .buildAndExpand(result.getId()).toUri();
            return ResponseEntity.created(location)
                    .body(new ApiResponse(true, "User registered successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "User registered unsuccessfully." + e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        userService.refreshToken(request, response);
    }
}