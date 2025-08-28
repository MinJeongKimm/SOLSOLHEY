-- V7: Update customization shop catalog (soft-delete, update, add new)

-- 1) SOFT DELETE (hide from shop)
-- UPDATE shop_items
--    SET is_active = FALSE,
--        updated_at = CURRENT_TIMESTAMP
--  WHERE name IN (
--        '콧수염',
--        '핑크 비니'
--  );

-- 2) UPDATE EXISTING (price/image/category)
-- UPDATE shop_items
--    SET price = 120,
--        image_url = '/items/item_head_blue_cap_v2.png',
--        category = 'head',
--        updated_at = CURRENT_TIMESTAMP
--  WHERE name = '파랑 모자';

-- 2-b) RENAME (display name)
-- UPDATE shop_items
--    SET name = '블루 캡',
--        updated_at = CURRENT_TIMESTAMP
--  WHERE name = '파랑 모자';

-- 2-c) Example tweak: adjust price of heart glasses
-- UPDATE shop_items
--    SET price = 60,
--        updated_at = CURRENT_TIMESTAMP
--  WHERE name = '하트 안경';

-- 3) ADD NEW (idempotent inserts)

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '파란 안경', null, 80, 'EQUIP', '/items/item_acc_blue_glasses.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '파란 안경');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '미니 가방', null, 110, 'EQUIP', '/items/item_acc_hand_bag.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '미니 가방');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '아이스크림', null, 70, 'EQUIP', '/items/item_acc_icecream.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '아이스크림');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '리본 넥타이', null, 50, 'EQUIP', '/items/item_acc_ribbon_tie.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '리본 넥타이');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '파란 줄무늬 상의', null, 120, 'EQUIP', '/items/item_cloth_blue_stripe.png', 'clothing', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '파란 줄무늬 상의');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '고양이 귀 모자', null, 300, 'EQUIP', '/items/item_head_yellow_cap_with_ears.png', 'clothing', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '고양이 귀 모자');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '노란 모자', null, 150, 'EQUIP', '/items/item_head_yellow_cap.png', 'clothing', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '노란 모자');

-- 4) ADD NEW (idempotent inserts) - 배경 스티커
INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '구름', null, 30, 'BACKGROUND', '/backgrounds/stickers/bg_cloud.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '구름');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '오렌지', null, 30, 'BACKGROUND', '/backgrounds/stickers/bg_orange.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '오렌지');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '별', null, 30, 'BACKGROUND', '/backgrounds/stickers/bg_star.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '별');

