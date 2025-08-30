-- Seed: 신용등급 조회하기 (멱등)
-- 목적: FINANCE 카테고리에 "신용등급 조회하기" 챌린지를 추가한다.

-- FINANCE 카테고리 보장 (멱등)
INSERT INTO challenge_category (category_name, display_name, description)
SELECT 'FINANCE', '금융', '금융 관련 챌린지'
WHERE NOT EXISTS (
    SELECT 1 FROM challenge_category WHERE category_name = 'FINANCE'
);

-- 업데이트: 이미 존재하면 속성 갱신
UPDATE challenge
SET category_id = (SELECT category_id FROM challenge_category WHERE category_name = 'FINANCE'),
    description = '외부 금융 API를 호출해 본인의 신용등급을 조회해보세요.',
    reward_points = 80,
    reward_exp = 40,
    challenge_type = 'DAILY',
    difficulty = 'EASY',
    target_count = 1,
    is_active = TRUE
WHERE challenge_name = '신용등급 조회하기';

-- 삽입: 없으면 추가
INSERT INTO challenge (
    category_id, challenge_name, description, reward_points, reward_exp,
    challenge_type, difficulty, start_date, end_date, target_count, is_active
)
SELECT (SELECT category_id FROM challenge_category WHERE category_name = 'FINANCE'),
       '신용등급 조회하기',
       '외부 금융 API를 호출해 본인의 신용등급을 조회해보세요.',
       80, 40,
       'DAILY', 'EASY', CURRENT_TIMESTAMP, NULL, 1, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM challenge WHERE challenge_name = '신용등급 조회하기'
);

