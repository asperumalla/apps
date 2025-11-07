package com.example.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PlaidAccessToken entity representing encrypted Plaid access tokens for users.
 * Maps to the 'plaid_access_tokens' table created by Liquibase.
 */
@Entity
@Table(name = "plaid_access_tokens", indexes = {
    @Index(name = "idx_plaid_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_plaid_tokens_item_id", columnList = "item_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaidAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plaid_token_user"))
    private User user;

    @Column(name = "access_token_encrypted", nullable = false, columnDefinition = "TEXT")
    private String accessTokenEncrypted;

    @Column(name = "item_id", length = 255)
    private String itemId;

    @Column(name = "institution_id", length = 255)
    private String institutionId;

    @Column(name = "institution_name", length = 255)
    private String institutionName;

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

