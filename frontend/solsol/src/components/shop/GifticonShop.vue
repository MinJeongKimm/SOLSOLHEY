<template>
  <div class="space-y-6">
    <!-- 카테고리 필터 -->
    <div class="overflow-x-auto">
      <div class="flex gap-2 pb-2 min-w-max">
        <button 
          @click="selectedCategory = 'all'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'all' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          전체
        </button>
        <button 
          @click="selectedCategory = 'cafe'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'cafe' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          카페
        </button>
        <button 
          @click="selectedCategory = 'makeup'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'makeup' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          메이크업
        </button>
        <button 
          @click="selectedCategory = 'living'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'living' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          리빙
        </button>
        <button 
          @click="selectedCategory = 'food'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'food' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          푸드
        </button>
      </div>
    </div>

    <!-- 정렬 옵션 -->
    <div class="flex justify-between items-center">
      <h3 class="text-lg font-bold text-gray-800">
        {{ getCategoryDisplayName(selectedCategory) }}
      </h3>
      <div class="flex items-center">
        <select 
          v-model="sortOrder" 
          class="text-sm border border-gray-300 rounded-md px-2 py-1"
        >
          <option value="default">기본</option>
          <option value="price-low">가격 낮은순</option>
          <option value="price-high">가격 높은순</option>
        </select>
      </div>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="flex justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="text-center py-8">
      <p class="text-red-500 mb-4">{{ error }}</p>
      <button 
        @click="loadGifticons"
        class="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
      >
        다시 시도
      </button>
    </div>

    <!-- 기프티콘 목록 -->
    <div v-else-if="filteredGifticons.length > 0" class="grid grid-cols-2 gap-4">
      <div 
        v-for="gifticon in filteredGifticons" 
        :key="gifticon.id"
        class="relative border-2 border-gray-200 rounded-xl p-4 transition-all hover:border-gray-300 hover:shadow-md cursor-pointer"
        @click="handleGifticonClick(gifticon)"
      >
        <!-- 기프티콘 이미지 -->
        <div class="w-full h-20 bg-gray-100 rounded-lg mb-3 flex items-center justify-center overflow-hidden">
          <img 
            :src="gifticon.imageUrl" 
            :alt="gifticon.name"
            class="w-full h-full object-contain"
            @error="handleImageError"
          />
        </div>

        <!-- 기프티콘 정보 -->
        <div>
          <h4 class="font-medium text-sm text-gray-800 mb-1">{{ gifticon.name }}</h4>
          <p class="text-xs text-gray-500 mb-2 line-clamp-2">{{ gifticon.description }}</p>
          
          <!-- 가격 및 구매 버튼 -->
          <div class="text-center">
            <div class="text-lg font-bold text-blue-600 mb-2">
              {{ gifticon.price }}P
            </div>
            <span class="text-xs font-medium px-3 py-1 rounded-full bg-blue-500 text-white hover:bg-blue-600">
              구매하기
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 빈 상태 -->
    <div v-else class="text-center py-8">
      <div class="text-6xl mb-4">🎁</div>
      <p class="text-gray-500 mb-2">해당 카테고리에 기프티콘이 없습니다</p>
      <p class="text-sm text-gray-400">다른 카테고리를 선택해보세요!</p>
    </div>

    <!-- 구매 다이얼로그 -->
    <PurchaseDialog
      v-if="selectedGifticon"
      :is-open="showPurchaseDialog"
      :item="selectedGifticon"
      :user-points="props.userPoints"
      :is-gifticon="true"
      @close="closePurchaseDialog"
      @purchase="handlePurchase"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getGifticons, createOrder } from '../../api/index';
import type { Gifticon } from '../../types/api';
import PurchaseDialog from './PurchaseDialog.vue';

// Props
interface Props {
  userPoints: number;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  'points-updated': [];
}>();

// 반응형 데이터
const gifticons = ref<Gifticon[]>([]);
const loading = ref(false);
const error = ref('');
const selectedCategory = ref<'all' | 'cafe' | 'makeup' | 'living' | 'food'>('all');
const sortOrder = ref<'default' | 'price-low' | 'price-high'>('default');

// 구매 다이얼로그 관련
const showPurchaseDialog = ref(false);
const selectedGifticon = ref<Gifticon | null>(null);

// 카테고리별 기프티콘 필터링
const filteredGifticons = computed(() => {
  // gifticons.value가 배열인지 확인하고, 아니면 빈 배열 반환
  if (!Array.isArray(gifticons.value)) {
    return [];
  }
  
  let filtered = [...gifticons.value];  // 배열 복사
  
  // 카테고리 필터링 (임시로 SKU 기반으로 필터링)
  if (selectedCategory.value !== 'all') {
    filtered = filtered.filter(gifticon => {
      const sku = gifticon.sku.toLowerCase();
      switch (selectedCategory.value) {
        case 'cafe':
          return sku.includes('cafe') || sku.includes('coffee') || sku.includes('starbucks');
        case 'makeup':
          return sku.includes('makeup') || sku.includes('cosmetic') || sku.includes('beauty');
        case 'living':
          return sku.includes('living') || sku.includes('home') || sku.includes('furniture');
        case 'food':
          return sku.includes('food') || sku.includes('restaurant') || sku.includes('delivery');
        default:
          return true;
      }
    });
  }
  
  // 정렬
  if (sortOrder.value === 'price-low') {
    filtered = filtered.sort((a, b) => a.price - b.price);
  } else if (sortOrder.value === 'price-high') {
    filtered = filtered.sort((a, b) => b.price - a.price);
  }
  
  return filtered;
});

// 카테고리 표시명
function getCategoryDisplayName(category: string): string {
  const categoryNames = {
    'all': '전체 기프티콘',
    'cafe': '카페 기프티콘',
    'makeup': '메이크업 기프티콘',
    'living': '리빙 기프티콘',
    'food': '푸드 기프티콘'
  };
  return categoryNames[category] || '기프티콘';
}

// 기프티콘 클릭 처리
function handleGifticonClick(gifticon: Gifticon) {
  selectedGifticon.value = gifticon;
  showPurchaseDialog.value = true;
}

// 구매 다이얼로그 닫기
function closePurchaseDialog() {
  showPurchaseDialog.value = false;
  selectedGifticon.value = null;
}

// 구매 처리
async function handlePurchase(gifticon: Gifticon, quantity: number) {
  try {
    const orderData = {
      type: 'GIFTICON' as const,
      sku: gifticon.sku,
      quantity: quantity
    };
    
    await createOrder(orderData);
    
    // 구매 성공 후 처리
    closePurchaseDialog();
    
    // 포인트 업데이트 (부모 컴포넌트에서 처리)
    emit('points-updated');
    
  } catch (err: any) {
    console.error('기프티콘 구매 실패:', err);
    alert('구매에 실패했습니다. 다시 시도해주세요.');
  }
}

// 이미지 로드 에러 처리
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.src = '/items/gifticon_default.png'; // 기본 이미지
}

// 기프티콘 목록 로드
async function loadGifticons() {
  loading.value = true;
  error.value = '';
  
  try {
    const gifticonsData = await getGifticons();
    gifticons.value = gifticonsData;
  } catch (err: any) {
    console.error('기프티콘 목록 로드 실패:', err);
    error.value = '기프티콘 목록을 불러오는데 실패했습니다.';
  } finally {
    loading.value = false;
  }
}

// 컴포넌트 마운트
onMounted(async () => {
  await loadGifticons();
});
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
