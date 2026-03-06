CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE tokenTypeEnum AS ENUM (
    'VERIFIED',
    'CHANGED_PASSWORD'
    );

CREATE TABLE hash_tokens
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(255) NOT NULL UNIQUE,
    token_type  tokenTypeEnum    DEFAULT 'CHANGED_PASSWORD',
    account_id  UUID         NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    revoked     BOOLEAN          DEFAULT FALSE,
    ip_address  VARCHAR(32),
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);