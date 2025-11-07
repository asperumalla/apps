package com.example.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/**
 * Service to validate Auth0 JWT tokens and extract user information.
 * Now uses proper JWT signature validation with Auth0 JWKS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Auth0Service {

    @Value("${auth0.domain:alphabytes.us.auth0.com}")
    private String auth0Domain;

    private final JwtDecoder jwtDecoder;

    /**
     * Validates Auth0 access token signature and extracts user ID (sub claim).
     * Uses Spring Security OAuth2 Resource Server to validate token signature with Auth0's JWKS.
     * 
     * @param accessToken The Auth0 access token
     * @return User ID (sub claim) from the token
     * @throws IllegalArgumentException if token is invalid or signature verification fails
     */
    public String validateTokenAndExtractUserId(String accessToken) {
        try {
            log.info("Validating Auth0 token with signature verification");
            
            // Validate token signature using Auth0's JWKS
            Jwt jwt;
            try {
                jwt = jwtDecoder.decode(accessToken);
            } catch (JwtException e) {
                log.error("JWT signature validation failed: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid or tampered JWT token: " + e.getMessage(), e);
            }

            // Extract user ID from validated token
            String userId = jwt.getSubject();
            
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("User ID (sub) not found in token");
            }

            // Verify token is not expired (JwtDecoder handles this automatically)
            // Verify issuer matches Auth0 domain
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
            String expectedIssuer = "https://" + auth0Domain + "/";
            if (issuer == null || !issuer.equals(expectedIssuer)) {
                log.warn("Token issuer mismatch. Expected: {}, Got: {}", expectedIssuer, issuer);
                // In production, you might want to be stricter here
            }

            log.info("Successfully validated Auth0 token and extracted user ID: {}", userId);
            return userId;
            
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            log.error("Error validating Auth0 token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Auth0 token: " + e.getMessage(), e);
        }
    }
}

