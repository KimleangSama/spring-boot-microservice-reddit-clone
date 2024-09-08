package com.kkimleang.gatewayservice.routes;

import com.kkimleang.gatewayservice.filter.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;

@RequiredArgsConstructor
@Configuration
@CrossOrigin(origins = "*")
public class APIGatewayRoute {
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${service.auth.url}")
    private String authServiceUrl;
    @Value("${service.post.url}")
    private String postServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("oauth2_service")
                .route(RequestPredicates.path("/api/auth/**"), HandlerFunctions.http())
                .route(RequestPredicates.path("/oauth2/**"), HandlerFunctions.http())
                .filter(lb(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> postServiceRoute() {
        return GatewayRouterFunctions.route("post_service")
                .route(RequestPredicates.path("/api/posts/**"), HandlerFunctions.http())
                .filter(jwtTokenFilter)
                .filter(lb(postServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return GatewayRouterFunctions.route("oauth2_user_service")
                .route(RequestPredicates.path("/api/demo/**"), HandlerFunctions.http())
                .filter(jwtTokenFilter)
                .filter(lb(authServiceUrl))
                .build();
    }
}
