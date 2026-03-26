CREATE TYPE tokenTypeEnum AS ENUM (
    'EMAIL_VERIFICATION_CODE',
    'EMAIL_VERIFICATION_LINK',
    'PASSWORD_RESET_CODE',
    'PASSWORD_RESET_LINK',
    'SESSION_REFRESH'
    );

CREATE TABLE hash_tokens
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hash_token  VARCHAR(255) NOT NULL UNIQUE,
    token_type  tokenTypeEnum    DEFAULT 'EMAIL_VERIFICATION_CODE',
    account_id  UUID         NOT NULL,
    expiry_date TIMESTAMPTZ  NOT NULL,
    ip_address  VARCHAR(32),
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ      DEFAULT now(),
    updated_at  TIMESTAMPTZ      DEFAULT now()
);
