package com.example.paymentservice.dto.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LinkTokenCreateResponse {
    @JsonProperty("link_token")
    private String linkToken;
    
    @JsonProperty("expiration")
    private String expiration;
    
    @JsonProperty("request_id")
    private String requestId;
}

