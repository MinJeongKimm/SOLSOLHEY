-- V3: Seed customization catalog (EQUIP wearable items + BACKGROUND base/stickers)
-- Idempotent inserts using WHERE NOT EXISTS

-- ========= EQUIP: wearable items =========
INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '하트 안경', null, 50, 'EQUIP', '/items/item_acc_glasses_heart.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '하트 안경');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '파랑 모자', null, 30, 'EQUIP', '/items/item_head_blue_cap.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '파랑 모자');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '토끼 머리띠', null, 120, 'EQUIP', '/items/item_head_bunny_ears.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '토끼 머리띠');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '콧수염', null, 100, 'EQUIP', '/items/item_head_mustache.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '콧수염');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '핑크 비니', null, 150, 'EQUIP', '/items/item_head_pink_beanie.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '핑크 비니');

-- ========= BACKGROUND: base (wallpaper-style) =========
INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '블루 배경', null, 500, 'BACKGROUND', '/backgrounds/base/bg_blue.png', 'base', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '블루 배경');

-- ========= BACKGROUND: stickers =========
INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT 'SSAFY 로고', null, 700, 'BACKGROUND', '/backgrounds/stickers/bg_ssafy.png', 'sticker', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = 'SSAFY 로고');

-- -- ========= (선택) backgrounds 테이블도 base 배경 멱등 시드 =========
-- INSERT INTO backgrounds (id, name, preview_url, enabled, created_at, updated_at)
-- SELECT 'bg_room_basic', '기본 방(블루)', '/backgrounds/base/bg_room_basic.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
-- WHERE NOT EXISTS (SELECT 1 FROM backgrounds WHERE id = 'bg_room_basic');

-- INSERT INTO backgrounds (id, name, preview_url, enabled, created_at, updated_at)
-- SELECT 'bg_campus_basic', 'SSAFY 캠퍼스', '/backgrounds/base/bg_campus_basic.png', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
-- WHERE NOT EXISTS (SELECT 1 FROM backgrounds WHERE id = 'bg_campus_basic');

