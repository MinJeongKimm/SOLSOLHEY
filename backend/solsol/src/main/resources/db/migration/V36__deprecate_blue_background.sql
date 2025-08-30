-- V36: Deprecate legacy blue background asset and references
-- The asset frontend/public/backgrounds/base/bg_blue.png was removed.
-- Soft-disable any catalog entries referencing it and clear the URL.

-- 1) Deactivate specific named item if present
UPDATE shop_items
   SET is_active = FALSE,
       image_url = NULL,
       updated_at = CURRENT_TIMESTAMP
 WHERE name = '블루 배경';

-- 2) Blanket cleanup by URL (in case of data drift)
UPDATE shop_items
   SET is_active = FALSE,
       image_url = NULL,
       updated_at = CURRENT_TIMESTAMP
 WHERE image_url = '/backgrounds/base/bg_blue.png';
