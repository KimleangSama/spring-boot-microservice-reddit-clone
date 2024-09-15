package com.kkimleang.gatewayservice.routes;

import com.kkimleang.gatewayservice.filter.CustomUserHeaderGlobalFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${service.auth.url}")
    private String authServiceUrl;
    @Value("${service.post_subreddit.url}")
    private String postSubredditServiceUrl;
    @Value("${service.comment.url}")
    private String commentServiceUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-swagger", r -> r.path("/aggregate/SBMS-DISCOVERY-SERVICE/v3/api-docs")
                        .filters(f -> f.setPath("/api-docs"))
                        .uri(authServiceUrl))
                .route("post-service-swagger", r -> r.path("/aggregate/SBMS-POST-SERVICE/v3/api-docs")
                        .filters(f -> f.setPath("/api-docs"))
                        .uri(postSubredditServiceUrl))
                .route("comment-service-swagger", r -> r.path("/aggregate/SBMS-COMMENT-SERVICE/v3/api-docs")
                        .filters(f -> f.setPath("/api-docs"))
                        .uri(commentServiceUrl))
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(brutalCorsFilter("/api/auth"))
                        .uri(authServiceUrl))
                .route("post-subreddit-service", r -> r.path("/api/posts/**", "/api/subreddits/**")
                        .filters(brutalCorsFilter("/api/posts_subreddits"))
                        .uri(postSubredditServiceUrl))
                .route("comment-service", r -> r.path("/api/comments/**")
                        .filters(brutalCorsFilter("/api/comments"))
                        .uri(commentServiceUrl))
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
