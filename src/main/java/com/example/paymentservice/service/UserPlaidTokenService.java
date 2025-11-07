package com.example.paymentservice.service;

import com.example.paymentservice.entity.PlaidAccessToken;
import com.example.paymentservice.entity.User;
import com.example.paymentservice.repository.PlaidAccessTokenRepository;
import com.example.paymentservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service to manage Plaid access tokens for users.
 * Stores and retrieves tokens from the database using JPA repositories.
 * 
 * Note: In production, you should encrypt tokens before storing them.
 * Consider using Spring Cloud Vault, AWS KMS, or a similar encryption service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPlaidTokenService {

    private final UserRepository userRepository;
    private final PlaidAccessTokenRepository plaidAccessTokenRepository;
    private final TokenEncryptionService tokenEncryptionService;

    @Value("${plaid.default-access-token:}")
    private String defaultPlaidAccessToken;

    /**
     * Gets all Plaid access tokens for a given Auth0 user ID.
     * 
     * @param auth0UserId The Auth0 user ID (sub claim)
     * @return List of Plaid access tokens (decrypted)
     */
    @Transactional(readOnly = true)
    public List<String> getAllPlaidAccessTokensForUser(String auth0UserId) {
        log.info("Getting all Plaid access tokens for user: {}", auth0UserId);
        
        // Find user by Auth0 user ID
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isEmpty()) {
            log.debug("User not found in database for Auth0 ID: {}", auth0UserId);
            return List.of();
        }
        
        User user = userOpt.get();
        
        // Find all Plaid access tokens for this user
        List<PlaidAccessToken> tokens = plaidAccessTokenRepository.findByUser(user);
        
        log.info("Found {} Plaid token(s) in database for user: {}", tokens.size(), auth0UserId);
        
        // Decrypt tokens before returning
        return tokens.stream()
                .map(token -> tokenEncryptionService.decrypt(token.getAccessTokenEncrypted()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the first Plaid access token for a given Auth0 user ID.
     * This is for backward compatibility with existing code.
     * 
     * @param auth0UserId The Auth0 user ID (sub claim)
     * @return First Plaid access token if found, otherwise returns default or empty
     */
    @Transactional(readOnly = true)
    public Optional<String> getPlaidAccessTokenForUser(String auth0UserId) {
        List<String> tokens = getAllPlaidAccessTokensForUser(auth0UserId);
        
        if (!tokens.isEmpty()) {
            log.info("Returning first Plaid token for user: {}", auth0UserId);
            return Optional.of(tokens.get(0));
        }
        
        log.debug("No Plaid token found in database for user: {}", auth0UserId);
        // Fall back to default token if configured
        return getDefaultToken(auth0UserId);
    }

    /**
     * Gets a specific Plaid access token by item ID for a given Auth0 user ID.
     * 
     * @param auth0UserId The Auth0 user ID (sub claim)
     * @param itemId The Plaid item ID
     * @return Plaid access token if found, otherwise returns empty
     */
    @Transactional(readOnly = true)
    public Optional<String> getPlaidAccessTokenForUserAndItem(String auth0UserId, String itemId) {
        log.info("Getting Plaid access token for user: {} and item: {}", auth0UserId, itemId);
        
        // Find user by Auth0 user ID
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isEmpty()) {
            log.debug("User not found in database for Auth0 ID: {}", auth0UserId);
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Find specific Plaid access token by item ID
        Optional<PlaidAccessToken> plaidTokenOpt = plaidAccessTokenRepository.findByUserAndItemId(user, itemId);
        
        if (plaidTokenOpt.isPresent()) {
            PlaidAccessToken plaidToken = plaidTokenOpt.get();
            log.info("Found Plaid token for user: {} and item: {}", auth0UserId, itemId);
            // Decrypt token before returning
            String decryptedToken = tokenEncryptionService.decrypt(plaidToken.getAccessTokenEncrypted());
            return Optional.of(decryptedToken);
        }
        
        log.debug("No Plaid token found for user: {} and item: {}", auth0UserId, itemId);
        return Optional.empty();
    }

    /**
     * Stores a Plaid access token for a user.
     * This method requires itemId to support multiple tokens per user.
     * 
     * @param auth0UserId The Auth0 user ID
     * @param plaidAccessToken The Plaid access token
     * @param itemId The Plaid item ID (required)
     * @throws IllegalArgumentException if itemId is null or empty
     */
    @Transactional
    public void storePlaidTokenForUser(String auth0UserId, String plaidAccessToken, String itemId) {
        storePlaidTokenForUser(auth0UserId, plaidAccessToken, itemId, null, null, null, null);
    }


    /**
     * Stores a Plaid access token with additional Plaid metadata.
     * 
     * @param auth0UserId The Auth0 user ID
     * @param plaidAccessToken The Plaid access token
     * @param itemId The Plaid item ID
     * @param institutionId The Plaid institution ID
     * @param institutionName The institution name
     * @param email Optional email address
     * @param name Optional user name
     */
    @Transactional
    public void storePlaidTokenForUser(String auth0UserId, String plaidAccessToken,
                                       String itemId, String institutionId, String institutionName,
                                       String email, String name) {
        log.info("Storing Plaid token with metadata for user: {}", auth0UserId);
        
        // Find or create user
        User user = userRepository.findByAuth0UserId(auth0UserId)
                .orElseGet(() -> {
                    log.info("Creating new user in database for Auth0 ID: {}", auth0UserId);
                    return userRepository.save(User.builder()
                            .auth0UserId(auth0UserId)
                            .email(email)
                            .name(name)
                            .build());
                });
        
        // Update user info if provided
        boolean needsUpdate = false;
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            user.setEmail(email);
            needsUpdate = true;
        }
        if (name != null && !name.isEmpty() && !name.equals(user.getName())) {
            user.setName(name);
            needsUpdate = true;
        }
        if (needsUpdate) {
            userRepository.save(user);
        }
        
        if (itemId == null || itemId.isEmpty()) {
            throw new IllegalArgumentException("itemId is required when storing Plaid tokens");
        }
        
        // Encrypt token before storing
        String encryptedToken = tokenEncryptionService.encrypt(plaidAccessToken);
        
        // Check if token already exists for this user and item
        Optional<PlaidAccessToken> existingTokenOpt = plaidAccessTokenRepository.findByUserAndItemId(user, itemId);
        
        PlaidAccessToken plaidToken;
        if (existingTokenOpt.isPresent()) {
            // Update existing token
            plaidToken = existingTokenOpt.get();
            plaidToken.setAccessTokenEncrypted(encryptedToken);
            plaidToken.setInstitutionId(institutionId);
            plaidToken.setInstitutionName(institutionName);
            log.info("Updating existing Plaid token for user: {} and item: {}", auth0UserId, itemId);
        } else {
            // Create new token - users can have multiple tokens (one per bank account/item)
            plaidToken = PlaidAccessToken.builder()
                    .user(user)
                    .accessTokenEncrypted(encryptedToken)
                    .itemId(itemId)
                    .institutionId(institutionId)
                    .institutionName(institutionName)
                    .build();
            log.info("Creating new Plaid token for user: {} and item: {}", auth0UserId, itemId);
        }
        
        plaidAccessTokenRepository.save(plaidToken);
        log.info("Successfully stored Plaid token for user: {} and item: {}", auth0UserId, itemId);
    }

    /**
     * Removes all Plaid access tokens for a user.
     * 
     * @param auth0UserId The Auth0 user ID
     */
    @Transactional
    public void removeAllPlaidTokensForUser(String auth0UserId) {
        log.info("Removing all Plaid tokens for user: {}", auth0UserId);
        
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isEmpty()) {
            log.warn("User not found for Auth0 ID: {}", auth0UserId);
            return;
        }
        
        plaidAccessTokenRepository.deleteByUser(userOpt.get());
        log.info("Successfully removed all Plaid tokens for user: {}", auth0UserId);
    }

    /**
     * Removes a specific Plaid access token by item ID for a user.
     * 
     * @param auth0UserId The Auth0 user ID
     * @param itemId The Plaid item ID
     */
    @Transactional
    public void removePlaidTokenForUser(String auth0UserId, String itemId) {
        log.info("Removing Plaid token for user: {} and item: {}", auth0UserId, itemId);
        
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isEmpty()) {
            log.warn("User not found for Auth0 ID: {}", auth0UserId);
            return;
        }
        
        User user = userOpt.get();
        plaidAccessTokenRepository.deleteByUserAndItemId(user, itemId);
        log.info("Successfully removed Plaid token for user: {} and item: {}", auth0UserId, itemId);
    }

    /**
     * Removes all Plaid access tokens for a user.
     * This method is kept for backward compatibility.
     * 
     * @param auth0UserId The Auth0 user ID
     */
    @Transactional
    public void removePlaidTokenForUser(String auth0UserId) {
        removeAllPlaidTokensForUser(auth0UserId);
    }

    /**
     * Checks if a Plaid access token exists for a user.
     * 
     * @param auth0UserId The Auth0 user ID
     * @return true if at least one token exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasPlaidToken(String auth0UserId) {
        return plaidAccessTokenRepository.existsByAuth0UserId(auth0UserId);
    }

    /**
     * Checks if a Plaid access token exists for a user and specific item.
     * 
     * @param auth0UserId The Auth0 user ID
     * @param itemId The Plaid item ID
     * @return true if token exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasPlaidTokenForItem(String auth0UserId, String itemId) {
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        return plaidAccessTokenRepository.findByUserAndItemId(userOpt.get(), itemId).isPresent();
    }

    /**
     * Gets all Plaid access token entities for a user (including metadata).
     * 
     * @param auth0UserId The Auth0 user ID
     * @return List of PlaidAccessToken entities
     */
    @Transactional(readOnly = true)
    public List<PlaidAccessToken> getAllPlaidTokenEntitiesForUser(String auth0UserId) {
        log.info("Getting all Plaid access token entities for user: {}", auth0UserId);
        
        Optional<User> userOpt = userRepository.findByAuth0UserId(auth0UserId);
        if (userOpt.isEmpty()) {
            log.debug("User not found in database for Auth0 ID: {}", auth0UserId);
            return List.of();
        }
        
        User user = userOpt.get();
        List<PlaidAccessToken> tokens = plaidAccessTokenRepository.findByUser(user);
        log.info("Found {} Plaid token(s) in database for user: {}", tokens.size(), auth0UserId);
        return tokens;
    }

    /**
     * Gets the default Plaid token if configured.
     * This is useful for development/testing when users haven't connected Plaid yet.
     */
    private Optional<String> getDefaultToken(String auth0UserId) {
        log.warn("No Plaid token found for user: {}", auth0UserId);
        return Optional.empty();
    }
}

