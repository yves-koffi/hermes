CREATE TABLE emergency_contacts
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES app_accounts(id) ON DELETE CASCADE,
    name       VARCHAR(120),
    email      VARCHAR(160) NOT NULL,
    language   VARCHAR(16)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX ux_emergency_contact_user ON emergency_contacts(user_id);