package com.example.paymentservice.exception;

import com.example.paymentservice.dto.plaid.PlaidError;
import lombok.Getter;

@Getter
public class PlaidApiException extends RuntimeException {
    private final PlaidError plaidError;
    private final int httpStatus;
    
    public PlaidApiException(String message, PlaidError plaidError, int httpStatus) {
        super(message);
        this.plaidError = plaidError;
        this.httpStatus = httpStatus;
    }
    
    public PlaidApiException(String message, Throwable cause) {
        super(message, cause);
        this.plaidError = null;
        this.httpStatus = 0;
    }
}

