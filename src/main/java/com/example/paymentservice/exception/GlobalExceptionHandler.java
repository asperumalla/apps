package com.example.paymentservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private boolean isProduction() {
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }

    @ExceptionHandler(PlaidApiException.class)
    public ResponseEntity<Map<String, Object>> handlePlaidApiException(PlaidApiException ex) {
        log.error("Plaid API Exception: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Plaid API Error");
        errorResponse.put("message", isProduction() 
            ? "An error occurred while processing your request. Please try again later." 
            : ex.getMessage());
        if (!isProduction() && ex.getPlaidError() != null) {
            errorResponse.put("plaidError", ex.getPlaidError());
        }
        errorResponse.put("httpStatus", ex.getHttpStatus());
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientErrorException(HttpClientErrorException ex) {
        log.error("HTTP Client Error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "HTTP Client Error");
        errorResponse.put("message", isProduction() 
            ? "An error occurred while processing your request." 
            : ex.getMessage());
        errorResponse.put("status", ex.getStatusCode().value());
        if (!isProduction()) {
            errorResponse.put("responseBody", ex.getResponseBodyAsString());
        }
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpServerErrorException(HttpServerErrorException ex) {
        log.error("HTTP Server Error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "HTTP Server Error");
        errorResponse.put("message", isProduction() 
            ? "An error occurred while processing your request. Please try again later." 
            : ex.getMessage());
        errorResponse.put("status", ex.getStatusCode().value());
        if (!isProduction()) {
            errorResponse.put("responseBody", ex.getResponseBodyAsString());
        }
        
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException ex) {
        log.error("Resource Access Error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Resource Access Error");
        errorResponse.put("message", isProduction() 
            ? "Service temporarily unavailable. Please try again later." 
            : "Unable to connect to Plaid API: " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", isProduction() 
            ? "An unexpected error occurred. Please contact support if the problem persists." 
            : "An unexpected error occurred: " + ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

