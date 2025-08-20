# 🎮 쏠쏠Hey (SOLSOLHEY)

**신한은행 x SSAFY 해커톤 프로젝트** — 대학생이 학교생활·금융 챌린지를 수행해 포인트/경험치를 얻고, 마스코트를 성장시키는 게임형 캠퍼스 서비스

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.8-6DB33F) ![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C) ![JWT](https://img.shields.io/badge/Auth-JWT-blue) 
![Vue](https://img.shields.io/badge/Vue-3-42b883) ![Vite](https://img.shields.io/badge/Vite-5-646cff) ![Tailwind](https://img.shields.io/badge/TailwindCSS-3-06b6d4) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791) ![Docker](https://img.shields.io/badge/Docker-20.10-2496ED) ![Redis](https://img.shields.io/badge/Redis-7.2-DC382D)

## 📋 소개
- **서비스명**: 쏠쏠Hey (쏠쏠해)
- **목표**: 대학생의 금융 습관 형성 + 게임 요소 + 학교 커뮤니티 감성
- **외부 연동**: 신한 금융 교육용 API
- **인프라**: Docker 기반 완전 컨테이너화된 개발/운영 환경

## 🧰 기술 스택
- **백엔드**: Spring Boot 3.4.8, Java 17, Spring Security, JPA, WebFlux, Flyway
- **프론트엔드**: Vue 3, Vite 5, TypeScript, Vue Router, Tailwind CSS
- **데이터베이스**: PostgreSQL 15, Redis 7.2
- **인프라**: Docker, Docker Compose, pgAdmin, Redis Commander
- **모니터링**: Spring Boot Actuator, Health Checks

## 🗂️ 프로젝트 구조
```
SOLSOLHEY/
├── frontend/                    # Vue 3 프론트엔드
│   ├── package.json
│   ├── vite.config.ts
│   └── solsol/
│       ├── index.html
│       └── src/
│           ├── api/, router/, views/, components/
│           └── assets/main.css
├── backend/                     # 🆕 Docker 환경 중심
│   ├── .env                    # 🆕 통합된 환경변수 (JWT, API 키 등)
│   ├── docker-compose.yml      # 🆕 메인 Docker Compose 설정
│   ├── docker-compose.dev.yml  # 🆕 개발 환경 오버라이드
│   ├── docker-compose.prod.yml # 🆕 운영 환경 오버라이드
│   ├── docker.env.dev          # 🆕 개발 환경 전용 변수
│   ├── docker.env.prod.example # 🆕 운영 환경 템플릿
│   ├── docker-run.sh           # 🆕 Docker 관리 스크립트
│   ├── DOCKER_GUIDE.md         # 🆕 Docker 사용 가이드
│   └── solsol/                 # Spring Boot 애플리케이션
│       ├── Dockerfile          # 🆕 멀티스테이지 빌드
│       ├── .dockerignore       # 🆕 Docker 빌드 최적화
│       ├── build.gradle
│       └── src/main/java/com/solsolhey/
│           ├── solsol/         # 인증, 공통, 설정, 챌린지 등
│           └── mascot/, finance/ # 마스코트/금융 모듈
└── documents/                   # 프로젝트 문서
```

## 🚀 빠른 시작 (Docker 환경)

### 1) 사전 요구사항
- **Docker Desktop** 20.10+ (macOS/Windows) 또는 Docker Engine 20.10+ (Linux)
- **Git** 2.30+
- **메모리**: 4GB 이상 권장
- **디스크 공간**: 2GB 이상

### 2) 프로젝트 클론 및 환경 설정
```bash
# 프로젝트 클론
git clone <repository-url>
cd SOLSOLHEY

# 환경변수 설정 (보안 정보)
cd backend
cp .env.example .env  # .env 파일이 없다면
nano .env             # JWT_SECRET_KEY, FINANCE_API_KEY 등 설정
```

### 3) Docker 환경 시작
```bash
# backend 디렉토리에서
cd backend

# 개발환경 시작 (권장)
./docker-run.sh up dev

# 또는 직접 실행
docker-compose -f docker-compose.yml -f docker-compose.dev.yml --env-file docker.env.dev up -d
```

### 4) 서비스 접속 확인
개발환경이 시작되면 다음 서비스에 접속할 수 있습니다:

- **🌐 Backend API**: http://localhost:8080/api/v1
- **📚 Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **🗄️ PostgreSQL**: localhost:5432 (solsol_dev/solsol/dev_password_123)
- **🔧 pgAdmin**: http://localhost:5050 (admin@solsolhey.com/admin123)
- **💾 Redis**: localhost:6379 (dev_password)
- **🔍 Redis Commander**: http://localhost:8081

## 🐳 Docker 환경 관리

### 기본 명령어
```bash
cd backend

# 개발환경 시작
./docker-run.sh up dev

# 상태 확인
./docker-run.sh status

# 로그 실시간 보기
./docker-run.sh logs dev

# 개발환경 중지
./docker-run.sh down dev

# 재시작
./docker-run.sh restart dev
```

### 환경별 실행
```bash
# 개발환경
./docker-run.sh up dev

# 운영환경 (먼저 환경변수 설정 필요)
cp docker.env.prod.example docker.env.prod
nano docker.env.prod  # 실제 값으로 수정
./docker-run.sh up prod
```

## 🔌 API 요약
아래 경로는 실제 컨트롤러 기준입니다.

- **헬스체크/공개**
  - GET `/api/v1/health`
  - GET `/api/v1/public/test`
  - GET `/api/v1/test/auth` (인증 필요)

- **인증 (`AuthController`, prefix: `/api/v1/auth`)**
  - POST `/signup`, POST `/login`, POST `/logout`
  - POST `/refresh`
  - GET `/check-username?username=...`, GET `/check-email?email=...`

- **마스코트 (`MascotController`, prefix: `/api/v1/mascot`)**
  - POST `/` (생성), GET `/` (조회), PATCH `/` (업데이트), DELETE `/` (삭제)
  - POST `/equip` (아이템 장착)

- **금융 (`FinanceController`, prefix: `/api/v1/finance`)**
  - GET `/exchange-rates?base=USD`
  - GET `/exchange-rate?base=USD`
  - POST `/exchange/estimate`
  - POST `/accounts/transactions`

- **챌린지 (`ChallengeController`, prefix: `/api/v1/challenges`)**
  - GET `/` (목록, `?category=`)
  - GET `/{challengeId}` (상세)
  - POST `/{challengeId}/join` (참여)
  - POST `/{challengeId}/progress` (진행도 갱신)
  - GET `/my?status=` (내 챌린지)

## ⚙️ 환경별 설정

### 개발환경 (dev)
- **데이터베이스**: PostgreSQL (solsol_dev)
- **JPA**: `ddl-auto=create-drop`, `show-sql=true`
- **Swagger UI**: 활성화
- **로깅**: DEBUG 레벨
- **pgAdmin, Redis Commander**: 포함

### 운영환경 (prod)
- **데이터베이스**: PostgreSQL (solsol_prod)
- **JPA**: `ddl-auto=validate`, `show-sql=false`
- **Swagger UI**: 비활성화 (보안)
- **로깅**: INFO 레벨
- **모니터링**: Prometheus, Grafana (향후 추가 예정)

## 🔒 보안 설정

### 주요 보안 기능
- **JWT 인증**: 256비트 강력한 Secret Key 사용, Access/Refresh 토큰 분리
- **CORS 보안**: 환경별 허용 도메인 제한 (`CORS_ALLOWED_ORIGINS` 환경변수)
- **환경별 접근 제어**: 개발용 엔드포인트는 운영환경에서 자동 비활성화
- **입력값 검증**: `@Valid` 기반 데이터 검증, 전역 예외 처리
- **Rate Limiting**: API 요청 제한 (향후 추가 예정)

### 보안 체크리스트
✅ JWT Secret Key를 강력한 랜덤 값으로 변경  
✅ CORS 허용 도메인을 운영 도메인으로 제한  
✅ 운영환경에서 `SPRING_PROFILES_ACTIVE=prod` 설정  
✅ 데이터베이스 접속 정보 보안 관리  
✅ HTTPS 설정 (운영환경)  

## 🧪 개발 가이드

### 로컬 개발 (IDE)
```bash
# Docker 환경에서 데이터베이스만 실행
cd backend
docker-compose up postgres redis -d

# Spring Boot 애플리케이션은 IDE에서 직접 실행
# application.yml에서 dev 프로필 사용
```

### 데이터베이스 마이그레이션
```bash
# Flyway를 통한 자동 마이그레이션
# Docker 환경 시작 시 자동으로 실행됨
# 수동 실행이 필요한 경우:
docker exec -it solsol-backend ./gradlew flywayMigrate
```

### 로그 확인
```bash
# 백엔드 로그
./docker-run.sh logs dev

# 특정 서비스 로그
docker logs solsol-backend
docker logs solsol-postgres
docker logs solsol-redis
```

## 🆘 트러블슈팅

### Docker 관련 문제
- **포트 충돌**: `lsof -i :8080`으로 사용 중인 포트 확인
- **메모리 부족**: Docker Desktop 메모리 할당량 증가
- **이미지 빌드 실패**: `docker system prune`으로 캐시 정리

### 일반적인 문제
- **H2 Console 404**: Docker 환경에서는 PostgreSQL 사용, H2 불필요
- **401 Unauthorized**: `Authorization: Bearer <accessToken>` 헤더 확인
- **DB 연결 실패**: Docker 컨테이너 상태 확인 (`./docker-run.sh status`)
- **CORS 오류**: `CORS_ALLOWED_ORIGINS` 환경변수에 프론트엔드 도메인 추가

### 보안 관련 문제
- **JWT 토큰 무효화**: Secret Key 변경 시 모든 기존 토큰이 무효됨 (정상 동작)
- **Swagger UI 접근 불가**: 운영환경에서는 보안상 접근 차단됨

## 🤝 기여 가이드
- **브랜치**: `main`(배포), `develop`(통합), `feature/기능명`
- **커밋 컨벤션**
```text
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
refactor: 코드 리팩토링
test: 테스트 코드 추가
```
- **개발 유의사항**
  - Entity 변경 전 합의, 환경 변수 템플릿 최신화, API 명세 준수, 테스트 코드 권장

## 📈 최근 진행 내역
- **8/16**: 환경/의존성 세팅, ERD, 핵심 엔티티 생성
- **8/17**: 공통 응답/예외 처리, JWT 인증·보안 설정, 헬스체크 추가
- **8/18**: 인증 API(회원가입/로그인/로그아웃/리프레시), FE 인증 플로우 및 라우팅/가드, Tailwind 적용
- **8/20**: 🆕 **Docker 환경 완성** - PostgreSQL, Redis, pgAdmin, Redis Commander 통합

## 🚀 향후 계획
- **프론트엔드 Docker화**: Vue.js 애플리케이션 컨테이너화
- **CI/CD 파이프라인**: GitHub Actions를 통한 자동 배포
- **모니터링 강화**: Prometheus, Grafana 통합
- **로드 밸런싱**: Nginx 리버스 프록시 추가
- **백업 전략**: 데이터베이스 자동 백업 및 복구

## 📜 라이선스
- 해커톤 진행용 내부 프로젝트 — 라이선스는 추후 결정 예정

---

## 📞 지원 및 문의
- **프로젝트 이슈**: GitHub Issues 사용
- **기술 문의**: 팀 내 기술 리더에게 문의
- **문서 개선**: README.md 수정 제안 환영

**Happy Coding! 🎉**