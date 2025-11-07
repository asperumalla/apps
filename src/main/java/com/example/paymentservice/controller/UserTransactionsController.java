package com.example.paymentservice.controller;

import com.example.paymentservice.dto.UserTransactionsRequest;
import com.example.paymentservice.dto.plaid.TransactionsGetResponse;
import com.example.paymentservice.service.Auth0Service;
import com.example.paymentservice.service.PlaidService;
import com.example.paymentservice.service.UserPlaidTokenService;
import com.example.paymentservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller to handle user-specific transaction requests.
 * This controller:
 * 1. Receives Auth0 access token from frontend
 * 2. Validates token and extracts user ID
 * 3. Gets user's Plaid access token
 * 4. Fetches transactions from Plaid
 * 5. Returns transactions to frontend
 */
@Slf4j
@RestController
@RequestMapping("/api/user/transactions")
@RequiredArgsConstructor
public class UserTransactionsController {

    private final Auth0Service auth0Service;
    private final UserPlaidTokenService userPlaidTokenService;
    private final PlaidService plaidService;
    private final UserService userService;

    /**
     * Fetches transactions for the authenticated user.
     * 
     * @param request Contains Auth0 access token and optional date range
     * @return Transactions for the user
     */
    @PostMapping
    public ResponseEntity<?> getUserTransactions(
            @Valid @RequestBody UserTransactionsRequest request) {
        
        try {
            log.info("Received request to fetch user transactions");
            
            // Step 1: Validate Auth0 token and get/create user
            String userId;
            try {
                // This will create the user if it doesn't exist
                userService.getOrCreateUserFromToken(request.getAuth0AccessToken());
                
                // Extract user ID for further operations
                userId = auth0Service.validateTokenAndExtractUserId(request.getAuth0AccessToken());
                log.info("Successfully validated Auth0 token and ensured user exists: {}", userId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Auth0 token: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid Auth0 token", e.getMessage()));
            }
            
            // Step 2: Get Plaid access token for this user
            Optional<String> plaidTokenOpt = userPlaidTokenService.getPlaidAccessTokenForUser(userId);
            if (plaidTokenOpt.isEmpty()) {
                log.info("No Plaid token found for user: {}. Returning empty transactions list.", userId);
                // Return empty transactions list instead of error
                TransactionsGetResponse emptyResponse = new TransactionsGetResponse();
                emptyResponse.setTransactions(java.util.Collections.emptyList());
                return ResponseEntity.ok(emptyResponse);
            }
            
            String plaidAccessToken = plaidTokenOpt.get();
            log.info("Retrieved Plaid token for user: {}", userId);
            
            // Step 3: Determine date range
            LocalDate startDate = request.getStartDate() != null && !request.getStartDate().isEmpty()
                    ? LocalDate.parse(request.getStartDate())
                    : LocalDate.now().minusDays(30); // Default: last 30 days
            
            LocalDate endDate = request.getEndDate() != null && !request.getEndDate().isEmpty()
                    ? LocalDate.parse(request.getEndDate())
                    : LocalDate.now(); // Default: today
            
            log.info("Fetching transactions from {} to {} for user: {}", startDate, endDate, userId);
            
            // Step 4: Fetch transactions from Plaid
            TransactionsGetResponse transactionsResponse;
            try {
                transactionsResponse = plaidService.getTransactions(plaidAccessToken, startDate, endDate);
                log.info("Successfully fetched {} transactions for user: {}", 
                        transactionsResponse.getTransactions() != null ? transactionsResponse.getTransactions().size() : 0, 
                        userId);
            } catch (Exception e) {
                log.error("Error fetching transactions from Plaid for user {}: {}", userId, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Plaid API Error", 
                                "Failed to fetch transactions from Plaid: " + e.getMessage()));
            }
            
            // Step 5: Return transactions
            return ResponseEntity.ok(transactionsResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error while fetching user transactions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", LocalDate.now().toString());
        return response;
    }
}

