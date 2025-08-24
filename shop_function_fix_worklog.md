# Shop 기능 userPoints undefined 에러 해결 작업일지

## 📋 **작업 개요**
- **작업 기간**: 2024년 현재
- **작업 목적**: Shop.vue에서 발생한 "Invalid prop: type check failed for prop 'userPoints'" 에러 해결
- **작업 범위**: 프론트엔드 Shop 관련 컴포넌트 수정

## 🐛 **발생한 문제**
```
Shop.vue:47 [Vue warn]: Invalid prop: type check failed for prop "userPoints". 
Expected Number with value NaN, got Undefined
```

### **문제 원인 분석**
1. **비동기 데이터 로딩 타이밍 문제**: `loadUserPoints()` API 호출이 비동기적으로 실행되는 동안 컴포넌트 렌더링
2. **타입 불일치**: `GifticonShop` 컴포넌트가 `userPoints`를 `number` 타입으로 요구하지만 `undefined` 전달
3. **초기화 순서 문제**: `userPoints`가 API 응답 대기 중에 `undefined` 상태로 남아있음

## 🔧 **수정 작업 내용**

### **1. Shop.vue 수정**
**파일 경로**: `frontend/solsol/src/views/Shop.vue`

#### **주요 변경사항**
- `userPoints` 초기화: `ref(15000)` → `ref<number | null>(null)`
- 로딩 상태 추가: `isLoading` 상태 관리
- `validUserPoints` computed 속성 추가로 안전한 값 제공
- 로딩 중 UI 표시 및 데이터 준비 완료 후 컴포넌트 렌더링

#### **수정된 코드**
```typescript
// 반응형 데이터
const userPoints = ref<number | null>(null); // null로 초기화하여 로딩 상태 표시
const isLoading = ref(true); // 로딩 상태 추가

// userPoints가 유효한지 확인하는 computed 속성
const validUserPoints = computed(() => {
  return userPoints.value !== null && userPoints.value !== undefined ? userPoints.value : 0;
});

// 사용자 포인트 로드
async function loadUserPoints() {
  try {
    isLoading.value = true;
    const user = auth.getUser();
    if (user && user.userId) {
      const userInfo = await getUserInfo(user.userId);
      userPoints.value = userInfo.totalPoints || 0;
    } else {
      userPoints.value = 0;
    }
  } catch (err) {
    console.error('사용자 포인트 로드 실패:', err);
    userPoints.value = 0;
  } finally {
    isLoading.value = false;
  }
}
```

#### **UI 수정사항**
```vue
<!-- 로딩 상태일 때 -->
<div v-if="isLoading" class="flex justify-center py-8">
  <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
</div>

<!-- 로딩 완료 후 탭 컨텐츠 표시 -->
<div v-else>
  <div v-if="activeTab === 'items'">
    <ItemShop :user-points="validUserPoints" @points-updated="loadUserPoints" />
  </div>
  <div v-else-if="activeTab === 'gifticons'">
    <GifticonShop :user-points="validUserPoints" @points-updated="loadUserPoints" />
  </div>
</div>
```

### **2. GifticonShop.vue 수정**
**파일 경로**: `frontend/solsol/src/components/shop/GifticonShop.vue`

#### **주요 변경사항**
- `ShopItem` 타입 import 추가
- `handlePurchase` 함수 시그니처 수정으로 타입 호환성 개선
- 타입 가드를 사용한 안전한 아이템 처리

#### **수정된 코드**
```typescript
import type { Gifticon, ShopItem } from '../../types/api';

// 구매 처리
async function handlePurchase(item: Gifticon | ShopItem, quantity: number) {
  // Gifticon 타입인지 확인
  if (!('sku' in item)) {
    console.error('잘못된 아이템 타입입니다.');
    return;
  }
  
  try {
    const orderData = {
      type: 'GIFTICON' as const,
      sku: item.sku,
      quantity: quantity
    };
    
    await createOrder(orderData);
    closePurchaseDialog();
    emit('points-updated');
    
  } catch (err: any) {
    console.error('기프티콘 구매 실패:', err);
    alert('구매에 실패했습니다. 다시 시도해주세요.');
  }
}
```

## 📁 **수정된 파일 목록**

### **프론트엔드 파일**
1. `frontend/solsol/src/views/Shop.vue`
   - userPoints 초기화 로직 개선
   - 로딩 상태 추가
   - validUserPoints computed 속성 추가

2. `frontend/solsol/src/components/shop/GifticonShop.vue`
   - 타입 안전성 개선
   - ShopItem 타입 import 추가
   - handlePurchase 함수 타입 호환성 수정

## ✅ **해결된 문제들**

1. **✅ userPoints undefined 에러 해결**
   - 컴포넌트 렌더링 시 undefined 전달 방지
   - 로딩 상태로 데이터 준비 완료 후 렌더링

2. **✅ 타입 안전성 개선**
   - validUserPoints computed 속성으로 항상 유효한 number 값 제공
   - 타입 가드를 사용한 안전한 처리

3. **✅ 사용자 경험 개선**
   - 로딩 스피너로 데이터 로딩 상태 표시
   - 에러 발생 시 기본값 설정으로 안정성 향상

## 🔍 **현재 Shop 기능 상태**

### **정상 작동하는 기능**
- ✅ 아이템 구매 시스템 (백엔드 DB 연동 완료)
- ✅ 기프티콘 목록 표시 (Mock 데이터 기반)
- ✅ 사용자 포인트 표시 (API 연동 완료)
- ✅ 로딩 상태 관리

### **아직 미구현된 기능**
- ❌ 포인트 차감 로직 (백엔드 TODO 상태)
- ❌ 실제 기프티콘 발급 시스템
- ❌ 포인트 부족 시 구매 제한

## 🚀 **테스트 방법**

1. **개발자 도구 Console 탭**
   - "Invalid prop" Vue 경고 메시지 없음 확인
   - JavaScript 에러 없음 확인

2. **Network 탭**
   - `/api/v1/users/{userId}` API 호출 성공 확인
   - 응답에 totalPoints 필드 포함 확인

3. **Vue Devtools**
   - Shop 컴포넌트의 userPoints 상태 변화 확인
   - isLoading 상태 변화 확인
   - GifticonShop의 userPoints prop 전달 확인

## 📝 **작업 완료 요약**

이번 작업을 통해 Shop 기능의 핵심적인 에러를 해결하고, 사용자 경험을 개선했습니다. 특히 비동기 데이터 로딩과 타입 안전성 측면에서 큰 개선이 이루어졌습니다.

**주요 성과:**
- Vue prop 타입 에러 완전 해결
- 로딩 상태 관리로 안정적인 UI 제공
- 타입 안전성 강화로 런타임 에러 방지
- 사용자 경험 개선 (로딩 표시, 에러 처리)

**다음 단계로 고려할 사항:**
- 백엔드 포인트 차감 로직 구현
- 실제 기프티콘 시스템 연동
- 포인트 부족 시 구매 제한 로직 추가
