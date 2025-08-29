-- 포인트 거래 내역 테이블 생성 (엔티티: com.solsolhey.point.entity.PointTransaction)

CREATE TABLE IF NOT EXISTS point_transactions (
    transaction_id   BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    point_amount     INTEGER NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    description      VARCHAR(200),
    reference_id     BIGINT,
    reference_type   VARCHAR(20),
    balance_after    INTEGER NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_point_tx_user ON point_transactions(user_id);
CREATE INDEX IF NOT EXISTS ix_point_tx_created ON point_transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS ix_point_tx_type ON point_transactions(transaction_type);

ALTER TABLE point_transactions
    ADD CONSTRAINT fk_point_transactions_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

