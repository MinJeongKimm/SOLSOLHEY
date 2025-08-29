-- 상점 주문 테이블 생성 (엔티티: com.solsolhey.shop.domain.Order)

CREATE TABLE IF NOT EXISTS shop_orders (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    item_id      BIGINT,
    quantity     INTEGER NOT NULL DEFAULT 1,
    total_price  INTEGER NOT NULL,
    order_type   VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    gifticon_sku VARCHAR(255),
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_shop_orders_user ON shop_orders(user_id);
CREATE INDEX IF NOT EXISTS ix_shop_orders_created ON shop_orders(created_at DESC);
CREATE INDEX IF NOT EXISTS ix_shop_orders_type ON shop_orders(order_type);

-- 외래키 (가능 시) 아이템/사용자와 연결
ALTER TABLE shop_orders
    ADD CONSTRAINT fk_shop_orders_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- item_id는 shop_items(id)와 연결되지만, 일부 주문 타입(기프티콘)은 null일 수 있으므로 FK는 선택적으로 추후 추가 가능합니다.

