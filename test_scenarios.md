# 🧪 **SOLSOLHEY 백엔드 API 테스트 시나리오**

## 📋 **테스트 개요**
**목적**: 사용자 아이템 보유 기능 API 검증  
**대상**: Phase 1-4에서 구현된 백엔드 API  
**환경**: 로컬 개발 환경 (H2/PostgreSQL)  
**도구**: Postman, curl, 브라우저 개발자 도구  

---

## 🎯 **테스트 대상 API**

### **1. 상점 아이템 목록 조회 API**
- **엔드포인트**: `GET /api/v1/shop/items`
- **기능**: 전체 아이템 목록 + 사용자별 보유 여부
- **인증**: JWT 토큰 필요

### **2. 꾸미기용 보유 아이템 조회 API**
- **엔드포인트**: `GET /api/v1/mascot/available-items`
- **기능**: 사용자가 보유한 아이템만 조회
- **인증**: JWT 토큰 필요

---

## 🔍 **상세 테스트 시나리오**

### **시나리오 1: 상점 아이템 목록 조회 (보유 여부 포함)**

#### **1.1 정상 케이스 - 전체 아이템 조회**
```bash
# 요청
curl -X GET "http://localhost:8080/api/v1/shop/items" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json"

# 예상 응답
{
  "result": "SUCCESS",
  "message": "상품 목록 조회가 완료되었습니다.",
  "data": [
    {
      "id": 1001,
      "name": "토끼 귀",
      "description": "귀여운 토끼 귀 머리띠입니다.",
      "price": 800,
      "type": "EQUIP",
      "imageUrl": "/items/item_head_bunny_ears.png",
      "owned": true  // 사용자가 보유한 아이템
    },
    {
      "id": 1002,
      "name": "핑크 비니",
      "description": "따뜻하고 스타일리시한 핑크 비니입니다.",
      "price": 500,
      "type": "EQUIP",
      "imageUrl": "/items/item_head_pink_beanie.png",
      "owned": true  // 사용자가 보유한 아이템
    },
    {
      "id": 2001,
      "name": "하트 안경",
      "description": "사랑스러운 하트 모양 안경입니다.",
      "price": 1200,
      "type": "EQUIP",
      "imageUrl": "/items/item_acc_glasses_heart.png",
      "owned": true  // 사용자가 보유한 아이템
    }
  ]
}
```

#### **1.2 정상 케이스 - 타입별 아이템 조회**
```bash
# 요청
curl -X GET "http://localhost:8080/api/v1/shop/items?type=EQUIP" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json"

# 예상 응답: EQUIP 타입 아이템만 반환
```

#### **1.3 에러 케이스 - 인증 실패**
```bash
# 요청 (토큰 없음)
curl -X GET "http://localhost:8080/api/v1/shop/items" \
  -H "Content-Type: application/json"

# 예상 응답: 401 Unauthorized
```

#### **1.4 에러 케이스 - 잘못된 타입**
```bash
# 요청
curl -X GET "http://localhost:8080/api/v1/shop/items?type=INVALID" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json"

# 예상 응답: 400 Bad Request
{
  "result": "ERROR",
  "message": "유효하지 않은 상품 타입입니다: INVALID",
  "data": null
}
```

### **시나리오 2: 꾸미기용 보유 아이템 조회**

#### **2.1 정상 케이스 - 보유 아이템 조회**
```bash
# 요청
curl -X GET "http://localhost:8080/api/v1/mascot/available-items" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json"

# 예상 응답
{
  "result": "SUCCESS",
  "message": "사용 가능한 아이템 조회가 완료되었습니다.",
  "data": [
    {
      "itemId": 1001,
      "itemName": "토끼 귀",
      "itemType": "EQUIP",
      "imageUrl": "/items/item_head_bunny_ears.png",
      "quantity": 1,
      "isEquipped": false,
      "purchasedAt": "2024-12-19T10:00:00"
    },
    {
      "itemId": 1002,
      "itemName": "핑크 비니",
      "itemType": "EQUIP",
      "imageUrl": "/items/item_head_pink_beanie.png",
      "quantity": 1,
      "isEquipped": false,
      "purchasedAt": "2024-12-19T10:00:00"
    },
    {
      "itemId": 2001,
      "itemName": "하트 안경",
      "itemType": "EQUIP",
      "imageUrl": "/items/item_acc_glasses_heart.png",
      "quantity": 1,
      "isEquipped": false,
      "purchasedAt": "2024-12-19T10:00:00"
    }
  ]
}
```

#### **2.2 에러 케이스 - 인증 실패**
```bash
# 요청 (토큰 없음)
curl -X GET "http://localhost:8080/api/v1/mascot/available-items" \
  -H "Content-Type: application/json"

# 예상 응답: 401 Unauthorized
```

#### **2.3 에러 케이스 - 보유 아이템 없음**
```bash
# 새로운 사용자로 테스트 (아이템을 구매하지 않은 사용자)
# 예상 응답: 빈 배열 반환
{
  "result": "SUCCESS",
  "message": "사용 가능한 아이템 조회가 완료되었습니다.",
  "data": []
}
```

---

## 🗄️ **데이터베이스 테스트**

### **테스트 1: 테이블 생성 확인**
```sql
-- H2 데이터베이스
SHOW TABLES;

-- PostgreSQL
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
```

**예상 결과**: `shop_user_items`, `backgrounds`, `user_backgrounds` 테이블 존재

### **테스트 2: 테스트 데이터 확인**
```sql
-- 사용자 아이템 보유 확인
SELECT * FROM shop_user_items WHERE user_id = 1;

-- 배경 확인
SELECT * FROM backgrounds WHERE enabled = true;

-- 사용자 배경 보유 확인
SELECT * FROM user_backgrounds WHERE user_id = 1;
```

**예상 결과**: 테스트 데이터가 정상적으로 삽입됨

### **테스트 3: 외래키 제약조건 확인**
```sql
-- 외래키 제약조건 확인
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_NAME IS NOT NULL
AND TABLE_SCHEMA = 'public';
```

---

## 🌐 **프론트엔드 연동 테스트**

### **테스트 1: 상점 화면**
1. **상점 진입**: 상점 탭 클릭
2. **아이템 목록 확인**: 전체 아이템이 표시되는지 확인
3. **보유 여부 표시**: 보유한 아이템이 비활성화되는지 확인
4. **타입별 필터링**: EQUIP, BACKGROUND 타입별 필터링 동작 확인

### **테스트 2: 꾸미기 화면**
1. **꾸미기 진입**: 꾸미기 탭 클릭
2. **보유 아이템 확인**: 보유한 아이템만 표시되는지 확인
3. **아이템 장착**: 아이템 클릭하여 장착 동작 확인
4. **아이템 해제**: 장착된 아이템 해제 동작 확인

---

## 🚨 **에러 케이스 테스트**

### **에러 1: 데이터베이스 연결 실패**
- **시나리오**: 데이터베이스 서버 중단
- **예상 동작**: 적절한 에러 메시지 반환
- **예상 응답**: 500 Internal Server Error

### **에러 2: 잘못된 사용자 ID**
- **시나리오**: 존재하지 않는 사용자 ID로 요청
- **예상 동작**: 적절한 에러 메시지 반환
- **예상 응답**: 400 Bad Request 또는 404 Not Found

### **에러 3: 잘못된 아이템 ID**
- **시나리오**: 데이터베이스에 존재하지 않는 아이템 ID 참조
- **예상 동작**: 해당 아이템은 제외하고 정상 아이템만 반환
- **예상 응답**: 200 OK (부분 데이터)

---

## 📊 **성능 테스트**

### **테스트 1: 응답 시간 측정**
```bash
# 응답 시간 측정
time curl -X GET "http://localhost:8080/api/v1/shop/items" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 예상 결과: 200ms 이하
```

### **테스트 2: 동시 요청 처리**
```bash
# 동시 요청 테스트 (10개)
for i in {1..10}; do
  curl -X GET "http://localhost:8080/api/v1/shop/items" \
    -H "Authorization: Bearer {JWT_TOKEN}" &
done
wait

# 예상 결과: 모든 요청이 정상 처리됨
```

---

## 🔧 **테스트 도구 및 환경**

### **필요한 도구**
1. **Postman**: API 테스트
2. **curl**: 커맨드라인 테스트
3. **브라우저 개발자 도구**: 프론트엔드 연동 테스트
4. **데이터베이스 클라이언트**: 데이터 확인

### **테스트 환경**
1. **로컬 개발 환경**: H2 데이터베이스
2. **개발 서버**: Spring Boot 애플리케이션
3. **프론트엔드**: Vue.js 애플리케이션

---

## 📝 **테스트 체크리스트**

### **API 기능 테스트**
- [ ] 상점 아이템 목록 조회 (전체)
- [ ] 상점 아이템 목록 조회 (타입별)
- [ ] 꾸미기용 보유 아이템 조회
- [ ] 보유 여부 정확성 확인
- [ ] 에러 케이스 처리 확인

### **데이터베이스 테스트**
- [ ] 테이블 생성 확인
- [ ] 테스트 데이터 삽입 확인
- [ ] 외래키 제약조건 확인
- [ ] 인덱스 생성 확인

### **프론트엔드 연동 테스트**
- [ ] 상점 화면 정상 동작
- [ ] 꾸미기 화면 정상 동작
- [ ] 아이템 장착/해제 동작
- [ ] 에러 처리 및 사용자 피드백

### **성능 테스트**
- [ ] 응답 시간 측정
- [ ] 동시 요청 처리 확인
- [ ] 메모리 사용량 확인

---

## 🎯 **테스트 완료 기준**

### **성공 기준**
1. ✅ 모든 API 엔드포인트가 정상 동작
2. ✅ 데이터베이스 테이블이 정상 생성
3. ✅ 테스트 데이터가 정상 삽입
4. ✅ 프론트엔드와 정상 연동
5. ✅ 에러 케이스가 적절히 처리
6. ✅ 성능 요구사항 충족

### **실패 시 조치사항**
1. **로그 확인**: 애플리케이션 로그 및 데이터베이스 로그 확인
2. **코드 검토**: 구현된 코드 재검토
3. **설정 확인**: 데이터베이스 연결 및 애플리케이션 설정 확인
4. **단계별 테스트**: 각 기능별로 개별 테스트 수행

---

**작성일**: 2024년 12월  
**작성자**: 개발팀  
**버전**: 1.0
