
# SSAFY 금융망 API 요약 정리 (예쁘게 포맷)
외부 금융 API를 호출하지만, 요청·응답 모두 최소 필드만 사용해서 빠르게 동작 확인하려고 한다

> **출처**: SSAFY 교육용 금융망 API 개발가이드  
> 본 문서는 아래 4개 API를 실사용하기 쉬운 형태로 재정리한 것입니다.

---

## 1) 환율 **전체** 조회

- **HTTP URL**: `https://finopenapi.ssafy.io/ssafy/api/v1/edu/exchangeRate`
- **Method**: `POST`
- **설명**: 지원 7개 통화(USD, EUR, JPY, CNY, GBP, CHF, CAD)의 실시간 환율을 조회합니다.  
  *(환율은 10분마다 갱신될 수 있습니다.)*

### 요청 (Request)

**Header (공통, userKey 제외)**

| 필드 | 타입 | 필수 | 비고 |
|---|---|---|---|
| apiName | String | Y | `exchangeRate` |
| transmissionDate | String | Y | `YYYYMMDD` |
| transmissionTime | String | Y | `HHmmss` |
| institutionCode | String | Y | 예: `00100` |
| fintechAppNo | String | Y | 예: `001` |
| apiServiceCode | String | Y | `exchangeRate` |
| institutionTransactionUniqueNo | String | Y | 트랜잭션 고유번호 |
| apiKey | String | Y | 발급받은 API KEY |

**요청 예시**

```json
{
  "Header": {
    "apiName": "exchangeRate",
    "transmissionDate": "20240724",
    "transmissionTime": "101641",
    "institutionCode": "00100",
    "fintechAppNo": "001",
    "apiServiceCode": "exchangeRate",
    "institutionTransactionUniqueNo": "20240724101641312362",
    "apiKey": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
  }
}
```

### 응답 (Response)

**Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| Header | Object | Y | 공통 헤더 |
| REC | Array | Y | 환율 리스트 |

**REC 항목**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | Long | Y | 환율 고유번호 |
| currency | String(8) | Y | 통화코드 (USD, EUR, …) |
| exchangeRate | double | Y | 환율 |
| exchangeMin | double | Y | 최소 환전 금액 |
| created | String(10) | Y | 생성일 |

**응답 예시(일부)**

```json
{
  "Header": { "...": "..." },
  "REC": [
    { "id": 2134, "currency": "USD", "exchangeRate": "1,385.1", "exchangeMin": "100", "created": "2024-07-25 23:55:04" },
    { "id": 2131, "currency": "GBP", "exchangeRate": "1,786.99", "exchangeMin": "80", "created": "2024-07-25 23:55:04" }
  ]
}
```

**에러 코드**

| 코드 | 설명 |
|---|---|
| H1000 | HEADER 정보가 유효하지 않습니다. |
| H1001 | API 이름이 유효하지 않습니다. |
| H1002 | 전송일자 형식이 유효하지 않습니다. |
| H1003 | 전송시각 형식이 유효하지 않습니다. |
| H1004 | 기관코드가 유효하지 않습니다. |
| H1005 | 핀테크앱 일련번호가 유효하지 않습니다. |
| H1006 | API 서비스코드가 유효하지 않습니다. |
| H1007 | 기관거래고유번호가 중복된 값입니다. |
| H1008 | API_KEY가 유효하지 않습니다. |
| H1010 | 기관거래고유번호가 유효하지 않습니다. |
| Q1000 | 그 외 에러 메시지 |
| Q1001 | 요청 본문 형식 오류 (JSON/타입 확인) |

---

## 2) 환율 **단건** 조회

- **HTTP URL**: `https://finopenapi.ssafy.io/ssafy/api/v1/edu/exchangeRate/exchangeRateSearch`
- **Method**: `POST`
- **설명**: 입력한 통화의 실시간 환율을 조회합니다. *(10분 주기 갱신)*

### 요청 (Request)

**Header (공통, userKey 제외)**

| 필드 | 타입 | 필수 | 비고 |
|---|---|---|---|
| apiName | String | Y | `exchangeRateSearch` |
| transmissionDate | String | Y | `YYYYMMDD` |
| transmissionTime | String | Y | `HHmmss` |
| institutionCode | String | Y | `00100` |
| fintechAppNo | String | Y | `001` |
| apiServiceCode | String | Y | `exchangeRateSearch` |
| institutionTransactionUniqueNo | String | Y | 고유번호 |
| apiKey | String | Y | API KEY |

**Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| currency | String(8) | Y | 통화코드 (예: `USD`) |

**요청 예시**

```json
{
  "Header": {
    "apiName": "exchangeRateSearch",
    "transmissionDate": "20240724",
    "transmissionTime": "101020",
    "institutionCode": "00100",
    "fintechAppNo": "001",
    "apiServiceCode": "exchangeRateSearch",
    "institutionTransactionUniqueNo": "20240724101020562463",
    "apiKey": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
  },
  "currency": "USD"
}
```

### 응답 (Response)

**REC 항목**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| id | Long | Y | 환율 고유번호 |
| currency | String(8) | Y | 통화코드 |
| exchangeRate | double(16) | Y | 환율 |
| exchangeMin | double | Y | 최소 환전 금액 |
| created | String(10) | Y | 생성일 |

**응답 예시**

```json
{
  "Header": { "...": "..." },
  "REC": {
    "id": 153,
    "currency": "USD",
    "exchangeRate": "1,388.6",
    "exchangeMin": "100",
    "created": "2024-07-23T15:20:55.972351+09:00"
  }
}
```

**에러 코드**  
(공통 + 아래 추가)

| 코드 | 설명 |
|---|---|
| A5001 | 통화코드가 유효하지 않습니다. |

---

## 3) 환전 **예상 금액** 조회

- **HTTP URL**: `https://finopenapi.ssafy.io/ssafy/api/v1/edu/exchange/estimate`
- **Method**: `POST`
- **설명**: 통화코드 조회 후 사용. 최소환전금액/단위 제한 없음.

### 요청 (Request)

**Header (공통, userKey 제외)**

| 필드 | 타입 | 필수 | 비고 |
|---|---|---|---|
| apiName | String | Y | `estimate` |
| transmissionDate | String | Y | `YYYYMMDD` |
| transmissionTime | String | Y | `HHmmss` |
| institutionCode | String | Y | `00100` |
| fintechAppNo | String | Y | `001` |
| apiServiceCode | String | Y | `estimate` |
| institutionTransactionUniqueNo | String | Y | 고유번호 |
| apiKey | String | Y | API KEY |

**Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| currency | String | Y | 소유 통화코드 (예: USD) |
| exchangeCurrency | String(8) | Y | 환전 대상 통화코드 (예: JPY) |
| amount | Double | Y | 환전 금액 *(단위=10)* |

**요청 예시**

```json
{
  "Header": {
    "apiName": "estimate",
    "transmissionDate": "20240724",
    "transmissionTime": "102710",
    "institutionCode": "00100",
    "fintechAppNo": "001",
    "apiServiceCode": "estimate",
    "institutionTransactionUniqueNo": "20240724102710632310",
    "apiKey": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
  },
  "currency": "USD",
  "exchangeCurrency": "JPY",
  "amount": "30000"
}
```

### 응답 (Response)

**REC 항목**

| 필드 | 타입 | 설명 |
|---|---|---|
| currency.amount | double | 환전 금액 |
| currency.currency | String | 통화코드 |
| currency.currencyName | String | 통화명 |
| exchangeCurrency.amount | double | 환전 **예상** 금액 *(원화 기준 환산 후 대상 통화)* |
| exchangeCurrency.currency | String | 통화코드 |
| exchangeCurrency.currencyName | String | 통화명 |

**응답 예시**

```json
{
  "Header": { "...": "..." },
  "REC": {
    "currency": { "amount": "1,000,000", "currency": "USD", "currencyName": "달러" },
    "exchangeCurrency": { "amount": "155,725,448.31", "currency": "JPY", "currencyName": "엔화" }
  }
}
```

**에러 코드**  
(공통 + 아래 추가)

| 코드 | 설명 |
|---|---|
| A1003 | 계좌번호가 유효하지 않습니다. |
| A5001 | 통화코드가 유효하지 않습니다. |
| A5002 | 환전금액을 입력해주세요. |

---

## 4) **계좌거래내역** 목록 조회

- **HTTP URL**: `https://finopenapi.ssafy.io/ssafy/api/v1/edu/demandDeposit/inquireTransactionHistoryList`
- **Method**: `POST`
- **설명**: 계좌거래내역 목록 조회 (원화계좌만 가능)

### 요청 (Request)

**Header (공통)**

> *이 API는 `userKey` 포함*

| 필드 | 타입 | 필수 | 비고 |
|---|---|---|---|
| apiName | String | Y | `inquireTransactionHistoryList` |
| transmissionDate | String | Y | `YYYYMMDD` |
| transmissionTime | String | Y | `HHmmss` |
| institutionCode | String | Y | `00100` |
| fintechAppNo | String | Y | `001` |
| apiServiceCode | String | Y | `inquireTransactionHistoryList` |
| institutionTransactionUniqueNo | String | Y | 고유번호 |
| apiKey | String | Y | API KEY |
| userKey | String | Y | 사용자 키 |

**Body**

| 필드 | 타입 | 길이 | 필수 | 설명 |
|---|---|---|---|---|
| accountNo | String | 16 | Y | 계좌번호 |
| startDate | String | 8 | Y | 조회 시작일자 `YYYYMMDD` |
| endDate | String | 8 | Y | 조회 종료일자 `YYYYMMDD` |
| transactionType | String | 1 | Y | `M`(입금), `D`(출금), `A`(전체) |
| orderByType | String | 4 | N | `ASC`(오름/이전), `DESC`(내림/최근) |

**요청 예시**

```json
{
  "Header": {
    "apiName": "inquireTransactionHistoryList",
    "transmissionDate": "20240401",
    "transmissionTime": "105000",
    "institutionCode": "00100",
    "fintechAppNo": "001",
    "apiServiceCode": "inquireTransactionHistoryList",
    "institutionTransactionUniqueNo": "20240215121212123459",
    "apiKey": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "userKey": "2695628f-11a1-418e-b533-9ae19e0650ec"
  },
  "accountNo": "0016174648358792",
  "startDate": "20240101",
  "endDate": "20241231",
  "transactionType": "A",
  "orderByType": "ASC"
}
```

### 응답 (Response)

**Body**

| 필드 | 타입 | 설명 |
|---|---|---|
| Header | Object | 공통 헤더 |
| REC | Object | 결과 |

**REC**

| 필드 | 타입 | 설명 |
|---|---|---|
| totalCount | String | 조회 총 건수 |
| list | Array | 거래 목록 |

**list 항목**

| 필드 | 타입 | 설명 |
|---|---|---|
| transactionUniqueNo | Long | 거래 고유번호 |
| transactionDate | String(8) | 거래일자 |
| transactionTime | String(6) | 거래시각 |
| transactionType | String(1) | `1`/`2` (입금/출금) |
| transactionTypeName | String(10) | 입금, 출금, 입금(이체), 출금(이체) |
| transactionAccountNo | String(16) | 거래계좌번호 |
| transactionBalance | Long | 거래금액 |
| transactionAfterBalance | Long | 거래 후 잔액 |
| transactionSummary | String(255) | 거래요약 |
| transactionMemo | String(255) | 거래메모 |

**응답 예시(일부)**

```json
{
  "Header": { "...": "..." },
  "REC": {
    "totalCount": "3",
    "list": [
      {
        "transactionUniqueNo": "61",
        "transactionDate": "20240401",
        "transactionTime": "103229",
        "transactionType": "2",
        "transactionTypeName": "출금(이체)",
        "transactionAccountNo": "0204667768182760",
        "transactionBalance": "10000000",
        "transactionAfterBalance": "89900000",
        "transactionSummary": "(수시입출금) : 출금(이체)",
        "transactionMemo": ""
      }
    ]
  }
}
```

**에러 코드**  
(공통 + 아래 추가)

| 코드 | 설명 |
|---|---|
| H1009 | USER_KEY가 유효하지 않습니다. |
| A1003 | 계좌번호가 유효하지 않습니다. |
| A1004 | 조회시작일자가 유효하지 않습니다. |
| A1005 | 조회종료일자가 유효하지 않습니다. |
| A1006 | 거래구분이 유효하지 않습니다. |
| A1007 | 정렬순서가 유효하지 않습니다. |
| A5004 | 원화계좌만 가능합니다. |

---

### 참고
- 모든 API는 **공통 Header** 규격을 따르며, 필요 시 `userKey` 포함 여부만 달라집니다.
- `institutionTransactionUniqueNo`는 매 호출마다 **고유** 값으로 생성하세요.
