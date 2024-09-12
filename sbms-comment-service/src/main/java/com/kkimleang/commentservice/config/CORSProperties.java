package com.kkimleang.commentservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "cors")
public class CORSProperties {
    private String[] allowedOrigins;
}
