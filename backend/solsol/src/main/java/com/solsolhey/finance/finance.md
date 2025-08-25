
# SSAFY 금융망 API 요약 정리 (예쁘게 포맷)
외부 금융 API를 호출하지만, 요청·응답 모두 최소 필드만 사용해서 빠르게 동작 확인하려고 한다

> **출처**: SSAFY 교육용 금융망 API 개발가이드  
> 본 문서는 아래 4개 API를 실사용하기 쉬운 형태로 재정리한 것입니다.

---

1) 환율전체 조회

⸻

SSAFY 교육용금융망 API 개발가이드

⸻

요청메시지
	•	URL:

https://finopenapi.ssafy.io/ssafy/api/v1/edu/exchangeRate


	•	HTTP Method: POST

● 요청메시지명세

변수명	설명	TYPE	길이	필수	비고
Header	공통			Y	userKey 제외


⸻

● 요청메시지형태 (JSON)

{
  "Header": {
    "apiName": "exchangeRate",
    "transmissionDate": "20240724",
    "transmissionTime": "101641",
    "institutionCode": "00100",
    "fintechAppNo": "001",
    "apiServiceCode": "exchangeRate",
    "institutionTransactionUniqueNo": "20240724101641312362",
    "apiKey": "a028e66ddbf42a6b783d78963163e29"  // -> 이건  /Users/nayukyung/solsolhey3/SOLSOLHEY/backend/.env 의FINANCE_API_KEY 변수값을 넣을꺼임 
  }
}

주의할 점 
	transmissionDate: API 호출 시점의 날짜
	transmissionTime: API 호출 시점의  시각 앞 뒤 ±2분 허용
	institutionTransactionUniqueNo: 20자는 반드시 지키면서 값은 매번 달라야 함(타임스탬프+랜덤 조합 추천)



⸻

응답메시지

● 응답메시지명세

변수명	설명	TYPE	길이	필수	비고
Header	공통			Y	
REC	List			Y	
id	환율고유번호	Long		Y	
currency	통화코드	String	8	Y	(USD, EUR, …)
exchangeRate	환율	double		Y	
exchangeMin	최소환전금액	double		Y	
created	생성일	String	10	Y	


⸻

● 응답메시지형태 (JSON)

{
  "Header": {
    "responseCode": "H0000",
    "responseMessage": "정상처리되었습니다.",
    "apiName": "exchangeRate",
    "transmissionDate": "20240724",
    "transmissionTime": "101641",
    "institutionCode": "00100",
    "apiKey": "e8fb2ac291804bc98834ff7bcef7e340",// -> 이건  /Users/nayukyung/solsolhey3/SOLSOLHEY/backend/.env 의FINANCE_API_KEY 변수값을 넣을꺼임 
    "apiServiceCode": "exchangeRate",
    "institutionTransactionUniqueNo": "20240724101641312362"
  },
  "REC": [
    {
      "id": 2127,
      "currency": "CAD",
      "exchangeRate": "1,003.08",
      "exchangeMin": "140",
      "created": "2024-07-25 23:55:04"
    },
    {
      "id": 2128,
      "currency": "CHF",
      "exchangeRate": "1,565.35",
      "exchangeMin": "100",
      "created": "2024-07-25 23:55:04"
    },
    {
      "id": 2129,
      "currency": "CNY",
      "exchangeRate": "190.06",
      "exchangeMin": "800",
      "created": "2024-07-25 23:55:04"
    },
    {
      "id": 2130,
      "currency": "EUR",
      "exchangeRate": "1,501.45",
      "exchangeMin": "100",
      "created": "2024-07-25 23:55:04"
    },
    {
      "id": 2131,
      "currency": "GBP",
      "exchangeRate": "1,786.99",
      "exchangeMin": "80",
      "created": "2024-07-25 23:55:04"
    },
    {
      "id": 2132,
      "currency": "JPY",
      "exchangeRate": "901.32",
      "exchangeMin": "100",
      "created": "2024-07-25 23:55:04"
    },
    {
      "id": 2134,
      "currency": "USD",
      "exchangeRate": "1,385.1",
      "exchangeMin": "100",
      "created": "2024-07-25 23:55:04"
    }
  ]
}


⸻

● 에러코드목록

코드	설명	비고
H1000	HEADER 정보가유효하지않습니다.	
H1001	API이름이유효하지않습니다.	
H1002	전송일자형식이유효하지않습니다.	
H1003	전송시각형식이유효하지않습니다.	
H1004	기관코드가유효하지않습니다.	
H1005	핀테크앱일련번호가유효하지않습니다.	
H1006	API서비스코드가유효하지않습니다.	
H1007	기관거래고유번호가중복된값입니다.	
H1008	API_KEY가유효하지않습니다.	
H1010	기관거래고유번호가유효하지않습니다.	
Q1000	그이외에에러메시지	
Q1001	요청본문의형식이잘못되었습니다. JSON 형식또는데이터타입을확인해주세요.	


⸻

## 2) 환율 단건 조회요청메시지
URL
HTTP URL
https://finopenapi.ssafy.io/ssafy/api/v1/edu/exchangeRate
HTTP Method
POST
●요청메시지명세
변수명
설명
TYPE
길이
필수
비고
Header
공통
Y
userKey 제외
●요청메시지형태
JSON
{"Header": {"apiName": "exchangeRate","transmissionDate": "20240724","transmissionTime": "101641","institutionCode": "00100","fintechAppNo": "001","apiServiceCode": "exchangeRate","institutionTransactionUniqueNo": "20240724101641312362","apiKey": "e8fb2ac291804bc98834ff7bcef7e340"}}// 

-> apiKey는  /Users/nayukyung/solsolhey3/SOLSOLHEY/backend/.env 의FINANCE_API_KEY 변수값을 넣을꺼임 


주의할 점 
	transmissionDate: API 호출 시점의 날짜
	transmissionTime: API 호출 시점의  시각 앞 뒤 ±2분 허용
	institutionTransactionUniqueNo: 20자는 반드시 지키면서 값은 매번 달라야 함(타임스탬프+랜덤 조합 추천)

●응답메시지명세
변수명
설명
TYPE
길이
필수
비고
Header
공통
Y
REC
List
Y
id
환율고유번호
Long
Y
currency
통화코드
String
8
Y
exchangeRate
환율
double
Y
exchangeMin
최소환전금액
double
Y
created
생성일
String
10
Y
●응답메시지형태
JSON
{"Header": {"responseCode": "H0000","responseMessage": "정상처리되었습니다.","apiName": "exchangeRate","transmissionDate": "20240724","transmissionTime": "101641","institutionCode": "00100","apiKey": "e8fb2ac291804bc98834ff7bcef7e340","apiServiceCode": "exchangeRate","institutionTransactionUniqueNo": "20240724101641312362"},"REC": [{"id": 2127,"currency": "CAD","exchangeRate": "1,003.08","exchangeMin": "140","created": "2024-07-25 23:55:04"
SSAFY 교육용금융망API 개발가이드214},{"id": 2128,"currency": "CHF","exchangeRate": "1,565.35","exchangeMin": "100","created": "2024-07-25 23:55:04"},{"id": 2129,"currency": "CNY","exchangeRate": "190.06","exchangeMin": "800","created": "2024-07-25 23:55:04"},{"id": 2130,"currency": "EUR","exchangeRate": "1,501.45","exchangeMin": "100","created": "2024-07-25 23:55:04"},{"id": 2131,"currency": "GBP","exchangeRate": "1,786.99","exchangeMin": "80","created": "2024-07-25 23:55:04"},{"id": 2132,"currency": "JPY","exchangeRate": "901.32","exchangeMin": "100","created": "2024-07-25 23:55:04"
SSAFY 교육용금융망API 개발가이드215},{"id": 2134,"currency": "USD","exchangeRate": "1,385.1","exchangeMin": "100","created": "2024-07-25 23:55:04"}]}
●에러코드목록
코드
설명
비고
H1000
HEADER 정보가유효하지않습니다.
H1001
API이름이유효하지않습니다.
H1002
전송일자형식이유효하지않습니다.
H1003
전송시각형식이유효하지않습니다.
H1004
기관코드가유효하지않습니다.
H1005
핀테크앱일련번호가유효하지않습니다.
H1006
API서비스코드가유효하지않습니다.
H1007
기관거래고유번호가중복된값입니다.
H1008
API_KEY가유효하지않습니다.
H1010
기관거래고유번호가유효하지않습니다.
Q1000
그이외에에러메시지
Q1001
요청본문의형식이잘못되었습니다. JSON 형식또는데이터타입을확인해주세요.


## 3 환전예상금액조회

⸻

●요청메시지
URL
HTTP URL
https://finopenapi.ssafy.io/ssafy/api/v1/edu/exchange/estimate
HTTP Method
POST

●요청메시지명세

변수명	설명	TYPE	길이	필수	비고
Header	공통			Y	userKey 제외
currency	소유통화코드	String		Y	소유한통화코드(달러>엔화환전의경우 USD 입력)
exchangeCurrency	환전통화코드	String	8	Y	환전할통화의코드입력(달러>엔화환전의경우 JPY 입력)
amount	환전금액	Double		Y	환전하고싶은금액(달러>엔화환전의경우 JPY의 30000 입력) 환전금액단위는 10 입니다.

●요청메시지형태
JSON

{"Header": {"apiName": "estimate","transmissionDate": "20240724","transmissionTime": "102710","institutionCode": "00100",
"fintechAppNo": "001","apiServiceCode": "estimate","institutionTransactionUniqueNo": "20240724102710632310","apiKey": "e8fb2ac291804bc98834ff7bcef7e340"},"currency": "USD","exchangeCurrency": "JPY","amount":"30000"}

-> apiKey는  /Users/nayukyung/solsolhey3/SOLSOLHEY/backend/.env 의FINANCE_API_KEY 변수값을 넣을꺼임 

주의할 점 
	transmissionDate: API 호출 시점의 날짜
	transmissionTime: API 호출 시점의  시각 앞 뒤 ±2분 허용
	institutionTransactionUniqueNo: 20자는 반드시 지키면서 값은 매번 달라야 함(타임스탬프+랜덤 조합 추천)


⸻

●응답메시지명세

변수명	설명	TYPE	길이	필수	비고
Header	공통			Y	
REC	List			Y	
currency	List			Y	
amount	환전금액	double	10	Y	
currency	통화코드	String	16	Y	
currencyName	통화명	String	10	Y	
exchangeCurrency	List			Y	
amount	환전예상금액	double		Y	외화간환전시 원화 기준으로 환전 (소유한통화 → 원화 → 환전할통화)
currency	통화코드	String	16	Y	
currencyName	통화명	String	10	Y	

●응답메시지형태
JSON

{"Header": {"responseCode": "H0000","responseMessage": "정상처리되었습니다.","apiName": "estimate",
"transmissionDate": "20240724","transmissionTime": "102710","institutionCode": "00100","apiKey": "e8fb2ac291804bc98834ff7bcef7e340","apiServiceCode": "estimate","institutionTransactionUniqueNo": "20240724102710632310"},"REC": {"currency": {"amount": "1,000,000","currency": "USD","currencyName": "달러",},"exchangeCurrency": {"amount": "155,725,448.31","currency": "JPY","currencyName": "엔화"}}}


⸻

●에러코드목록

코드	설명	비고
H1000	HEADER 정보가 유효하지않습니다.	
H1001	API 이름이 유효하지않습니다.	
H1002	전송일자 형식이 유효하지않습니다.	
H1003	전송시각 형식이 유효하지않습니다.	
H1004	기관코드가 유효하지않습니다.	
H1005	핀테크앱 일련번호가 유효하지않습니다.	
H1006	API 서비스코드가 유효하지않습니다.	
H1007	기관거래고유번호가 중복된 값입니다.	
H1008	API_KEY가 유효하지않습니다.	
H1010	기관거래고유번호가 유효하지않습니다.	
A1003	계좌번호가 유효하지않습니다.	
A5001	통화코드가 유효하지않습니다.	
A5002	환전금액을 입력해주세요.	
Q1000	그 이외에 에러메시지	
Q1001	요청 본문의 형식이 잘못되었습니다.  JSON 형식 또는 데이터타입을 확인해주세요.	


## 4) 계좌거래내역목록조회


⸻

●요청메시지
URL
HTTP URL
https://finopenapi.ssafy.io/ssafy/api/v1/edu/demandDeposit/inquireTransactionHistoryList
HTTP Method
POST

●요청메시지명세

변수명	설명	TYPE	길이	필수	비고
Header	공통			Y	
accountNo	계좌번호	String	16	Y	
startDate	조회시작일자	String	8	Y	YYYYMMDD
endDate	조회종료일자	String	8	Y	YYYYMMDD
transactionType	거래구분	String	1	Y	M:입금, D:출금, A:전체
orderByType	정렬순서	String	4	N	거래고유번호 기준 ASC:오름차순(이전거래), DESC:내림차순(최근거래)

●요청메시지형태
JSON

{"Header": {"apiName": "inquireTransactionHistoryList","transmissionDate": "20240401","transmissionTime": "105000",
"institutionCode": "00100","fintechAppNo": "001","apiServiceCode": "inquireTransactionHistoryList","institutionTransactionUniqueNo": "20240215121212123459","apiKey": "6a028e66ddbf42a6b783d78963163e29","userKey": "2695628f-11a1-418e-b533-9ae19e0650ec"},"accountNo": "0016174648358792","startDate": "20240101","endDate": "20241231","transactionType": "A","orderByType": "ASC"}

-> apiKey는  /Users/nayukyung/solsolhey3/SOLSOLHEY/backend/.env 의FINANCE_API_KEY 변수값을 넣을꺼임 
주의할 점 
	transmissionDate: API 호출 시점의 날짜
	transmissionTime: API 호출 시점의  시각 앞 뒤 ±2분 허용
	institutionTransactionUniqueNo: 20자는 반드시 지키면서 값은 매번 달라야 함(타임스탬프+랜덤 조합 추천)

⸻

●응답메시지명세

변수명	설명	TYPE	길이	필수	비고
Header	공통			Y	
REC	거래내역	List		N	
totalCount	조회총건수	String		N	
list	거래목록	List		N	
transactionUniqueNo	거래고유번호	Long		Y	
transactionDate	거래일자	String	8	Y	
transactionTime	거래시각	String	6	Y	
transactionType	입금출금구분	String	1	Y	1,2
transactionTypeName	입금출금구분명	String	10	Y	입금, 출금, 입금(이체), 출금(이체)
transactionAccountNo	거래계좌번호	String	16	N	
transactionBalance	거래금액	Long		Y	
transactionAfterBalance	거래후잔액	Long		Y	
transactionSummary	거래요약내용	String	255	N	
transactionMemo	거래메모	String	255	N	

●응답메시지형태
JSON

{"Header": {"responseCode": "H0000","responseMessage": "정상처리되었습니다.","apiName": "inquireTransactionHistoryList","transmissionDate": "20240401","transmissionTime": "105000","institutionCode": "00100","apiKey": "6a028e66ddbf42a6b783d78963163e29","apiServiceCode": "inquireTransactionHistoryList","institutionTransactionUniqueNo": "20240215121212123459"},"REC": {"totalCount": "3","list": [{"transactionUniqueNo": "59","transactionDate": "20240401","transactionTime": "102447","transactionType": "1","transactionTypeName": "입금","transactionAccountNo": "","transactionBalance": "100000000","transactionAfterBalance": "100000000","transactionSummary": "(수시입출금) : 입금","transactionMemo": ""},{
"transactionUniqueNo": "60","transactionDate": "20240401","transactionTime": "102452","transactionType": "2","transactionTypeName": "출금","transactionAccountNo": "","transactionBalance": "100000","transactionAfterBalance": "99900000","transactionSummary": "(수시입출금) : 출금","transactionMemo": ""},{"transactionUniqueNo": "61","transactionDate": "20240401","transactionTime": "103229","transactionType": "2","transactionTypeName": "출금(이체)","transactionAccountNo": "0204667768182760","transactionBalance": "10000000","transactionAfterBalance": "89900000","transactionSummary": "(수시입출금) : 출금(이체)","transactionMemo": ""}]}}


⸻

●에러코드목록

코드	설명	비고
H1000	HEADER 정보가유효하지않습니다.	
H1001	API 이름이유효하지않습니다.	
H1002	전송일자형식이유효하지않습니다.	
H1003	전송시각형식이유효하지않습니다.	
H1004	기관코드가유효하지않습니다.	
H1005	핀테크앱 일련번호가유효하지않습니다.	
H1006	API 서비스코드가유효하지않습니다.	
H1010	기관거래고유번호가유효하지않습니다.	
H1007	기관거래고유번호가중복된값입니다.	
H1008	API_KEY가유효하지않습니다.	
H1009	USER_KEY가유효하지않습니다.	
A1003	계좌번호가유효하지않습니다.	
A1004	조회시작일자가유효하지않습니다.	
A1005	조회종료일자가유효하지않습니다.	
A1006	거래구분이유효하지않습니다.	
A1007	정렬순서가유효하지않습니다.	
A5004	원화계좌만가능합니다.	
Q1000	그이외에에러메시지	
Q1001	요청본문의형식이잘못되었습니다. JSON 형식또는 데이터타입을확인해주세요	


