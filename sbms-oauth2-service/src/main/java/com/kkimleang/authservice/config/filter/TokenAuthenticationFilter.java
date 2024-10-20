package com.kkimleang.authservice.config.filter;

import java.io.IOException;

import com.kkimleang.authservice.util.TokenProvider;
import com.kkimleang.authservice.model.User;
import com.kkimleang.authservice.repository.TokenRepository;
import com.kkimleang.authservice.service.user.CustomUserDetails;
import com.kkimleang.authservice.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String email;
        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && tokenProvider.isTokenExpired(jwt)) {
                email = tokenProvider.getUserEmailFromToken(jwt);
                User user = userService.findByEmail(email);
                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);
                if (tokenProvider.isTokenValid(jwt, user) && isTokenValid) {
                    CustomUserDetails customUserDetails = new CustomUserDetails(user, null);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            customUserDetails,
                            null,
                            customUserDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
