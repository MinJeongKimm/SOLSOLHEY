# 🔐 회원가입, 로그인, 로그아웃 기능 구현 가이드

## 📋 구현 완료 내용

### 1. API 타입 정의 (`src/types/api.ts`)
- **SignupRequest/Response**: 회원가입 요청/응답 인터페이스
- **LoginRequest/Response**: 로그인 요청/응답 인터페이스  
- **LogoutResponse**: 로그아웃 응답 인터페이스
- **ApiError**: API 에러 클래스
- **User**: 사용자 정보 타입

### 2. API 함수 구현 (`src/api/index.ts`)
- **apiRequest()**: 공통 API 요청 헬퍼 함수
  - 자동 토큰 헤더 추가
  - 에러 핸들링
  - JSON 응답 처리
- **signup()**: 회원가입 API 호출
- **login()**: 로그인 API 호출  
- **logout()**: 로그아웃 API 호출
- **auth 객체**: 인증 상태 관리 유틸리티
  - 토큰 저장/조회/삭제
  - 사용자 정보 저장/조회/삭제
  - 로그인 상태 확인
- **handleApiError()**: API 에러 메시지 처리

### 3. 회원가입 페이지 (`src/views/Signup.vue`)
**변경사항:**
- ❌ `email` → ✅ `userId` (API 명세서 따라 필드명 변경)
- ❌ `address` 필드 제거 (API 명세서에 없음)
- ✅ 실제 API 연동으로 변경 (기존 모킹 제거)
- ✅ 서버 측 유효성 검사 에러 처리 추가
- ✅ 성공 시 자동 로그인 페이지 이동

**API 명세서 준수:**
```json
{
  "userId": "ssafy@ssafy.co.kr",
  "password": "ssafy1234", 
  "nickname": "김싸피"
}
```

### 4. 로그인 페이지 (`src/views/Login.vue`)
**변경사항:**
- ❌ `email` → ✅ `userId` (API 명세서 따라 필드명 변경)
- ✅ 실제 API 연동으로 변경 (기존 모킹 제거)
- ✅ JWT 토큰 자동 저장
- ✅ 사용자 정보 저장
- ✅ 성공 시 대시보드 이동

**API 명세서 준수:**
```json
{
  "userId": "ssafy@ssafy.co.kr",
  "password": "userpassword"
}
```

### 5. 헤더 컴포넌트 (`src/components/Header.vue`)
**새로 구현:**
- ✅ 로고 및 네비게이션 메뉴
- ✅ 사용자 정보 표시 (아바타, 닉네임)
- ✅ 로그아웃 버튼 및 기능
- ✅ 모바일 반응형 메뉴
- ✅ 로그아웃 API 호출 및 상태 관리
- ✅ 로그아웃 후 자동 로그인 페이지 이동

**API 명세서 준수:**
- DELETE `/logout` 방식
- Authorization 헤더에 JWT 토큰 전송

### 6. 라우터 인증 가드 (`src/router/index.ts`)
**추가 기능:**
- ✅ 인증이 필요한 페이지 보호 (`requiresAuth`)
- ✅ 게스트만 접근 가능한 페이지 (`requiresGuest`)
- ✅ 자동 리다이렉션 (로그인 상태에 따라)
- ✅ Challenge, Mascot 라우트 추가

## 🚀 사용 방법

### 환경 설정
`.env.development` 파일 생성 (root 또는 frontend 폴더):
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### API 서버 설정
백엔드 서버가 다음 엔드포인트를 제공해야 합니다:
- `POST /api/signup` - 회원가입
- `POST /api/login` - 로그인  
- `DELETE /api/logout` - 로그아웃

### 페이지 플로우
1. **미인증 사용자**: `/` (로그인) 또는 `/signup` (회원가입)만 접근 가능
2. **인증된 사용자**: `/dashboard`, `/challenge`, `/mascot` 접근 가능
3. **자동 리다이렉션**: 상태에 맞지 않는 페이지 접근 시 자동 이동

## 🔧 기술적 특징

### 보안
- JWT 토큰을 localStorage에 저장
- Authorization 헤더 자동 추가
- 인증 상태 기반 라우트 보호
- 로그아웃 시 모든 인증 정보 삭제

### 사용성
- 실시간 폼 유효성 검사
- 로딩 상태 표시
- 에러 메시지 표시
- 모바일 반응형 UI
- 접근성 고려 (ARIA 속성)

### 확장성
- 타입 안전성 (TypeScript)
- 모듈화된 API 함수
- 재사용 가능한 에러 핸들링
- 환경변수 기반 설정

## 🐛 주의사항

1. **CORS 설정**: 백엔드에서 프론트엔드 도메인 허용 필요
2. **토큰 만료**: JWT 토큰 만료 시 자동 로그아웃 처리 고려
3. **브라우저 새로고침**: localStorage 기반이므로 새로고침 시에도 로그인 상태 유지
4. **보안**: 실제 운영에서는 httpOnly 쿠키 사용 권장

## 📁 파일 구조
```
frontend/solsol/src/
├── api/
│   └── index.ts          # API 함수 및 인증 유틸리티
├── types/
│   └── api.ts           # API 타입 정의
├── views/
│   ├── Login.vue        # 로그인 페이지
│   ├── Signup.vue       # 회원가입 페이지
│   ├── Dashboard.vue    # 대시보드 (인증 필요)
│   ├── Challenge.vue    # 챌린지 (인증 필요)
│   └── Mascot.vue       # 마스코트 (인증 필요)
├── components/
│   └── Header.vue       # 헤더 (로그아웃 포함)
└── router/
    └── index.ts         # 라우터 및 인증 가드
```

---

**구현 완료일**: $(date)  
**개발자**: Frontend Developer  
**프로젝트**: 쏠쏠Hey (SOLSOLHEY)  
**기술스택**: Vue 3 + TypeScript + Vite




