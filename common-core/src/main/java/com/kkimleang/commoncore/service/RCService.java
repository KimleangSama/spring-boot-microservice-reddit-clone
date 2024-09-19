package com.kkimleang.commoncore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public class RCService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RCService.class);

    private static final RestClient REST_CLIENT = createRestClient();

    public static RestClient createRestClient() {
        RestClient client = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .build();
        LOGGER.info("Rest client created: {}", client);
        return client;
    }
}
