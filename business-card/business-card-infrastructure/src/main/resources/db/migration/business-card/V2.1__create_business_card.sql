CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE business_cards
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uid              VARCHAR(255)                           NOT NULL,
    account_id       UUID                           NOT NULL,
    raw              JSONB,
    type             VARCHAR(16) DEFAULT 'main'
        CHECK (type IN ('main', 'shared')),
    avatar_url   VARCHAR(255),
    soft_deleted_at  TIMESTAMPTZ,
    save_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at       TIMESTAMPTZ DEFAULT now(),
    updated_at       TIMESTAMPTZ DEFAULT now(),

    CONSTRAINT fk_business_cards_account_id
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_business_cards_updated_at
    BEFORE UPDATE ON business_cards
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_business_cards_account_id ON business_cards(account_id);
CREATE INDEX idx_business_cards_uid ON business_cards(uid);
CREATE INDEX idx_business_cards_soft_deleted_at ON business_cards(soft_deleted_at);