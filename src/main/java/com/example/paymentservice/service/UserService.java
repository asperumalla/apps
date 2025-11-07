package com.example.paymentservice.service;

import com.example.paymentservice.entity.User;
import com.example.paymentservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service to manage User entities.
 * Creates users automatically when they authenticate with Auth0 if they don't exist.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Auth0Service auth0Service;

    /**
     * Gets or creates a user based on Auth0 access token.
     * If user doesn't exist, creates a new user record.
     * 
     * @param accessToken The Auth0 access token
     * @return The User entity (existing or newly created)
     * @throws IllegalArgumentException if token is invalid
     */
    @Transactional
    public User getOrCreateUserFromToken(String accessToken) {
        // Validate token and extract user ID
        String auth0UserId = auth0Service.validateTokenAndExtractUserId(accessToken);
        log.info("Getting or creating user for Auth0 ID: {}", auth0UserId);

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByAuth0UserId(auth0UserId);
        if (existingUser.isPresent()) {
            log.info("User already exists: {}", auth0UserId);
            return existingUser.get();
        }

        // Extract email and name from token if available
        // For now, we'll create user with just the Auth0 user ID
        // In the future, we can enhance Auth0Service to extract email and name from token
        User newUser = User.builder()
                .auth0UserId(auth0UserId)
                .email(null) // Can be enhanced to extract from token
                .name(null)  // Can be enhanced to extract from token
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Created new user with ID: {} for Auth0 ID: {}", savedUser.getId(), auth0UserId);

        return savedUser;
    }

    /**
     * Gets user by Auth0 user ID.
     * 
     * @param auth0UserId The Auth0 user ID
     * @return Optional containing the User if found
     */
    public Optional<User> getUserByAuth0Id(String auth0UserId) {
        return userRepository.findByAuth0UserId(auth0UserId);
    }

    /**
     * Checks if a user exists with the given Auth0 user ID.
     * 
     * @param auth0UserId The Auth0 user ID
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String auth0UserId) {
        return userRepository.existsByAuth0UserId(auth0UserId);
    }
}

