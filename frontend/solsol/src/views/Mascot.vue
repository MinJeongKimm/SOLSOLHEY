<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 메인 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="flex justify-between items-center mb-6">
        <!-- 좌측: My Room 타이틀 -->
        <h1 class="text-xl font-bold text-gray-800">My Room</h1>
        
        <!-- 우측: 3개 아이콘 버튼들 -->
        <div class="flex space-x-3">
          <button 
            @click="showNotReady('출석체크')"
            class="hover:opacity-70 transition-opacity"
          >
            <img src="/icons/icon_attendance.png" alt="출석" class="w-7 h-7" />
          </button>
          <button 
            @click="showNotReady('챌린지')"
            class="hover:opacity-70 transition-opacity"
          >
            <img src="/icons/icon_challenge.png" alt="챌린지" class="w-7 h-7" />
          </button>
          <button 
            @click="showNotReady('랭킹')"
            class="hover:opacity-70 transition-opacity"
          >
            <img src="/icons/icon_ranking.png" alt="랭킹" class="w-7 h-7" />
          </button>
        </div>
      </div>

      <!-- 포인트 & 좋아요 + 친구 썸네일 리스트 -->
      <div class="pb-4 mb-6 border-b border-gray-100">
        <div class="flex items-center justify-between">
          <!-- 좌측: 포인트 & 좋아요 (위아래 배치) -->
          <div class="flex flex-col space-y-2">
            <!-- 포인트 -->
            <div class="flex items-center space-x-2">
              <img src="/icons/icon_point.png" alt="포인트" class="w-5 h-5" />
              <span class="font-bold text-gray-900">{{ userCoins }}P</span>
            </div>
            <!-- 좋아요 -->
            <div class="flex items-center space-x-2">
              <img src="/icons/icon_like.png" alt="좋아요" class="w-5 h-5" />
              <span class="font-bold text-gray-900">{{ userLikes }}</span>
            </div>
          </div>
          
          <!-- 우측: 친구 썸네일 리스트 -->
          <div class="flex space-x-2">
            <div 
              v-for="friend in friends" 
              :key="friend.id"
              class="flex flex-col items-center"
            >
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white font-bold text-xs mb-1">
                {{ friend.name.charAt(0) }}
              </div>
              <span class="text-xs text-gray-600 truncate w-10 text-center">{{ friend.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 마스코트가 없는 경우 생성 버튼 -->
      <div v-if="!currentMascot" class="text-center py-8">
        <div class="text-6xl mb-4">🥚</div>
        <p class="text-gray-600 mb-4">아직 마스코트가 없습니다</p>
        <button 
          @click="goToCreate"
          class="bg-purple-500 hover:bg-purple-600 text-white px-6 py-3 rounded-lg font-medium transition-colors"
        >
          마스코트 생성하기
        </button>
      </div>

      <!-- 마스코트가 있는 경우 메인 영역 -->
      <div v-else class="space-y-6">
        <!-- 메인 캔버스: 방 배경 + 마스코트 -->
        <div class="relative">
          <!-- 방 배경 -->
          <div 
            class="w-full h-80 rounded-xl shadow-lg relative overflow-hidden flex items-center justify-center"
            style="background: linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)"
          >
            <!-- 배경 이미지 (크기 조정) -->
            <img 
              src="/backgrounds/bg_base.png" 
              alt="방 배경" 
              class="w-3/4 h-3/4 object-contain"
            />
            
            <!-- 마스코트 -->
            <div class="absolute inset-0 flex items-center justify-center">
              <div class="relative">
                <!-- 마스코트 이미지 (크기 키움) -->
                <img 
                  :src="getMascotImageUrl(currentMascot.type)" 
                  :alt="currentMascot.name" 
                  class="w-32 h-32 object-contain animate-float"
                  @error="handleImageError"
                />
                
                <!-- 장착된 아이템들 -->
                <div class="absolute inset-0">
                  <!-- 머리 아이템 -->
                  <img 
                    v-if="currentMascot.equippedItems.head" 
                    :src="currentMascot.equippedItems.head.imageUrl" 
                    :alt="currentMascot.equippedItems.head.name"
                    class="item-head absolute"
                  />
                  <!-- 액세서리 -->
                  <img 
                    v-if="currentMascot.equippedItems.accessory" 
                    :src="currentMascot.equippedItems.accessory.imageUrl" 
                    :alt="currentMascot.equippedItems.accessory.name"
                    class="item-accessory absolute"
                  />
                </div>
              </div>
            </div>
            
            <!-- 마스코트 이름 -->
            <div class="absolute top-3 left-3">
              <div class="bg-white bg-opacity-90 px-2 py-1 rounded-full">
                <span class="text-xs font-medium text-gray-800">{{ currentMascot.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 레벨 카드 -->
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center space-x-2">
              <span class="text-xl">⭐</span>
              <span class="text-lg font-bold text-gray-800">Lv.{{ currentMascot.level }}</span>
            </div>
            <span class="text-sm text-gray-500">{{ currentMascot.experiencePoint }} / {{ getNextLevelExp() }} XP</span>
          </div>
          
          <!-- 경험치 진행바 -->
          <div class="w-full bg-gray-200 rounded-full h-2">
            <div 
              class="h-2 rounded-full transition-all duration-500"
              :style="{ 
                width: getExpPercentage() + '%',
                background: 'linear-gradient(90deg, #0046FF 0%, #4A90E2 100%)'
              }"
            ></div>
          </div>
        </div>

        <!-- 퀵 액션 버튼들 -->
        <div class="grid grid-cols-3 gap-3">
          <!-- 꾸미기 -->
          <button 
            @click="goToCustomize"
            class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105"
          >
            <div class="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
              <img src="/action/action_customize.png" alt="꾸미기" class="w-6 h-6" />
            </div>
            <span class="text-xs font-medium text-gray-700">꾸미기</span>
          </button>
          
          <!-- 밥주기 -->
          <button 
            @click="showNotReady('밥주기')"
            class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105"
          >
            <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
              <img src="/action/action_feed.png" alt="밥주기" class="w-6 h-6" />
            </div>
            <span class="text-xs font-medium text-gray-700">밥주기</span>
          </button>
          
          <!-- 쇼핑하기 -->
          <button 
            @click="showNotReady('쇼핑하기')"
            class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105"
          >
            <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
              <img src="/action/action_shop.png" alt="쇼핑하기" class="w-6 h-6" />
            </div>
            <span class="text-xs font-medium text-gray-700">쇼핑하기</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 알림 토스트 -->
    <div 
      v-if="showToast" 
      class="fixed bottom-4 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white px-4 py-2 rounded-lg shadow-lg z-50 transition-all"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { createMascot as createMascotApi, handleApiError, mascot } from '../api/index';
import { mockMascot, mascotTypes, levelExperience } from '../data/mockData';
import type { Mascot } from '../types/api';

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(null);
const userCoins = ref(15000);
const userLikes = ref(151);

// 친구 목록 데이터
const friends = ref([
  { id: 1, name: 'Danne' },
  { id: 2, name: 'Joan Co' },
  { id: 3, name: 'Jerome' }
]);

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 유틸리티 함수들
function getMascotImageUrl(type: string): string {
  console.log('getMascotImageUrl 호출됨:', { type });
  const typeObj = mascotTypes.find(t => t.id === type);
  const imageUrl = typeObj ? typeObj.imageUrl : '/mascot/soll.png';
  console.log('결정된 이미지 URL:', imageUrl);
  return imageUrl;
}

function handleImageError(event: Event) {
  const target = event.target as HTMLImageElement;
  target.src = '/mascot/soll.png'; // 기본 이미지로 대체
  console.error('이미지 로드 실패:', target.src);
}

function getMascotTypeDisplay(type: string): string {
  const typeObj = mascotTypes.find(t => t.id === type);
  return typeObj ? typeObj.name : type;
}

function getNextLevelExp(): number {
  if (!currentMascot.value) return 0;
  const nextLevel = currentMascot.value.level + 1;
  const levelData = levelExperience.find(l => l.level === nextLevel);
  return levelData ? levelData.requiredExp : 9999;
}

function getExpPercentage(): number {
  if (!currentMascot.value) return 0;
  const currentLevel = levelExperience.find(l => l.level === currentMascot.value!.level);
  const nextLevel = levelExperience.find(l => l.level === currentMascot.value!.level + 1);
  
  if (!currentLevel || !nextLevel) return 100;
  
  const currentExp = currentMascot.value.experiencePoint - currentLevel.requiredExp;
  const totalExp = nextLevel.requiredExp - currentLevel.requiredExp;
  
  return Math.min(100, (currentExp / totalExp) * 100);
}

// 꾸미기 화면으로 이동
function goToCustomize() {
  router.push('/mascot/customize');
}

// 마스코트 생성 화면으로 이동
function goToCreate() {
  router.push('/mascot/create');
}

// 준비중 알림
function showNotReady(feature: string) {
  showToastMessage(`${feature} 기능은 준비중입니다! 🚧`);
}

// 토스트 메시지 표시
function showToastMessage(message: string) {
  toastMessage.value = message;
  showToast.value = true;
  
  setTimeout(() => {
    showToast.value = false;
  }, 3000);
}

// 마스코트 데이터 로드
function loadMascotData() {
  const mascotData = mascot.getMascot();
  console.log('로드된 마스코트 데이터:', mascotData); // 디버깅용
  if (mascotData) {
    currentMascot.value = mascotData;
    console.log('currentMascot 설정 완료:', currentMascot.value); // 디버깅용
  } else {
    console.log('마스코트 데이터가 없습니다. 생성 페이지로 이동합니다.'); // 디버깅용
    // 마스코트가 없으면 생성 페이지로 이동
    router.push('/mascot/create');
  }
}

// currentMascot 변경 감지
watch(currentMascot, (newValue, oldValue) => {
  console.log('currentMascot 변경됨:', {
    oldValue,
    newValue,
    type: newValue?.type,
    name: newValue?.name
  });
}, { deep: true });

// 컴포넌트 마운트
onMounted(() => {
  console.log('Mascot 컴포넌트 마운트됨');
  loadMascotData();
});
</script>

<style scoped>
/* 플로팅 애니메이션 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

/* 스무스 전환 */
.transition-all {
  transition: all 0.3s ease;
}

/* 아이템별 기본 스타일 */
.item-head {
  width: 60%;
  height: 60%;
  top: -15%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  object-fit: contain;
}

.item-accessory {
  width: 30%;
  height: 30%;
  top: 25%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  object-fit: contain;
}

.item-clothing {
  width: 80%;
  height: 80%;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  object-fit: contain;
}
</style>