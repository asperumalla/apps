package com.example.paymentservice.dto.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookFireRequest {
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("secret")
    private String secret;

    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("webhook_code")
    private String webhookCode;
}
