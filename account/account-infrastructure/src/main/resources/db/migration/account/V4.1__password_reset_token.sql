CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE password_reset_tokens
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    account_id  UUID         NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    used        BOOLEAN          DEFAULT FALSE,
    ip_address  VARCHAR(100),
    used_at     TIMESTAMP,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

