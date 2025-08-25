-- V1__initial_schema.sql
-- 기본 데이터베이스 스키마 생성

-- users 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    campus VARCHAR(50),
    total_points INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- shop_items 테이블
CREATE TABLE IF NOT EXISTS shop_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL,
    image_url VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- shop_user_items 테이블
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

-- backgrounds 테이블
CREATE TABLE IF NOT EXISTS backgrounds (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    preview_url VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tags TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- user_backgrounds 테이블
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
CREATE INDEX IF NOT EXISTS idx_shop_items_category ON shop_items(category);
CREATE INDEX IF NOT EXISTS idx_shop_items_type_category ON shop_items(type, category);
CREATE INDEX IF NOT EXISTS idx_shop_user_items_user_id ON shop_user_items(user_id);
CREATE INDEX IF NOT EXISTS idx_shop_user_items_item_id ON shop_user_items(item_id);
CREATE INDEX IF NOT EXISTS idx_user_backgrounds_user_id ON user_backgrounds(user_id);
CREATE INDEX IF NOT EXISTS idx_user_backgrounds_background_id ON user_backgrounds(background_id);
CREATE INDEX IF NOT EXISTS idx_backgrounds_enabled ON backgrounds(enabled);
