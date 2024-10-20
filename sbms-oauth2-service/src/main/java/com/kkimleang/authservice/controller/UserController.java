package com.kkimleang.authservice.controller;

import com.kkimleang.authservice.annotation.CurrentUser;
import com.kkimleang.authservice.dto.Response;
import com.kkimleang.authservice.dto.user.UserDto;
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
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public Response<UserDto> getCurrentUser(@CurrentUser CustomUserDetails currentUser) {
        try {
            User user = userService.findByEmail(currentUser.getEmail());
            UserDto userDto = UserDto.fromUser(user);
            return Response.<UserDto>ok()
                    .setPayload(userDto);
        } catch (Exception e) {
            return Response.<UserDto>exception()
                    .setErrors(e.getMessage());
        }
    }
}
