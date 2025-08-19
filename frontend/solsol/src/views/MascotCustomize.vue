<template>
  <div class="min-h-screen bg-gray-100">
    <!-- 상단 헤더 -->
    <div class="bg-white shadow-sm">
      <div class="container mx-auto px-4 py-4">
        <div class="flex justify-between items-center">
          <!-- 뒤로가기 버튼 -->
          <button 
            @click="goBack"
            class="flex items-center space-x-2 text-gray-600 hover:text-gray-800 transition-colors"
          >
            <span class="text-xl">←</span>
            <span class="font-medium">꾸미기</span>
          </button>
          
          <!-- 코인 정보 -->
          <div class="flex items-center space-x-2">
            <span class="text-xl">🪙</span>
            <span class="text-lg font-bold text-yellow-600">{{ userCoins }}P</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="container mx-auto px-4 py-6">
      <!-- 마스코트 미리보기 영역 -->
      <div class="bg-white rounded-2xl shadow-lg p-6 mb-6">
        <div class="relative h-64 flex items-center justify-center bg-gradient-to-br from-blue-50 to-purple-50 rounded-xl">
          <!-- 배경 -->
          <div 
            v-if="currentMascot?.equippedItems.background" 
            class="absolute inset-0 rounded-xl opacity-40"
            :style="{ backgroundImage: `url(${currentMascot.equippedItems.background.imageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
          ></div>
          
          <!-- 마스코트 캐릭터 -->
          <div v-if="currentMascot" class="relative z-10 text-center">
            <div class="text-8xl mb-2">
              {{ getMascotEmoji(currentMascot.type) }}
            </div>
            <div class="text-sm text-gray-600">
              {{ currentMascot.name }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 아이템 카테고리 버튼들 -->
      <div class="bg-white rounded-2xl shadow-lg p-4 mb-6">
        <div class="grid grid-cols-5 gap-3">
          <button 
            v-for="category in itemCategories" 
            :key="category.id"
            @click="selectedCategory = category.id"
            :class="[
              'flex flex-col items-center p-3 rounded-xl transition-all',
              selectedCategory === category.id 
                ? 'bg-purple-500 text-white shadow-lg' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            <div class="w-12 h-12 rounded-full flex items-center justify-center mb-2" 
                 :class="selectedCategory === category.id ? 'bg-white bg-opacity-20' : 'bg-white'">
              <span class="text-2xl">{{ category.icon }}</span>
            </div>
            <span class="text-xs font-medium">{{ category.name }}</span>
          </button>
        </div>
      </div>
      
      <!-- 아이템 목록 -->
      <div class="bg-white rounded-2xl shadow-lg p-4">
        <h3 class="text-lg font-bold text-gray-800 mb-4">{{ getCategoryName(selectedCategory) }}</h3>
        
        <div class="grid grid-cols-2 gap-4">
          <div 
            v-for="item in filteredItems" 
            :key="item.id"
            :class="[
              'border-2 rounded-xl p-4 cursor-pointer transition-all hover:shadow-md',
              rarityColors[item.rarity]?.bg,
              rarityColors[item.rarity]?.border,
              isEquipped(item) ? 'ring-2 ring-purple-500' : ''
            ]"
            @click="equipItem(item)"
          >
            <!-- 아이템 이미지 영역 (임시) -->
            <div class="w-full h-24 bg-gray-100 rounded-lg mb-3 flex items-center justify-center">
              <span class="text-3xl opacity-50">{{ getCategoryIcon(item.type) }}</span>
            </div>
            
            <!-- 아이템 정보 -->
            <div>
              <h4 :class="['font-medium text-sm mb-1', rarityColors[item.rarity]?.text]">
                {{ item.name }}
                <span v-if="isEquipped(item)" class="text-purple-600 text-xs ml-1">✓</span>
              </h4>
              <p class="text-xs text-gray-600 mb-2">{{ item.description }}</p>
              
              <div class="flex justify-between items-center">
                <span :class="['text-xs font-medium px-2 py-1 rounded', rarityColors[item.rarity]?.bg]">
                  {{ getRarityDisplay(item.rarity) }}
                </span>
                <span class="text-xs font-bold text-yellow-600">{{ item.price }}P</span>
              </div>
              
              <!-- 액션 버튼 -->
              <div class="mt-3">
                <button 
                  v-if="item.isOwned"
                  @click.stop="equipItem(item)"
                  :disabled="isEquipped(item)"
                  :class="[
                    'w-full py-2 px-3 rounded-lg text-xs font-medium transition-colors',
                    isEquipped(item) 
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
                      : 'bg-purple-500 hover:bg-purple-600 text-white'
                  ]"
                >
                  {{ isEquipped(item) ? '착용중' : '착용하기' }}
                </button>
                <button 
                  v-else
                  @click.stop="showNotReady('아이템 구매')"
                  class="w-full bg-green-500 hover:bg-green-600 text-white py-2 px-3 rounded-lg text-xs font-medium transition-colors"
                >
                  구매하기
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 아이템이 없는 경우 -->
        <div v-if="filteredItems.length === 0" class="text-center py-8">
          <div class="text-4xl mb-2 opacity-50">📦</div>
          <p class="text-gray-500">해당 카테고리에 아이템이 없습니다.</p>
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
import { equipItems, handleApiError } from '../api/index';
import { mockMascot, mockItems, rarityColors } from '../data/mockData';
import type { Mascot, Item, EquipItemsRequest } from '../types/api';

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(mockMascot);
const items = ref<Item[]>(mockItems);
const userCoins = ref(15000);
const selectedCategory = ref<'clothing' | 'background' | 'accessory' | 'shoes' | 'bag'>('clothing');

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 아이템 카테고리
const itemCategories = [
  { id: 'clothing', name: 'Top', icon: '👕' },
  { id: 'background', name: 'BG', icon: '🏞️' },
  { id: 'accessory', name: 'Acc', icon: '👓' },
  { id: 'shoes', name: 'Shoes', icon: '👟' },
  { id: 'bag', name: 'Bag', icon: '🎒' }
];

// 필터링된 아이템 목록
const filteredItems = computed(() => {
  let categoryType = selectedCategory.value;
  
  // shoes와 bag은 아직 데이터에 없으므로 accessory로 대체
  if (categoryType === 'shoes' || categoryType === 'bag') {
    categoryType = 'accessory';
  }
  
  return items.value.filter(item => item.type === categoryType);
});

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

function getRarityDisplay(rarity: string): string {
  const rarityMap: Record<string, string> = {
    common: '일반',
    rare: '희귀',
    epic: '영웅',
    legendary: '전설'
  };
  return rarityMap[rarity] || rarity;
}

function getCategoryName(category: string): string {
  const categoryMap: Record<string, string> = {
    clothing: '상의',
    background: '배경',
    accessory: '액세서리',
    shoes: '신발',
    bag: '가방'
  };
  return categoryMap[category] || category;
}

function getCategoryIcon(type: string): string {
  const iconMap: Record<string, string> = {
    clothing: '👕',
    background: '🏞️',
    accessory: '👓',
    shoes: '👟',
    bag: '🎒'
  };
  return iconMap[type] || '📦';
}

function isEquipped(item: Item): boolean {
  if (!currentMascot.value) return false;
  const equipped = currentMascot.value.equippedItems;
  
  switch (item.type) {
    case 'clothing':
      return equipped.clothing?.id === item.id;
    case 'background':
      return equipped.background?.id === item.id;
    case 'accessory':
      return equipped.accessory?.id === item.id;
    default:
      return false;
  }
}

// 뒤로가기
function goBack() {
  router.push('/');
}

// 아이템 장착
async function equipItem(item: Item) {
  if (!currentMascot.value || !item.isOwned) return;
  
  try {
    // Mock 데이터로 시뮬레이션
    const updatedMascot = { ...currentMascot.value };
    updatedMascot.equippedItems = { ...updatedMascot.equippedItems };
    
    switch (item.type) {
      case 'clothing':
        updatedMascot.equippedItems.clothing = item;
        break;
      case 'background':
        updatedMascot.equippedItems.background = item;
        break;
      case 'accessory':
        updatedMascot.equippedItems.accessory = item;
        break;
    }
    
    currentMascot.value = updatedMascot;
    showToast.value = true;
    toastMessage.value = `${item.name}을(를) 착용했습니다!`;
    
    setTimeout(() => {
      showToast.value = false;
    }, 2000);
    
    // 실제 API 호출 (주석 처리)
    // const equipData: EquipItemsRequest = {
    //   equipItems: {
    //     [`${item.type}Id`]: item.id
    //   }
    // };
    // const response = await equipItems(equipData);
  } catch (error) {
    console.error('아이템 착용 실패:', error);
    showNotReady('아이템 착용');
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
  console.log('마스코트 꾸미기 페이지 로드됨');
});
</script>

<style scoped>
/* 스크롤바 스타일링 */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
