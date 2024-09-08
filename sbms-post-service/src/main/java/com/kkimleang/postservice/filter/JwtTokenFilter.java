package com.kkimleang.postservice.filter;

import com.kkimleang.postservice.client.UserAuthClient;
import com.kkimleang.postservice.dto.UserResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Configuration
public class JwtTokenFilter extends OncePerRequestFilter {
    private final UserAuthClient userAuthClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader("Authorization");
            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                UserResponse userResponse = userAuthClient.getAuthenticatedUser(token);
                if (userResponse != null) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userResponse, null, userResponse.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
