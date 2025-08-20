<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 마스코트 생성 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
      <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">🐾 새 마스코트 생성</h2>
      
      <div class="space-y-6">
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
          <div class="grid grid-cols-2 gap-3">
            <button 
              v-for="type in mascotTypes" 
              :key="type.id"
              @click="newMascot.type = type.id"
              :class="[
                'p-4 rounded-lg border-2 transition-all',
                newMascot.type === type.id 
                  ? 'border-purple-500 bg-purple-50' 
                  : 'border-gray-200 hover:border-gray-300'
              ]"
            >
              <div class="text-3xl mb-2">{{ getMascotEmoji(type.id) }}</div>
              <div class="text-sm font-medium">{{ type.name }}</div>
            </button>
          </div>
        </div>
        
        <!-- 선택된 마스코트 미리보기 -->
        <div v-if="newMascot.type" class="text-center p-4 bg-gray-50 rounded-lg">
          <div class="text-6xl mb-2 animate-bounce">{{ getMascotEmoji(newMascot.type) }}</div>
          <p class="text-sm text-gray-600">{{ newMascot.name || '새로운 친구' }}</p>
        </div>
      </div>
      
      <div class="flex space-x-3 mt-8">
        <button 
          @click="goBack"
          class="flex-1 bg-gray-500 hover:bg-gray-600 text-white py-3 px-4 rounded-lg font-medium transition-colors"
        >
          뒤로가기
        </button>
        <button 
          @click="createMascot"
          :disabled="!newMascot.name || !newMascot.type"
          class="flex-1 bg-purple-500 hover:bg-purple-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white py-3 px-4 rounded-lg font-medium transition-colors"
        >
          생성하기
        </button>
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createMascot as createMascotApi, handleApiError, mascot } from '../api/index';
import { mascotTypes } from '../data/mockData';
import type { Mascot, CreateMascotRequest } from '../types/api';

const router = useRouter();

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

// 뒤로가기
function goBack() {
  // 마스코트가 있으면 마스코트 페이지로, 없으면 대시보드로
  if (mascot.hasMascot()) {
    router.push('/mascot');
  } else {
    router.push('/dashboard');
  }
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
    
    // localStorage에 마스코트 데이터 저장
    mascot.setMascot(newMascotData);
    
    showToast.value = true;
    toastMessage.value = `${newMascotData.name}이(가) 태어났습니다! 🎉`;
    
    setTimeout(() => {
      showToast.value = false;
      // 마스코트 메인 화면으로 이동
      router.push('/mascot');
    }, 2000);
    
  } catch (error) {
    console.error('마스코트 생성 실패:', error);
    showToast.value = true;
    toastMessage.value = '마스코트 생성에 실패했습니다. 다시 시도해주세요.';
    
    setTimeout(() => {
      showToast.value = false;
    }, 2000);
  }
}
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
