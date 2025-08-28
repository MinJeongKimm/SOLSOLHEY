-- shop_items 테이블에 required_level 컬럼 추가 (안전한 기본값 설정)
ALTER TABLE shop_items
    ADD COLUMN IF NOT EXISTS required_level INTEGER;

-- 기존 데이터 보정: NULL인 경우 1로 채움
UPDATE shop_items SET required_level = 1 WHERE required_level IS NULL;

-- 제약 설정: 이후 행에 대해 기본값/NOT NULL 적용
-- H2는 ALTER TABLE ... ALTER COLUMN ... 구문을 한 번에 여러 속성으로 변경하는 것을 허용하지 않으므로 분리
ALTER TABLE shop_items ALTER COLUMN required_level SET DEFAULT 1;
ALTER TABLE shop_items ALTER COLUMN required_level SET NOT NULL;
