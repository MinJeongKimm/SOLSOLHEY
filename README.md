## 🎮 쏠쏠Hey (SOLSOLHEY)
**신한은행 x SSAFY 해커톤 프로젝트** — 대학생이 학교생활·금융 챌린지를 수행해 포인트/경험치를 얻고, 마스코트를 성장시키는 게임형 캠퍼스 서비스

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.8-6DB33F) ![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C) ![JWT](https://img.shields.io/badge/Auth-JWT-blue) 
![Vue](https://img.shields.io/badge/Vue-3-42b883) ![Vite](https://img.shields.io/badge/Vite-5-646cff) ![Tailwind](https://img.shields.io/badge/TailwindCSS-3-06b6d4) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)

## 📋 소개
- **서비스명**: 쏠쏠Hey (쏠쏠해)
- **목표**: 대학생의 금융 습관 형성 + 게임 요소 + 학교 커뮤니티 감성
- **외부 연동**: 신한 금융 교육용 API

## 🧰 기술 스택
- **백엔드**: Spring Boot 3.4.8, Java 17, Spring Security, JPA, WebFlux, Flyway
- **프론트엔드**: Vue 3, Vite 5, TypeScript, Vue Router, Tailwind CSS
- **DB/인프라**: H2(local file), PostgreSQL(dev)

## 🗂️ 프로젝트 구조
```bash
SOLSOLHEY/
├── backend/solsol/
│   ├── src/main/java/com/solsolhey/
│   │   ├── solsol/            # 인증, 공통, 설정, 챌린지 등
│   │   └── mascot/, finance/  # 마스코트/금융 모듈
│   ├── src/main/resources/application.yml
│   ├── src/main/resources/db/migration/  # Flyway 마이그레이션 (VXX__*.sql)
│   ├── build.gradle
│   └── env.example            # .env 템플릿 (복사해 사용)
└── frontend/
    ├── package.json
    ├── vite.config.ts         # root: 'solsol'
    └── solsol/
        ├── index.html
        └── src/
            ├── api/, router/, views/, components/
            └── assets/main.css
```

## 🚀 시작하기
### 1) 사전 요구사항
- Node.js 18+
- Java 17+
- (dev) PostgreSQL

### 2) 환경 변수 설정 (백엔드)
```bash
cd SOLSOLHEY/backend/solsol
cp env.example .env
# 필요한 값 입력: FINANCE_API_KEY, DB_* , JWT_SECRET_KEY 등
```
주요 항목: `FINANCE_API_KEY`, `DB_HOST/PORT/NAME/USER/PASSWORD`, `JWT_SECRET_KEY`, `JWT_ACCESS_TOKEN_EXPIRATION`.

### 3) 백엔드 실행
```bash
cd SOLSOLHEY/backend/solsol
./gradlew bootRun
```
- 프로필: 기본 `local` (H2 파일 DB)
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC: `jdbc:h2:file:./.localdb/solsol;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`
  - 사용자: `sa`
  - ⚠️ **보안**: 개발환경(`local`, `dev`)에서만 접근 가능

### 4) 프론트엔드 실행
```bash
cd SOLSOLHEY/frontend
npm ci
npm run dev
```
- 접속: `http://localhost:5173`
- API 베이스 URL(선택): `frontend/solsol/.env`에 `VITE_API_BASE_URL=http://localhost:8080` 설정 가능

## 🔌 API 요약

- **헬스체크/공개**
  - GET `/health`
  - GET `/public/test`
  - GET `/test/auth` (인증 필요)

- **인증 (`AuthController`, prefix: `/api/v1/auth`)**
  - POST `/signup`, POST `/login`, POST `/logout`
  - POST `/refresh`, GET `/me`
  - 회원가입 직후, 외부 금융 API 사용자도 비동기 생성(옵션 B). 실패해도 가입은 성공하며, 백그라운드에서 재시도합니다.

- **마스코트 (`MascotController`, prefix: `/api/v1/mascot`)**
  - POST ``/`` (생성), GET ``/`` (조회), PATCH ``/`` (업데이트), DELETE ``/`` (삭제)
  - POST `/equip` (아이템 장착)

- **금융 (`FinanceController`, prefix: `/api/v1/finance`)**
  - GET `/exchange-rates?base=USD`
  - GET `/exchange-rate?base=USD`
  - POST `/exchange/estimate`
  - POST `/accounts/transactions`

- **챌린지 (`ChallengeController`, prefix: `/challenges`)**
  - GET ``/`` (목록, `?category=`)
  - GET `/{challengeId}` (상세)
  - POST `/{challengeId}/join` (참여)
  - POST `/{challengeId}/progress` (진행도 갱신)
  - GET `/my?status=` (내 챌린지)

참고: **보안 강화됨** - CORS는 환경변수로 제한된 도메인만 허용. 개발환경: `localhost` 포트들, 배포 환경: 실제 도메인 설정 필요.
헬스엔드포인트는 `/health` (permitAll)이며, Actuator 일부 경로도 허용됩니다.

## 🛡️ 보안 기능

### Rate Limiting
- **일반 API**: 분당 100회 요청 제한 (설정 가능)
- **로그인**: 15분당 10회 시도 제한 (설정 가능)
- **IP 기반 차단**: 자동 정리 및 캐시 관리
- **설정**: `env.example`의 `RATE_LIMIT_*` 변수로 조정

### 테스트
```bash
cd backend/solsol
./test-rate-limit.sh  # Rate Limiting 동작 테스트
```

## ⚙️ 프로필/설정
- `local`: H2, `ddl-auto=update`, 콘솔 활성화
- `dev`: PostgreSQL(`jdbc:postgresql://localhost:5432/solsol_dev`), `ddl-auto=validate`
- 로깅: `com.solsolhey=DEBUG`, WebClient DEBUG 활성

### 마이그레이션(Flyway)
- 마이그레이션 파일은 불변입니다. 기존 파일을 수정하지 말고 항상 새로운 버전을 추가하세요.
- 로컬에서 체크섬 불일치 발생 시: `rm -rf backend/solsol/.localdb` 후 재기동하거나 `./gradlew flywayRepair` 실행(로컬에서만 권장).

## 🔒 보안 설정
### 주요 보안 기능
- **JWT 인증**: 256비트 강력한 Secret Key 사용, Access/Refresh 토큰 분리
- **CORS 보안**: 환경별 허용 도메인 제한 (`CORS_ALLOWED_ORIGINS` 환경변수)
- **환경별 접근 제어**: 개발용 엔드포인트(H2 Console, Swagger)는 배포 환경에서 자동 비활성화(구성 시)
- **입력값 검증**: `@Valid` 기반 데이터 검증, 전역 예외 처리

### 보안 체크리스트
✅ JWT Secret Key를 강력한 랜덤 값으로 변경  
✅ CORS 허용 도메인을 운영 도메인으로 제한  
✅ 데이터베이스 접속 정보 보안 관리  
✅ HTTPS 설정 (배포 환경)  

### 환경별 보안 설정 예시
```bash
# 로컬 개발환경 (.env)
SPRING_PROFILES_ACTIVE=local  # H2 Console, Swagger 접근 가능
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# 개발/배포 환경 (.env)
SPRING_PROFILES_ACTIVE=dev
CORS_ALLOWED_ORIGINS=https://your-domain.com
JWT_SECRET_KEY=<256비트-강력한-랜덤-키>
```

## 🤝 기여 가이드
- 브랜치: `main`(배포), `develop`(통합), `feature/기능명`
- 커밋 컨벤션
```text
Type        설명
Feat        새로운 기능 추가
Fix         버그 수정 또는 typo
Refactor    리팩토링
Design      CSS 등 사용자 UI 디자인 변경
Comment     필요한 주석 추가 및 변경
Style       코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
Test        테스트(테스트 코드 추가, 수정, 삭제, 비즈니스 로직에 변경이 없는 경우)
Chore       위에 걸리지 않는 기타 변경사항(빌드 스크립트 수정, assets image, 패키지 매니저 등)
Init        프로젝트 초기 생성
Rename      파일 혹은 폴더명 수정하거나 옮기는 경우
Remove      파일을 삭제하는 작업만 수행하는 경우
```
- 개발 유의사항
  - Entity 변경 전 합의, 환경 변수 템플릿 최신화, API 명세 준수, 테스트 코드 권장

## 🧷 금융 API 연동 개요
- 목적: 금융 기능 호출 시 사용자 식별을 위해 외부 `userKey`를 사용합니다.
- 시점: 우리 서비스 회원가입 성공 직후, 커밋 이후 비동기로 금융 사용자 생성 요청을 전송합니다.
- 금융용 이메일 생성 규칙(전역 중복 방지): `<local>+<createdAtEpochMs>@<domain>`
  - 예: `alice@example.com` + `created_at=2025-08-27T12:34:56.789Z` → `alice+1756307696789@example.com`
  - 로컬 파트 길이 보호: 40자로 절단. 재시도 시 동일 값으로 멱등 처리.
- 실패 처리(옵션 B): 가입은 성공으로 유지. `users.finance_user_key`는 NULL이며, 스케줄러(60초 주기)가 재시도합니다.
- 중복 응답 처리: 생성 시 중복이면 `/member/search`로 `userKey`를 조회하여 저장합니다.

## 📜 라이선스
- 해커톤 진행용 내부 프로젝트 — 라이선스는 추후 결정 예정

## 🐳 Docker 개발 실행
- 요구사항: Docker Desktop(또는 Compose v2), 네트워크 연결
- 실행: `docker compose -f backend/docker-compose.dev.yml up -d --build`
- 종료: `docker compose -f backend/docker-compose.dev.yml down`
- 상태/로그:
  - 상태: `docker compose -f backend/docker-compose.dev.yml ps`
  - 로그: `docker compose -f backend/docker-compose.dev.yml logs -f`
- 접속:
  - API 헬스: `http://localhost:8080/health`
  - pgAdmin: `http://localhost:5050` (로그인은 `backend/.env`의 `PGADMIN_DEFAULT_*` 사용)
- 프로필/DB:
  - `backend/.env`의 `SPRING_PROFILES_ACTIVE=dev|local`로 전환
  - 원격 DB 사용 시 `SPRING_DATASOURCE_URL`의 Public host/port 및 `sslmode=require` 확인
- pgAdmin 서버 등록(원격 DB 예시):
  - Host: `.env`의 JDBC URL 호스트 (예: `yamabiko.proxy.rlwy.net`)
  - Port: `.env`의 Public 포트 (예: `37256`)
  - DB: `railway` | User: `.env`의 `SPRING_DATASOURCE_USERNAME` | Password: `.env`의 `SPRING_DATASOURCE_PASSWORD`
  - SSL Mode: Require
