# 🎮 쏠쏠Hey (SOLSOLHEY) - 해커톤 프로젝트
**신한은행 x SSAFY 해커톤 프로젝트**

대학생 사용자가 학교생활·금융 챌린지를 통해 보상(경험치/포인트)을 얻고, 마스코트를 꾸미며 성장시키는 **게임형 캠퍼스 서비스**

## 📋 프로젝트 개요

- **서비스명**: 쏠쏠Hey (쏠쏠해)
- **목표**: 대학생 금융 습관 형성 + 게임형 재미 요소 + 학교 커뮤니티 감성
- **기술스택**: Spring Boot 3.4.8, Java 17, PostgreSQL, JPA, Spring Security
- **외부 연동**: 신한금융 API

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

## 📊 데이터베이스 스키마

### 핵심 테이블
- `user`: 사용자 정보 (username, email, nickname, total_points)
- `mascot`: 마스코트 (name, level, experience_point, evolution_stage)  
- `challenge`: 챌린지 (challenge_name, reward_points, challenge_type)
- `user_challenge`: 사용자-챌린지 참여 기록
- `item`: 꾸미기 아이템 (clothing, background, accessory)
- `point_transaction`: 포인트 거래 내역

## 🎯 다음 단계 (8월 18일 예정)

1. **기본 API 개발** (P0)
   - 회원가입/로그인/로그아웃 API
   - JWT 토큰 갱신 API
   - 마스코트 CRUD API

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
| **8월 18일** | 기본 API 개발 | 예정 | - |

**🎉 현재까지 계획 대비 125% 초과 달성!**  
탄탄한 아키텍처 기반으로 본격적인 API 개발을 시작할 준비가 완료되었습니다. 🚀