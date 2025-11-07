--liquibase formatted sql

--changeset payment-service:002-create-plaid-tokens-table
CREATE TABLE plaid_access_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    access_token_encrypted TEXT NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    institution_id VARCHAR(255),
    institution_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plaid_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    -- Ensure each Plaid item (bank account) can only be linked once per user
    CONSTRAINT uq_plaid_token_user_item UNIQUE (user_id, item_id)
);

--changeset payment-service:002-create-plaid-tokens-indexes
CREATE INDEX idx_plaid_tokens_user_id ON plaid_access_tokens(user_id);
CREATE INDEX idx_plaid_tokens_item_id ON plaid_access_tokens(item_id);
-- Composite index for efficient lookups
CREATE INDEX idx_plaid_tokens_user_item ON plaid_access_tokens(user_id, item_id);

--rollback DROP TABLE plaid_access_tokens;

