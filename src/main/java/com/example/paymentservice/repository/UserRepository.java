package com.example.paymentservice.repository;

import com.example.paymentservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their Auth0 user ID (sub claim).
     *
     * @param auth0UserId The Auth0 user ID
     * @return Optional containing the User if found
     */
    Optional<User> findByAuth0UserId(String auth0UserId);

    /**
     * Check if a user exists with the given Auth0 user ID.
     *
     * @param auth0UserId The Auth0 user ID
     * @return true if user exists, false otherwise
     */
    boolean existsByAuth0UserId(String auth0UserId);

    /**
     * Find a user by their email address.
     *
     * @param email The email address
     * @return Optional containing the User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their email address (case-insensitive).
     *
     * @param email The email address
     * @return Optional containing the User if found
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
}

