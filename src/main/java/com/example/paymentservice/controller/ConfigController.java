package com.example.paymentservice.controller;

import com.example.paymentservice.dto.ConfigResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to provide public configuration to frontend.
 * This endpoint returns only public configuration - no secrets.
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    @Value("${auth0.domain:alphabytes.us.auth0.com}")
    private String auth0Domain;

    @Value("${auth0.client-id:}")
    private String auth0ClientId;

    @Value("${auth0.redirect-uri:}")
    private String auth0RedirectUri;

    @Value("${auth0.audience:}")
    private String auth0Audience;

    @Value("${app.api.base-url:http://localhost:8090}")
    private String apiBaseUrl;

    @Value("${app.api.plaid-url:http://localhost:8090}")
    private String plaidApiUrl;

    @Value("${app.features.enable-plaid:true}")
    private Boolean enablePlaid;

    @Value("${app.features.enable-reports:false}")
    private Boolean enableReports;

    @Value("${app.ui.name:BudgetGuard}")
    private String appName;

    @Value("${app.ui.theme.primary-color:#667eea}")
    private String primaryColor;

    @GetMapping
    public ResponseEntity<ConfigResponse> getConfig() {
        log.debug("Providing configuration to frontend");
        log.debug("Providing configuration to frontend");
        
        ConfigResponse config = ConfigResponse.builder()
                .auth0(ConfigResponse.Auth0Config.builder()
                        .domain(auth0Domain)
                        .clientId(auth0ClientId)
                        .redirectUri(auth0RedirectUri)
                        .audience(auth0Audience)
                        .build())
                .api(ConfigResponse.ApiConfig.builder()
                        .baseUrl(apiBaseUrl)
                        .plaidApiUrl(plaidApiUrl)
                        .build())
                .features(ConfigResponse.FeaturesConfig.builder()
                        .enablePlaid(enablePlaid)
                        .enableReports(enableReports)
                        .build())
                .ui(ConfigResponse.UiConfig.builder()
                        .appName(appName)
                        .theme(ConfigResponse.UiConfig.ThemeConfig.builder()
                                .primaryColor(primaryColor)
                                .build())
                        .build())
                .build();
        
        return ResponseEntity.ok(config);
    }
}

