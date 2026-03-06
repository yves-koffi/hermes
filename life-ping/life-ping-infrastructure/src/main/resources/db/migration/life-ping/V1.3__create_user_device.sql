CREATE TABLE user_devices
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    account_id   UUID        NOT NULL REFERENCES app_accounts(id) ON DELETE CASCADE,
    platform     VARCHAR(16) NOT NULL, -- ANDROID / IOS
    fcm_token    TEXT        NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    soft_deleted_at TIMESTAMP NULL,
    UNIQUE (account_id, fcm_token)
);

CREATE INDEX idx_user_device_account ON user_devices(account_id);
CREATE INDEX idx_user_device_last_seen ON user_devices(last_seen_at);
