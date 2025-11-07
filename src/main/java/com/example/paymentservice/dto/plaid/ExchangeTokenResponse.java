package com.example.paymentservice.dto.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExchangeTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("item_id")
    private String itemId;
    
    @JsonProperty("request_id")
    private String requestId;
}

