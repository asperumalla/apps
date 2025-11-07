package com.example.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for exchanging public token with Auth0 authentication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeTokenWithAuth0Request {
    
    @NotBlank(message = "Public token is required")
    @JsonProperty("public_token")
    private String publicToken;
    
    @NotBlank(message = "Auth0 access token is required")
    @JsonProperty("auth0_access_token")
    private String auth0AccessToken;
}

