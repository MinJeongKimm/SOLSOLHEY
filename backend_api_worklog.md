# 백엔드 API 추가 작업 일지

## 📋 작업 개요
**목적**: 사용자 아이템 보유 기능을 위한 백엔드 API 구현  
**기간**: 2025.08.24
**담당**: 김준이

## 🎯 구현 목표
1. **상점 시스템**: 전체 아이템 목록 표시 + 사용자 보유 아이템 비활성화
2. **꾸미기 시스템**: 사용자가 보유한 아이템만 표시
3. **아이템 소유권 관리**: 사용자별 아이템 보유 상태 추적

## 📊 현재 상황 분석

### ✅ 이미 구현된 부분
- `UserItem` 엔티티 (shop_user_items 테이블용)
- `Background` 엔티티 (backgrounds 테이블용)  
- `UserBackground` 엔티티 (user_backgrounds 테이블용)
- `Item` 엔티티 (shop_items 테이블용)
- `ShopController` 기본 구조
- `ShopService` 기본 구조

### ❌ 누락된 부분
- 상점 아이템 목록에 보유 여부 포함 API
- 꾸미기 탭용 보유 아이템 조회 API (인벤토리 + 꾸미기 통합)
- 실제 데이터베이스 테이블 (엔티티만 정의됨)

## 🔧 구현 계획

### Phase 1: 데이터베이스 테이블 생성
- [x] `shop_user_items` 테이블 생성 (사용자 아이템 보유)
- [x] `backgrounds` 테이블 생성 (배경 아이템)
- [x] `user_backgrounds` 테이블 생성 (사용자 배경 보유)
- [x] 외래키 제약조건 설정
- [x] 인덱스 생성 (성능 최적화)

### Phase 2: 백엔드 API 구현
- [x] `ShopController`의 `getItems` 엔드포인트 수정 (보유 여부 포함)
- [x] `MascotController`에 `getAvailableItems` 엔드포인트 추가
- [x] `ShopService`에 `getItemsWithOwnership` 메서드 추가
- [x] `MascotService`에 `getAvailableItems` 메서드 추가
- [x] `AvailableItemResponse` DTO 수정
- [x] `ItemResponse` DTO 수정 (owned 필드 추가)

### Phase 3: 테스트 및 검증
- [ ] API 엔드포인트 테스트
- [ ] 데이터베이스 연동 테스트
- [ ] 프론트엔드 연동 테스트

## 📝 상세 작업 내용

### 1. 새로운 API 엔드포인트

#### A. 꾸미기 탭용 보유 아이템 조회 API (통합)
```
GET /api/v1/mascot/available-items
- 목적: 보유한 아이템 조회 (인벤토리 + 꾸미기 공통 사용)
- 인증: JWT 토큰 필요
- 응답: AvailableItemResponse[] (quantity, isEquipped, purchasedAt 포함)
- 사용처: 사용자 인벤토리, 마스코트 꾸미기 화면
```

#### B. 상점 아이템 목록 (보유 여부 포함) API
```
GET /api/v1/shop/items?type={type}
- 목적: 상점 아이템 목록 + 사용자 보유 여부
- 인증: JWT 토큰 필요  
- 응답: ItemResponse[] (owned 필드 포함)
- 사용처: 상점 화면 (보유한 아이템 비활성화)
```

### 2. 새로운 DTO 클래스

#### A. AvailableItemResponse.java (수정)
```java
public record AvailableItemResponse(
    Long itemId,
    String itemName,
    String itemType,
    String imageUrl,
    Integer quantity,        // 추가: 수량 정보
    Boolean isEquipped,     // 기존: 장착 상태
    LocalDateTime purchasedAt // 추가: 구매일
) {}
```

**참고**: UserItemResponse.java는 제거 (중복 제거)

### 3. 수정할 기존 DTO

#### A. ItemResponse.java
```java
// owned 필드 추가
private Boolean owned;

// fromWithOwnership 팩토리 메서드 추가
public static ItemResponse fromWithOwnership(Item item, Boolean owned) {
    // 구현 내용
}
```

## 🚨 주의사항

### 1. 기존 코드 충돌 방지
- 기존 API 엔드포인트 경로 유지 (`/api/v1/shop/items`)
- 기존 DTO 필드 구조 유지 (owned 필드만 추가)
- 기존 서비스 메서드 시그니처 유지
- **중복 API 제거**: 사용자 아이템 보유 조회 API는 제거

### 2. 데이터베이스 설계 고려사항
- 외래키 제약조건으로 데이터 무결성 보장
- 인덱스 생성으로 쿼리 성능 최적화
- 트랜잭션 처리로 데이터 일관성 보장

### 3. 보안 고려사항
- JWT 토큰 기반 사용자 인증
- 사용자별 데이터 접근 권한 검증
- SQL 인젝션 방지

## 📅 작업 일정

| 단계 | 작업 내용 | 예상 소요 시간 | 담당자 |
|------|-----------|----------------|--------|
| Phase 1 | DB 테이블 생성 | 2시간 | - |
| Phase 2 | 백엔드 API 구현 | 3시간 | - |
| Phase 3 | 테스트 및 검증 | 2시간 | - |
| **총계** | **전체 작업** | **7시간** | **-** |

**참고**: 중복 API 제거로 인해 Phase 2 작업 시간 단축

## 🔍 진행 상황 체크리스트

### Phase 1: 데이터베이스
- [ ] 테이블 생성 SQL 스크립트 작성
- [ ] 테이블 생성 실행
- [ ] 외래키 제약조건 확인
- [ ] 인덱스 생성

### Phase 2: 백엔드 구현  
- [x] ShopController 수정 (getItems에 보유 여부 포함)
- [x] ShopService 수정 (getItemsWithOwnership 메서드 추가)
- [x] MascotController 추가 (getAvailableItems 엔드포인트)
- [x] MascotService 추가 (getAvailableItems 메서드)
- [x] DTO 클래스 수정 (AvailableItemResponse, ItemResponse)
- [x] Repository 메서드 확인

### Phase 3: 테스트
- [ ] API 엔드포인트 테스트
- [ ] 데이터베이스 연동 테스트
- [ ] 에러 처리 테스트
- [ ] 성능 테스트

## 📚 참고 자료

### 기존 코드 파일
- `ShopController.java`: 상점 관련 API 컨트롤러
- `ShopService.java`: 상점 비즈니스 로직 인터페이스
- `ShopServiceImpl.java`: 상점 비즈니스 로직 구현체
- `MascotController.java`: 마스코트 관련 API 컨트롤러
- `MascotService.java`: 마스코트 비즈니스 로직 인터페이스
- `UserItem.java`: 사용자 아이템 보유 엔티티
- `Item.java`: 상점 아이템 엔티티
- `Background.java`: 배경 아이템 엔티티
- `UserBackground.java`: 사용자 배경 보유 엔티티

### 관련 문서
- [프로젝트 ERD 설계](./erd_design.md)
- [API 명세서](./api_specification.md)
- [데이터베이스 스키마](./database_schema.md)

---

**작성일**: 2024년 12월  
**작성자**: 개발팀  
**최종 수정일**: 2024년 12월  
**버전**: 1.3

## 📝 **수정 이력**

### v1.1 (2024년 12월)
- **중복 API 제거**: 사용자 아이템 보유 조회 API 제거
- **API 통합**: 꾸미기 탭용 보유 아이템 조회 API로 통합
- **작업 시간 단축**: Phase 2 작업 시간 4시간 → 3시간
- **전체 작업 시간**: 8시간 → 7시간
- **DTO 설계 개선**: AvailableItemResponse에 quantity, purchasedAt 필드 추가

### v1.2 (2024년 12월)
- **Phase 1 완료**: DTO 수정 및 생성 완료
- **Phase 2 완료**: 서비스 레이어 수정 완료
- **Phase 3 완료**: 컨트롤러 레이어 수정 완료
- **백엔드 API 구현 완료**: 사용자 아이템 보유 기능 API 구현 완료
- **다음 단계**: 데이터베이스 테이블 생성 및 테스트 진행 예정

### v1.3 (2024년 12월)
- **Phase 4 완료**: 데이터베이스 테이블 생성 완료
- **테이블 생성**: shop_user_items, backgrounds, user_backgrounds
- **테스트 데이터**: 사용자 아이템 보유, 배경 데이터 삽입
- **SQL 스크립트**: H2 및 PostgreSQL 환경별 스크립트 생성
- **다음 단계**: API 테스트 및 검증 진행 예정
