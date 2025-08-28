<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center">
    <!-- 배경 오버레이 -->
    <div 
      class="absolute inset-0 bg-black bg-opacity-50"
      @click="handleClose"
    ></div>

    <!-- 다이얼로그 -->
    <div class="relative bg-white rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl">
      <!-- 헤더 -->
      <div class="text-center mb-6">
        <h3 class="text-xl font-bold text-gray-800 mb-2">
          {{ isGifticon ? '기프티콘 구매' : '아이템 구매' }}
        </h3>
        <p class="text-sm text-gray-600">
          구매 전 내용을 확인해주세요
        </p>
      </div>

      <!-- 구매 정보 -->
      <div class="space-y-4 mb-6">
        <!-- 아이템/기프티콘 정보 -->
        <div class="flex items-center space-x-4 p-4 bg-gray-50 rounded-lg">
          <div class="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden">
            <img 
              :src="item.imageUrl" 
              :alt="item.name"
              class="w-full h-full object-contain"
              :data-sku="(item as any).sku || ''"
              @error="handleImageError"
            />
          </div>
          <div class="flex-1">
            <h4 class="font-medium text-gray-800">{{ item.name }}</h4>
            <p class="text-sm text-gray-500 line-clamp-2">{{ item.description }}</p>
          </div>
        </div>

        <!-- 가격 정보 -->
        <div class="bg-blue-50 p-4 rounded-lg">
          <div class="flex justify-between items-center">
            <span class="text-gray-700">가격:</span>
            <span class="text-xl font-bold text-blue-600">{{ item.price }}P</span>
          </div>
          
          <!-- 수량 선택 (기프티콘인 경우) -->
          <div v-if="isGifticon" class="mt-3 flex items-center justify-between">
            <span class="text-gray-700">수량:</span>
            <div class="flex items-center space-x-2">
              <button 
                @click="decreaseQuantity"
                :disabled="quantity <= 1"
                class="w-8 h-8 rounded-full bg-white border border-gray-300 flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span class="text-gray-600">-</span>
              </button>
              <span class="w-12 text-center font-medium">{{ quantity }}</span>
              <button 
                @click="increaseQuantity"
                class="w-8 h-8 rounded-full bg-white border border-gray-300 flex items-center justify-center"
              >
                <span class="text-gray-600">+</span>
              </button>
            </div>
          </div>

          <!-- 총 가격 -->
          <div v-if="isGifticon" class="mt-3 pt-3 border-t border-blue-200">
            <div class="flex justify-between items-center">
              <span class="text-gray-700 font-medium">총 가격:</span>
              <span class="text-xl font-bold text-blue-600">{{ totalPrice }}P</span>
            </div>
          </div>
        </div>

        <!-- 포인트 잔액 -->
        <div class="bg-yellow-50 p-4 rounded-lg">
          <div class="flex justify-between items-center">
            <span class="text-gray-700">보유 포인트:</span>
            <span class="font-medium text-gray-800">{{ userPoints }}P</span>
          </div>
          
          <!-- 포인트 부족 경고 -->
          <div v-if="isInsufficientPoints" class="mt-2 text-red-600 text-sm font-medium">
            ⚠️ 포인트가 부족합니다! (필요: {{ totalPrice }}P, 보유: {{ userPoints }}P)
          </div>
          
          <!-- 구매 후 잔액 -->
          <div v-else class="mt-2 text-sm text-gray-600">
            구매 후 잔액: <span class="font-medium">{{ remainingPoints }}P</span>
          </div>
        </div>
      </div>

      <!-- 액션 버튼 -->
      <div class="flex space-x-3">
        <button 
          @click="handleClose"
          class="flex-1 py-3 px-4 border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors"
        >
          취소
        </button>
        <button 
          @click="handlePurchase"
          :disabled="isInsufficientPoints || loading"
          :class="[
            'flex-1 py-3 px-4 rounded-lg font-medium transition-colors',
            isInsufficientPoints || loading
              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
              : 'bg-blue-500 text-white hover:bg-blue-600'
          ]"
        >
          <span v-if="loading" class="flex items-center justify-center">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
            구매 중...
          </span>
          <span v-else>구매하기</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { Gifticon, ShopItem } from '../../types/api';

// Props
interface Props {
  isOpen: boolean;
  item: ShopItem | Gifticon;
  userPoints: number;
  isGifticon?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isGifticon: false
});

// Emits
const emit = defineEmits<{
  close: [];
  purchase: [item: ShopItem | Gifticon, quantity: number];
}>();

// 반응형 데이터
const quantity = ref(1);
const loading = ref(false);

// 계산된 속성
const totalPrice = computed(() => {
  if (props.isGifticon) {
    return props.item.price * quantity.value;
  }
  return props.item.price;
});

const isInsufficientPoints = computed(() => {
  return totalPrice.value > props.userPoints;
});

const remainingPoints = computed(() => {
  return props.userPoints - totalPrice.value;
});

// 수량 조절
function increaseQuantity() {
  quantity.value++;
}

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--;
  }
}

// 이미지 로드 에러 처리
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement;
  if (props.isGifticon) {
    const sku = (img.dataset.sku || '').toUpperCase();
    switch (sku) {
      case 'STARBUCKS_AMERICANO':
        img.src = '/gifticons/originals/gifticon_iced_americano.png';
        break;
      case 'STARBUCKS_LATTE':
        img.src = '/gifticons/originals/gifticon_iced_vanilla_latte.png';
        break;
      case 'BASKIN_ICE_CREAM':
        img.src = '/gifticons/originals/gifticon_icecream.png';
        break;
      case 'GONGCHA_BROWN_SUGAR':
        img.src = '/gifticons/originals/gifticon_gongcha_brownsugar_milktea.png';
        break;
      default:
        img.src = '/gifticons/thumbnails/gift_coffee.png';
    }
  } else {
    // 일반 아이템의 경우 기본 아이템 이미지가 없으면 그냥 로고성 자산으로 대체하지 않고 유지
    img.src = '/items/item_head_blue_cap.png';
  }
}

// 다이얼로그 닫기
function handleClose() {
  emit('close');
}

// 구매 처리
async function handlePurchase() {
  if (isInsufficientPoints.value || loading.value) {
    return;
  }

  loading.value = true;
  
  try {
    emit('purchase', props.item, quantity.value);
  } catch (error) {
    console.error('구매 처리 실패:', error);
  } finally {
    loading.value = false;
  }
}

// 다이얼로그가 열릴 때마다 수량 초기화
watch(() => props.isOpen, (newValue) => {
  if (newValue) {
    quantity.value = 1;
  }
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
