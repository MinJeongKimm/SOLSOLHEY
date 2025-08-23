<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 커스터마이즈 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="flex justify-between items-center mb-6">
        <!-- 좌측: 뒤로가기 + 제목 -->
        <div class="flex items-center space-x-3">
          <button 
            @click="goBack"
            class="w-8 h-8 flex items-center justify-center hover:bg-gray-100 rounded-full transition-colors"
          >
            <img src="/icons/icon_back.png" alt="뒤로가기" class="w-5 h-5" />
          </button>
          <h1 class="text-xl font-bold text-gray-800">Customize</h1>
        </div>
        
        <!-- 우측: 포인트 -->
        <div class="flex items-center space-x-2">
          <img src="/icons/icon_point.png" alt="포인트" class="w-5 h-5" />
          <span class="font-bold text-orange-600">{{ userCoins }}P</span>
        </div>
      </div>

      <!-- 마스코트 미리보기 영역 -->
      <div class="bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl p-8 mb-6">
        <div 
          class="relative h-64 rounded-xl overflow-hidden flex items-center justify-center"
          style="background: linear-gradient(135deg, #bfdbfe 0%, #ddd6fe 100%)"
        >
          <!-- 커스터마이즈 배경 -->
          <img 
            src="/backgrounds/bg_customize.png" 
            alt="꾸미기 배경" 
            class="absolute inset-0 w-full h-full object-cover"
          />
          
          <!-- 마스코트 + 장착된 아이템들 -->
          <div class="absolute inset-0 flex items-center justify-center">
            <div class="relative">
              <!-- 마스코트 이미지 -->
              <img 
                :src="currentMascot ? getMascotImageUrl(currentMascot.type) : '/mascot/soll.png'" 
                :alt="currentMascot?.name || '마스코트'" 
                class="w-32 h-32 object-contain"
                @error="handleMascotImageError"
              />
              
                              <!-- 장착된 아이템들 (단순 문자열로 표시) -->
                <div class="absolute inset-0">
                  <!-- 아이템 정보 표시 -->
                  <div v-if="currentMascot?.equippedItem" class="absolute top-0 right-0 bg-white bg-opacity-90 px-2 py-1 rounded-full text-xs">
                    {{ currentMascot.equippedItem }}
                  </div>
                </div>
            </div>
          </div>
          
          <!-- 마스코트 이름 -->
          <div class="absolute bottom-4 left-1/2 transform -translate-x-1/2">
            <div class="bg-white bg-opacity-90 px-3 py-1 rounded-full">
              <span class="text-sm font-medium text-gray-800">{{ currentMascot?.name || '쏠' }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 카테고리 탭들 -->
      <div class="mb-6">
        <div class="flex space-x-2 overflow-x-auto">
          <button 
            v-for="category in itemCategories" 
            :key="category.id"
            @click="selectedCategory = category.id"
            :class="[
              'flex-shrink-0 flex flex-col items-center p-3 rounded-xl transition-all min-w-[80px]',
              selectedCategory === category.id 
                ? 'bg-purple-500 text-white shadow-lg' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            <div class="w-10 h-10 rounded-full flex items-center justify-center mb-2" 
                 :class="selectedCategory === category.id ? 'bg-white bg-opacity-20' : 'bg-white'">
              <span class="text-xl">{{ category.icon }}</span>
            </div>
            <span class="text-xs font-medium">{{ category.name }}</span>
          </button>
        </div>
      </div>
      
      <!-- 아이템 목록 -->
      <div class="space-y-4">
        <h3 class="text-lg font-bold text-gray-800">{{ getCategoryName(selectedCategory) }}</h3>
        
        <div class="grid grid-cols-2 gap-4">
          <div 
            v-for="item in filteredItems" 
            :key="item.id"
            :class="[
              'relative border-2 rounded-xl p-4 cursor-pointer transition-all hover:shadow-md',
              isEquipped(item) 
                ? 'border-purple-500 bg-purple-50' 
                : 'border-gray-200 hover:border-gray-300'
            ]"
            @click="toggleEquipItem(item)"
          >
            <!-- 아이템 이미지 -->
            <div class="w-full h-20 bg-gray-100 rounded-lg mb-3 flex items-center justify-center overflow-hidden">
              <img 
                :src="item.imageUrl" 
                :alt="item.name"
                class="w-full h-full object-contain"
                @error="handleImageError"
              />
            </div>
            
            <!-- 아이템 정보 -->
            <div>
              <h4 class="font-medium text-sm text-gray-800 mb-1 flex items-center">
                {{ item.name }}
                <span v-if="isEquipped(item)" class="text-purple-600 text-xs ml-1">✓</span>
              </h4>
              <p class="text-xs text-gray-600 mb-2 line-clamp-2">{{ item.description }}</p>
              
              <!-- 착용 상태 표시 -->
              <div class="text-center">
                <span 
                  :class="[
                    'text-xs font-medium px-3 py-1 rounded-full',
                    isEquipped(item) 
                      ? 'bg-purple-500 text-white' 
                      : 'bg-gray-200 text-gray-600'
                  ]"
                >
                  {{ isEquipped(item) ? '착용중' : '착용하기' }}
                </span>
              </div>
            </div>

            <!-- 장착 아이콘 -->
            <div 
              v-if="isEquipped(item)"
              class="absolute top-2 right-2 w-6 h-6 bg-purple-500 rounded-full flex items-center justify-center"
            >
              <span class="text-white text-xs">✓</span>
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
      class="fixed bottom-4 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-all"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { realItems, mascotTypes } from '../data/mockData';
import { getMascot, equipItems, handleApiError } from '../api/index';
import type { Mascot, Item } from '../types/api';

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(null);
const items = ref<Item[]>(realItems);
const userCoins = ref(15000);
const selectedCategory = ref<'top' | 'pants' | 'accessory' | 'shoes' | 'bag'>('top');

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 아이템 카테고리
const itemCategories = [
  { id: 'top', name: 'Top', icon: '👕' },
  { id: 'pants', name: 'Pants', icon: '👖' },
  { id: 'accessory', name: 'Acc', icon: '👓' },
  { id: 'shoes', name: 'Shoes', icon: '👟' },
  { id: 'bag', name: 'Bag', icon: '🎒' }
];

// 필터링된 아이템 목록 (보유한 아이템만)
const filteredItems = computed(() => {
  let categoryType = selectedCategory.value;
  
  // 카테고리별 매핑
  if (categoryType === 'top') categoryType = 'head'; // Top은 머리 아이템으로
  if (categoryType === 'pants') return []; // Pants는 아직 아이템이 없음
  if (categoryType === 'shoes') return []; // Shoes는 아직 아이템이 없음
  if (categoryType === 'bag') return []; // Bag은 아직 아이템이 없음
  
  return items.value.filter(item => 
    item.type === categoryType && item.isOwned
  );
});

// 유틸리티 함수들
function getMascotImageUrl(type: string): string {
  console.log('꾸미기 화면에서 getMascotImageUrl 호출됨:', { type });
  const typeObj = mascotTypes.find(t => t.id === type);
  const imageUrl = typeObj ? typeObj.imageUrl : '/mascot/soll.png';
  console.log('꾸미기 화면에서 결정된 이미지 URL:', imageUrl);
  return imageUrl;
}

function getCategoryName(category: string): string {
  const categoryMap: Record<string, string> = {
    top: '상의',
    pants: '하의', 
    accessory: '액세서리',
    shoes: '신발',
    bag: '가방'
  };
  return categoryMap[category] || category;
}

function isEquipped(item: Item): boolean {
  if (!currentMascot.value || !currentMascot.value.equippedItem) return false;
  
  // 단순 문자열 비교로 아이템 장착 여부 확인
  return currentMascot.value.equippedItem.includes(item.name);
}

// 뒤로가기
function goBack() {
  // 백엔드와 실시간 동기화되므로 변경사항 확인 불필요
  // 바로 메인 페이지로 이동
  router.push('/mascot');
}

// 아이템 장착/해제 토글
async function toggleEquipItem(item: Item) {
  if (!currentMascot.value) return;
  
  try {
    const isCurrentlyEquipped = isEquipped(item);
    
    // 백엔드 API 호출
    const updatedMascot = await equipItems({
      equippedItem: isCurrentlyEquipped ? '' : item.name
    });
    
    // 마스코트 데이터 업데이트
    currentMascot.value = updatedMascot;
    
    console.log('백엔드에서 아이템 변경 완료:', updatedMascot);
    
    const message = isCurrentlyEquipped 
      ? `${item.name}을(를) 해제했습니다!`
      : `${item.name}을(를) 착용했습니다!`;
    
    showToastMessage(message);
  } catch (error) {
    console.error('아이템 장착/해제 실패:', error);
    
    // 에러 메시지 표시
    const errorMessage = handleApiError(error);
    showToastMessage(`아이템 변경 실패: ${errorMessage}`);
  }
}

// 마스코트 이미지 에러 처리
function handleMascotImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.src = '/mascot/soll.png'; // 기본 마스코트 이미지로 대체
  console.error('마스코트 이미지 로드 실패:', img.src);
}

// 아이템 이미지 에러 처리
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjRjNGNEY2Ii8+Cjx0ZXh0IHg9IjIwIiB5PSIyNCIgZmlsbD0iIzlDQTNBRiIgZm9udC1zaXplPSIyNCIgdGV4dC1hbmNob3I9Im1pZGRsZSI+📦PC90ZXh0Pgo8L3N2Zz4K';
}

// 토스트 메시지 표시
function showToastMessage(message: string) {
  toastMessage.value = message;
  showToast.value = true;
  
  setTimeout(() => {
    showToast.value = false;
  }, 2000);
}

// 마스코트 데이터 로드
async function loadMascotData() {
  try {
    console.log('백엔드에서 마스코트 데이터를 로드합니다...');
    
    const mascotData = await getMascot();
    if (mascotData) {
      currentMascot.value = mascotData;
      console.log('마스코트 데이터 로드됨:', mascotData);
    } else {
      console.error('마스코트 데이터를 찾을 수 없습니다.');
      // 마스코트가 없으면 메인 페이지로 이동
      router.push('/mascot');
    }
  } catch (error) {
    console.error('마스코트 데이터 로드 실패:', error);
    
    // 에러 메시지 표시
    const errorMessage = handleApiError(error);
    showToastMessage(`마스코트 로드 실패: ${errorMessage}`);
    
    // 에러 발생 시 메인 페이지로 이동
    setTimeout(() => {
      router.push('/mascot');
    }, 2000);
  }
}

// 컴포넌트 마운트
onMounted(() => {
  console.log('마스코트 꾸미기 페이지 로드됨');
  loadMascotData();
  console.log('사용 가능한 아이템들:', items.value);
});
</script>

<style scoped>
/* 라인 클램프 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 스크롤바 스타일링 */
.overflow-x-auto::-webkit-scrollbar {
  height: 4px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
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