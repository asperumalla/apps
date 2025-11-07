package com.example.paymentservice.dto.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkTokenCreateRequest {
    @JsonProperty("client_id")
    private String clientId;
    
    @JsonProperty("secret")
    private String secret;
    
    @JsonProperty("client_name")
    private String clientName;
    
    @JsonProperty("products")
    private List<String> products;
    
    @JsonProperty("country_codes")
    private List<String> countryCodes;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("user")
    private User user;
    
    @JsonProperty("webhook")
    private String webhook;
    
    @JsonProperty("redirect_uri")
    private String redirectUri;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        @JsonProperty("client_user_id")
        private String clientUserId;
    }
}

