<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 상단 헤더 -->
    <div class="bg-white px-4 py-3 flex justify-between items-center shadow-sm">
      <!-- 좌측: My Room 타이틀 -->
      <h1 class="text-xl font-bold text-gray-800">My Room</h1>
      
      <!-- 우측: 3개 원형 아이콘 버튼들 -->
      <div class="flex space-x-2">
        <button 
          @click="showNotReady('출석체크')"
          class="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center hover:bg-orange-200 transition-colors"
        >
          <img src="/icons/icon_attendance.png" alt="출석" class="w-6 h-6" />
        </button>
        <button 
          @click="showNotReady('챌린지')"
          class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center hover:bg-blue-200 transition-colors"
        >
          <img src="/icons/icon_challenge.png" alt="챌린지" class="w-6 h-6" />
        </button>
        <button 
          @click="showNotReady('랭킹')"
          class="w-10 h-10 rounded-full bg-yellow-100 flex items-center justify-center hover:bg-yellow-200 transition-colors"
        >
          <img src="/icons/icon_ranking.png" alt="랭킹" class="w-6 h-6" />
        </button>
      </div>
    </div>

    <!-- 포인트 & 좋아요 정보 -->
    <div class="px-4 py-3 bg-white border-b border-gray-100">
      <div class="flex items-center space-x-4">
        <!-- 포인트 -->
        <div class="flex items-center space-x-2">
          <img src="/icons/icon_point.png" alt="포인트" class="w-5 h-5" />
          <span class="font-bold text-orange-600">{{ userCoins }}P</span>
        </div>
        <!-- 좋아요 -->
        <div class="flex items-center space-x-2">
          <img src="/icons/icon_like.png" alt="좋아요" class="w-5 h-5" />
          <span class="font-bold text-red-500">{{ userLikes }}</span>
        </div>
      </div>
    </div>

    <!-- 친구 썸네일 리스트 -->
    <div class="px-4 py-3 bg-white border-b border-gray-100">
      <div class="flex space-x-3">
        <div 
          v-for="friend in friends" 
          :key="friend.id"
          class="flex flex-col items-center"
        >
          <div class="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white font-bold text-sm mb-1">
            {{ friend.name.charAt(0) }}
          </div>
          <span class="text-xs text-gray-600 truncate w-12 text-center">{{ friend.name }}</span>
        </div>
      </div>
    </div>

    <!-- 메인 컨텐츠 영역 -->
    <div class="flex-1 px-4 py-6 space-y-6">
      <!-- 마스코트가 없는 경우 생성 버튼 -->
      <div v-if="!currentMascot" class="text-center py-8">
        <div class="text-6xl mb-4">🥚</div>
        <p class="text-gray-600 mb-4">아직 마스코트가 없습니다</p>
        <button 
          @click="showCreateModal = true"
          class="bg-purple-500 hover:bg-purple-600 text-white px-6 py-2 rounded-lg font-medium transition-colors"
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
            class="w-full h-80 rounded-2xl shadow-lg relative overflow-hidden"
            style="background: linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)"
          >
            <!-- 배경 이미지 -->
            <img 
              src="/backgrounds/bg_base.png" 
              alt="방 배경" 
              class="absolute inset-0 w-full h-full object-cover"
            />
            
            <!-- 마스코트 -->
            <div class="absolute inset-0 flex items-center justify-center">
              <div class="relative">
                <!-- 마스코트 이미지 -->
                <img 
                  src="/mascot/mascot_sol_base.png" 
                  alt="마스코트" 
                  class="w-32 h-32 object-contain animate-float"
                />
                
                <!-- 장착된 아이템들 -->
                <div class="absolute inset-0">
                  <!-- 머리 아이템 -->
                  <img 
                    v-if="currentMascot.equippedItems.head" 
                    :src="currentMascot.equippedItems.head.imageUrl" 
                    :alt="currentMascot.equippedItems.head.name"
                    class="w-32 h-32 object-contain absolute top-0 left-0"
                  />
                  <!-- 액세서리 -->
                  <img 
                    v-if="currentMascot.equippedItems.accessory" 
                    :src="currentMascot.equippedItems.accessory.imageUrl" 
                    :alt="currentMascot.equippedItems.accessory.name"
                    class="w-32 h-32 object-contain absolute top-0 left-0"
                  />
                </div>
              </div>
            </div>
            
            <!-- 마스코트 이름 -->
            <div class="absolute top-4 left-4">
              <div class="bg-white bg-opacity-90 px-3 py-1 rounded-full">
                <span class="text-sm font-medium text-gray-800">{{ currentMascot.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 레벨 카드 -->
        <div class="bg-white rounded-2xl shadow-lg p-4">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center space-x-2">
              <span class="text-2xl">⭐</span>
              <span class="text-lg font-bold text-gray-800">Lv.{{ currentMascot.level }}</span>
            </div>
            <span class="text-sm text-gray-500">{{ currentMascot.experiencePoint }} / {{ getNextLevelExp() }} XP</span>
          </div>
          
          <!-- 경험치 진행바 -->
          <div class="w-full bg-gray-200 rounded-full h-3">
            <div 
              class="bg-gradient-to-r from-green-400 to-blue-500 h-3 rounded-full transition-all duration-500"
              :style="{ width: getExpPercentage() + '%' }"
            ></div>
          </div>
        </div>

        <!-- 퀵 액션 버튼들 -->
        <div class="grid grid-cols-3 gap-4">
          <!-- 꾸미기 -->
          <button 
            @click="goToCustomize"
            class="bg-white rounded-2xl shadow-lg p-4 flex flex-col items-center space-y-2 hover:shadow-xl transition-all transform hover:scale-105"
          >
            <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
              <span class="text-2xl">🎨</span>
            </div>
            <span class="text-sm font-medium text-gray-700">꾸미기</span>
          </button>
          
          <!-- 밥주기 -->
          <button 
            @click="showNotReady('밥주기')"
            class="bg-white rounded-2xl shadow-lg p-4 flex flex-col items-center space-y-2 hover:shadow-xl transition-all transform hover:scale-105"
          >
            <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
              <span class="text-2xl">🍎</span>
            </div>
            <span class="text-sm font-medium text-gray-700">밥주기</span>
          </button>
          
          <!-- 쇼핑하기 -->
          <button 
            @click="showNotReady('쇼핑하기')"
            class="bg-white rounded-2xl shadow-lg p-4 flex flex-col items-center space-y-2 hover:shadow-xl transition-all transform hover:scale-105"
          >
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
              <span class="text-2xl">🛍️</span>
            </div>
            <span class="text-sm font-medium text-gray-700">쇼핑하기</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 하단 탭바 -->
    <div class="bg-white border-t border-gray-200 px-4 py-3">
      <div class="flex justify-center">
        <div class="flex items-center space-x-2 bg-gray-900 text-white px-4 py-2 rounded-full">
          <span class="text-lg">🏠</span>
          <span class="text-sm font-medium">Home</span>
        </div>
      </div>
    </div>

    <!-- 마스코트 생성 모달 -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
        <h3 class="text-xl font-bold text-gray-800 mb-4">🐾 새 마스코트 생성</h3>
        
        <div class="space-y-4">
          <!-- 이름 입력 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">마스코트 이름</label>
            <input 
              v-model="newMascot.name"
              type="text" 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:outline-none"
              placeholder="예: 쏠쏠이"
              maxlength="20"
            />
          </div>
          
          <!-- 종류 선택 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">마스코트 종류</label>
            <div class="grid grid-cols-2 gap-2">
              <button 
                v-for="type in mascotTypes" 
                :key="type.id"
                @click="newMascot.type = type.id"
                :class="[
                  'p-3 rounded-lg border-2 transition-all',
                  newMascot.type === type.id 
                    ? 'border-purple-500 bg-purple-50' 
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <div class="text-2xl mb-1">🐻</div>
                <div class="text-sm font-medium">{{ type.name }}</div>
              </button>
            </div>
          </div>
        </div>
        
        <div class="flex space-x-3 mt-6">
          <button 
            @click="showCreateModal = false"
            class="flex-1 bg-gray-500 hover:bg-gray-600 text-white py-2 px-4 rounded-lg font-medium transition-colors"
          >
            취소
          </button>
          <button 
            @click="createMascot"
            :disabled="!newMascot.name || !newMascot.type"
            class="flex-1 bg-purple-500 hover:bg-purple-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white py-2 px-4 rounded-lg font-medium transition-colors"
          >
            생성
          </button>
        </div>
      </div>
    </div>
    
    <!-- 알림 토스트 -->
    <div 
      v-if="showToast" 
      class="fixed bottom-20 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-all"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { mockMascot, mascotTypes, levelExperience } from '../data/mockData';
import type { Mascot, CreateMascotRequest } from '../types/api';

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(mockMascot);
const userCoins = ref(15000);
const userLikes = ref(151);

// 친구 목록 데이터
const friends = ref([
  { id: 1, name: 'Danne' },
  { id: 2, name: 'Joan Co' },
  { id: 3, name: 'Jerome' }
]);

// 모달 상태
const showCreateModal = ref(false);

// 폼 데이터
const newMascot = ref<CreateMascotRequest>({
  name: '',
  type: ''
});

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 유틸리티 함수들
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

// 마스코트 생성
async function createMascot() {
  try {
    const newMascotData: Mascot = {
      id: Date.now(),
      name: newMascot.value.name,
      type: newMascot.value.type,
      level: 1,
      experiencePoint: 0,
      evolutionStage: 0,
      equippedItems: {},
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    
    currentMascot.value = newMascotData;
    showCreateModal.value = false;
    showToastMessage(`${newMascotData.name}이(가) 태어났습니다! 🎉`);
  } catch (error) {
    console.error('마스코트 생성 실패:', error);
    showNotReady('마스코트 생성');
  }
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

// 컴포넌트 마운트
onMounted(() => {
  console.log('마스코트 메인 페이지 로드됨');
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
</style>