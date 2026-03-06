CREATE TABLE daily_checkins
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id    UUID        NOT NULL REFERENCES app_accounts(id) ON DELETE CASCADE,
    local_date    DATE        NOT NULL, -- date locale (timezone user)
    checked_in_at TIMESTAMP   NOT NULL,
    source        VARCHAR(16) NOT NULL, -- MOBILE / SYNC
    UNIQUE (account_id, local_date)
);

CREATE INDEX idx_daily_checkin_account_date ON daily_checkins (account_id, local_date);
CREATE INDEX idx_daily_checkin_checked_at ON daily_checkins (checked_in_at);
