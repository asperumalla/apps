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
public class WebhookFireResponse {
    @JsonProperty("webhook_fired")
    private boolean webhookFired;
    
    @JsonProperty("request_id")
    private String requestId;
}


