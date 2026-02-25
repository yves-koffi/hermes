CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE accounts
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255)                   NOT NULL,
    email        VARCHAR(120)                   NOT NULL UNIQUE,
    avatar_url   VARCHAR(255),
    google_id    VARCHAR(255),
    apple_id     VARCHAR(255),
    provider     VARCHAR(24) DEFAULT 'google'
        CHECK (provider IN ('google', 'apple')),
    activated_at TIMESTAMP(0) WITHOUT TIME ZONE,
    created_at   TIMESTAMP(0) WITHOUT TIME ZONE DEFAULT now(),
    updated_at   TIMESTAMP(0) WITHOUT TIME ZONE DEFAULT now()
);


-- Fonction
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_accounts_updated_at
    BEFORE UPDATE ON accounts
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();