package com.example.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction entity representing user transactions from Plaid.
 * Maps to the 'transactions' table created by Liquibase.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_user_id", columnList = "user_id"),
    @Index(name = "idx_transactions_plaid_transaction_id", columnList = "plaid_transaction_id"),
    @Index(name = "idx_transactions_user_date", columnList = "user_id,date"),
    @Index(name = "idx_transactions_date", columnList = "date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_user"))
    private User user;

    @Column(name = "plaid_transaction_id", unique = true, nullable = false, length = 255)
    private String plaidTransactionId;

    @Column(name = "account_id", length = 255)
    private String accountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "category_id", length = 100)
    private String categoryId;

    @Column(name = "name", length = 500)
    private String name;

    @Column(name = "payment_channel", length = 50)
    private String paymentChannel;

    @Column(name = "pending", nullable = false)
    @Builder.Default
    private Boolean pending = false;

    @Column(name = "iso_currency_code", length = 3)
    private String isoCurrencyCode;

    @Column(name = "unofficial_currency_code", length = 10)
    private String unofficialCurrencyCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

