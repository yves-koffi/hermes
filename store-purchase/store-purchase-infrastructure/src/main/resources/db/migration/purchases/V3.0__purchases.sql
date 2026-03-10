CREATE TABLE purchases
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    iap_source             VARCHAR(20)                 DEFAULT 'google_play' CHECK (iap_source IN ('google_play','app_store')),
    order_id               VARCHAR(255) UNIQUE NOT NULL,
    external_product_id             VARCHAR(255)        NOT NULL,
    purchase_date          VARCHAR(255)        NOT NULL,
    expiry_date           VARCHAR(255),
    purchase_type           VARCHAR(20)                 DEFAULT 'nonSubscription' CHECK (purchase_type IN ('subscription', 'nonSubscription')),
    status                  VARCHAR(20)        NOT NULL CHECK (status IN ('pending', 'completed', 'cancelled', 'active', 'expired')),
    account_id            UUID NOT NULL,
    deleted_at  TIMESTAMPTZ DEFAULT now(),
    created_at  TIMESTAMPTZ DEFAULT now(),
    updated_at  TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE purchases
    ADD CONSTRAINT chk_purchases_status_by_type
        CHECK (
            (purchase_type = 'nonSubscription' AND status IN ('pending', 'completed', 'cancelled'))
                OR
            (purchase_type = 'subscription' AND status IN ('pending', 'active', 'expired'))
        );

-- Fonction
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_purchases_updated_at
    BEFORE UPDATE ON purchases
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
