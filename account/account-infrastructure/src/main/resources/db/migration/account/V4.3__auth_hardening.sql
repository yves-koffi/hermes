ALTER TYPE tokenTypeEnum ADD VALUE IF NOT EXISTS 'EMAIL_VERIFICATION_CODE';
ALTER TYPE tokenTypeEnum ADD VALUE IF NOT EXISTS 'EMAIL_VERIFICATION_LINK';
ALTER TYPE tokenTypeEnum ADD VALUE IF NOT EXISTS 'PASSWORD_RESET_CODE';
ALTER TYPE tokenTypeEnum ADD VALUE IF NOT EXISTS 'PASSWORD_RESET_LINK';
ALTER TYPE tokenTypeEnum ADD VALUE IF NOT EXISTS 'SESSION_REFRESH';


CREATE TABLE auth_sessions
(
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id              UUID         NOT NULL,
    refresh_token_hash      VARCHAR(255) NOT NULL UNIQUE,
    expiry_date             TIMESTAMPTZ  NOT NULL,
    ip_address              VARCHAR(64),
    user_agent              VARCHAR(512),
    rotated_from_session_id UUID,
    last_used_at            TIMESTAMPTZ,
    revoked_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ      DEFAULT now(),
    updated_at              TIMESTAMPTZ      DEFAULT now()
);

CREATE INDEX idx_auth_sessions_account_id ON auth_sessions (account_id);
CREATE INDEX idx_auth_sessions_rotated_from ON auth_sessions (rotated_from_session_id);

CREATE TRIGGER trg_auth_sessions_updated_at
    BEFORE UPDATE
    ON auth_sessions
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
