CREATE TABLE IF NOT EXISTS missed_day_markers
(
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES app_accounts(id) ON DELETE CASCADE,
    local_date DATE        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_missed_day_marker_user_local_date UNIQUE (user_id, local_date)
);

CREATE INDEX IF NOT EXISTS idx_missed_day_marker_user_id ON missed_day_markers (user_id);
