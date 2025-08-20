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
- **DB/인프라**: H2(local), PostgreSQL(dev/prod)

## 🗂️ 프로젝트 구조
```bash
SOLSOLHEY/
├── backend/solsol/
│   ├── src/main/java/com/solsolhey/
│   │   ├── solsol/            # 인증, 공통, 설정, 챌린지 등
│   │   └── mascot/, finance/  # 마스코트/금융 모듈
│   ├── src/main/resources/application.yml
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
- (dev/prod) PostgreSQL

### 2) 환경 변수 설정 (백엔드)
```bash
cd SOLSOLHEY/backend/solsol
cp env.example .env
# 필요한 값 입력: FINANCE_API_KEY, DB_* , JWT_SECRET 등
```
주요 항목: `FINANCE_API_KEY`, `DB_HOST/PORT/NAME/USER/PASSWORD`, `JWT_SECRET`, `JWT_EXPIRES_IN`.

### 3) 백엔드 실행
```bash
cd SOLSOLHEY/backend/solsol
./gradlew bootRun
```
- 프로필: 기본 `local` (H2 메모리 DB)
- H2 Console: `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:testdb`, 사용자: `sa`)

### 4) 프론트엔드 실행
```bash
cd SOLSOLHEY/frontend
npm ci
npm run dev
```
- 접속: `http://localhost:5173`
- API 베이스 URL(선택): `frontend/solsol/.env`에 `VITE_API_BASE_URL=http://localhost:8080` 설정 가능

## 🔌 API 요약
아래 경로는 실제 컨트롤러 기준입니다.

- **헬스체크/공개**
  - GET `/health`
  - GET `/public/test`
  - GET `/test/auth` (인증 필요)

- **인증 (`AuthController`, prefix: `/api/v1/auth`)**
  - POST `/signup`, POST `/login`, POST `/logout`
  - POST `/refresh`
  - GET `/check-username?username=...`, GET `/check-email?email=...`

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

참고: 개발 환경 CORS는 모든 Origin 허용(`*`). 운영에서는 도메인 제한 권장.

## ⚙️ 프로필/설정
- `local`: H2, `ddl-auto=update`, 콘솔 활성화
- `dev`: PostgreSQL(`jdbc:postgresql://localhost:5432/solsol_dev`), `ddl-auto=validate`
- `prod`: PostgreSQL(환경변수 기반), `ddl-auto=validate`
- 로깅: `com.solsolhey=DEBUG`, WebClient DEBUG 활성

## 🤝 기여 가이드
- 브랜치: `main`(배포), `develop`(통합), `feature/기능명`
- 커밋 컨벤션
```text
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
refactor: 코드 리팩토링
test: 테스트 코드 추가
```
- 개발 유의사항
  - Entity 변경 전 합의, 환경 변수 템플릿 최신화, API 명세 준수, 테스트 코드 권장

## 🧪 최근 진행 내역 (요약)
- 8/16: 환경/의존성 세팅, ERD, 핵심 엔티티 생성
- 8/17: 공통 응답/예외 처리, JWT 인증·보안 설정, 헬스체크 추가
- 8/18: 인증 API(회원가입/로그인/로그아웃/리프레시), FE 인증 플로우 및 라우팅/가드, Tailwind 적용

## 🆘 트러블슈팅
- H2 Console 404: 프로필이 `local`인지 확인, URL은 `http://localhost:8080/h2-console`
- 401 Unauthorized: `Authorization: Bearer <accessToken>` 헤더 확인
- DB 연결 실패: `.env`의 `DB_*` 값과 `application.yml` 프로파일 확인
- CORS 오류: 개발환경은 허용되나, 브라우저 캐시로 인해 발생 시 새로고침/다시 실행

## 📜 라이선스
- 해커톤 진행용 내부 프로젝트 — 라이선스는 추후 결정 예정