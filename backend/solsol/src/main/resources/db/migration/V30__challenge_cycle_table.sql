-- Add reset_policy column to challenge (for selective resets)
ALTER TABLE challenge
    ADD COLUMN IF NOT EXISTS reset_policy VARCHAR(20) NOT NULL DEFAULT 'NONE';
ALTER TABLE challenge
    ADD COLUMN IF NOT EXISTS reset_anchor VARCHAR(20) NOT NULL DEFAULT 'CALENDAR';

-- Create generalized cycle table for challenge resets (DAILY/WEEKLY/MONTHLY)
CREATE TABLE IF NOT EXISTS user_challenge_cycle (
    cycle_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    cycle_type VARCHAR(20) NOT NULL,
    cycle_key_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    progress_count INT NOT NULL DEFAULT 0,
    target_count INT NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    progress_data TEXT,
    CONSTRAINT uq_user_challenge_cycle UNIQUE (user_id, challenge_id, cycle_type, cycle_key_date),
    CONSTRAINT fk_ucc_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_ucc_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(challenge_id)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_ucc_challenge_cycle ON user_challenge_cycle (challenge_id, cycle_type, cycle_key_date);
CREATE INDEX IF NOT EXISTS idx_ucc_user_cycle ON user_challenge_cycle (user_id, cycle_type, cycle_key_date);

-- Set reset policies for specific challenges (idempotent name-based updates)
-- DAILY resets
UPDATE challenge SET reset_policy = 'DAILY'
WHERE challenge_name IN (
  '매일 2시간 공부하기',
  '환율 전체 조회하기',
  '환율 단건 확인하기',
  '환율 단건 조회',
  '환전 예상 계산하기',
  '환전 예상 계산',
  '나의 계좌 거래내역 조회',
  '나의 계좌거래내역 조회'
);

-- WEEKLY resets
UPDATE challenge SET reset_policy = 'WEEKLY'
WHERE challenge_name IN (
  '친구와 함께 커피 마시기',
  '7일 연속 용돈 기입장 작성하기',
  '과제 미루지 않기'
);

-- MONTHLY resets
UPDATE challenge SET reset_policy = 'MONTHLY'
WHERE challenge_name IN (
  '한 달 예산 계획 세우기',
  '한달 예산 계획'
);

-- Anchor rules: weekly/monthly use COMPLETION-based reset; daily keeps CALENDAR(default)
UPDATE challenge SET reset_anchor = 'COMPLETION' WHERE reset_policy IN ('WEEKLY', 'MONTHLY');
