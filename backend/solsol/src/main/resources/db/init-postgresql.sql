-- =====================================================
-- SOLSOLHEY PostgreSQL 데이터베이스 초기화 스크립트
-- =====================================================

-- 사용자 아이템 보유 테이블
CREATE TABLE IF NOT EXISTS shop_user_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    purchased_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_user_items_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_user_items_item_id FOREIGN KEY (item_id) REFERENCES shop_items(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_item UNIQUE (user_id, item_id)
);

-- 배경 테이블
CREATE TABLE IF NOT EXISTS backgrounds (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    preview_url VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tags TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 배경 보유 테이블
CREATE TABLE IF NOT EXISTS user_backgrounds (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    background_id VARCHAR(50) NOT NULL,
    acquired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_backgrounds_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_backgrounds_background_id FOREIGN KEY (background_id) REFERENCES backgrounds(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_background UNIQUE (user_id, background_id)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_shop_user_items_user_id ON shop_user_items(user_id);
CREATE INDEX IF NOT EXISTS idx_shop_user_items_item_id ON shop_user_items(item_id);
CREATE INDEX IF NOT EXISTS idx_user_backgrounds_user_id ON user_backgrounds(user_id);
CREATE INDEX IF NOT EXISTS idx_user_backgrounds_background_id ON user_backgrounds(background_id);
CREATE INDEX IF NOT EXISTS idx_backgrounds_enabled ON backgrounds(enabled);

-- =====================================================
-- 테스트 데이터 삽입
-- =====================================================

-- 테스트 사용자 (ON CONFLICT DO NOTHING 사용)
INSERT INTO users (user_id, username, email, password_hash, nickname, campus, total_points, is_active, created_at, updated_at) 
VALUES (1, 'testuser', 'test@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '테스트유저', '서울캠퍼스', 5000, true, NOW(), NOW())
ON CONFLICT (user_id) DO NOTHING;

-- 테스트 아이템 (ON CONFLICT DO NOTHING 사용)
INSERT INTO shop_items (id, name, description, price, type, image_url, is_active, created_at, updated_at) VALUES
(1001, '토끼 귀', '귀여운 토끼 귀 머리띠입니다.', 800, 'EQUIP', '/items/item_head_bunny_ears.png', true, NOW(), NOW()),
(1002, '핑크 비니', '따뜻하고 스타일리시한 핑크 비니입니다.', 500, 'EQUIP', '/items/item_head_pink_beanie.png', true, NOW(), NOW()),
(2001, '하트 안경', '사랑스러운 하트 모양 안경입니다.', 1200, 'EQUIP', '/items/item_acc_glasses_heart.png', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 테스트 사용자 아이템 보유 (ON CONFLICT DO NOTHING 사용)
INSERT INTO shop_user_items (id, user_id, item_id, quantity, purchased_at) VALUES
(1, 1, 1001, 1, NOW()),
(2, 1, 1002, 1, NOW()),
(3, 1, 2001, 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 테스트 배경 (ON CONFLICT DO NOTHING 사용)
INSERT INTO backgrounds (id, name, preview_url, enabled, tags) VALUES
('bg_base', '기본 배경', '/backgrounds/base/bg_blue.png', true, 'basic,default'),
('bg_customize', '커스터마이징 배경', '/backgrounds/base/bg_blue.png', true, 'customize,special')
ON CONFLICT (id) DO NOTHING;

-- 테스트 사용자 배경 보유 (ON CONFLICT DO NOTHING 사용)
INSERT INTO user_backgrounds (id, user_id, background_id, acquired_at) VALUES
(1, 1, 'bg_base', NOW()),
(2, 1, 'bg_customize', NOW())
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 테이블 생성 확인 쿼리
-- =====================================================

-- 테이블 목록 확인
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- 테이블 구조 확인
-- \d shop_user_items
-- \d backgrounds
-- \d user_backgrounds

-- 테스트 데이터 확인
-- SELECT * FROM shop_user_items WHERE user_id = 1;
-- SELECT * FROM backgrounds WHERE enabled = true;
-- SELECT * FROM user_backgrounds WHERE user_id = 1;
