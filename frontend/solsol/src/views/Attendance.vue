<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 메인 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-4xl w-full p-8">
      <!-- 상단 헤더 -->
      <div class="relative mb-6">
        <!-- 뒤로가기 버튼 (절대 위치) -->
        <button 
          @click="goBack"
          class="absolute left-0 top-1/2 transform -translate-y-1/2 p-2 rounded-lg hover:bg-gray-100 transition-colors z-10"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        
        <!-- 중앙: 출석체크 타이틀 (전체 화면 중앙) -->
        <h1 class="text-xl font-bold text-gray-800 text-center w-full">출석체크</h1>
      </div>



      <!-- 상단: 연속 출석일 정보 -->
      <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-6 mb-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <div class="relative">
              <!-- 말풍선 (출석체크 안했을 때만 표시) -->
              <div v-if="!todayAttended" class="absolute -top-12 left-1/2 transform -translate-x-1/2 bg-white border-2 border-purple-200 rounded-lg px-3 py-2 shadow-lg animate-float whitespace-nowrap">
                <div class="text-center text-sm text-purple-700 font-medium">
                  오늘도 출석해요! ✨
                </div>
                <!-- 말풍선 꼬리 -->
                <div class="absolute top-full left-1/2 transform -translate-x-1/2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-white"></div>
              </div>
              
              <div :class="todayAttended ? 'w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center' : 'w-12 h-12 bg-purple-300 rounded-full flex items-center justify-center'">
                <span class="text-white text-xl font-bold">{{ todayAttended ? '🔥' : '💤' }}</span>
              </div>
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-800">연속 출석</h2>
              <p :class="todayAttended ? 'text-2xl font-bold text-blue-600' : 'text-2xl font-bold text-purple-500'">{{ consecutiveDays }}일</p>
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
import { useRouter } from 'vue-router';
import { apiRequest } from '../api/index';
import { useToastStore } from '../stores/toast';
import { auth } from '../api/index';

const router = useRouter();

// 뒤로가기 함수
function goBack() {
  router.push('/mascot');
}

// 사용자 정보
const userCoins = ref(1000);
const userExp = ref(250);

// 출석 정보
const attendanceRecords = ref<Set<string>>(new Set()); // 출석 기록 (YYYY-MM-DD 형식)

// 달력 관련
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const todayAttended = ref(false);

// 출석률 계산 (computed로 실시간 계산)
const monthlyAttendanceRate = computed(() => {
  const currentMonthDays = calendarDays.value.filter(day => day.isCurrentMonth);
  const attendedDays = currentMonthDays.filter(day => day.isAttended);
  
  if (currentMonthDays.length === 0) return 0;
  
  return Math.round((attendedDays.length / currentMonthDays.length) * 100);
});

// 연속 출석 일수 계산 (computed로 실시간 계산)
const consecutiveDays = computed(() => {
  let count = 0;
  const today = new Date();
  
  // 오늘 출석 여부에 따라 시작점 결정
  let startOffset = todayAttended.value ? 0 : 1;
  
  // 시작점부터 역순으로 연속 출석 확인
  for (let i = startOffset; i < 365; i++) { // 최대 1년치 확인
    const checkDate = new Date(today);
    checkDate.setDate(today.getDate() - i);
    
    // 해당 날짜가 출석했는지 확인
    const isAttended = isAttendedDay(checkDate);
    
    if (isAttended) {
      count++;
    } else {
      break; // 출석하지 않은 날을 만나면 중단
    }
  }
  
  return count;
});

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
    // 오늘 날짜도 출석 여부를 확인하도록 수정
    const isAttended = isAttendedDay(date);
    
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

// 로컬 시간 기준으로 YYYY-MM-DD 형식의 날짜 문자열 생성
function getLocalDateString(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// 출석 여부 확인 (실제 출석 기록에서 확인)
function isAttendedDay(date: Date): boolean {
  const dateString = getLocalDateString(date);
  const today = new Date();
  const todayString = getLocalDateString(today);
  
  // 오늘 날짜이고 오늘 출석했다면 true 반환
  if (dateString === todayString && todayAttended.value) {
    return true;
  }
  
  // 과거 날짜는 출석 기록에서 확인
  return attendanceRecords.value.has(dateString);
}

// 출석 기록 조회
async function fetchAttendanceRecords() {
  try {
    // 새로운 출석 기록 API 호출 - 중복 경로 수정
    const response = await apiRequest<any>('/attendance/records', {
      method: 'GET'
    });
    
    if (response && response.success && response.data) {
      // 응답 데이터를 Set으로 변환하여 저장
      const records = response.data;
      records.forEach((record: any) => {
        // 배열 형태의 날짜를 YYYY-MM-DD 형식의 문자열로 변환
        const [year, month, day] = record.attendanceDate;
        const dateString = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        attendanceRecords.value.add(dateString);
      });
    } else {
      attendanceRecords.value = new Set();
    }
  } catch (error) {
    console.error('출석 기록 조회 오류:', error);
    attendanceRecords.value = new Set();
  }
}


// 이전 월
async function previousMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
  
  // 월 변경 시 해당 월의 출석 기록 조회
  await fetchAttendanceRecords();
}

// 다음 월
async function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
  
  // 월 변경 시 해당 월의 출석 기록 조회
  await fetchAttendanceRecords();
}

// 출석체크
async function checkAttendance() {
  try {
    // 실제 API 호출 (쿠키+CSRF는 apiRequest가 처리)
    const result = await apiRequest<any>('/attendance', { method: 'POST' });
    
    // 백엔드 응답 구조에 맞게 처리
    if (result && result.success && result.data) {
      // API 호출 성공 시에만 상태 변경
      todayAttended.value = true;
      
      // 백엔드에서 받은 보상 정보 사용
      const { consecutiveDays: newConsecutiveDays, pointReward, expAwarded } = result.data;
      
      // 포인트 증가 제거(출석은 포인트 미지급 정책)
      userCoins.value += 0;
      
      // 출석 기록에 오늘 날짜 추가 (로컬 시간 기준)
      const today = new Date();
      const todayString = getLocalDateString(today);
      attendanceRecords.value.add(todayString);
      
      // EXP 토스트는 apiRequest 인터셉터에서 자동 표출됨. 별도 성공 알림은 생략해 중복을 줄임.
    } else {
      throw new Error('출석체크 응답이 올바르지 않습니다.');
    }
  } catch (error) {
    console.error('출석체크 오류:', error);
    
    // 사용자에게 더 구체적인 에러 메시지 제공
    let errorMessage = '출석체크 중 오류가 발생했습니다.';
    if (error instanceof Error) {
      if (error.message.includes('401')) {
        errorMessage = '로그인이 필요합니다.';
      } else if (error.message.includes('409')) {
        errorMessage = '이미 오늘 출석체크를 완료했습니다.';
      } else if (error.message.includes('500')) {
        errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
      }
    }
    
    const toast = useToastStore();
    toast.show(errorMessage, 'error');
  }
}

// 컴포넌트 마운트 시 오늘 출석 여부 확인 및 출석 기록 조회
onMounted(async () => {
  try {
    // 오늘 출석 상태 확인 API 호출
    const res = await apiRequest<any>('/attendance/today');
    
    // 백엔드 응답 구조에 맞게 처리
    if (res && res.success && res.data) {
      todayAttended.value = !!res.data.attended;
      
      // 오늘 출석했다면 출석 기록에 추가 (로컬 시간 기준)
      if (todayAttended.value) {
        const today = new Date();
        const todayString = getLocalDateString(today);
        attendanceRecords.value.add(todayString);
      }
    } else {
      todayAttended.value = false;
    }
    
    // 출석 기록 조회
    await fetchAttendanceRecords();
  } catch (error) {
    console.error('오늘 출석 상태 확인 오류:', error);
    todayAttended.value = false;
    attendanceRecords.value = new Set();
  }
});
</script>

<style scoped>
/* float 애니메이션 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px) translateX(-50%);
  }
  50% {
    transform: translateY(-4px) translateX(-50%);
  }
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

/* 말풍선 호버 효과 */
.animate-float:hover {
  animation-play-state: paused;
}
</style>
