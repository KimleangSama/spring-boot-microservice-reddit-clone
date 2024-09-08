package com.kkimleang.gatewayservice.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "cors")
public class CORSProperties {
    private List<String> allowedOrigins;
}