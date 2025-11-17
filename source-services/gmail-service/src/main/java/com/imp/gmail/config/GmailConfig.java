package com.imp.gmail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Gmail configuration properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gmail")
public class GmailConfig {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private Integer maxResults = 100;
    private Integer cacheExpirationMinutes = 60;
}
