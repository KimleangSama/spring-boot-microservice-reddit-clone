package com.kkimleang.gatewayservice.routes;

import com.kkimleang.gatewayservice.filter.CustomUserHeaderGlobalFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.function.Function;

@RequiredArgsConstructor
@Configuration
@CrossOrigin(origins = "*")
public class APIGatewayRoute {
    private final CustomUserHeaderGlobalFilter customGlobalFilter;

//    @Value("${service.auth.url}")
//    private String authServiceUrl;
//    @Value("${service.post.url}")
//    private String postServiceUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(brutalCorsFilter("/api/auth"))
                        .uri("http://localhost:8888"))
//                TODO: Change to LB Eureka Service
                .build();
    }



    Function<GatewayFilterSpec, UriSpec> brutalCorsFilter(final String serviceName) {
        return f -> f
                .filter(customGlobalFilter)
                .rewritePath("/" + serviceName + "/(?<segment>.*)", "/${segment}")
                .setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Expose-Headers", "*");
    }

}
