<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100">
    <!-- 상단 정보 바 -->
    <div class="bg-white shadow-sm">
      <div class="container mx-auto px-4 py-4">
        <div class="flex justify-between items-center">
          <!-- 코인 정보 -->
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-2">
              <span class="text-2xl">🪙</span>
              <span class="text-lg font-bold text-yellow-600">{{ userCoins }}P</span>
            </div>
          </div>
          
          <!-- 좋아요 수 -->
          <div class="flex items-center space-x-2">
            <span class="text-xl">❤️</span>
            <span class="text-lg font-bold text-red-500">{{ userLikes }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="container mx-auto px-4 py-8">
      <!-- 마스코트 정보 카드 -->
      <div class="bg-white rounded-2xl shadow-lg p-6 mb-6">
        <div v-if="currentMascot" class="text-center">
          <!-- 마스코트 이름 -->
          <h2 class="text-2xl font-bold text-purple-600 mb-2">{{ currentMascot.name }}</h2>
          <p class="text-gray-600 mb-4">{{ getMascotTypeDisplay(currentMascot.type) }}</p>
          
          <!-- 레벨 정보 -->
          <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-4 mb-4">
            <div class="flex justify-between items-center mb-2">
              <span class="font-semibold text-gray-700">레벨 {{ currentMascot.level }}</span>
              <span class="text-sm text-gray-500">{{ currentMascot.experiencePoint }} / {{ getNextLevelExp() }} XP</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-3">
              <div 
                class="bg-gradient-to-r from-blue-500 to-purple-500 h-3 rounded-full transition-all duration-500"
                :style="{ width: getExpPercentage() + '%' }"
              ></div>
            </div>
          </div>
          
          <!-- 진화 단계 -->
          <div class="mb-6">
            <span class="inline-block bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-medium">
              진화 단계 {{ currentMascot.evolutionStage }}
            </span>
          </div>
        </div>
        
        <!-- 마스코트가 없는 경우 -->
        <div v-else class="text-center py-8">
          <div class="text-6xl mb-4">🥚</div>
          <p class="text-gray-600 mb-4">아직 마스코트가 없습니다</p>
          <button 
            @click="showCreateModal = true"
            class="bg-purple-500 hover:bg-purple-600 text-white px-6 py-2 rounded-lg font-medium transition-colors"
          >
            마스코트 생성하기
          </button>
        </div>
      </div>
      
      <!-- 마스코트 캐릭터 영역 -->
      <div class="bg-white rounded-2xl shadow-lg p-8 mb-6">
        <div class="relative h-80 flex items-center justify-center">
          <!-- 배경 -->
          <div 
            v-if="currentMascot?.equippedItems.background" 
            class="absolute inset-0 rounded-xl opacity-30"
            :style="{ backgroundImage: `url(${currentMascot.equippedItems.background.imageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
          ></div>
          
          <!-- 마스코트 캐릭터 -->
          <div v-if="currentMascot" class="relative z-10 text-center">
            <!-- 메인 캐릭터 (이모지로 대체) -->
            <div class="text-9xl mb-4 animate-bounce">
              {{ getMascotEmoji(currentMascot.type) }}
            </div>
            
            <!-- 장착된 아이템 정보 -->
            <div class="text-center space-y-1">
              <div v-if="currentMascot.equippedItems.clothing" class="text-sm text-gray-600">
                착용중: {{ currentMascot.equippedItems.clothing.name }}
              </div>
              <div v-if="currentMascot.equippedItems.accessory" class="text-sm text-gray-600">
                액세서리: {{ currentMascot.equippedItems.accessory.name }}
              </div>
            </div>
          </div>
          
          <div v-else class="text-center">
            <div class="text-9xl mb-4 opacity-50">🥚</div>
            <p class="text-gray-500">마스코트를 생성해주세요!</p>
          </div>
        </div>
      </div>
      
      <!-- 하단 액션 버튼들 -->
      <div v-if="currentMascot" class="grid grid-cols-3 gap-4">
        <!-- 꾸미기 -->
        <button 
          @click="goToCustomize"
          class="bg-purple-500 hover:bg-purple-600 text-white py-4 px-6 rounded-2xl font-bold text-lg shadow-lg transition-all transform hover:scale-105 flex flex-col items-center space-y-2"
        >
          <span class="text-2xl">🎨</span>
          <span>꾸미기</span>
        </button>
        
        <!-- 밥주기 -->
        <button 
          @click="showNotReady('밥주기')"
          class="bg-green-500 hover:bg-green-600 text-white py-4 px-6 rounded-2xl font-bold text-lg shadow-lg transition-all transform hover:scale-105 flex flex-col items-center space-y-2"
        >
          <span class="text-2xl">🍎</span>
          <span>밥주기</span>
        </button>
        
        <!-- 쇼핑하기 -->
        <button 
          @click="showNotReady('쇼핑하기')"
          class="bg-blue-500 hover:bg-blue-600 text-white py-4 px-6 rounded-2xl font-bold text-lg shadow-lg transition-all transform hover:scale-105 flex flex-col items-center space-y-2"
        >
          <span class="text-2xl">🛍️</span>
          <span>쇼핑하기</span>
        </button>
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
                <div class="text-2xl mb-1">{{ getMascotEmoji(type.id) }}</div>
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
      class="fixed bottom-4 right-4 bg-blue-500 text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-all"
      :class="{ 'opacity-0': !showToast }"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { createMascot as createMascotApi, handleApiError } from '../api/index';
import { mockMascot, mascotTypes, levelExperience } from '../data/mockData';
import type { Mascot, CreateMascotRequest } from '../types/api';

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(mockMascot);
const userCoins = ref(15000);
const userLikes = ref(151);

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
function getMascotEmoji(type: string): string {
  const emojiMap: Record<string, string> = {
    bear: '🐻',
    tiger: '🐯',
    eagle: '🦅',
    lion: '🦁',
    panda: '🐼'
  };
  return emojiMap[type] || '🐾';
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

// 마스코트 생성
async function createMascot() {
  try {
    // Mock 데이터로 시뮬레이션
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
    showToast.value = true;
    toastMessage.value = `${newMascotData.name}이(가) 태어났습니다! 🎉`;
    
    setTimeout(() => {
      showToast.value = false;
    }, 3000);
  } catch (error) {
    console.error('마스코트 생성 실패:', error);
    showNotReady('마스코트 생성');
  }
}

// 준비중 알림
function showNotReady(feature: string) {
  showToast.value = true;
  toastMessage.value = `${feature} 기능은 준비중입니다! 🚧`;
  
  setTimeout(() => {
    showToast.value = false;
  }, 2000);
}

// 컴포넌트 마운트
onMounted(() => {
  console.log('마스코트 메인 페이지 로드됨');
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
</style>