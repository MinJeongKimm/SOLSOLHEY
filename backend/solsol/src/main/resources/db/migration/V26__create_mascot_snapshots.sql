-- 마스코트 스냅샷 테이블 생성 (엔티티: com.solsolhey.mascot.domain.MascotSnapshot)

CREATE TABLE IF NOT EXISTS mascot_snapshots (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    mascot_id  BIGINT NOT NULL,
    image_url  VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_mascot_snapshots_user ON mascot_snapshots(user_id);
CREATE INDEX IF NOT EXISTS ix_mascot_snapshots_mascot ON mascot_snapshots(mascot_id);
CREATE INDEX IF NOT EXISTS ix_mascot_snapshots_created ON mascot_snapshots(created_at DESC);

ALTER TABLE mascot_snapshots
    ADD CONSTRAINT fk_mascot_snapshots_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE mascot_snapshots
    ADD CONSTRAINT fk_mascot_snapshots_mascot
        FOREIGN KEY (mascot_id) REFERENCES mascots(id) ON DELETE CASCADE;

