package com.example.paymentservice.service;

import com.example.paymentservice.config.PlaidProperties;
import com.example.paymentservice.dto.plaid.*;
import com.example.paymentservice.exception.PlaidApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaidService {

    private final RestTemplate restTemplate;
    private final PlaidProperties plaidProperties;
    private final ObjectMapper objectMapper;

    public LinkTokenCreateResponse createLinkToken() {
        log.info("Creating link token for client: {}", plaidProperties.getClientId());
        
        LinkTokenCreateRequest request = LinkTokenCreateRequest.builder()
                .clientId(plaidProperties.getClientId())
                .secret(plaidProperties.getSecret())
                .clientName("Payment Service Demo")
                .products(Arrays.asList("transactions", "auth"))
                .countryCodes(Arrays.asList("US"))
                .language("en")
                .user(LinkTokenCreateRequest.User.builder()
                        .clientUserId(UUID.randomUUID().toString())
                        .build())
                .build();

        try {
            String url = plaidProperties.getBaseUrl() + "/link/token/create";
            HttpHeaders headers = createHeaders();
            HttpEntity<LinkTokenCreateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<LinkTokenCreateResponse> response = restTemplate.postForEntity(
                    url, entity, LinkTokenCreateResponse.class);
            
            log.info("Link token created successfully");
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handlePlaidError(e);
            throw e;
        }
    }

    public ExchangeTokenResponse exchangePublicToken(String publicToken) {
        log.info("Exchanging public token for access token");
        
        ExchangeTokenRequest request = ExchangeTokenRequest.builder()
                .clientId(plaidProperties.getClientId())
                .secret(plaidProperties.getSecret())
                .publicToken(publicToken)
                .build();

        try {
            String url = plaidProperties.getBaseUrl() + "/item/public_token/exchange";
            HttpHeaders headers = createHeaders();
            HttpEntity<ExchangeTokenRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ExchangeTokenResponse> response = restTemplate.postForEntity(
                    url, entity, ExchangeTokenResponse.class);
            
            log.info("Public token exchanged successfully: {}", response.getBody());
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handlePlaidError(e);
            throw e;
        }
    }

    public AccountsGetResponse getAccounts(String accessToken) {
        log.info("Retrieving accounts for access token");
        
        AccountsGetRequest request = AccountsGetRequest.builder()
                .clientId(plaidProperties.getClientId())
                .secret(plaidProperties.getSecret())
                .accessToken(accessToken)
                .build();

        try {
            String url = plaidProperties.getBaseUrl() + "/accounts/get";
            HttpHeaders headers = createHeaders();
            HttpEntity<AccountsGetRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<AccountsGetResponse> response = restTemplate.postForEntity(
                    url, entity, AccountsGetResponse.class);
            
            log.info("Accounts retrieved successfully");
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handlePlaidError(e);
            throw e;
        }
    }

    public TransactionsGetResponse getTransactions(String accessToken, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving transactions for access token from {} to {}", startDate, endDate);
        
        TransactionsGetRequest request = TransactionsGetRequest.builder()
                .clientId(plaidProperties.getClientId())
                .secret(plaidProperties.getSecret())
                .accessToken(accessToken)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        try {
            String url = plaidProperties.getBaseUrl() + "/transactions/get";
            HttpHeaders headers = createHeaders();
            HttpEntity<TransactionsGetRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TransactionsGetResponse> response = restTemplate.postForEntity(
                    url, entity, TransactionsGetResponse.class);
            
            log.info("Transactions retrieved successfully");
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handlePlaidError(e);
            throw e;
        }
    }

    public WebhookFireResponse fireWebhook(String accessToken, String webhookCode) {
        log.info("Firing webhook for access token with webhook code: {}", webhookCode);
        
        WebhookFireRequest request = WebhookFireRequest.builder()
                .clientId(plaidProperties.getClientId())
                .secret(plaidProperties.getSecret())
                .accessToken(accessToken)
                .webhookCode(webhookCode)
                .build();

        try {
            String url = plaidProperties.getBaseUrl() + "/sandbox/item/fire_webhook";
            HttpHeaders headers = createHeaders();
            HttpEntity<WebhookFireRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<WebhookFireResponse> response = restTemplate.postForEntity(
                    url, entity, WebhookFireResponse.class);
            
            log.info("Webhook fired successfully: {}", response.getBody());
            return response.getBody();
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handlePlaidError(e);
            throw e;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void handlePlaidError(Exception e) {
        try {
            String responseBody = "";
            int statusCode = 500;
            
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException clientError = (HttpClientErrorException) e;
                responseBody = clientError.getResponseBodyAsString();
                statusCode = clientError.getStatusCode().value();
            } else if (e instanceof HttpServerErrorException) {
                HttpServerErrorException serverError = (HttpServerErrorException) e;
                responseBody = serverError.getResponseBodyAsString();
                statusCode = serverError.getStatusCode().value();
            }
            
            log.error("Plaid API Error - Status: {}, Body: {}", statusCode, responseBody);
            
            if (!responseBody.isEmpty()) {
                PlaidError plaidError = objectMapper.readValue(responseBody, PlaidError.class);
                throw new PlaidApiException(
                        "Plaid API Error: " + plaidError.getErrorMessage(),
                        plaidError,
                        statusCode
                );
            } else {
                throw new PlaidApiException(
                        "Plaid API Error: " + e.getMessage(),
                        null,
                        statusCode
                );
            }
        } catch (Exception parseException) {
            log.error("Failed to parse Plaid error response", parseException);
            throw new PlaidApiException(
                    "Plaid API Error: " + e.getMessage(),
                    null,
                    500
            );
        }
    }
}
