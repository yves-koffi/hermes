CREATE TYPE ProductType AS ENUM ('Subscription', 'Consumable', 'NonConsumable');
CREATE TYPE Platform AS ENUM ('IOS', 'ANDROID');

CREATE TABLE IF NOT EXISTS products
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Une clé primaire auto-incrémentée pour identifier chaque produit de manière unique
    platform     Platform            NOT NULL,
    type         ProductType         NOT NULL,
    external_product_id   VARCHAR(255) UNIQUE NOT NULL,               -- L'ID du produit, doit être unique
    package_name VARCHAR(255)        NOT NULL,
    bundle_id    VARCHAR(255),                               -- Peut être vide, donc pas NOT NULL
    app_apple_id BIGINT,                                     -- Correspond à 'long' en Java, peut être 0
    deleted_at   TIMESTAMPTZ      DEFAULT NULL,
    created_at   TIMESTAMPTZ      DEFAULT now(),
    updated_at   TIMESTAMPTZ      DEFAULT now()
);

-- Fonction
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE
    ON products
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();