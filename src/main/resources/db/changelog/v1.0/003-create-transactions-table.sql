--liquibase formatted sql

--changeset payment-service:003-create-transactions-table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    plaid_transaction_id VARCHAR(255) UNIQUE NOT NULL,
    account_id VARCHAR(255),
    amount DECIMAL(19, 4) NOT NULL,
    date DATE NOT NULL,
    merchant_name VARCHAR(255),
    category VARCHAR(100),
    category_id VARCHAR(100),
    name VARCHAR(500),
    payment_channel VARCHAR(50),
    pending BOOLEAN DEFAULT FALSE,
    iso_currency_code VARCHAR(3),
    unofficial_currency_code VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

--changeset payment-service:003-create-transactions-indexes
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_plaid_transaction_id ON transactions(plaid_transaction_id);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_date ON transactions(date);

--rollback DROP TABLE transactions;

