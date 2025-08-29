-- Deactivate finance challenge: 나의 계좌 거래내역 조회
UPDATE challenge
SET is_active = FALSE
WHERE challenge_name = '나의 계좌 거래내역 조회';

