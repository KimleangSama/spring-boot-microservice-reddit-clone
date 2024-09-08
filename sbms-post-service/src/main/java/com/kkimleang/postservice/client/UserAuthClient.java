package com.kkimleang.postservice.client;

import com.kkimleang.postservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "SBMS-OAUTH2-SERVICE")
public interface UserAuthClient {
    @GetMapping("/api/demo/user/me")
    UserResponse getAuthenticatedUser(@RequestHeader("Authorization") String token);
}
