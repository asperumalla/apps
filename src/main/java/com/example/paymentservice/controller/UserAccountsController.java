package com.example.paymentservice.controller;

import com.example.paymentservice.dto.plaid.AccountsGetResponse;
import com.example.paymentservice.service.Auth0Service;
import com.example.paymentservice.service.PlaidService;
import com.example.paymentservice.service.UserPlaidTokenService;
import com.example.paymentservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to handle user-specific account requests.
 * Returns all accounts from all connected bank accounts for the authenticated user.
 */
@Slf4j
@RestController
@RequestMapping("/api/user/accounts")
@RequiredArgsConstructor
public class UserAccountsController {

    private final Auth0Service auth0Service;
    private final UserPlaidTokenService userPlaidTokenService;
    private final PlaidService plaidService;
    private final UserService userService;

    /**
     * Gets all accounts for the authenticated user from all connected Plaid items.
     * 
     * @param auth0AccessToken The Auth0 access token (can be in body or header)
     * @return All accounts grouped by institution/item
     */
    @PostMapping
    public ResponseEntity<?> getUserAccounts(
            @RequestBody(required = false) Map<String, String> requestBody,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            // Extract Auth0 token from body or header
            String auth0Token = null;
            if (requestBody != null && requestBody.containsKey("auth0AccessToken")) {
                auth0Token = requestBody.get("auth0AccessToken");
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                auth0Token = authHeader.substring(7);
            }
            
            if (auth0Token == null || auth0Token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Missing Auth0 token", "Auth0 access token is required"));
            }
            
            log.info("Received request to fetch user accounts");
            
            // Step 1: Validate Auth0 token and get/create user
            String userId;
            try {
                userService.getOrCreateUserFromToken(auth0Token);
                userId = auth0Service.validateTokenAndExtractUserId(auth0Token);
                log.info("Successfully validated Auth0 token for user: {}", userId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Auth0 token: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid Auth0 token", e.getMessage()));
            }
            
            // Step 2: Get all Plaid access tokens for this user
            List<String> plaidTokens = userPlaidTokenService.getAllPlaidAccessTokensForUser(userId);
            
            if (plaidTokens.isEmpty()) {
                log.info("No Plaid tokens found for user: {}. Returning empty accounts list.", userId);
                Map<String, Object> response = new HashMap<>();
                response.put("accounts", new ArrayList<>());
                response.put("items", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            log.info("Found {} Plaid token(s) for user: {}", plaidTokens.size(), userId);
            
            // Step 3: Fetch accounts from all Plaid items
            // Also fetch transactions to get logo_url from Item object (logo_url is more reliably present in transactions response)
            List<AccountsGetResponse> allAccountsResponses = new ArrayList<>();
            Map<String, String> itemLogoUrlMap = new HashMap<>(); // Map item_id -> logo_url
            
            for (String plaidToken : plaidTokens) {
                try {
                    AccountsGetResponse accountsResponse = plaidService.getAccounts(plaidToken);
                    if (accountsResponse != null) {
                        allAccountsResponses.add(accountsResponse);
                        
                        // Try to get logo_url from transactions response if not present in accounts response
                        String itemId = accountsResponse.getItem() != null ? accountsResponse.getItem().getItemId() : null;
                        if (itemId != null) {
                            // Check if logo_url exists in accounts response
                            if (accountsResponse.getItem().getLogoUrl() != null && !accountsResponse.getItem().getLogoUrl().isEmpty()) {
                                itemLogoUrlMap.put(itemId, accountsResponse.getItem().getLogoUrl());
                                log.info("Found logo_url in accounts response for item {}: {}", itemId, accountsResponse.getItem().getLogoUrl());
                            } else {
                                // Try to get logo_url from transactions response
                                try {
                                    log.debug("Fetching transactions to get logo_url for item: {}", itemId);
                                    com.example.paymentservice.dto.plaid.TransactionsGetResponse transactionsResponse = 
                                        plaidService.getTransactions(plaidToken, java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now());
                                    if (transactionsResponse != null && transactionsResponse.getItem() != null 
                                        && transactionsResponse.getItem().getLogoUrl() != null 
                                        && !transactionsResponse.getItem().getLogoUrl().isEmpty()) {
                                        itemLogoUrlMap.put(itemId, transactionsResponse.getItem().getLogoUrl());
                                        log.info("Retrieved logo_url from transactions response for item {}: {}", itemId, transactionsResponse.getItem().getLogoUrl());
                                    } else {
                                        log.debug("No logo_url found in transactions response for item: {}", itemId);
                                    }
                                } catch (Exception e) {
                                    log.warn("Could not fetch transactions to get logo_url for item {}: {}", itemId, e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch accounts for one token: {}", e.getMessage());
                    // Continue with other tokens
                }
            }
            
            // Step 4: Aggregate all accounts
            List<Map<String, Object>> allAccounts = new ArrayList<>();
            List<Map<String, Object>> items = new ArrayList<>();
            
            for (AccountsGetResponse response : allAccountsResponses) {
                if (response.getAccounts() != null) {
                    for (AccountsGetResponse.Account account : response.getAccounts()) {
                        Map<String, Object> accountMap = new HashMap<>();
                        accountMap.put("account_id", account.getAccountId());
                        accountMap.put("name", account.getName());
                        accountMap.put("type", account.getType());
                        accountMap.put("subtype", account.getSubtype());
                        accountMap.put("mask", account.getMask());
                        if (account.getBalances() != null) {
                            Map<String, Object> balances = new HashMap<>();
                            balances.put("available", account.getBalances().getAvailable());
                            balances.put("current", account.getBalances().getCurrent());
                            balances.put("limit", account.getBalances().getLimit());
                            balances.put("iso_currency_code", account.getBalances().getIsoCurrencyCode());
                            accountMap.put("balances", balances);
                        }
                        // Add institution info from item
                        if (response.getItem() != null) {
                            accountMap.put("institution_id", response.getItem().getInstitutionId());
                            String itemId = response.getItem().getItemId();
                            accountMap.put("item_id", itemId);
                            
                            // Get logo_url: priority 1) from accounts response, 2) from transactions response (stored in map), 3) construct from institution_id
                            String logoUrl = null;
                            if (response.getItem().getLogoUrl() != null && !response.getItem().getLogoUrl().isEmpty()) {
                                logoUrl = response.getItem().getLogoUrl();
                            } else if (itemId != null && itemLogoUrlMap.containsKey(itemId)) {
                                logoUrl = itemLogoUrlMap.get(itemId);
                            } else if (response.getItem().getInstitutionId() != null) {
                                // Fallback: construct URL from institution_id
                                logoUrl = "https://cdn.plaid.com/institutions/logos/" + response.getItem().getInstitutionId() + ".png";
                            }
                            
                            // Always set institution_icon_url if we have institution_id (at minimum, use constructed URL)
                            if (logoUrl != null) {
                                accountMap.put("institution_icon_url", logoUrl);
                                log.debug("Set institution_icon_url for account {} (item {}): {}", account.getAccountId(), itemId, logoUrl);
                            } else if (response.getItem().getInstitutionId() != null) {
                                // Final fallback - ensure we always have an icon URL
                                logoUrl = "https://cdn.plaid.com/institutions/logos/" + response.getItem().getInstitutionId() + ".png";
                                accountMap.put("institution_icon_url", logoUrl);
                                log.debug("Using fallback institution_icon_url for account {} (item {}): {}", account.getAccountId(), itemId, logoUrl);
                            }
                        }
                        allAccounts.add(accountMap);
                    }
                }
                
                // Collect item information
                if (response.getItem() != null) {
                    Map<String, Object> itemMap = new HashMap<>();
                    String itemId = response.getItem().getItemId();
                    itemMap.put("item_id", itemId);
                    itemMap.put("institution_id", response.getItem().getInstitutionId());
                    
                    // Get logo_url: priority 1) from accounts response, 2) from transactions response (stored in map), 3) construct from institution_id
                    String logoUrl = null;
                    if (response.getItem().getLogoUrl() != null && !response.getItem().getLogoUrl().isEmpty()) {
                        logoUrl = response.getItem().getLogoUrl();
                    } else if (itemId != null && itemLogoUrlMap.containsKey(itemId)) {
                        logoUrl = itemLogoUrlMap.get(itemId);
                    } else if (response.getItem().getInstitutionId() != null) {
                        // Fallback: construct URL from institution_id
                        logoUrl = "https://cdn.plaid.com/institutions/logos/" + response.getItem().getInstitutionId() + ".png";
                    }
                    
                    if (logoUrl != null) {
                        itemMap.put("institution_icon_url", logoUrl);
                    }
                    items.add(itemMap);
                }
            }
            
            // Step 5: Return aggregated accounts
            Map<String, Object> response = new HashMap<>();
            response.put("accounts", allAccounts);
            response.put("items", items);
            response.put("total_accounts", allAccounts.size());
            
            log.info("Successfully retrieved {} accounts for user: {}", allAccounts.size(), userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Unexpected error while fetching user accounts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal Server Error", e.getMessage()));
        }
    }

    /**
     * Convenience GET endpoint with Auth0 token in header.
     * Usage: GET /api/user/accounts
     * Header: Authorization: Bearer <auth0-token>
     */
    @GetMapping
    public ResponseEntity<?> getUserAccountsByHeader(
            @RequestHeader("Authorization") @NotBlank String authHeader) {
        return getUserAccounts(null, authHeader);
    }

    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}

