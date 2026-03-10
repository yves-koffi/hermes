CREATE TYPE tokenTypeEnum AS ENUM (
    'VERIFY_CODE',
    'VERIFY_TOKEN',
    'REFRESH_TOKEN'
    );

CREATE TABLE hash_tokens
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hash_token  VARCHAR(255) NOT NULL UNIQUE,
    token       VARCHAR(255),
    token_type  tokenTypeEnum    DEFAULT 'VERIFY_CODE',
    account_id  UUID         NOT NULL,
    expiry_date TIMESTAMPTZ  NOT NULL,
    ip_address  VARCHAR(32),
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ      DEFAULT now(),
    updated_at  TIMESTAMPTZ      DEFAULT now()
);
