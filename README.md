# 🎮 쏠쏠Hey (SOLSOLHEY) - 해커톤 프로젝트
**신한은행 x SSAFY 해커톤 프로젝트**

대학생 사용자가 학교생활·금융 챌린지를 통해 보상(경험치/포인트)을 얻고, 마스코트를 꾸미며 성장시키는 **게임형 캠퍼스 서비스**

## 📋 프로젝트 개요

- **서비스명**: 쏠쏠Hey (쏠쏠해)
- **목표**: 대학생 금융 습관 형성 + 게임형 재미 요소 + 학교 커뮤니티 감성
- **기술스택**: Spring Boot 3.4.8, Java 17, PostgreSQL, JPA, Spring Security
- **외부 연동**: 신한금융 API
  
  프론트엔드: Vue 3, Vite, TypeScript, Vue Router, Tailwind CSS

## 🚀 8월 16일 완료사항 ✅

### ✅ 개발환경 세팅 완료
- Spring Boot 3.4.8 기반 프로젝트 구성
- 필요한 의존성 추가 (JPA, Security, Validation, JWT, WebFlux 등)
- Gradle 빌드 시스템 설정

### ✅ DB 설계 최종 확정 및 ERD 작성
- 개선된 ERD 설계 완료 (총 20개+ 엔티티)
- 핵심 도메인: User, Mascot, Challenge, Item, PointTransaction 등
- 관계 설정 및 제약조건 정의

### ✅ JPA Entity 클래스 생성
생성된 핵심 엔티티들:
- `User`: 사용자 정보
- `Mascot`: 마스코트 시스템 
- `Item`: 꾸미기 아이템
- `Challenge` / `ChallengeCategory` / `UserChallenge`: 챌린지 시스템
- `PointTransaction`: 포인트 관리
- `Attendance`: 출석 시스템

### ✅ 환경변수 설정 및 공유
- `application.yml`: 프로파일별 설정 (local/dev/prod)
- `env-template.txt`: 팀원 공유용 환경변수 템플릿
- H2(로컬) + PostgreSQL(운영) 지원

## 🔧 8월 17일 완료사항 ✅

### ✅ 공통 응답 포맷 개발 완료
- `ApiResponse<T>` 제네릭 응답 클래스 구현
- 성공/실패/상태별 응답 메서드 완비
- 타임스탬프 자동 추가 및 JSON 최적화

### ✅ 예외 처리 시스템 구축 완료
- 6개 예외 클래스 체계적 설계 (`BaseException`, `BusinessException` 등)
- `@ControllerAdvice` 기반 `GlobalExceptionHandler` 구현
- 모든 예외의 `ApiResponse` 형태 자동 변환

### ✅ JWT 인증 시스템 완전 구현
- **JWT 토큰 관리**: Access(1h) + Refresh(7d) 토큰 분리
- **인증 필터**: `JwtAuthenticationFilter` 구현
- **Spring Security 설정**: Stateless 인증, CORS, 경로별 권한
- **사용자 서비스**: `CustomUserDetailsService` + `UserRepository`

### ✅ 추가 개발 사항
- **설정 클래스**: `WebConfig`, `JpaConfig` 추가
- **테스트 컨트롤러**: `HealthController` (헬스체크 + 인증테스트)
- **H2 호환성**: 예약어 충돌 해결 (`user` → `users`)

## 🧪 8월 18일 완료사항 ✅

### ✅ 백엔드 (인증 API)
- `/api/auth/signup`, `/api/auth/login`, `/api/auth/logout(POST)` 구현 및 연동 완료
- JWT 기반 인증 연동, `JwtAuthenticationFilter` 적용, Stateless 세션 구성
- `Logout` 요청시 `Authorization: Bearer <token>` 처리, 기본 검증 및 토큰 무효화 수행
- `/api/auth/refresh` 토큰 갱신 엔드포인트 구현
- CORS 개발환경 허용(OriginPattern: `*`), 서버 컨텍스트 경로 `/api`

### ✅ 프론트엔드 (인증 화면/라우팅)
- 로그인/회원가입 화면 구현 및 입력값 검증, API 연동 완료
- 공통 API 헬퍼 및 `auth` 유틸 작성(토큰/사용자정보 로컬스토리지 저장)
- Vue Router 가드 적용: `requiresAuth`, `requiresGuest` 분기 처리
- 헤더 컴포넌트 및 대시보드 기본 화면 구현, 인증페이지에서만 헤더 노출
- Tailwind CSS 설정 및 전역 스타일 적용, Vite 빌드 구성 정리(`emptyOutDir` 포함)

## 🏗️ 프로젝트 구조

```
backend/solsol/
├── src/main/java/com/solsolhey/solsol/
│   ├── SolsolApplication.java
│   │
│   ├── auth/                # 인증 시스템
│   │   ├── dto/CustomUserDetails.java
│   │   ├── jwt/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtTokenProvider.java
│   │   └── service/CustomUserDetailsService.java
│   │
│   ├── common/              # 공통 모듈
│   │   ├── exception/       # 예외 처리
│   │   │   ├── BaseException.java
│   │   │   ├── BusinessException.java
│   │   │   ├── AuthException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   └── response/
│   │       └── ApiResponse.java  # 공통 응답 포맷
│   │
│   ├── config/              # 설정 클래스
│   │   ├── JpaConfig.java
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   │
│   ├── controller/          # 컨트롤러
│   │   └── HealthController.java
│   │
│   ├── entity/              # JPA 엔티티 (9개)
│   │   ├── BaseEntity.java
│   │   ├── User.java
│   │   ├── Mascot.java
│   │   ├── Challenge.java
│   │   └── ...
│   │
│   └── repository/          # 데이터 접근 계층
│       └── UserRepository.java
│
├── src/main/resources/
│   └── application.yml      # 프로파일별 설정
├── env-template.txt         # 환경변수 템플릿
├── 개발일지_2024-08-16.md   # 1일차 개발일지
├── 개발일지_2024-08-17.md   # 2일차 개발일지
└── build.gradle             # 빌드 설정

frontend/
├── solsol/
│   ├── index.html
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── assets/main.css          # Tailwind 엔트리
│       ├── router/index.ts          # 라우팅+가드
│       ├── api/index.ts             # 공통 API 헬퍼 + auth 유틸
│       ├── components/Header.vue
│       └── views/
│           ├── Login.vue
│           ├── Signup.vue
│           ├── Dashboard.vue
│           ├── Challenge.vue
│           └── Mascot.vue
├── vite.config.ts                    # root: 'solsol', outDir: '../dist'
├── tailwind.config.js
├── postcss.config.js
└── package.json
```

## 💻 로컬 개발 환경 실행

### 1. 프로젝트 클론 후 이동
```bash
cd backend/solsol
```

### 2. 환경변수 설정 (선택사항)
`env-template.txt` 파일을 참고하여 IDE의 Run Configuration에서 환경변수 설정

### 3. 애플리케이션 실행
```bash
# Gradle로 실행
./gradlew bootRun

# 또는 IDE에서 SolsolApplication.main() 실행
```

### 4. H2 Console 접속 (로컬 개발용)
- URL: http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (빈칸)

---

### 🖥️ 프론트엔드 실행 (Vue 3 + Vite)

#### 사전 요구사항
- Node.js 18 이상, npm 9 이상 권장
- 백엔드 서버: `http://localhost:8080/api` (기본값)

#### 1) 의존성 설치
```bash
cd frontend
npm ci
```

#### 2) 개발 서버 실행
```bash
npm run dev
```
- 접속: http://localhost:5173/
- 주요 경로: `/`(로그인), `/signup`, `/dashboard`, `/challenge`, `/mascot`

#### 3) API 베이스 URL 설정 (선택)
백엔드 주소가 기본값과 다를 경우 다음 파일을 생성해 설정합니다.
```bash
# 파일 위치: frontend/solsol/.env
VITE_API_BASE_URL=http://localhost:8080/api
```

#### 4) 프로덕션 빌드/미리보기
```bash
npm run build
npm run preview
```
- 접속: http://localhost:4173/

#### 참고
- 인증 API 경로: `/api/auth/signup`, `/api/auth/login`, `/api/auth/logout(POST)`
- CORS: 개발 환경에서는 모든 Origin 허용으로 설정되어 있습니다.

## 📊 데이터베이스 스키마

### 핵심 테이블
- `user`: 사용자 정보 (username, email, nickname, total_points)
- `mascot`: 마스코트 (name, level, experience_point, evolution_stage)  
- `challenge`: 챌린지 (challenge_name, reward_points, challenge_type)
- `user_challenge`: 사용자-챌린지 참여 기록
- `item`: 꾸미기 아이템 (clothing, background, accessory)
- `point_transaction`: 포인트 거래 내역

## 🎯 다음 단계 (8월 19일 예정)

1. **기본 도메인 API 확장** (P0)
   - 마스코트 CRUD API
   - 챌린지 생성/목록/상세 API
   - 포인트 적립 로직(챌린지 보상) 기본 구현

2. **챌린지 시스템 API** (P1)
   - 챌린지 목록/상세 조회
   - 챌린지 참여/진행도 업데이트
   - 포인트 보상 시스템

3. **신한 API 연동** (P1)
   - 환율 조회 API 연동
   - 계좌 정보 조회 API 연동

## 🤝 팀원 개발 가이드

### 브랜치 전략
- `main`: 운영 배포용
- `develop`: 개발 통합
- `feature/기능명`: 기능별 개발

### 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정  
docs: 문서 수정
refactor: 코드 리팩토링
test: 테스트 코드 추가
```

### 개발 시 주의사항
1. Entity 변경시 팀원들과 상의 후 진행
2. 환경변수는 `env-template.txt` 업데이트
3. API 개발시 `documents.md`의 API 명세서 참고
4. 테스트 코드 작성 권장

---

## 📈 개발 진척도 현황

| 일자 | 목표 | 실제 완료 | 완성도 |
|------|------|----------|--------|
| **8월 16일** | 개발환경+DB설계+ERD | 9개 Entity + Repository + Config | **110%** ✅ |
| **8월 17일** | 공통모듈 개발 | 완전한 인증시스템 + 예외처리 + 응답포맷 | **140%** ✅ |
| **8월 18일** | 기본 인증/FE Auth | BE 인증 API(회원가입/로그인/로그아웃/리프레시) + FE 로그인/회원가입/라우팅/가드 + Tailwind 빌드 | **120%** ✅ |

**🎉 현재까지 계획 대비 125% 초과 달성!**  
탄탄한 아키텍처 기반으로 본격적인 API 개발을 시작할 준비가 완료되었습니다. 🚀