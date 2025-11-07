package com.example.paymentservice.dto.plaid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TransactionsGetResponse {
    @JsonProperty("transactions")
    private List<Transaction> transactions;
    
    @JsonProperty("total_transactions")
    private Integer totalTransactions;
    
    @JsonProperty("accounts")
    private List<Account> accounts;
    
    @JsonProperty("item")
    private Item item;
    
    @JsonProperty("request_id")
    private String requestId;
    
    @Data
    public static class Transaction {
        @JsonProperty("transaction_id")
        private String transactionId;
        
        @JsonProperty("account_id")
        private String accountId;
        
        @JsonProperty("amount")
        private Double amount;
        
        @JsonProperty("date")
        private String date;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("merchant_name")
        private String merchantName;
        
        @JsonProperty("category")
        private List<String> category;
        
        @JsonProperty("account_owner")
        private String accountOwner;
        
        @JsonProperty("iso_currency_code")
        private String isoCurrencyCode;
        
        @JsonProperty("unofficial_currency_code")
        private String unofficialCurrencyCode;
    }
    
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

