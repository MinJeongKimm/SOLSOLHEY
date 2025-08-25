-- =====================================================
-- SOLSOLHEY PostgreSQL 데이터베이스 초기화 스크립트
-- =====================================================

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL UNIQUE,
    campus VARCHAR(255),
    total_points INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 아이템 테이블
CREATE TABLE IF NOT EXISTS shop_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price INT NOT NULL,
    type VARCHAR(50) NOT NULL, -- EQUIP, BACKGROUND, ETC
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

-- 캠퍼스 테이블
CREATE TABLE IF NOT EXISTS campus (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_shop_user_items_user_id ON shop_user_items(user_id);
CREATE INDEX IF NOT EXISTS idx_shop_user_items_item_id ON shop_user_items(item_id);
CREATE INDEX IF NOT EXISTS idx_user_backgrounds_user_id ON user_backgrounds(user_id);
CREATE INDEX IF NOT EXISTS idx_user_backgrounds_background_id ON user_backgrounds(background_id);
CREATE INDEX IF NOT EXISTS idx_backgrounds_enabled ON backgrounds(enabled);

-- =====================================================
-- 초기 데이터 삽입
-- =====================================================

-- 캠퍼스 데이터 삽입
-- H2(PostgreSQL 모드)와 PostgreSQL 모두 호환되도록, 중복 방지는 WHERE NOT EXISTS로 처리
INSERT INTO campus (name)
SELECT 'SSAFY 서울캠퍼스' WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'SSAFY 서울캠퍼스');
INSERT INTO campus (name)
SELECT 'SSAFY 대전캠퍼스' WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'SSAFY 대전캠퍼스');
INSERT INTO campus (name)
SELECT 'SSAFY 광주캠퍼스' WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'SSAFY 광주캠퍼스');
INSERT INTO campus (name)
SELECT 'SSAFY 구미캠퍼스' WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'SSAFY 구미캠퍼스');
INSERT INTO campus (name)
SELECT 'SSAFY 부울경캠퍼스' WHERE NOT EXISTS (SELECT 1 FROM campus WHERE name = 'SSAFY 부울경캠퍼스');
