###############################################
# 🔐 환경변수 설정 예시 (env.example)
# 이 파일을 .env 로 복사해서 사용하세요
#   cp env.example .env
#
# 주의: 실제 키 값은 팀원 개별 발급 키로 교체해야 합니다
###############################################

# ---------------------------------------------
# 📌 Finance API 설정
# - 실제 발급받은 API 키 입력
# - 예: FINANCE_API_KEY=abcd1234xyz
# ---------------------------------------------
FINANCE_API_KEY=your-finance-api-key

# ---------------------------------------------
# 📌 데이터베이스 설정 (PostgreSQL)
# - 개발 환경(local) 기본값 유지 가능
# - 비밀번호는 개별 설정 필요
# ---------------------------------------------
DB_HOST=localhost
DB_PORT=5432
DB_NAME=solsol
DB_USER=solsol
DB_PASSWORD=your-db-password

# ---------------------------------------------
# 📌 JWT 설정
# - JWT_SECRET : 임의의 문자열 (보안용)
# - JWT_EXPIRES_IN : 초 단위 (기본값: 86400 = 24시간)
# ---------------------------------------------
JWT_SECRET=your-jwt-secret
JWT_EXPIRES_IN=86400

# ---------------------------------------------
# 📌 신한 API 설정 (교육용 샌드박스)
# - 실제 API 키로 교체 필요
# - BASE URL은 기본적으로 고정
# ---------------------------------------------
SHINHAN_API_KEY=your-shinhan-api-key
SHINHAN_API_BASE=https://finopenapi.ssafy.io/ssafy/api/v1/edu