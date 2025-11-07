package com.example.paymentservice.dto.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AccountsGetResponse {
    @JsonProperty("accounts")
    private List<Account> accounts;
    
    @JsonProperty("item")
    private Item item;
    
    @JsonProperty("request_id")
    private String requestId;
    
    @Data
    public static class Account {
        @JsonProperty("account_id")
        private String accountId;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("subtype")
        private String subtype;
        
        @JsonProperty("mask")
        private String mask;
        
        @JsonProperty("balances")
        private Balances balances;
    }
    
    @Data
    public static class Balances {
        @JsonProperty("available")
        private Double available;
        
        @JsonProperty("current")
        private Double current;
        
        @JsonProperty("limit")
        private Double limit;
        
        @JsonProperty("iso_currency_code")
        private String isoCurrencyCode;
        
        @JsonProperty("unofficial_currency_code")
        private String unofficialCurrencyCode;
    }
    
    @Data
    public static class Item {
        @JsonProperty("item_id")
        private String itemId;
        
        @JsonProperty("institution_id")
        private String institutionId;
        
        @JsonProperty("webhook")
        private String webhook;
        
        @JsonProperty("error")
        private Object error;
        
        @JsonProperty("logo_url")
        private String logoUrl;
    }
}

