CREATE TABLE user_devices
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES app_accounts(id) ON DELETE CASCADE,
    platform     VARCHAR(16) NOT NULL, -- ANDROID / IOS
    fcm_token    TEXT        NOT NULL,
    last_seen_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, fcm_token)
);

CREATE INDEX idx_user_device_user ON user_devices(user_id);
CREATE INDEX idx_user_device_last_seen ON user_devices(last_seen_at);