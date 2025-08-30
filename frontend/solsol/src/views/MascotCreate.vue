<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 마스코트 생성 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 단계 표시 -->
      <div class="flex justify-center items-center mb-8">
        <div class="flex items-center space-x-4">
          <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all',
            currentStep >= 1 ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-500']">
            1
          </div>
          <div :class="['w-12 h-1 rounded transition-all',
            currentStep >= 2 ? 'bg-purple-500' : 'bg-gray-200']"></div>
          <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all',
            currentStep >= 2 ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-500']">
            2
          </div>
          <div :class="['w-12 h-1 rounded transition-all',
            currentStep >= 3 ? 'bg-purple-500' : 'bg-gray-200']"></div>
          <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all',
            currentStep >= 3 ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-500']">
            3
          </div>
        </div>
      </div>

      <!-- 단계 1: 마스코트 선택 -->
      <div v-if="currentStep === 1" class="space-y-6">
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-800 mb-2">🐾 마스코트를 선택해주세요</h2>
          <p class="text-gray-600">화살표 버튼이나 키보드 방향키로 <br/> 마음에 드는 친구를 찾아보세요!</p>
          

          

        </div>
        
        <!-- 커스텀 슬라이더 -->
        <div 
          class="relative" 
          @keydown.left="previousMascot" 
          @keydown.right="nextMascot"
          tabindex="0"
        >
          <!-- 현재 마스코트 카드 -->
          <div 
            class="bg-gradient-to-br from-purple-50 to-blue-50 rounded-2xl p-8 w-full text-center transition-all duration-300 transform"
            :key="selectedMascotIndex"
          >
            <div class="mb-6">
              <img 
                :src="currentMascotType.imageUrl" 
                :alt="currentMascotType.name"
                class="w-48 h-48 object-contain mx-auto rounded-xl transition-all duration-300 hover:scale-105"
                @error="handleImageError"
                @load="handleImageLoad"
              />
              <div class="flex justify-center items-center mt-2 space-x-2">
                <span class="text-xs text-gray-400">{{ selectedMascotIndex + 1 }} / {{ availableMascotTypes.length }}</span>
                <span class="text-xs text-purple-500">• {{ currentMascotType.name }}</span>
              </div>
            </div>
            <h3 class="text-2xl font-bold text-purple-600 mb-2">{{ currentMascotType.name }}</h3>
            <p class="text-gray-600 text-sm">{{ currentMascotType.description }}</p>
          </div>

          <!-- 네비게이션 버튼들 -->
          <button 
            @click="previousMascot"
            :disabled="selectedMascotIndex === 0"
            class="absolute left-4 top-1/2 transform -translate-y-1/2 bg-white rounded-full p-3 shadow-lg nav-button disabled:opacity-50 disabled:cursor-not-allowed z-10"
          >
            <svg class="w-6 h-6 text-purple-600 transition-transform duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          
          <button 
            @click="nextMascot"
            :disabled="selectedMascotIndex === availableMascotTypes.length - 1"
            class="absolute right-4 top-1/2 transform -translate-y-1/2 bg-white rounded-full p-3 shadow-lg nav-button disabled:opacity-50 disabled:cursor-not-allowed z-10"
          >
            <svg class="w-6 h-6 text-purple-600 transition-transform duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>

          <!-- 인디케이터 -->
          <div class="flex justify-center mt-6 space-x-2">
            <button
              v-for="(type, index) in availableMascotTypes"
              :key="index"
              @click="selectedMascotIndex = index"
              :class="[
                'w-3 h-3 rounded-full transition-all',
                selectedMascotIndex === index ? 'bg-purple-600 scale-125' : 'bg-gray-300 hover:bg-gray-400'
              ]"
            ></button>
          </div>
        </div>

        <div class="flex justify-center mt-8">
          <button 
            @click="selectCurrentMascot"
            class="inline-flex items-center whitespace-nowrap bg-purple-500 hover:bg-purple-600 text-white py-3 px-6 sm:px-8 rounded-lg font-medium transition-all transform hover:scale-105 shadow-lg hover:shadow-xl text-sm sm:text-base"
          >
            {{ currentMascotType.name }}(으)로 선택! ✨
          </button>
        </div>
      </div>

      <!-- 단계 2: 이름 입력 -->
      <div v-if="currentStep === 2" class="space-y-6">
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-800 mb-2">✏️ 이름을 지어주세요</h2>
          <p class="text-gray-600">특별한 이름으로 친구를 불러보세요!</p>
        </div>

        <!-- 선택된 마스코트 미리보기 -->
        <div class="text-center p-6 bg-gradient-to-br from-purple-50 to-blue-50 rounded-xl">
          <img 
            :src="selectedMascot.imageUrl" 
            :alt="selectedMascot.name"
            class="w-32 h-32 object-contain mx-auto mb-4 rounded-xl"
          />
          <h3 class="text-xl font-bold text-purple-600">{{ selectedMascot.name }}</h3>
        </div>

        <!-- 이름 입력 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">마스코트 이름</label>
          <input 
            ref="nameInput"
            v-model="newMascot.name"
            type="text" 
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:outline-none text-center text-lg"
            placeholder="예: 쏠쏠이"
            maxlength="20"
            @keyup.enter="proceedToFinal"
          />
          <p class="text-xs text-gray-500 mt-1 text-center">{{ newMascot.name.length }}/20</p>
        </div>

        <div class="flex space-x-3">
          <button 
            @click="currentStep = 1"
            class="inline-flex items-center justify-center whitespace-nowrap flex-1 bg-gray-500 hover:bg-gray-600 text-white py-3 px-4 rounded-lg font-medium transition-colors text-sm sm:text-base"
          >
            이전
          </button>
          <button 
            @click="proceedToFinal"
            :disabled="!newMascot.name.trim()"
            class="inline-flex items-center justify-center whitespace-nowrap flex-1 bg-purple-500 hover:bg-purple-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white py-3 px-4 rounded-lg font-medium transition-colors text-sm sm:text-base"
          >
            다음
          </button>
        </div>
      </div>

      <!-- 단계 3: 최종 확인 -->
      <div v-if="currentStep === 3" class="space-y-6">
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-800 mb-2">🎉 마스코트 생성 완료!</h2>
          <p class="text-gray-600">새로운 친구와 함께 여정을 시작해보세요!</p>
        </div>

        <!-- 최종 마스코트 정보 -->
        <div class="text-center p-8 bg-gradient-to-br from-purple-50 to-blue-50 rounded-xl">
          <img 
            :src="selectedMascot.imageUrl" 
            :alt="selectedMascot.name"
            class="w-40 h-40 object-contain mx-auto mb-4 rounded-xl animate-bounce"
          />
          <h3 class="text-2xl font-bold text-purple-600 mb-2">{{ newMascot.name }}</h3>
          <p class="text-gray-600">{{ selectedMascot.description }}</p>
        </div>

        <div class="flex space-x-3">
          <button 
            @click="currentStep = 2"
            class="inline-flex items-center justify-center whitespace-nowrap flex-1 bg-gray-500 hover:bg-gray-600 text-white py-3 px-4 rounded-lg font-medium transition-colors text-sm sm:text-base"
          >
            수정
          </button>
          <button 
            @click="createMascot"
            class="inline-flex items-center justify-center whitespace-nowrap flex-1 bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600 text-white py-3 px-4 rounded-lg font-medium transition-all transform hover:scale-105 text-sm sm:text-base"
          >
            생성 완료! 🚀
          </button>
        </div>
      </div>
    </div>
    
    <!-- 알림 토스트 -->
    <div 
      v-if="showToast" 
      class="fixed bottom-4 right-4 bg-blue-500 text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-all"
      :class="{ 'opacity-0': !showToast }"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { createMascot as createMascotApi, handleApiError, auth } from '../api/index';
import { mascotTypes } from '../data/mockData';
import type { CreateMascotRequest } from '../types/api';

const router = useRouter();

// 단계 관리
const currentStep = ref(1);

// 사용자 캠퍼스 정보
const campusName = ref<string>('');
const isLoadingCampus = ref(true);

// 사용 가능한 마스코트 타입 (캠퍼스별 필터링)
const availableMascotTypes = computed(() => {
  if (campusName.value === '숙명여자대학교') {
    // 숙명여자대학교: 눈송이를 맨 앞으로, 나머지는 기존 순서
    const sookmyung = mascotTypes.find(type => type.id === 'sookmyung');
    const others = mascotTypes.filter(type => type.id !== 'sookmyung');
    return sookmyung ? [sookmyung, ...others] : mascotTypes;
  } else {
    // 기타 캠퍼스: 눈송이 제외
    return mascotTypes.filter(type => type.id !== 'sookmyung');
  }
});

// 마스코트 선택 관련
const selectedMascotIndex = ref(0);
const selectedMascot = computed(() => availableMascotTypes.value[selectedMascotIndex.value]);
const currentMascotType = computed(() => availableMascotTypes.value[selectedMascotIndex.value]);

// 폼 데이터
const newMascot = ref<CreateMascotRequest>({
  name: '',
  type: ''
});

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 이름 입력 참조
const nameInput = ref<HTMLInputElement>();

// 슬라이더 네비게이션 함수들
function nextMascot() {
  if (selectedMascotIndex.value < availableMascotTypes.value.length - 1) {
    selectedMascotIndex.value++;
  }
}

function previousMascot() {
  if (selectedMascotIndex.value > 0) {
    selectedMascotIndex.value--;
  }
}

// 현재 마스코트 선택
function selectCurrentMascot() {
  newMascot.value.type = selectedMascot.value.id;
  currentStep.value = 2;
  
  // 이름 입력 필드에 포커스
  nextTick(() => {
    nameInput.value?.focus();
  });
}

// 최종 단계로 진행
function proceedToFinal() {
  if (!newMascot.value.name.trim()) return;
  currentStep.value = 3;
}



// 이미지 에러 핸들링
function handleImageError(event: Event) {
  const target = event.target as HTMLImageElement;
  console.error('이미지 로드 실패:', target.src);
  // 기본 이미지로 대체
  target.src = '/mascot/soll.png';
}

// 이미지 로드 성공 핸들링
function handleImageLoad(event: Event) {
  // 이미지 로드 성공 시 필요한 처리가 있으면 여기에 추가
}

// 마스코트 생성
async function createMascot() {
  try {
    // 로딩 상태 표시
    showToast.value = true;
    toastMessage.value = '마스코트를 생성하고 있습니다... 🚀';
    
    // 백엔드 API 호출
    const newMascotData = await createMascotApi({
      name: newMascot.value.name,
      type: newMascot.value.type
    });
    
    console.log('백엔드에서 생성된 마스코트:', newMascotData);
    
    // 성공 메시지 표시
    toastMessage.value = `${newMascotData.name}이(가) 태어났습니다! 🎉`;
    
    setTimeout(() => {
      showToast.value = false;
      // 마스코트 메인 화면으로 이동
      router.push('/mascot');
    }, 2000);
    
  } catch (error) {
    console.error('마스코트 생성 실패:', error);
    
    // 에러 메시지 표시
    const errorMessage = handleApiError(error);
    toastMessage.value = `마스코트 생성에 실패했습니다: ${errorMessage}`;
    
    setTimeout(() => {
      showToast.value = false;
    }, 3000);
  }
}

// 키보드 이벤트 핸들러
function handleKeydown(event: KeyboardEvent) {
  if (currentStep.value === 1) {
    if (event.key === 'ArrowLeft') {
      event.preventDefault();
      previousMascot();
    } else if (event.key === 'ArrowRight') {
      event.preventDefault();
      nextMascot();
    } else if (event.key === 'Enter') {
      event.preventDefault();
      selectCurrentMascot();
    }
  }
}

// 사용자 캠퍼스 정보 조회
async function fetchUserCampus() {
  try {
    isLoadingCampus.value = true;
    const user = await auth.fetchUser();
    if (user && user.campus) {
      campusName.value = user.campus;
      console.log('사용자 캠퍼스:', user.campus);
      
      // 캠퍼스 정보에 따라 선택된 마스코트 인덱스 조정
      if (campusName.value !== '숙명여자대학교' && selectedMascotIndex.value >= availableMascotTypes.value.length) {
        selectedMascotIndex.value = Math.max(0, availableMascotTypes.value.length - 1);
      }
    }
  } catch (error) {
    console.error('사용자 캠퍼스 정보 조회 실패:', error);
    // 기본값으로 설정 (눈송이 제외)
    campusName.value = '';
  } finally {
    isLoadingCampus.value = false;
  }
}

// 컴포넌트 마운트
onMounted(async () => {
  // 사용자 캠퍼스 정보 조회
  await fetchUserCampus();
  
  // 키보드 이벤트 리스너 등록
  document.addEventListener('keydown', handleKeydown);
});

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});
</script>

<style scoped>
/* 애니메이션 */
@keyframes bounce {
  0%, 20%, 53%, 80%, 100% {
    transform: translate3d(0,0,0);
  }
  40%, 43% {
    transform: translate3d(0,-30px,0);
  }
  70% {
    transform: translate3d(0,-15px,0);
  }
  90% {
    transform: translate3d(0,-4px,0);
  }
}

.animate-bounce {
  animation: bounce 2s infinite;
}

/* 커스텀 슬라이더 스타일 */
.custom-slider {
  user-select: none;
}

/* 네비게이션 버튼 애니메이션 */
.nav-button {
  transition: all 0.2s ease;
}

.nav-button:hover:not(:disabled) {
  background-color: #f8fafc;
  box-shadow: 0 10px 20px rgba(139, 92, 246, 0.15);
}

.nav-button:hover:not(:disabled) svg {
  transform: scale(1.1);
}

.nav-button:active:not(:disabled) svg {
  transform: scale(0.95);
}

/* 단계 전환 애니메이션 */
.step-enter-active,
.step-leave-active {
  transition: all 0.5s ease;
}

.step-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.step-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 마스코트 이미지 호버 효과 */
.mascot-swiper img {
  transition: all 0.3s ease;
}

.mascot-swiper img:hover {
  transform: scale(1.05);
}

/* 부드러운 단계 표시기 애니메이션 */
.step-indicator {
  transition: all 0.3s ease;
}
</style>
