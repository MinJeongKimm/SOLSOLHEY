-- 마스코트 커스터마이징 레이아웃 컬럼 추가
ALTER TABLE IF EXISTS mascots ADD COLUMN IF NOT EXISTS equipped_layout TEXT;

