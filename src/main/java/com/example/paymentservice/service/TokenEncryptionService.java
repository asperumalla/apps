package com.example.paymentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service for encrypting and decrypting sensitive data like Plaid access tokens.
 * Uses Jasypt for encryption.
 */
@Slf4j
@Service
public class TokenEncryptionService {

    @Value("${app.encryption.password:}")
    private String encryptionPassword;

    private StringEncryptor encryptor;

    @PostConstruct
    public void init() {
        if (encryptionPassword == null || encryptionPassword.isEmpty()) {
            log.warn("Encryption password not set. Using default (INSECURE - FOR DEVELOPMENT ONLY)");
            encryptionPassword = "DEFAULT_ENCRYPTION_KEY_CHANGE_IN_PRODUCTION";
        }

        PooledPBEStringEncryptor pooledEncryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(encryptionPassword);
        config.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        config.setKeyObtentionIterations(1000);
        config.setPoolSize(1);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        
        pooledEncryptor.setConfig(config);
        this.encryptor = pooledEncryptor;
        
        log.info("Token encryption service initialized");
    }

    /**
     * Encrypts a token before storing in database.
     */
    public String encrypt(String plainToken) {
        if (plainToken == null || plainToken.isEmpty()) {
            return plainToken;
        }
        try {
            return encryptor.encrypt(plainToken);
        } catch (Exception e) {
            log.error("Error encrypting token", e);
            throw new RuntimeException("Failed to encrypt token", e);
        }
    }

    /**
     * Decrypts a token retrieved from database.
     */
    public String decrypt(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.isEmpty()) {
            return encryptedToken;
        }
        try {
            return encryptor.decrypt(encryptedToken);
        } catch (Exception e) {
            log.error("Error decrypting token", e);
            throw new RuntimeException("Failed to decrypt token", e);
        }
    }
}

