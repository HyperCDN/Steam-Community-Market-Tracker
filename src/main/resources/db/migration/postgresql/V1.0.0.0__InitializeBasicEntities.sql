START TRANSACTION;

-- uuid extensions to handle uuid types
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- trgm to calculate proximity on strings
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TABLE apps
(
    app_id     INTEGER   NOT NULL,
    app_name   TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    tracked    BOOLEAN   NOT NULL,

    PRIMARY KEY (app_id)
);

CREATE TABLE market_items
(
    app_id           INTEGER   NOT NULL,
    market_hash_name TEXT      NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    tracked          BOOLEAN   NOT NULL,

    PRIMARY KEY (app_id, market_hash_name),
    CONSTRAINT fk_app
        FOREIGN KEY (app_id)
            REFERENCES apps (app_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE market_snapshots
(
    market_snapshot_id UUID      NOT NULL DEFAULT gen_random_uuid(),
    app_id             INTEGER   NOT NULL,
    market_hash_name   TEXT      NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    volume             INTEGER            DEFAULT NULL,
    lowest_price       FLOAT              DEFAULT NULL,
    median_price       FLOAT              DEFAULT NULL,
    listings           INTEGER            DEFAULT NULL,
    listing_price      FLOAT              DEFAULT NULL,
    currency           VARCHAR(3)         DEFAULT NULL,

    PRIMARY KEY (market_snapshot_id),
    CONSTRAINT fk_app
        FOREIGN KEY (app_id)
            REFERENCES apps (app_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_market_item
        FOREIGN KEY (app_id, market_hash_name)
            REFERENCES market_items (app_id, market_hash_name)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

COMMIT;