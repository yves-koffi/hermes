ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS disabled_at TIMESTAMPTZ;

CREATE TABLE IF NOT EXISTS account_security_events
(
    id          UUID PRIMARY KEY,
    account_id  UUID        NOT NULL,
    event_type  VARCHAR(48) NOT NULL,
    detail      VARCHAR(255),
    ip_address  VARCHAR(64),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_account_security_events_account_id_occurred_at
    ON account_security_events (account_id, occurred_at DESC);
