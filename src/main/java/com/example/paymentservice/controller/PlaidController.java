package com.example.paymentservice.controller;

import com.example.paymentservice.dto.plaid.*;
import com.example.paymentservice.dto.ExchangeTokenWithAuth0Request;
import com.example.paymentservice.service.PlaidService;
import com.example.paymentservice.service.UserService;
import com.example.paymentservice.service.UserPlaidTokenService;
import com.example.paymentservice.service.Auth0Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidController {

    private final PlaidService plaidService;
    private final UserService userService;
    private final UserPlaidTokenService userPlaidTokenService;
    private final Auth0Service auth0Service;

    @PostMapping("/link-token/create")
    public ResponseEntity<LinkTokenCreateResponse> createLinkToken() {
        log.info("Creating link token via REST endpoint");
        LinkTokenCreateResponse response = plaidService.createLinkToken();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/access-token/exchange")
    public ResponseEntity<?> exchangePublicToken(
            @Valid @RequestBody ExchangeTokenWithAuth0Request request) {
        log.info("Exchanging public token via REST endpoint with Auth0 authentication");
        
        try {
            // Step 1: Validate Auth0 token and get/create user
            String userId;
            try {
                userService.getOrCreateUserFromToken(request.getAuth0AccessToken());
                userId = auth0Service.validateTokenAndExtractUserId(request.getAuth0AccessToken());
                log.info("Successfully validated Auth0 token for user: {}", userId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Auth0 token: {}", e.getMessage());
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid Auth0 token");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Step 2: Exchange public token for access token
            ExchangeTokenResponse exchangeResponse = plaidService.exchangePublicToken(request.getPublicToken());
            String accessToken = exchangeResponse.getAccessToken();
            String itemId = exchangeResponse.getItemId();
            log.info("Successfully exchanged public token for access token, item_id: {}", itemId);
            
            // Step 3: Get accounts to retrieve institution information
            AccountsGetResponse accountsResponse = null;
            String institutionId = null;
            String institutionName = null;
            try {
                accountsResponse = plaidService.getAccounts(accessToken);
                if (accountsResponse != null && accountsResponse.getItem() != null) {
                    institutionId = accountsResponse.getItem().getInstitutionId();
                    log.info("Retrieved institution_id: {}", institutionId);
                    // Institution name might need to be fetched separately, but we can store what we have
                    // For now, we'll store the institution_id and fetch name later if needed
                }
            } catch (Exception e) {
                log.warn("Could not fetch accounts for institution info: {}", e.getMessage());
                // Continue even if accounts fetch fails - we'll store token without institution name
            }
            
            // Step 4: Store the Plaid access token for the user
            try {
                userPlaidTokenService.storePlaidTokenForUser(
                    userId,
                    accessToken,
                    itemId,
                    institutionId,
                    institutionName, // Will be null if not available
                    null, // email
                    null  // name
                );
                log.info("Successfully stored Plaid access token for user: {} and item: {}", userId, itemId);
            } catch (Exception e) {
                log.error("Failed to store Plaid token for user: {}", e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to store access token");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            // Step 5: Return success response
            return ResponseEntity.ok(exchangeResponse);
            
        } catch (Exception e) {
            log.error("Error during token exchange: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Token exchange failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /*@PostMapping("/accounts/get")
    public ResponseEntity<AccountsGetResponse> getAccounts(
            @Valid @RequestBody AccountsGetRequest request) {
        log.info("Getting accounts via REST endpoint");
        AccountsGetResponse response = plaidService.getAccounts(request.getAccessToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/get")
    public ResponseEntity<TransactionsGetResponse> getTransactions(
            @Valid @RequestBody TransactionsGetRequest request) {
        log.info("Getting transactions via REST endpoint");
        TransactionsGetResponse response = plaidService.getTransactions(
                request.getAccessToken(),
                request.getStartDate(),
                request.getEndDate()
        );
        return ResponseEntity.ok(response);
    }

    // Convenience endpoints for testing with query parameters
    @GetMapping("/accounts/get")
    public ResponseEntity<AccountsGetResponse> getAccountsByToken(
            @RequestParam String accessToken) {
        log.info("Getting accounts via GET endpoint with access token");
        AccountsGetResponse response = plaidService.getAccounts(accessToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/get")
    public ResponseEntity<TransactionsGetResponse> getTransactionsByToken(
            @RequestParam String accessToken,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting transactions via GET endpoint with access token");
        TransactionsGetResponse response = plaidService.getTransactions(accessToken, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sandbox/item/fire_webhook")
    public ResponseEntity<WebhookFireResponse> fireWebhook(
            @RequestBody WebhookFireRequest webhookFireRequest) {
        log.info("Firing webhook via POST endpoint with access token and webhook code: {}", webhookFireRequest);
        WebhookFireResponse response = plaidService.fireWebhook(webhookFireRequest.getAccessToken(), webhookFireRequest.getWebhookCode());
        return ResponseEntity.ok(response);
    }*/

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Plaid Integration Service",
                "timestamp", LocalDate.now().toString()
        ));
    }
}

