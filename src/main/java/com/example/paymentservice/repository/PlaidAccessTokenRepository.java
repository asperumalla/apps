package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PlaidAccessToken;
import com.example.paymentservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PlaidAccessToken entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface PlaidAccessTokenRepository extends JpaRepository<PlaidAccessToken, UUID> {

    /**
     * Find all Plaid access tokens for a user.
     *
     * @param user The user entity
     * @return List of PlaidAccessToken for the user
     */
    List<PlaidAccessToken> findByUser(User user);

    /**
     * Find all Plaid access tokens for a user ID.
     *
     * @param userId The user ID
     * @return List of PlaidAccessToken for the user
     */
    @Query("SELECT pat FROM PlaidAccessToken pat WHERE pat.user.id = :userId ORDER BY pat.createdAt DESC")
    List<PlaidAccessToken> findByUserId(@Param("userId") UUID userId);

    /**
     * Find all Plaid access tokens for an Auth0 user ID.
     *
     * @param auth0UserId The Auth0 user ID
     * @return List of PlaidAccessToken for the user
     */
    @Query("SELECT pat FROM PlaidAccessToken pat WHERE pat.user.auth0UserId = :auth0UserId ORDER BY pat.createdAt DESC")
    List<PlaidAccessToken> findByAuth0UserId(@Param("auth0UserId") String auth0UserId);

    /**
     * Find a Plaid access token by Plaid item ID.
     *
     * @param itemId The Plaid item ID
     * @return Optional containing the PlaidAccessToken if found
     */
    Optional<PlaidAccessToken> findByItemId(String itemId);

    /**
     * Find a Plaid access token by user and item ID.
     * This ensures we get the specific token for a specific bank account.
     *
     * @param user The user entity
     * @param itemId The Plaid item ID
     * @return Optional containing the PlaidAccessToken if found
     */
    Optional<PlaidAccessToken> findByUserAndItemId(User user, String itemId);

    /**
     * Find a Plaid access token by Auth0 user ID and item ID.
     *
     * @param auth0UserId The Auth0 user ID
     * @param itemId The Plaid item ID
     * @return Optional containing the PlaidAccessToken if found
     */
    @Query("SELECT pat FROM PlaidAccessToken pat WHERE pat.user.auth0UserId = :auth0UserId AND pat.itemId = :itemId")
    Optional<PlaidAccessToken> findByAuth0UserIdAndItemId(@Param("auth0UserId") String auth0UserId, @Param("itemId") String itemId);

    /**
     * Check if a Plaid access token exists for the given user.
     *
     * @param user The user entity
     * @return true if token exists, false otherwise
     */
    boolean existsByUser(User user);

    /**
     * Delete all Plaid access tokens for a user.
     *
     * @param user The user entity
     */
    void deleteByUser(User user);

    /**
     * Delete a Plaid access token by user and item ID.
     *
     * @param user The user entity
     * @param itemId The Plaid item ID
     */
    void deleteByUserAndItemId(User user, String itemId);

    /**
     * Delete a Plaid access token by Auth0 user ID and item ID.
     *
     * @param auth0UserId The Auth0 user ID
     * @param itemId The Plaid item ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PlaidAccessToken pat WHERE pat.user.auth0UserId = :auth0UserId AND pat.itemId = :itemId")
    void deleteByAuth0UserIdAndItemId(@Param("auth0UserId") String auth0UserId, @Param("itemId") String itemId);

    /**
     * Check if a Plaid access token exists for the given user ID.
     *
     * @param userId The user ID
     * @return true if token exists, false otherwise
     */
    @Query("SELECT COUNT(pat) > 0 FROM PlaidAccessToken pat WHERE pat.user.id = :userId")
    boolean existsByUserId(@Param("userId") UUID userId);

    /**
     * Check if a Plaid access token exists for the given Auth0 user ID.
     *
     * @param auth0UserId The Auth0 user ID
     * @return true if token exists, false otherwise
     */
    @Query("SELECT COUNT(pat) > 0 FROM PlaidAccessToken pat WHERE pat.user.auth0UserId = :auth0UserId")
    boolean existsByAuth0UserId(@Param("auth0UserId") String auth0UserId);

    /**
     * Delete a Plaid access token by Auth0 user ID.
     *
     * @param auth0UserId The Auth0 user ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PlaidAccessToken pat WHERE pat.user.auth0UserId = :auth0UserId")
    void deleteByAuth0UserId(@Param("auth0UserId") String auth0UserId);
}

