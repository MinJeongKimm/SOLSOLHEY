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
          @click="selectedCategory = 'head'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'head' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          Head
        </button>
        <button 
          @click="selectedCategory = 'clothing'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'clothing' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          Clothing
        </button>
        <button 
          @click="selectedCategory = 'accessory'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'accessory' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          Accessory
        </button>
        <button 
          @click="selectedCategory = 'background'"
          :class="[
            'px-3 py-2 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
            selectedCategory === 'background' 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          Background
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
        @click="loadItems"
        class="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
      >
        다시 시도
      </button>
    </div>

    <!-- 아이템 목록 -->
    <div v-else-if="filteredItems.length > 0" class="grid grid-cols-2 gap-4">
      <div 
        v-for="item in filteredItems" 
        :key="item.id"
        class="relative border-2 border-gray-200 rounded-xl p-4 transition-all cursor-pointer hover:border-gray-300 hover:shadow-md"
        @click="handleItemClick(item)"
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
          <h4 class="font-medium text-sm text-gray-800 mb-1">
            {{ item.name }}
          </h4>
          <p class="text-xs text-gray-500 mb-2 line-clamp-2">{{ item.description }}</p>
          
          <!-- 가격 및 상태 -->
          <div class="text-center">
            <div class="text-lg font-bold text-blue-600 mb-2">
              {{ item.price }}P
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
      <div class="text-6xl mb-4">🛍️</div>
      <p class="text-gray-500 mb-2">해당 카테고리에 아이템이 없습니다</p>
      <p class="text-sm text-gray-400">다른 카테고리를 선택해보세요!</p>
    </div>

    <!-- 구매 다이얼로그 -->
    <PurchaseDialog
      v-if="selectedItem"
      :is-open="showPurchaseDialog"
      :item="selectedItem"
      :user-points="currentUserPoints"
      :is-gifticon="false"
      @close="closePurchaseDialog"
      @purchase="handlePurchase"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getShopItems, createOrder } from '../../api/index';
import type { ShopItem } from '../../types/api';
import PurchaseDialog from './PurchaseDialog.vue';

// Props
interface Props {
  userPoints?: number;
}

const props = defineProps<Props>();

// userPoints의 기본값 설정
const currentUserPoints = computed(() => props.userPoints ?? 0);

// Emits
const emit = defineEmits<{
  'points-updated': [];
}>();

// 반응형 데이터
const items = ref<ShopItem[]>([]);
const loading = ref(false);
const error = ref('');
const selectedCategory = ref<'all' | 'head' | 'clothing' | 'accessory' | 'background'>('all');
const sortOrder = ref<'default' | 'price-low' | 'price-high'>('default');

// 구매 다이얼로그 관련
const showPurchaseDialog = ref(false);
const selectedItem = ref<ShopItem | null>(null);

// 아이템 타입 매핑 (프론트엔드 → 백엔드)
const itemTypeMapping: Record<string, string> = {
  'head': 'EQUIP',
  'clothing': 'EQUIP',
  'accessory': 'EQUIP',
  'background': 'BACKGROUND'
};

// 카테고리별 아이템 필터링
const filteredItems = computed(() => {
  // items.value가 배열인지 확인하고, 아니면 빈 배열 반환
  if (!Array.isArray(items.value)) {
    return [];
  }
  
  let filtered = [...items.value];  // 배열 복사
  
  // 카테고리 필터링
  if (selectedCategory.value !== 'all') {
    const backendType = itemTypeMapping[selectedCategory.value];
    filtered = filtered.filter(item => item.type === backendType);
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
    'all': '전체 아이템',
    'head': 'Head 아이템',
    'clothing': 'Clothing 아이템',
    'accessory': 'Accessory 아이템',
    'background': 'Background 아이템'
  };
  return categoryNames[category] || '아이템';
}

// 아이템 보유 여부 확인 (임시로 항상 false 반환)
function isOwned(item: ShopItem): boolean {
  return false; // 보유여부 표시 기능 비활성화
}

// 아이템 클릭 처리
function handleItemClick(item: ShopItem) {
  selectedItem.value = item;
  showPurchaseDialog.value = true;
}

// 구매 다이얼로그 닫기
function closePurchaseDialog() {
  showPurchaseDialog.value = false;
  selectedItem.value = null;
}

// 구매 처리
async function handlePurchase(item: ShopItem, quantity: number) {
  try {
    const orderData = {
      type: 'ITEM' as const,
      itemId: item.id,
      quantity: quantity
    };
    
    await createOrder(orderData);
    
    // 구매 성공 후 처리
    closePurchaseDialog();
    
    // 아이템 목록 새로고침
    await loadItems();
    
    // 포인트 업데이트 (부모 컴포넌트에서 처리)
    emit('points-updated');
    
  } catch (err: any) {
    console.error('아이템 구매 실패:', err);
    alert('구매에 실패했습니다. 다시 시도해주세요.');
  }
}

// 이미지 로드 에러 처리
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  img.src = '/items/item_default.png'; // 기본 이미지
}

// 아이템 목록 로드
async function loadItems() {
  loading.value = true;
  error.value = '';
  
  try {
    const itemsData = await getShopItems();
    items.value = itemsData;
  } catch (err: any) {
    console.error('아이템 목록 로드 실패:', err);
    error.value = '아이템 목록을 불러오는데 실패했습니다.';
  } finally {
    loading.value = false;
  }
}

// 컴포넌트 마운트
onMounted(async () => {
  await loadItems();
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
