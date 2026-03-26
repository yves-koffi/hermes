ALTER TABLE accounts
    ALTER COLUMN provider DROP DEFAULT;

UPDATE accounts
SET email = lower(trim(email))
WHERE email <> lower(trim(email));

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_basic_password
        CHECK (provider <> 'basic' OR password IS NOT NULL);

CREATE UNIQUE INDEX IF NOT EXISTS uk_accounts_email_lower
    ON accounts ((lower(email)));
