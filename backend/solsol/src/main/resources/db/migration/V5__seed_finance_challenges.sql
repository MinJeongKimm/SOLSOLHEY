-- Seed additional FINANCE challenges (idempotent)

-- 1) 환율 전체 조회하기
INSERT INTO challenge (
    category_id, challenge_name, description, reward_points, reward_exp,
    challenge_type, difficulty, start_date, end_date, target_count, is_active
)
SELECT (SELECT category_id FROM challenge_category WHERE category_name = 'FINANCE'),
       '환율 전체 조회하기',
       '외부 환율 API를 호출해 전체 환율 목록을 확인해보세요.',
       80, 40,
       'DAILY', 'EASY', CURRENT_TIMESTAMP, NULL, 1, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM challenge WHERE challenge_name = '환율 전체 조회하기'
);

-- 2)  환율 단건 확인하기
INSERT INTO challenge (
    category_id, challenge_name, description, reward_points, reward_exp,
    challenge_type, difficulty, start_date, end_date, target_count, is_active
)
SELECT (SELECT category_id FROM challenge_category WHERE category_name = 'FINANCE'),
       '환율 단건 확인하기',
       'USD 등 특정 통화의 환율을 조회해보세요.',
       80, 40,
       'DAILY', 'EASY', CURRENT_TIMESTAMP, NULL, 1, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM challenge WHERE challenge_name = '환율 단건 확인하기'
);

-- 3) 환전 예상 계산하기
INSERT INTO challenge (
    category_id, challenge_name, description, reward_points, reward_exp,
    challenge_type, difficulty, start_date, end_date, target_count, is_active
)
SELECT (SELECT category_id FROM challenge_category WHERE category_name = 'FINANCE'),
       '환전 예상 계산하기',
       '환전 예상 금액을 계산해 결과를 확인해보세요.',
       120, 60,
       'WEEKLY', 'MEDIUM', CURRENT_TIMESTAMP, NULL, 1, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM challenge WHERE challenge_name = '환전 예상 계산하기'
);

-- 4) 나의 계좌 거래내역 조회
INSERT INTO challenge (
    category_id, challenge_name, description, reward_points, reward_exp,
    challenge_type, difficulty, start_date, end_date, target_count, is_active
)
SELECT (SELECT category_id FROM challenge_category WHERE category_name = 'FINANCE'),
       '나의 계좌 거래내역 조회',
       '기간과 조건을 입력해 계좌 거래내역을 조회해보세요.',
       150, 75,
       'WEEKLY', 'MEDIUM', CURRENT_TIMESTAMP, NULL, 1, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM challenge WHERE challenge_name = '나의 계좌 거래내역 조회'
);

