# 포인트 시스템 완성 작업 계획

## 📋 **작업 개요**
- **작업 제목**: 포인트 시스템 완성 - 챌린지 적립부터 쇼핑 소모까지
- **작업 기간**: 2025-08-24 오후 5시 12분부터
- **작업 목적**: 포인트 시스템의 핵심 로직 완성으로 챌린지→적립→쇼핑→소모의 완전한 흐름 구현
- **작업 범위**: 백엔드 포인트 로직 구현 + 프론트엔드 에러 처리 개선

## 🎯 **구현 목표**

### **1. 포인트 적립 시스템**
- 챌린지 완료 시 자동 포인트 적립
- 적립 내역 추적 및 거래 내역 저장
- ReferenceType.CHALLENGE로 챌린지 완료 참조

### **2. 포인트 소모 시스템**
- 쇼핑하기에서 아이템/기프티콘 구매 시 포인트 차감
- 차감 내역 추적 및 거래 내역 저장
- ReferenceType.PURCHASE로 상품 구매 참조

### **3. 실시간 포인트 반영**
- 구매 후 즉시 포인트 잔액 업데이트
- UI에 실시간 반영
- 포인트 부족 시 구매 제한

### **4. 포인트 부족 시 구매 제한**
- 백엔드에서 포인트 부족 시 구매 차단
- 프론트엔드에서 적절한 에러 메시지 표시
- 사용자 경험 개선

## 📊 **현재 구현 상태 분석**

### ✅ **이미 구현된 부분**
- **포인트 시스템 인프라**: PointService, PointTransaction, DTO 등 완벽
- **포인트 부족 체크**: PointServiceImpl에서 잔액 확인 로직 완벽
- **프론트엔드 UI**: 실시간 포인트 반영, 구매 제한 UI 완벽
- **거래 내역 시스템**: PointTransaction 엔티티 및 저장 로직 완벽

### ❌ **아직 미구현된 부분**
- **챌린지 포인트 적립**: ChallengeServiceImpl에서 TODO 주석 상태
- **쇼핑 포인트 차감**: ShopServiceImpl에서 TODO 주석 상태
- **PointService 연동**: ShopServiceImpl에서 PointService 미연동

## 🔧 **단계별 작업 계획**

### **Phase 1: PointService 확장 (30분)**
- [ ] `PointService.java`에 `awardChallengeReward` 메서드 추가
- [ ] `PointServiceImpl.java`에 `awardChallengeReward` 구현
- [ ] 챌린지 완료 시 포인트 적립 로직 구현

### **Phase 2: 챌린지 포인트 적립 구현 (30분)**
- [ ] `ChallengeServiceImpl.java`에서 TODO 주석 해제
- [ ] `pointService.awardChallengeReward()` 호출 활성화
- [ ] 챌린지 완료 시 포인트 적립 테스트

### **Phase 3: 쇼핑 포인트 차감 구현 (1시간)**
- [ ] `ShopServiceImpl.java`에 PointService import 추가
- [ ] `processItemOrder()`에서 포인트 차감 로직 구현
- [ ] `processGifticonOrder()`에서 포인트 차감 로직 구현
- [ ] 포인트 부족 시 구매 차단 로직 추가

### **Phase 4: 프론트엔드 에러 처리 개선 (30분)**
- [ ] `PurchaseDialog.vue`에서 포인트 부족 시 에러 메시지 개선
- [ ] 구매 실패 시 적절한 사용자 안내 메시지 추가

### **Phase 5: 테스트 및 검증 (30분)**
- [ ] 챌린지 완료 시 포인트 적립 테스트
- [ ] 쇼핑하기에서 포인트 차감 테스트
- [ ] 포인트 부족 시 구매 차단 테스트
- [ ] 실시간 포인트 반영 테스트

## 📁 **수정 대상 파일 목록**

### **백엔드 파일**
1. **`backend/solsol/src/main/java/com/solsolhey/point/service/PointService.java`**
   - `awardChallengeReward` 메서드 인터페이스 추가

2. **`backend/solsol/src/main/java/com/solsolhey/point/service/PointServiceImpl.java`**
   - `awardChallengeReward` 메서드 실제 구현

3. **`backend/solsol/src/main/java/com/solsolhey/challenge/service/ChallengeServiceImpl.java`**
   - TODO 주석 해제 및 포인트 적립 로직 활성화

4. **`backend/solsol/src/main/java/com/solsolhey/shop/service/ShopServiceImpl.java`**
   - PointService import 추가
   - 포인트 차감 로직 구현
   - 포인트 부족 시 구매 차단 로직 추가

### **프론트엔드 파일**
1. **`frontend/solsol/src/components/shop/PurchaseDialog.vue`**
   - 포인트 부족 시 에러 메시지 개선
   - 구매 실패 시 사용자 안내 개선

## 💻 **상세 구현 내용**

### **1. PointService 인터페이스 확장**

#### **PointService.java에 메서드 추가**
```java
/**
 * 챌린지 완료 보상 포인트 지급
 */
PointTransactionResponse awardChallengeReward(User user, Integer rewardPoints, Long challengeId);
```

#### **PointServiceImpl.java에 구현 추가**
```java
@Override
public PointTransactionResponse awardChallengeReward(User user, Integer rewardPoints, Long challengeId) {
    log.debug("챌린지 완료 보상 지급: userId={}, challengeId={}, points={}", 
              user.getUserId(), challengeId, rewardPoints);
    
    // 포인트 적립
    user.addPoints(rewardPoints);
    userRepository.save(user);
    
    // 거래 내역 생성
    PointTransaction transaction = PointTransaction.builder()
            .user(user)
            .pointAmount(rewardPoints)
            .transactionType(TransactionType.EARN)
            .description("챌린지 완료 보상")
            .referenceId(challengeId)
            .referenceType(ReferenceType.CHALLENGE)
            .balanceAfter(user.getTotalPoints())
            .build();
    
    PointTransaction savedTransaction = pointTransactionRepository.save(transaction);
    return PointTransactionResponse.from(savedTransaction);
}
```

### **2. 챌린지 포인트 적립 활성화**

#### **ChallengeServiceImpl.java 수정**
```java
// 기존 TODO 주석 해제
if (!wasCompleted && savedUserChallenge.isCompleted()) {
    try {
        // 포인트 적립 로직 활성화
        pointService.awardChallengeReward(user, challenge.getRewardPoints(), challenge.getChallengeId());
        log.info("챌린지 완료 보상 지급 완료: userId={}, challengeId={}, points={}", 
                user.getUserId(), challengeId, challenge.getRewardPoints());
    } catch (Exception e) {
        log.error("챌린지 보상 지급 실패: userId={}, challengeId={}", user.getUserId(), challengeId, e);
        // 보상 지급 실패해도 진행도 업데이트는 성공으로 처리
    }
}
```

### **3. 쇼핑 포인트 차감 구현**

#### **ShopServiceImpl.java import 추가**
```java
import com.solsolhey.point.service.PointService;
import com.solsolhey.point.dto.request.PointSpendRequest;
import com.solsolhey.point.entity.PointTransaction.ReferenceType;
```

#### **ShopServiceImpl.java 의존성 추가**
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShopServiceImpl implements ShopService {
    
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserItemRepository userItemRepository;
    private final PointService pointService; // 추가
```

#### **processItemOrder 메서드 수정**
```java
private Order processItemOrder(Long userId, OrderRequest request) {
    // ... 기존 코드 ...
    
    // 총 가격 계산
    int totalPrice = item.getPrice() * quantity;
    
    // 포인트 차감 로직 구현
    try {
        PointSpendRequest spendRequest = new PointSpendRequest(
            totalPrice, 
            "상품 구매: " + item.getName(), 
            null, // 주문 ID는 주문 생성 후 설정
            ReferenceType.PURCHASE
        );
        pointService.spendPoints(user, spendRequest);
        log.info("포인트 차감 완료: userId={}, amount={}, item={}", userId, totalPrice, item.getName());
    } catch (Exception e) {
        log.error("포인트 차감 실패: userId={}, amount={}, item={}", userId, totalPrice, item.getName(), e);
        throw new IllegalStateException("포인트 차감에 실패했습니다: " + e.getMessage());
    }
    
    // ... 기존 코드 ...
}
```

#### **processGifticonOrder 메서드 수정**
```java
private Order processGifticonOrder(Long userId, OrderRequest request) {
    // ... 기존 코드 ...
    
    // Mock 기프티콘 가격
    int gifticonPrice = getGifticonPrice(request.getSku());
    
    // 포인트 차감 로직 구현
    try {
        PointSpendRequest spendRequest = new PointSpendRequest(
            gifticonPrice, 
            "기프티콘 구매: " + request.getSku(), 
            null, // 주문 ID는 주문 생성 후 설정
            ReferenceType.PURCHASE
        );
        pointService.spendPoints(user, spendRequest);
        log.info("포인트 차감 완료: userId={}, amount={}, sku={}", userId, gifticonPrice, request.getSku());
    } catch (Exception e) {
        log.error("포인트 차감 실패: userId={}, amount={}, sku={}", userId, gifticonPrice, request.getSku(), e);
        throw new IllegalStateException("포인트 차감에 실패했습니다: " + e.getMessage());
    }
    
    // ... 기존 코드 ...
}
```

### **4. 프론트엔드 에러 처리 개선**

#### **PurchaseDialog.vue 에러 메시지 개선**
```vue
<!-- 포인트 부족 경고 -->
<div v-if="isInsufficientPoints" class="mt-2 text-red-600 text-sm font-medium">
  ⚠️ 포인트가 부족합니다! (필요: {{ totalPrice }}P, 보유: {{ userPoints }}P)
</div>

<!-- 구매 후 잔액 -->
<div v-else class="mt-2 text-sm text-gray-600">
  구매 후 잔액: <span class="font-medium">{{ remainingPoints }}P</span>
</div>
```

## 🚨 **주의사항 및 고려사항**

### **1. 트랜잭션 관리**
- 포인트 차감과 주문 생성을 하나의 트랜잭션으로 처리
- 실패 시 자동 롤백 보장
- 포인트 차감 실패 시 주문 생성 차단

### **2. 에러 처리**
- 포인트 부족 시 적절한 에러 메시지
- 포인트 차감 실패 시 구매 취소
- 사용자에게 명확한 안내 메시지 제공

### **3. 성능 최적화**
- 포인트 잔액 조회 시 캐싱 고려
- 대량 구매 시 배치 처리 고려
- 적절한 로깅으로 모니터링

### **4. 보안**
- 포인트 차감 시 사용자 권한 확인
- 중복 구매 방지
- 부정 사용 방지

## 📅 **작업 일정**

| 단계 | 작업 내용 | 예상 소요 시간 | 담당자 | 상태 |
|------|-----------|----------------|--------|------|
| Phase 1 | PointService 확장 | 30분 | - | ⏳ 대기 |
| Phase 2 | 챌린지 포인트 적립 | 30분 | - | ⏳ 대기 |
| Phase 3 | 쇼핑 포인트 차감 | 1시간 | - | ⏳ 대기 |
| Phase 4 | 프론트엔드 에러 처리 | 30분 | - | ⏳ 대기 |
| Phase 5 | 테스트 및 검증 | 30분 | - | ⏳ 대기 |
| **총계** | **전체 작업** | **3시간** | **-** | **-** |

## 🔍 **테스트 시나리오**

### **1. 챌린지 포인트 적립 테스트**
- 챌린지 진행도 100% 달성
- 포인트 적립 확인
- 거래 내역에 CHALLENGE 타입으로 기록 확인

### **2. 쇼핑 포인트 차감 테스트**
- 충분한 포인트로 아이템 구매
- 포인트 차감 확인
- 거래 내역에 PURCHASE 타입으로 기록 확인

### **3. 포인트 부족 시 구매 차단 테스트**
- 부족한 포인트로 구매 시도
- 백엔드에서 구매 차단 확인
- 프론트엔드에서 적절한 에러 메시지 표시 확인

### **4. 실시간 포인트 반영 테스트**
- 구매 후 포인트 잔액 즉시 업데이트 확인
- UI에 실시간 반영 확인

## 📚 **참고 자료**

### **기존 코드 파일**
- `PointService.java`: 포인트 서비스 인터페이스
- `PointServiceImpl.java`: 포인트 서비스 구현체
- `ChallengeServiceImpl.java`: 챌린지 서비스 (포인트 적립 로직)
- `ShopServiceImpl.java`: 쇼핑 서비스 (포인트 차감 로직)
- `PurchaseDialog.vue`: 구매 다이얼로그 (포인트 부족 체크)

### **관련 문서**
- [포인트 시스템 설계 문서](./point_system_design.md)
- [챌린지 시스템 API 문서](./challenge_api_docs.md)
- [쇼핑 시스템 API 문서](./shop_api_docs.md)

---

**작성일**: 2025년 8월 24일 오후 5시 12분  
**작성자**: 개발팀  
**최종 수정일**: 2025년 8월 24일  
**버전**: 1.0
