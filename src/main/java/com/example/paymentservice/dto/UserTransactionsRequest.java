package com.example.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for fetching user transactions with Auth0 token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTransactionsRequest {
    
    @NotBlank(message = "Auth0 access token is required")
    private String auth0AccessToken;
    
    // Optional: if not provided, will use default date range
    private String startDate; // Format: yyyy-MM-dd
    private String endDate;   // Format: yyyy-MM-dd
}

