package com.kkimleang.gatewayservice.filter;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements WebFilter {
    @Value("${cors.allowedOrigins}")
    private String coreAllowedHost;

    @NotNull
    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, @NotNull final WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final String origin = request.getHeaders().getOrigin();
        final HttpHeaders headers = exchange.getResponse().getHeaders();

        final List<String> allowedHosts = List.of(coreAllowedHost.split(","));
        if (origin != null && allowedHosts.contains(origin)) {
            headers.add("Access-Control-Allow-Origin", origin);
        }
        // Handle preflight requests (OPTIONS method)
        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
            headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "*");
            headers.add("Access-Control-Max-Age", "3600");
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        }
        // Set security headers for all responses
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-XSS-Protection", "1; mode=block");
        headers.add("X-Frame-Options", "DENY");
        headers.add("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains");
        headers.add("Content-Security-Policy",
                "default-src 'self'; script-src 'self' https://trusted.cdn.com; style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; font-src 'self'; frame-ancestors 'none';");
        return chain.filter(exchange);
    }
}
