-- V6: Create locker table for purchased gifticons

CREATE TABLE IF NOT EXISTS shop_user_gifticons (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    barcode VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_gifticons_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_user_gifticons_user_id ON shop_user_gifticons(user_id);
CREATE INDEX IF NOT EXISTS idx_user_gifticons_status ON shop_user_gifticons(status);
CREATE INDEX IF NOT EXISTS idx_user_gifticons_created_at ON shop_user_gifticons(created_at);

