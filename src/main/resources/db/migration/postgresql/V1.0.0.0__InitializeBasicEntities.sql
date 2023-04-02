START TRANSACTION;

-- uuid extensions to handle uuid types
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE apps
(
    app_uuid       UUID      NOT NULL DEFAULT gen_random_uuid(),

    app_id         INTEGER   NOT NULL,
    app_name       TEXT      NOT NULL,

    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    tracked        BOOLEAN   NOT NULL,
    last_item_scan TIMESTAMP          DEFAULT NULL,

    PRIMARY KEY (app_uuid),
    UNIQUE (app_id)
);

CREATE TABLE market_items
(
    market_item_uuid UUID      NOT NULL DEFAULT gen_random_uuid(),

    app_uuid         UUID      NOT NULL,
    context_id       BIGINT             DEFAULT NULL,
    asset_id         BIGINT             DEFAULT NULL,
    market_hash_name TEXT      NOT NULL,

    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    tracked          BOOLEAN   NOT NULL,
    last_item_scan   TIMESTAMP          DEFAULT NULL,

    PRIMARY KEY (market_item_uuid),
    UNIQUE (app_uuid, context_id, asset_id),

    CONSTRAINT fk_app
        FOREIGN KEY (app_uuid)
            REFERENCES apps (app_uuid)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE market_snapshots
(
    market_snapshot_uuid UUID      NOT NULL DEFAULT gen_random_uuid(),

    market_item_uuid     UUID      NOT NULL,

    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    volume               INTEGER            DEFAULT NULL,
    lowest_price         FLOAT              DEFAULT NULL,
    median_price         FLOAT              DEFAULT NULL,
    listings             INTEGER            DEFAULT NULL,
    listing_price        FLOAT              DEFAULT NULL,
    currency             VARCHAR(3)         DEFAULT NULL,

    PRIMARY KEY (market_snapshot_uuid),
    CONSTRAINT fk_market_item
        FOREIGN KEY (market_item_uuid)
            REFERENCES market_items (market_item_uuid)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE user_inventories
(
    user_inventory_uuid UUID      NOT NULL DEFAULT gen_random_uuid(),

    user_id             BIGINT    NOT NULL,
    app_uuid            UUID      NOT NULL,

    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    tracked             BOOLEAN   NOT NULL,
    last_item_scan      timestamp          DEFAULT NULL,

    PRIMARY KEY (user_inventory_uuid),
    UNIQUE (user_id, app_uuid),
    CONSTRAINT fk_app
        FOREIGN KEY (app_uuid)
            REFERENCES apps (app_uuid)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE inventory_items
(
    inventory_items_uuid UUID      NOT NULL DEFAULT gen_random_uuid(),

    user_inventory_uuid  UUID      NOT NULL,
    market_item_uuid     uuid      NOT NULL,

    amount               INT       NOT NULL DEFAULT 0,
    createdAt            TIMESTAMP NOT NULL DEFAULT NOW(),
    superseded           TIMESTAMP          DEFAULT NULL,
    automaticFetched     BOOLEAN   NOT NULL,

    PRIMARY KEY (inventory_items_uuid),
    CONSTRAINT fk_user_inv
        FOREIGN KEY (user_inventory_uuid)
            REFERENCES user_inventories (user_inventory_uuid)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_market_item
        FOREIGN KEY (market_item_uuid)
            REFERENCES market_items (market_item_uuid)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

COMMIT;