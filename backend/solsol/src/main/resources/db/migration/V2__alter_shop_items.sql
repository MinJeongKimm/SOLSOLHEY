-- V2: Adjust shop_items schema for customization catalog

-- 1) Add category column (nullable first), then backfill default and enforce NOT NULL
ALTER TABLE shop_items 
    ADD COLUMN IF NOT EXISTS category VARCHAR(50);

UPDATE shop_items SET category = 'head' WHERE category IS NULL;

ALTER TABLE shop_items 
    ALTER COLUMN category SET NOT NULL;

-- 2) Enlarge image_url to 500 chars for longer public paths
ALTER TABLE shop_items 
    ALTER COLUMN image_url TYPE VARCHAR(500);

-- 3) Helpful indexes
CREATE INDEX IF NOT EXISTS idx_shop_items_type ON shop_items(type);
CREATE INDEX IF NOT EXISTS idx_shop_items_category ON shop_items(category);

