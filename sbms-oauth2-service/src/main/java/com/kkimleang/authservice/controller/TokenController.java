package com.kkimleang.authservice.controller;

import com.kkimleang.authservice.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final UserService userService;

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyToken(@RequestParam String token) {
        return ResponseEntity.ok(userService.verifyToken(token));
    }
}
