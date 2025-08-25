-- V2__add_test_data_and_fix_images.sql
-- 테스트 데이터 추가 및 배경 이미지 경로 수정

-- 테스트 사용자 추가
INSERT INTO users (user_id, username, email, password_hash, nickname, campus, total_points, is_active, created_at, updated_at) 
VALUES (1, 'testuser', 'test@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '테스트유저', '서울캠퍼스', 5000, true, NOW(), NOW())
ON CONFLICT (user_id) DO NOTHING;

-- 테스트 아이템 추가
INSERT INTO shop_items (id, name, description, price, type, image_url, category, is_active, created_at, updated_at) VALUES
(1, '하트 안경', '귀여운 하트 모양 안경으로 마스코트를 더욱 사랑스럽게 만들어요!', 100, 'EQUIP', '/items/item_acc_glasses_heart.png', 'accessory', true, NOW(), NOW()),
(2, '토끼 귀', '귀여운 토끼 귀 장식으로 마스코트를 더욱 귀엽게 만들어요!', 40, 'EQUIP', '/items/item_head_bunny_ears.png', 'head', true, NOW(), NOW()),
(3, '핑크 비니', '따뜻하고 귀여운 핑크 비니로 마스코트를 더욱 멋지게 만들어요!', 120, 'EQUIP', '/items/item_head_pink_beanie.png', 'head', true, NOW(), NOW()),
(4, '기본 배경', '깔끔하고 기본적인 배경으로 마스코트를 돋보이게 만들어요!', 50, 'BACKGROUND', '/backgrounds/base/bg_blue.png', 'background', true, NOW(), NOW()),
(5, '커스텀 배경', '특별한 커스텀 배경으로 마스코트를 더욱 독특하게 만들어요!', 80, 'BACKGROUND', '/backgrounds/base/bg_blue.png', 'background', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 테스트 사용자 아이템 보유
INSERT INTO shop_user_items (id, user_id, item_id, quantity, purchased_at) VALUES
(1, 1, 2, 1, NOW()),
(2, 1, 4, 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 테스트 배경 추가
INSERT INTO backgrounds (id, name, preview_url, enabled, tags) VALUES
('bg_base', '기본 배경', '/backgrounds/base/bg_blue.png', true, 'basic,default'),
('bg_customize', '커스터마이징 배경', '/backgrounds/base/bg_blue.png', true, 'customize,special')
ON CONFLICT (id) DO NOTHING;

-- 테스트 사용자 배경 보유
INSERT INTO user_backgrounds (id, user_id, background_id, acquired_at) VALUES
(1, 1, 'bg_base', NOW()),
(2, 1, 'bg_customize', NOW())
ON CONFLICT (id) DO NOTHING;
