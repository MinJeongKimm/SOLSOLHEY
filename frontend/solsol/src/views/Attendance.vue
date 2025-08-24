<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 메인 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-4xl w-full p-8">
      <!-- 상단 헤더 -->
      <div class="flex justify-between items-start mb-6">
        <!-- 좌측: 출석체크 타이틀 -->
        <h1 class="text-xl font-bold text-gray-800">출석체크</h1>
        
        <!-- 우측: 포인트 & 경험치 -->
        <div class="flex flex-col space-y-1">
          <!-- 포인트 -->
          <div class="flex items-center justify-end">
            <img src="/icons/icon_point.png" alt="포인트" class="w-5 h-5 mr-2" />
            <span class="font-bold text-gray-900 min-w-[60px] text-center">{{ userCoins }}P</span>
          </div>
          <!-- 경험치 -->
          <div class="flex items-center justify-end">
            <span class="text-sm text-gray-500 mr-2">XP</span>
            <span class="font-bold text-gray-900 min-w-[60px] text-center">{{ userExp }}</span>
          </div>
        </div>
      </div>



      <!-- 상단: 연속 출석일 정보 -->
      <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-6 mb-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <div class="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
              <span class="text-white text-xl font-bold">🔥</span>
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-800">연속 출석</h2>
              <p class="text-2xl font-bold text-blue-600">{{ consecutiveDays }}일</p>
            </div>
          </div>
          
          <div class="text-right">
            <p class="text-sm text-gray-500">이번달 출석률</p>
            <p class="text-xl font-bold text-gray-800">{{ monthlyAttendanceRate }}%</p>
          </div>
        </div>
      </div>

      <!-- 중단: 달력과 출석일 표시 -->
      <div class="bg-white border border-gray-200 rounded-xl p-6">
        <!-- 달력 헤더 -->
        <div class="flex items-center justify-between mb-4">
          <button 
            @click="previousMonth"
            class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          
          <h3 class="text-lg font-semibold text-gray-800">
            {{ currentYear }}년 {{ currentMonth }}월
          </h3>
          
          <button 
            @click="nextMonth"
            class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>

        <!-- 요일 헤더 -->
        <div class="grid grid-cols-7 gap-1 mb-2">
          <div class="text-center text-sm font-medium text-red-500 py-2">일</div>
          <div class="text-center text-sm font-medium text-gray-600 py-2">월</div>
          <div class="text-center text-sm font-medium text-gray-600 py-2">화</div>
          <div class="text-center text-sm font-medium text-gray-600 py-2">수</div>
          <div class="text-center text-sm font-medium text-gray-600 py-2">목</div>
          <div class="text-center text-sm font-medium text-gray-600 py-2">금</div>
          <div class="text-center text-sm font-medium text-blue-500 py-2">토</div>
        </div>

        <!-- 달력 그리드 -->
        <div class="grid grid-cols-7 gap-0.5">
          <div 
            v-for="day in calendarDays" 
            :key="day.key"
            class="aspect-square p-1"
          >
            <div 
              v-if="day.isCurrentMonth"
              class="w-full h-full flex items-center justify-center relative"
            >
              <!-- 출석한 날: 파란색 동그라미 안에 흰색 숫자 -->
              <div v-if="day.isAttended" class="w-full aspect-square bg-blue-500 rounded-full flex items-center justify-center">
                <span class="text-sm font-medium text-white">{{ day.dayOfMonth }}</span>
              </div>
              
              <!-- 오늘: 출석 여부에 따라 다른 스타일 -->
              <div v-else-if="day.isToday" 
                   :class="todayAttended ? 'w-full aspect-square bg-blue-500 rounded-full flex items-center justify-center' : 'w-full aspect-square border-2 border-blue-500 rounded-full flex items-center justify-center'">
                <span :class="todayAttended ? 'text-sm font-medium text-white' : 'text-sm font-medium text-blue-500'">{{ day.dayOfMonth }}</span>
              </div>
              
              <!-- 출석 안한 날: 숫자만 표시 -->
              <span v-else class="text-sm font-medium text-gray-700">{{ day.dayOfMonth }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 하단: 출석체크 버튼 -->
      <div class="mt-6 text-center">
        <button 
          v-if="!todayAttended"
          @click="checkAttendance"
          class="bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white px-8 py-3 rounded-lg font-medium transition-all transform hover:scale-105 shadow-lg"
        >
          오늘 출석체크하기
        </button>
        <div 
          v-else
          class="bg-green-100 text-green-700 px-8 py-3 rounded-lg font-medium"
        >
          오늘 출석 완료! 🎉
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { auth } from '../api/index';

// 사용자 정보
const userCoins = ref(1000);
const userExp = ref(250);

// 출석 정보
const consecutiveDays = ref(5);
const monthlyAttendanceRate = ref(85);

// 달력 관련
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const todayAttended = ref(false);

// 달력 데이터 생성
const calendarDays = computed(() => {
  const days = [];
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1);
  const lastDay = new Date(currentYear.value, currentMonth.value, 0);
  const startDate = new Date(firstDay);
  startDate.setDate(startDate.getDate() - firstDay.getDay());
  
  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate);
    date.setDate(startDate.getDate() + i);
    
    const isCurrentMonth = date.getMonth() === currentMonth.value - 1;
    const isToday = isSameDay(date, new Date());
    const isAttended = !isToday && isAttendedDay(date);
    
    days.push({
      key: i,
      date,
      dayOfMonth: date.getDate(),
      isCurrentMonth,
      isToday,
      isAttended
    });
  }
  
  return days;
});

// 날짜 비교 함수
function isSameDay(date1: Date, date2: Date): boolean {
  return date1.getFullYear() === date2.getFullYear() &&
         date1.getMonth() === date2.getMonth() &&
         date1.getDate() === date2.getDate();
}

// 출석 여부 확인 (임시 로직)
function isAttendedDay(date: Date): boolean {
  // 실제로는 API에서 출석 데이터를 가져와야 함
  const today = new Date();
  
  // 오늘 날짜는 제외
  if (isSameDay(date, today)) return false;
  
  const diffTime = today.getTime() - date.getTime();
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
  
  // 임시로 최근 5일간 출석했다고 가정 (과거 날짜만)
  return diffDays >= 1 && diffDays <= 5;
}



// 이전 월
function previousMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
}

// 다음 월
function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
}

// 출석체크
async function checkAttendance() {
  try {
    // 실제 API 호출
    const response = await fetch('http://localhost:8080/api/attendance', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        attendanceDate: new Date().toISOString().split('T')[0]
      })
    });

    if (!response.ok) {
      throw new Error('출석체크 API 호출 실패');
    }

    const result = await response.json();
    
    // API 호출 성공 시에만 상태 변경
    todayAttended.value = true;
    consecutiveDays.value++;
    monthlyAttendanceRate.value = Math.min(100, monthlyAttendanceRate.value + 3);
    
    // 포인트와 경험치 증가
    userCoins.value += 10;
    userExp.value += 5;
    
    alert('출석체크 완료! +10P, +5XP 획득!');
  } catch (error) {
    console.error('출석체크 오류:', error);
    alert('출석체크 중 오류가 발생했습니다.');
  }
}

// 컴포넌트 마운트 시 오늘 출석 여부 확인
onMounted(async () => {
  try {
    // 오늘 출석 상태 확인 API 호출
    const response = await fetch('http://localhost:8080/api/attendance/today', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (response.ok) {
      const result = await response.json();
      todayAttended.value = result.attended || false;
    } else {
      todayAttended.value = false;
    }
  } catch (error) {
    console.error('오늘 출석 상태 확인 오류:', error);
    todayAttended.value = false;
  }
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>
