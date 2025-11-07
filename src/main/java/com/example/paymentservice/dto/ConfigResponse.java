package com.example.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration response DTO for frontend.
 * Contains all public configuration needed by the UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigResponse {
    
    @JsonProperty("auth0")
    private Auth0Config auth0;
    
    @JsonProperty("api")
    private ApiConfig api;
    
    @JsonProperty("features")
    private FeaturesConfig features;
    
    @JsonProperty("ui")
    private UiConfig ui;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Auth0Config {
        @JsonProperty("domain")
        private String domain;
        
        @JsonProperty("clientId")
        private String clientId;
        
        @JsonProperty("redirectUri")
        private String redirectUri;
        
        @JsonProperty("audience")
        private String audience;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiConfig {
        @JsonProperty("baseUrl")
        private String baseUrl;
        
        @JsonProperty("plaidApiUrl")
        private String plaidApiUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturesConfig {
        @JsonProperty("enablePlaid")
        private Boolean enablePlaid;
        
        @JsonProperty("enableReports")
        private Boolean enableReports;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UiConfig {
        @JsonProperty("appName")
        private String appName;
        
        @JsonProperty("theme")
        private ThemeConfig theme;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ThemeConfig {
            @JsonProperty("primaryColor")
            private String primaryColor;
        }
    }
}

