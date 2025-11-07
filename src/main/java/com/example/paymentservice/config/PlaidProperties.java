package com.example.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "plaid")
public class PlaidProperties {
    private String clientId;
    private String secret;
    private String environment;
    private String baseUrl;
}

