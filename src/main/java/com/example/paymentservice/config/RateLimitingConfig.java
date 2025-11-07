package com.example.paymentservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate limiting configuration using Bucket4j.
 * Limits API requests to prevent abuse and DoS attacks.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Creates a rate limiting bucket.
     * Configuration:
     * - 100 requests per minute per IP
     * - Refills at 100 tokens per minute
     * 
     * Adjust these values based on your needs.
     */
    @Bean
    public Bucket rateLimitBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

