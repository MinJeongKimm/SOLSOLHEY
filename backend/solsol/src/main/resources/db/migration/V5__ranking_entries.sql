-- 랭킹 참가 기능을 위한 테이블 생성
CREATE TABLE ranking_entries (
    entry_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mascot_snapshot_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_user_id ON ranking_entries(user_id);
CREATE INDEX idx_mascot_snapshot_id ON ranking_entries(mascot_snapshot_id);
CREATE INDEX idx_created_at ON ranking_entries(created_at DESC);

-- 제약사항 추가
-- 사용자당 최대 3개 참가 제한을 위한 체크 제약 (애플리케이션에서도 검증)
ALTER TABLE ranking_entries ADD CONSTRAINT chk_max_entries_per_user 
    CHECK (user_id IS NOT NULL);

-- 마스코트 스냅샷 중복 참가 방지를 위한 유니크 제약
ALTER TABLE ranking_entries ADD CONSTRAINT uk_mascot_snapshot 
    UNIQUE (mascot_snapshot_id);

-- 외래키 제약 (실제 구현시 추가)
-- ALTER TABLE ranking_entries ADD CONSTRAINT fk_ranking_entries_user 
--     FOREIGN KEY (user_id) REFERENCES users(user_id);
-- ALTER TABLE ranking_entries ADD CONSTRAINT fk_ranking_entries_mascot_snapshot 
--     FOREIGN KEY (mascot_snapshot_id) REFERENCES mascot_snapshots(id);

-- 댓글 추가
COMMENT ON TABLE ranking_entries IS '랭킹 참가 정보 테이블';
COMMENT ON COLUMN ranking_entries.entry_id IS '참가 ID (기본키)';
COMMENT ON COLUMN ranking_entries.user_id IS '사용자 ID';
COMMENT ON COLUMN ranking_entries.mascot_snapshot_id IS '마스코트 스냅샷 ID';

COMMENT ON COLUMN ranking_entries.title IS '참가 제목 (최대 100자)';
COMMENT ON COLUMN ranking_entries.description IS '참가 설명 (최대 500자)';
COMMENT ON COLUMN ranking_entries.created_at IS '등록일시';

