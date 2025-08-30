#!/bin/bash

# Rate Limiting 테스트 스크립트
# 사용법: ./test-rate-limit.sh

BASE_URL="http://localhost:8080"
TEST_ENDPOINT="/api/v1/test/rate-limit/test"
STATUS_ENDPOINT="/api/v1/test/rate-limit/status/127.0.0.1"

echo "🚀 Rate Limiting 테스트 시작"
echo "=================================="

# 1. 기본 테스트
echo "1️⃣ 기본 테스트 (성공해야 함)"
response=$(curl -s -w "%{http_code}" -o /tmp/response1.json "$BASE_URL$TEST_ENDPOINT")
http_code="${response: -3}"
echo "HTTP Status: $http_code"
if [ "$http_code" = "200" ]; then
    echo "✅ 기본 테스트 성공"
else
    echo "❌ 기본 테스트 실패"
fi

# 2. Rate Limit 상태 확인
echo ""
echo "2️⃣ Rate Limit 상태 확인"
status_response=$(curl -s "$BASE_URL$STATUS_ENDPOINT")
echo "Status: $status_response"

# 3. 연속 요청 테스트 (Rate Limit 트리거)
echo ""
echo "3️⃣ 연속 요청 테스트 (Rate Limit 트리거)"
echo "분당 100회 제한을 테스트합니다..."

success_count=0
fail_count=0

for i in {1..110}; do
    response=$(curl -s -w "%{http_code}" -o /tmp/response_$i.json "$BASE_URL$TEST_ENDPOINT")
    http_code="${response: -3}"
    
    if [ "$http_code" = "200" ]; then
        success_count=$((success_count + 1))
        echo -n "."
    else
        fail_count=$((fail_count + 1))
        echo -n "X"
    fi
    
    # 100ms 간격으로 요청
    sleep 0.1
done

echo ""
echo ""
echo "📊 테스트 결과:"
echo "✅ 성공: $success_count"
echo "❌ 실패: $fail_count"

if [ $fail_count -gt 0 ]; then
    echo "🎯 Rate Limiting이 정상 작동하고 있습니다!"
else
    echo "⚠️  Rate Limiting이 작동하지 않을 수 있습니다."
fi

echo ""
echo "4️⃣ 최종 상태 확인"
final_status=$(curl -s "$BASE_URL$STATUS_ENDPOINT")
echo "Final Status: $final_status"

echo ""
echo "🏁 테스트 완료!"
