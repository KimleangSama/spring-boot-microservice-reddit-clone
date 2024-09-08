package com.kkimleang.gatewayservice.filter;

import com.kkimleang.gatewayservice.dto.AuthResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

@Component
public class JwtTokenFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public ServerResponse filter(ServerRequest request, @NonNull HandlerFunction<ServerResponse> next) throws Exception {
        try {
            List<String> headers = request.headers().header("Authorization");
            if (headers.isEmpty()) {
                return ServerResponse
                        .status(401)
                        .body(
                                AuthResponse.builder()
                                        .status(false)
                                        .statusCode("401")
                                        .message("Unauthorized - No Authorization Header")
                                        .build()
                        );
            }
            String token = headers.getFirst();
            if (token == null || token.isEmpty()) {
                return ServerResponse
                        .status(401)
                        .body(
                                AuthResponse.builder()
                                        .status(false)
                                        .statusCode("401")
                                        .message("Unauthorized - No Token Provided")
                                        .build()
                        );
            }
            token = token.replace("Bearer ", "");
            RestClient restClient = RestClient
                    .builder()
                    .baseUrl("http://localhost:8888")
                    .build();
            boolean isValid = Boolean.TRUE.equals(restClient.post().uri("/api/token/verify?token=" + token)
                    .retrieve()
                    .body(Boolean.class));
            if (isValid) {
                return next.handle(request);
            } else {
                return ServerResponse
                        .status(401)
                        .body(
                                AuthResponse.builder()
                                        .status(false)
                                        .statusCode("401")
                                        .message("Unauthorized - Invalid Token")
                                        .build()
                        );
            }
        } catch (Exception e) {
            return ServerResponse
                    .status(500)
                    .body(
                            AuthResponse.builder()
                                    .status(false)
                                    .statusCode("500")
                                    .message("Internal Server Error - " + e.getMessage())
                                    .build()
                    );
        }
    }
}
