START TRANSACTION;

ALTER TABLE market_snapshots RENAME TO market_item_snapshots;
ALTER TABLE inventory_items RENAME TO user_inventory_item_snapshots;

ALTER TABLE apps
    RENAME COLUMN app_uuid TO __uuid;
ALTER TABLE market_items
    RENAME COLUMN market_item_uuid TO __uuid;
ALTER TABLE market_item_snapshots
    RENAME COLUMN market_snapshot_uuid TO __uuid;
ALTER TABLE user_inventories
    RENAME COLUMN user_inventory_uuid TO __uuid;
ALTER TABLE user_inventory_item_snapshots
    RENAME COLUMN inventory_items_uuid TO __uuid;

COMMIT;