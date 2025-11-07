package com.example.paymentservice.dto.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlaidError {
    @JsonProperty("error_type")
    private String errorType;
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("display_message")
    private String displayMessage;
    
    @JsonProperty("request_id")
    private String requestId;
}

