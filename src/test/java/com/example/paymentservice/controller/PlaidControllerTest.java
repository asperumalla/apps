package com.example.paymentservice.controller;

import com.example.paymentservice.dto.plaid.LinkTokenCreateResponse;
import com.example.paymentservice.service.PlaidService;
import com.example.paymentservice.service.UserService;
import com.example.paymentservice.service.UserPlaidTokenService;
import com.example.paymentservice.service.Auth0Service;
import com.example.paymentservice.service.TokenEncryptionService;
import com.example.paymentservice.config.RateLimitingInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import io.github.bucket4j.Bucket;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PlaidController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
class PlaidControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaidService plaidService;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private UserPlaidTokenService userPlaidTokenService;
    
    @MockBean
    private Auth0Service auth0Service;
    
    @MockBean
    private TokenEncryptionService tokenEncryptionService;
    
    @MockBean
    private JwtDecoder jwtDecoder;
    
    @MockBean
    private RateLimitingInterceptor rateLimitingInterceptor;
    
    @MockBean
    private Bucket rateLimitBucket;

    @Test
    void createLinkToken_ShouldReturnLinkToken() throws Exception {
        // Given
        LinkTokenCreateResponse mockResponse = new LinkTokenCreateResponse();
        mockResponse.setLinkToken("link-sandbox-test-token");
        mockResponse.setExpiration("2024-12-31T23:59:59Z");
        mockResponse.setRequestId("test-request-id");

        when(plaidService.createLinkToken()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/plaid/link-token/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.link_token").value("link-sandbox-test-token"))
                .andExpect(jsonPath("$.expiration").value("2024-12-31T23:59:59Z"))
                .andExpect(jsonPath("$.request_id").value("test-request-id"));
    }

    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/plaid/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Plaid Integration Service"));
    }
}
