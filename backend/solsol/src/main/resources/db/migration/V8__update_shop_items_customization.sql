-- V7: Update customization shop catalog (soft-delete, update, add new)


-- 1) UPDATE EXISTING (price/image/category)
UPDATE shop_items
   SET price = 120,
       type = 'EQUIP',
       image_url = '/backgrounds/stickers/bg_ssafy.png',
       category = 'accessory',
       updated_at = CURRENT_TIMESTAMP
 WHERE name = 'SSAFY 로고';


UPDATE shop_items
   SET price = 300,
       image_url = '/backgrounds/base/bg_blue.png',
       category = 'base',
       updated_at = CURRENT_TIMESTAMP
 WHERE name = '블루 배경';

-- 2) ADD NEW (idempotent inserts)

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
SELECT '고양이 귀 모자', null, 300, 'EQUIP', '/items/item_head_yellow_cap_with_ears.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '고양이 귀 모자');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '노란 모자', null, 150, 'EQUIP', '/items/item_head_yellow_cap.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '노란 모자');

-- 3) ADD NEW (idempotent inserts) - 배경 스티커
INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '구름', null, 30, 'EQUIP', '/backgrounds/stickers/bg_cloud.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '구름');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '오렌지', null, 30, 'EQUIP', '/backgrounds/stickers/bg_orange.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '오렌지');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '별', null, 30, 'EQUIP', '/backgrounds/stickers/bg_star.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '별');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v1', null, 70, 'EQUIP', '/backgrounds/stickers/bg_stickers_v1.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v1');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v2', null, 70, 'EQUIP', '/backgrounds/stickers/bg_stickers_v2.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v2');

 INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v3', null, 40, 'EQUIP', '/backgrounds/stickers/bg_stickers_v3.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v3');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v4', null, 40, 'EQUIP', '/backgrounds/stickers/bg_stickers_v4.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v4');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v5', null, 50, 'EQUIP', '/backgrounds/stickers/bg_stickers_v5.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v5');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v6', null, 30, 'EQUIP', '/backgrounds/stickers/bg_stickers_v6.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v6');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v7', null, 50, 'EQUIP', '/backgrounds/stickers/bg_stickers_v7.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v7');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v8', null, 60, 'EQUIP', '/backgrounds/stickers/bg_stickers_v8.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v8');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '말풍선v9', null, 50, 'EQUIP', '/backgrounds/stickers/bg_stickers_v9.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '말풍선v9');


-- 4) ADD NEW (idempotent inserts) - 배경
INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '배경v1', null, 50, 'BACKGROUND', '/backgrounds/base/bg_v1.jpg', 'base', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '배경v1');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '배경v2', null, 50, 'BACKGROUND', '/backgrounds/base/bg_v2.jpg', 'base', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '배경v2');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '배경v3', null, 50, 'BACKGROUND', '/backgrounds/base/bg_v3.jpg', 'base', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '배경v3');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '배경v4', null, 50, 'BACKGROUND', '/backgrounds/base/bg_v4.jpg', 'base', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '배경v4');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '배경v5', null, 50, 'BACKGROUND', '/backgrounds/base/bg_v5.jpg', 'base', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '배경v5');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
SELECT '배경v6', null, 50, 'BACKGROUND', '/backgrounds/base/bg_v6.jpg', 'base', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '배경v6');