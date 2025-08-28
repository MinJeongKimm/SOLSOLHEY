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
# 📌 JWT 인증 설정 (보안 극중요!)
# - JWT_SECRET_KEY : 256비트 이상 강력한 랜덤 키 (필수!)
# - JWT_ACCESS_TOKEN_EXPIRATION : Access Token 만료시간 (밀리초, 기본: 15분)
# - JWT_REFRESH_TOKEN_EXPIRATION : Refresh Token 만료시간 (밀리초, 기본: 7일)
# 
# ⚠️ 보안 주의: 반드시 강력한 랜덤 키로 변경하세요!
# 키 생성: openssl rand -hex 32 또는 openssl rand -base64 32
# ---------------------------------------------
JWT_SECRET_KEY=your-256-bit-strong-random-secret-key-here
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# ---------------------------------------------
# 📌 신한 API 설정 (교육용 샌드박스)
# - 실제 API 키로 교체 필요
# - BASE URL은 기본적으로 고정
# ---------------------------------------------
SHINHAN_API_KEY=your-shinhan-api-key
SHINHAN_API_BASE=https://finopenapi.ssafy.io/ssafy/api/v1/edu

# ---------------------------------------------
# 🔒 보안 관련 추가 설정
# ---------------------------------------------

# CORS 허용 도메인 (보안 중요!)
# 개발환경: 로컬 포트들만 허용
# 운영환경: 실제 프론트엔드 도메인으로 변경 필수!
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:8080

# Spring 환경 프로필
# local/dev: 개발환경 (H2 Console, Swagger 접근 가능)
# prod: 운영환경 (개발용 엔드포인트 자동 차단)
SPRING_PROFILES_ACTIVE=local

# BCrypt 비밀번호 암호화 강도 (4~31, 높을수록 보안↑/성능↓)
BCRYPT_STRENGTH=12

# ---------------------------------------------
# 🛡️ 보안 체크리스트 (배포 전 필수 확인!)
# ---------------------------------------------
# ✅ JWT_SECRET_KEY를 강력한 랜덤 키로 변경했는가?
# ✅ CORS_ALLOWED_ORIGINS를 실제 도메인으로 제한했는가?
# ✅ 운영환경에서 SPRING_PROFILES_ACTIVE=prod로 설정했는가?
# ✅ DB 접속 정보가 안전하게 관리되는가?
# ✅ .env 파일이 .gitignore에 포함되어 있는가?