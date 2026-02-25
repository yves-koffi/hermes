CREATE TABLE app_accounts
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    user_name          VARCHAR(225),
    app_uuid           VARCHAR(255) NOT NULL UNIQUE,

    device_unique_id   VARCHAR(128) NOT NULL,               -- identifiant unique du device (ANDROID_ID / IDFV)
    device_model       VARCHAR(100) NOT NULL,
    device_platform    VARCHAR(16)  NOT NULL,               -- ANDROID / IOS

    timezone           VARCHAR(64)  NOT NULL DEFAULT 'UTC', -- fuseau horaire IANA
    callback_time      TIME,                                --(callbackTime) heure de rappel quotidienne
    check_in_frequency INT          NOT NULL DEFAULT 1440,  -- fréquence de pointage 1 jour
    threshold_period   INT          NOT NULL DEFAULT 2880,  -- période de seuil 2 jours

    last_checkin_at    TIMESTAMP    NULL,                   -- dernière confirmation
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Useful indexes
CREATE INDEX idx_app_user_platform ON app_accounts (device_platform);
CREATE INDEX idx_app_user_updated_at ON app_accounts (updated_at);
