-- =====================================================
-- 꾸미기 아이템 추가: 마스코트 head 아이템 가발
-- =====================================================


-- INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at)
-- SELECT '파란 안경', null, 80, 'EQUIP', '/items/item_acc_blue_glasses.png', 'accessory', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
--  WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '파란 안경');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at, required_level)
SELECT '가발v1', null, 40, 'EQUIP', '/items/item_head_wig_v1.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '가발v1');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at, required_level)
SELECT '가발v2', null, 60, 'EQUIP', '/items/item_head_wig_v2.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '가발v2');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at, required_level)
SELECT '가발v3', null, 55, 'EQUIP', '/items/item_head_wig_v3.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '가발v3');

INSERT INTO shop_items (name, description, price, type, image_url, category, is_active, created_at, updated_at, required_level)
SELECT '가발v4', null, 65, 'EQUIP', '/items/item_head_wig_v4.png', 'head', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1
 WHERE NOT EXISTS (SELECT 1 FROM shop_items WHERE name = '가발v4');
