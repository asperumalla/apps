package com.example.paymentservice.repository;

import com.example.paymentservice.entity.Transaction;
import com.example.paymentservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Transaction entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find all transactions for a user, paginated.
     *
     * @param user The user entity
     * @param pageable Pagination information
     * @return Page of transactions
     */
    Page<Transaction> findByUser(User user, Pageable pageable);

    /**
     * Find all transactions for a user.
     *
     * @param user The user entity
     * @return List of transactions
     */
    List<Transaction> findByUser(User user);

    /**
     * Find all transactions for a user within a date range.
     *
     * @param user The user entity
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of transactions
     */
    List<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Find all transactions for a user ordered by date (descending).
     *
     * @param user The user entity
     * @return List of transactions, newest first
     */
    List<Transaction> findByUserOrderByDateDesc(User user);

    /**
     * Find a transaction by its Plaid transaction ID.
     *
     * @param plaidTransactionId The Plaid transaction ID
     * @return Optional containing the Transaction if found
     */
    Optional<Transaction> findByPlaidTransactionId(String plaidTransactionId);

    /**
     * Check if a transaction exists with the given Plaid transaction ID.
     *
     * @param plaidTransactionId The Plaid transaction ID
     * @return true if transaction exists, false otherwise
     */
    boolean existsByPlaidTransactionId(String plaidTransactionId);

    /**
     * Find all transactions for a user by Auth0 user ID.
     *
     * @param auth0UserId The Auth0 user ID
     * @return List of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.auth0UserId = :auth0UserId")
    List<Transaction> findByAuth0UserId(@Param("auth0UserId") String auth0UserId);

    /**
     * Find all transactions for a user by Auth0 user ID within a date range.
     *
     * @param auth0UserId The Auth0 user ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.auth0UserId = :auth0UserId " +
           "AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    List<Transaction> findByAuth0UserIdAndDateBetween(
            @Param("auth0UserId") String auth0UserId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count transactions for a user.
     *
     * @param user The user entity
     * @return Count of transactions
     */
    long countByUser(User user);

    /**
     * Delete all transactions for a user.
     *
     * @param user The user entity
     */
    void deleteByUser(User user);

    /**
     * Delete a transaction by its Plaid transaction ID.
     *
     * @param plaidTransactionId The Plaid transaction ID
     */
    void deleteByPlaidTransactionId(String plaidTransactionId);

    /**
     * Find all pending transactions for a user.
     *
     * @param user The user entity
     * @return List of pending transactions
     */
    List<Transaction> findByUserAndPendingTrue(User user);

    /**
     * Find all completed (non-pending) transactions for a user.
     *
     * @param user The user entity
     * @return List of completed transactions
     */
    List<Transaction> findByUserAndPendingFalse(User user);

    /**
     * Find transactions for a user by category.
     *
     * @param user The user entity
     * @param category The category
     * @return List of transactions
     */
    List<Transaction> findByUserAndCategory(User user, String category);

    /**
     * Find transactions for a user within a date range, ordered by date descending.
     *
     * @param user The user entity
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of transactions, newest first
     */
    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Find transactions for a user by Auth0 user ID, ordered by date descending.
     *
     * @param auth0UserId The Auth0 user ID
     * @return List of transactions, newest first
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.auth0UserId = :auth0UserId ORDER BY t.date DESC, t.createdAt DESC")
    List<Transaction> findByAuth0UserIdOrderByDateDesc(@Param("auth0UserId") String auth0UserId);
}

