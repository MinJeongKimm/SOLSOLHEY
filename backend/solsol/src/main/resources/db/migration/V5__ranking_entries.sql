-- 랭킹 참가 기능을 위한 테이블 생성
CREATE TABLE ranking_entries (
    entry_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mascot_id BIGINT NOT NULL, -- 마스코트 ID 추가
    mascot_snapshot_id BIGINT, -- nullable로 변경 (이미지 업로드 방식 지원)
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL, -- 필수로 변경
    image_url VARCHAR(500), -- 이미지 URL 저장
    ranking_type VARCHAR(20) NOT NULL DEFAULT 'NATIONAL', -- 전국/교내 구분
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_user_id ON ranking_entries(user_id);
CREATE INDEX idx_mascot_id ON ranking_entries(mascot_id);
CREATE INDEX idx_mascot_snapshot_id ON ranking_entries(mascot_snapshot_id);
CREATE INDEX idx_created_at ON ranking_entries(created_at DESC);

-- 새로운 인덱스 (ranking_type 포함)
CREATE INDEX idx_ranking_entries_user_type ON ranking_entries(user_id, ranking_type);
CREATE INDEX idx_ranking_entries_type_created ON ranking_entries(ranking_type, created_at DESC);

-- 제약사항 추가
-- 사용자당 최대 3개 참가 제한을 위한 체크 제약 (애플리케이션에서도 검증)
ALTER TABLE ranking_entries ADD CONSTRAINT chk_max_entries_per_user 
    CHECK (user_id IS NOT NULL);

-- 마스코트 스냅샷 중복 참가 방지를 위한 유니크 제약 (mascot_snapshot_id가 null이 아닐 때만)
-- 이미지 업로드 방식에서는 mascot_snapshot_id가 null이므로 유니크 제약 불필요
-- ALTER TABLE ranking_entries ADD CONSTRAINT uk_mascot_snapshot 
--     UNIQUE (mascot_snapshot_id);

-- 외래키 제약 (실제 구현시 추가)
-- ALTER TABLE ranking_entries ADD CONSTRAINT fk_ranking_entries_user 
--     FOREIGN KEY (user_id) REFERENCES users(user_id);
-- ALTER TABLE ranking_entries ADD CONSTRAINT fk_ranking_entries_mascot_snapshot 
--     FOREIGN KEY (mascot_snapshot_id) REFERENCES mascot_snapshots(id);

-- 댓글 추가
COMMENT ON TABLE ranking_entries IS '랭킹 참가 정보 테이블';
COMMENT ON COLUMN ranking_entries.entry_id IS '참가 ID (기본키)';
COMMENT ON COLUMN ranking_entries.user_id IS '사용자 ID';
COMMENT ON COLUMN ranking_entries.mascot_id IS '마스코트 ID';
COMMENT ON COLUMN ranking_entries.mascot_snapshot_id IS '마스코트 스냅샷 ID (null 가능, 이미지 업로드 방식)';
COMMENT ON COLUMN ranking_entries.title IS '참가 제목 (최대 100자)';
COMMENT ON COLUMN ranking_entries.description IS '참가 설명 (최대 500자, 필수)';
COMMENT ON COLUMN ranking_entries.image_url IS '마스코트 이미지 URL';
COMMENT ON COLUMN ranking_entries.ranking_type IS '랭킹 타입 (NATIONAL/CAMPUS)';
COMMENT ON COLUMN ranking_entries.created_at IS '등록일시';
COMMENT ON COLUMN ranking_entries.updated_at IS '수정일시';

-- ===== 투표 시스템 생성 =====
-- Vote 테이블 생성
CREATE TABLE votes (
    vote_id BIGSERIAL PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    mascot_id BIGINT NOT NULL,
    voter_id BIGINT NOT NULL,
    vote_type VARCHAR(20) NOT NULL, -- CAMPUS, NATIONAL
    weight INTEGER NOT NULL DEFAULT 1,
    idempotency_key VARCHAR(100),
    campus_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 투표 시스템 인덱스 생성
CREATE INDEX idx_voter_entry ON votes (voter_id, entry_id);
CREATE INDEX idx_entry_id ON votes (entry_id);
CREATE INDEX idx_idempotency_key ON votes (idempotency_key);

-- 투표 시스템 댓글 추가
COMMENT ON TABLE votes IS '투표 기록 테이블';
COMMENT ON COLUMN votes.vote_id IS '투표 ID (기본키)';
COMMENT ON COLUMN votes.entry_id IS '랭킹 엔트리 ID (투표 대상)';
COMMENT ON COLUMN votes.mascot_id IS '마스코트 ID';
COMMENT ON COLUMN votes.voter_id IS '투표자 ID';
COMMENT ON COLUMN votes.vote_type IS '투표 타입 (CAMPUS/NATIONAL)';
COMMENT ON COLUMN votes.weight IS '투표 가중치';
COMMENT ON COLUMN votes.idempotency_key IS '멱등키 (중복 투표 방지)';
COMMENT ON COLUMN votes.campus_id IS '캠퍼스 ID';
COMMENT ON COLUMN votes.created_at IS '투표 일시';
COMMENT ON COLUMN votes.updated_at IS '수정 일시';

