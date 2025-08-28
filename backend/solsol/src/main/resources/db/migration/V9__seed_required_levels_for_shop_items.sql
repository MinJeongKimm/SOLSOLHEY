-- 기존 아이템에 의미 있는 최소 레벨(required_level) 값 채우기
-- 1) 카테고리 기준 기본 레벨 설정
UPDATE shop_items SET required_level = 1 WHERE category IN ('head','clothing') AND required_level < 1;
UPDATE shop_items SET required_level = 3 WHERE category = 'accessory' AND required_level < 3;
UPDATE shop_items SET required_level = 5 WHERE category = 'background' AND required_level < 5;

-- 2) 가격 임계값 기준 상향 (현재값과 임계값 중 더 큰 값 유지)
UPDATE shop_items SET required_level = 8 WHERE price >= 1000 AND required_level < 8;
UPDATE shop_items SET required_level = 5 WHERE price >= 600 AND required_level < 5;
UPDATE shop_items SET required_level = 3 WHERE price >= 300 AND required_level < 3;
