# 🚀 백엔드 API 구현 상세 작업 계획

## 📋 **작업 개요**
**목적**: 사용자 아이템 보유 기능을 위한 백엔드 API 구현  
**기간**: 2024년 12월  
**담당**: 개발팀  
**이미지**: 작업 완료 후 추가 예정

## 🎯 **구현 목표**
1. **상점 시스템**: 전체 아이템 목록 표시 + 사용자 보유 아이템 비활성화
2. **꾸미기 시스템**: 사용자가 보유한 아이템만 표시
3. **아이템 소유권 관리**: 사용자별 아이템 보유 상태 추적

## 📊 **현재 상황 분석**

### ✅ **이미 구현된 부분**
- `UserItem` 엔티티 (shop_user_items 테이블용)
- `Background` 엔티티 (backgrounds 테이블용)  
- `UserBackground` 엔티티 (user_backgrounds 테이블용)
- `Item` 엔티티 (shop_items 테이블용)
- `ShopController` 기본 구조
- `ShopService` 기본 구조
- `MascotController` 기본 구조

### ❌ **누락된 부분**
- `ItemResponse`에 `owned` 필드 없음
- `ShopController.getItems`에서 사용자 인증 정보 미사용
- `ShopService`에서 보유 여부 확인 로직 없음
- 꾸미기용 보유 아이템 조회 API 없음
- 실제 데이터베이스 테이블 (엔티티만 정의됨)

## 🔧 **단계별 작업 계획**

### **Phase 1: DTO 수정 및 생성 (1시간)**
- [ ] `ItemResponse.java`에 `owned` 필드 추가
- [ ] `ItemResponse.java`에 `fromWithOwnership` 팩토리 메서드 추가
- [ ] `AvailableItemResponse.java` 생성
- [ ] 기존 `from` 메서드 유지 (호환성 보장)

### **Phase 2: 서비스 레이어 수정 (2시간)**
- [ ] `ShopService.java`에 `getItemsWithOwnership` 메서드 추가
- [ ] `ShopServiceImpl.java`에 `getItemsWithOwnership` 구현
- [ ] `MascotService.java`에 `getAvailableItems` 메서드 추가
- [ ] `MascotServiceImpl.java`에 `getAvailableItems` 구현
- [ ] 보유 여부 확인 로직 구현
- [ ] 장착 상태 확인 로직 구현

### **Phase 3: 컨트롤러 레이어 수정 (1시간)**
- [ ] `ShopController.java`의 `getItems` 수정 (사용자 인증 정보 추가)
- [ ] `MascotController.java`에 `getAvailableItems` 엔드포인트 추가
- [ ] 응답 형식 통일 (result, message, data 구조)
- [ ] 에러 처리 및 로깅 추가

### **Phase 4: 데이터베이스 테이블 생성 (1시간)**
- [ ] `shop_user_items` 테이블 생성
- [ ] `backgrounds` 테이블 생성
- [ ] `user_backgrounds` 테이블 생성
- [ ] 외래키 제약조건 설정
- [ ] 테스트 데이터 삽입

### **Phase 5: 테스트 및 검증 (1시간)**
- [ ] API 엔드포인트 테스트
- [ ] 데이터베이스 연동 테스트
- [ ] 프론트엔드 연동 테스트
- [ ] 에러 케이스 테스트
- [ ] 성능 테스트

## 📝 **상세 작업 내용**

### **Phase 1: DTO 수정 및 생성**

#### **1.1 ItemResponse.java 수정**
```java
// owned 필드 추가
@Schema(description = "보유 여부", example = "true")
private Boolean owned;

// 기존 from 메서드 유지 (호환성)
public static ItemResponse from(Item item) { ... }

// 새로운 팩토리 메서드 추가
public static ItemResponse fromWithOwnership(Item item, Boolean owned) {
    ItemResponse response = from(item);
    response.setOwned(owned);
    return response;
}
```

#### **1.2 AvailableItemResponse.java 생성**
```java
public record AvailableItemResponse(
    Long itemId,
    String itemName,
    String itemType,
    String imageUrl,
    Integer quantity,
    Boolean isEquipped,
    LocalDateTime purchasedAt
) {}
```

### **Phase 2: 서비스 레이어 수정**

#### **2.1 ShopService.java 인터페이스 수정**
```java
// 기존 메서드 유지
List<ItemResponse> getItems(String type);

// 새로운 메서드 추가
List<ItemResponse> getItemsWithOwnership(Long userId, String type);
```

#### **2.2 ShopServiceImpl.java 구현체 수정**
```java
@Override
public List<ItemResponse> getItemsWithOwnership(Long userId, String type) {
    List<Item> items = getItemsByType(type);
    
    return items.stream()
        .map(item -> {
            Boolean owned = userItemRepository.existsByUserIdAndItemId(userId, item.getId());
            return ItemResponse.fromWithOwnership(item, owned);
        })
        .collect(Collectors.toList());
}
```

#### **2.3 MascotService.java 인터페이스 추가**
```java
List<AvailableItemResponse> getAvailableItems(Long userId);
```

#### **2.4 MascotServiceImpl.java 구현체 추가**
```java
@Override
public List<AvailableItemResponse> getAvailableItems(Long userId) {
    List<UserItem> userItems = userItemRepository.findByUserId(userId);
    
    return userItems.stream()
        .map(userItem -> {
            Item item = itemRepository.findById(userItem.getItemId())
                .orElse(null);
            if (item == null) return null;
            
            Boolean isEquipped = checkIfEquipped(userId, item.getId());
            
            return new AvailableItemResponse(
                item.getId(),
                item.getName(),
                item.getType().name(),
                item.getImageUrl(),
                userItem.getQuantity(),
                isEquipped,
                userItem.getPurchasedAt()
            );
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
}
```

### **Phase 3: 컨트롤러 레이어 수정**

#### **3.1 ShopController.java 수정**
```java
@GetMapping("/items")
public ResponseEntity<Map<String, Object>> getItems(
        @AuthenticationPrincipal CustomUserDetails userDetails,  // 추가
        @RequestParam(required = false) String type) {
    
    try {
        Long userId = userDetails.getUserId();
        log.info("상품 목록 조회 API 호출 - 사용자 ID: {}, type: {}", userId, type);
        
        // 기존 로직을 새로운 메서드로 교체
        List<ItemResponse> items = shopService.getItemsWithOwnership(userId, type);
        
        // 기존 응답 형식 유지
        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("message", "상품 목록 조회가 완료되었습니다.");
        result.put("data", items);
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        log.error("상품 목록 조회 중 오류 발생", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "상품 목록 조회에 실패했습니다.");
    }
}
```

#### **3.2 MascotController.java에 새로운 엔드포인트 추가**
```java
@GetMapping("/available-items")
public ResponseEntity<Map<String, Object>> getAvailableItems(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    try {
        Long userId = userDetails.getUserId();
        log.info("사용 가능한 아이템 조회 API 호출 - 사용자 ID: {}", userId);
        
        List<AvailableItemResponse> items = mascotService.getAvailableItems(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("message", "사용 가능한 아이템 조회가 완료되었습니다.");
        result.put("data", items);
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        log.error("사용 가능한 아이템 조회 중 오류 발생", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "사용 가능한 아이템 조회에 실패했습니다.");
    }
}
```

### **Phase 4: 데이터베이스 테이블 생성**

#### **4.1 테이블 생성 SQL**
```sql
-- 사용자 아이템 보유 테이블
CREATE TABLE shop_user_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    purchased_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES shop_items(id)
);

-- 배경 테이블
CREATE TABLE backgrounds (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    preview_url VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    tags TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- 사용자 배경 보유 테이블
CREATE TABLE user_backgrounds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    background_id VARCHAR(50) NOT NULL,
    acquired_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (background_id) REFERENCES backgrounds(id)
);
```

#### **4.2 테스트 데이터 삽입**
```sql
-- 테스트 사용자
INSERT INTO users (user_id, username, email, password_hash, nickname, campus, total_points, is_active, created_at, updated_at) 
VALUES (1, 'testuser', 'test@test.com', 'hashedpassword', '테스트유저', '서울캠퍼스', 5000, true, NOW(), NOW());

-- 테스트 아이템
INSERT INTO shop_items (id, name, description, price, type, image_url, is_active, created_at, updated_at) VALUES
(1001, '토끼 귀', '귀여운 토끼 귀 머리띠입니다.', 800, 'EQUIP', '/items/item_head_bunny_ears.png', true, NOW(), NOW()),
(1002, '핑크 비니', '따뜻하고 스타일리시한 핑크 비니입니다.', 500, 'EQUIP', '/items/item_head_pink_beanie.png', true, NOW(), NOW()),
(2001, '하트 안경', '사랑스러운 하트 모양 안경입니다.', 1200, 'EQUIP', '/items/item_acc_glasses_heart.png', true, NOW(), NOW());

-- 테스트 사용자 아이템 보유
INSERT INTO shop_user_items (id, user_id, item_id, quantity, purchased_at) VALUES
(1, 1, 1001, 1, NOW()),
(2, 1, 1002, 1, NOW());
```

### **Phase 5: 테스트 및 검증**

#### **5.1 API 테스트**
```bash
# 1. 상점 아이템 목록 조회 (보유 여부 포함)
curl -X GET "http://localhost:8080/api/v1/shop/items" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json"

# 2. 꾸미기용 보유 아이템 조회
curl -X GET "http://localhost:8080/api/v1/mascot/available-items" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json"
```

#### **5.2 예상 응답 검증**
- `owned` 필드가 정확히 반환되는지
- 보유한 아이템만 `getAvailableItems`에서 반환되는지
- 에러 케이스에서 적절한 에러 응답이 오는지

## 🚨 **주의사항 및 고려사항**

### **1. 기존 코드 호환성 유지**
- 기존 `getItems` 메서드 시그니처 변경 없음
- 기존 응답 형식 유지
- 기존 클라이언트 코드 영향 최소화

### **2. 성능 최적화**
- `existsByUserIdAndItemId` 쿼리 최적화
- 필요한 경우 배치 쿼리 사용
- 적절한 인덱스 생성

### **3. 에러 처리**
- 사용자 인증 실패 시 적절한 에러 응답
- 데이터베이스 오류 시 로깅 및 에러 처리
- 일관된 에러 응답 형식

### **4. 로깅 및 모니터링**
- 각 단계별 상세 로깅
- 성능 메트릭 수집
- 에러 발생 시 알림

## 📅 **작업 일정**

| 단계 | 작업 내용 | 예상 소요 시간 | 담당자 | 상태 |
|------|-----------|----------------|--------|------|
| Phase 1 | DTO 수정/생성 | 1시간 | - | 🔄 진행 예정 |
| Phase 2 | 서비스 레이어 수정 | 2시간 | - | ⏳ 대기 |
| Phase 3 | 컨트롤러 수정 | 1시간 | - | ⏳ 대기 |
| Phase 4 | DB 테이블 생성 | 1시간 | - | ⏳ 대기 |
| Phase 5 | 테스트 및 검증 | 1시간 | - | ⏳ 대기 |
| **총계** | **전체 작업** | **6시간** | **-** | **-** |

## 🔍 **진행 상황 체크리스트**

### **Phase 1: DTO 수정 및 생성**
- [ ] ItemResponse.java에 owned 필드 추가
- [ ] ItemResponse.java에 fromWithOwnership 팩토리 메서드 추가
- [ ] AvailableItemResponse.java 생성
- [ ] 기존 from 메서드 유지 확인

### **Phase 2: 서비스 레이어 수정**
- [ ] ShopService.java에 getItemsWithOwnership 메서드 추가
- [ ] ShopServiceImpl.java에 getItemsWithOwnership 구현
- [ ] MascotService.java에 getAvailableItems 메서드 추가
- [ ] MascotServiceImpl.java에 getAvailableItems 구현
- [ ] 보유 여부 확인 로직 구현
- [ ] 장착 상태 확인 로직 구현

### **Phase 3: 컨트롤러 레이어 수정**
- [ ] ShopController.java의 getItems 수정
- [ ] MascotController.java에 getAvailableItems 엔드포인트 추가
- [ ] 응답 형식 통일
- [ ] 에러 처리 및 로깅 추가

### **Phase 4: 데이터베이스 테이블 생성**
- [ ] shop_user_items 테이블 생성
- [ ] backgrounds 테이블 생성
- [ ] user_backgrounds 테이블 생성
- [ ] 외래키 제약조건 설정
- [ ] 테스트 데이터 삽입

### **Phase 5: 테스트 및 검증**
- [ ] API 엔드포인트 테스트
- [ ] 데이터베이스 연동 테스트
- [ ] 프론트엔드 연동 테스트
- [ ] 에러 케이스 테스트
- [ ] 성능 테스트

## 📚 **참고 자료**

### **기존 코드 파일**
- `ShopController.java`: 상점 관련 API 컨트롤러
- `ShopService.java`: 상점 비즈니스 로직 인터페이스
- `ShopServiceImpl.java`: 상점 비즈니스 로직 구현체
- `MascotController.java`: 마스코트 관련 API 컨트롤러
- `MascotService.java`: 마스코트 비즈니스 로직 인터페이스
- `UserItem.java`: 사용자 아이템 보유 엔티티
- `Item.java`: 상점 아이템 엔티티
- `Background.java`: 배경 아이템 엔티티
- `UserBackground.java`: 사용자 배경 보유 엔티티

### **관련 문서**
- [백엔드 API 작업 일지](./backend_api_worklog.md)
- [프로젝트 ERD 설계](./erd_design.md)

---

**작성일**: 2024년 12월  
**작성자**: 개발팀  
**최종 수정일**: 2024년 12월  
**버전**: 1.0
