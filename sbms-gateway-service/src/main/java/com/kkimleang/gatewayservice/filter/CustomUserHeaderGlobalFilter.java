package com.kkimleang.gatewayservice.filter;


import com.kkimleang.gatewayservice.dto.UserResponse;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomUserHeaderGlobalFilter implements GatewayFilter {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final WebClient webClient;
    @Value("${service.auth.verify_user_uri}")
    private String verifyUserUri;

    public CustomUserHeaderGlobalFilter(WebClient webClient) {
        this.webClient = webClient;
    }

    public static boolean isAnonymousPath(String requestPath) {
        String[] freeURLS = {
                "/api/auth/**", "/api/token/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                "/swagger-resources/**", "/api-docs/**", "/aggregate/**", "/actuator/**"
        };
        for (String pattern : freeURLS) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!isAnonymousPath(exchange.getRequest().getURI().getPath())) {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.error(new ForbiddenException("missing or invalid Authorization header"));
            }
            String token = authHeader.substring(7);
            Mono<UserResponse> userResponse = webClient
                    .get()
                    .uri(verifyUserUri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(UserResponse.class);
            return userResponse.flatMap(user -> {
                if (user.getId() == null) {
                    return Mono.error(new RuntimeException("Invalid user!"));
                }
                exchange.getRequest().mutate().header("User-Id", user.getId().toString()).build();
                exchange.getRequest().mutate().header("Username", user.getUsername()).build();
                return chain.filter(exchange);
            });
        }
        return chain.filter(exchange);
    }

}

