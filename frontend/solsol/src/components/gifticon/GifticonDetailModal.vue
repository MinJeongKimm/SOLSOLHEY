<template>
  <div v-if="open && gifticon" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/50" @click="$emit('close')"></div>
    <div class="relative bg-white rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl">
      <div class="flex items-start justify-between mb-4">
        <h3 class="text-lg font-bold text-gray-800">기프티콘 상세</h3>
        <button class="p-2 rounded-md hover:bg-gray-100" @click="$emit('close')">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      <div class="space-y-4">
        <div class="w-full h-40 bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden">
          <img :src="gifticon.imageUrl" :alt="gifticon.name" class="w-full h-full object-contain" @error="onImgError" />
        </div>
        <div>
          <div class="text-base font-semibold text-gray-800">{{ gifticon.name }}</div>
          <div class="text-sm text-gray-500">SKU: {{ gifticon.sku }}</div>
          <div class="text-sm text-gray-500" v-if="gifticon.expiresAt">만료일: {{ formatDate(gifticon.expiresAt) }}</div>
        </div>

        <!-- 바코드 영역: 간단한 스타일로 대체 표시 -->
        <div class="p-4 bg-gray-50 rounded-lg text-center">
          <div class="text-xs text-gray-500 mb-1">바코드</div>
          <div class="text-2xl font-mono tracking-widest">{{ spacedCode }}</div>
        </div>

        <div class="flex space-x-3 pt-2">
          <button class="flex-1 py-3 rounded-lg border border-gray-300" @click="$emit('close')">닫기</button>
          <button class="flex-1 py-3 rounded-lg text-white"
                  :class="canRedeem ? 'bg-blue-600 hover:bg-blue-700' : 'bg-gray-400 cursor-not-allowed'"
                  :disabled="!canRedeem || loading"
                  @click="redeem">
            <span v-if="loading">처리 중...</span>
            <span v-else>{{ redeemLabel }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { redeemPurchasedGifticon } from '../../api/index';
import type { PurchasedGifticonDetail } from '../../types/api';

const props = defineProps<{ open: boolean; gifticon: PurchasedGifticonDetail | null }>();
const emit = defineEmits<{ close: []; redeemed: [] }>();
const loading = ref(false);

const spacedCode = computed(() => (props.gifticon?.barcode || '').replace(/(.{4})/g, '$1 ').trim());
const canRedeem = computed(() => props.gifticon && props.gifticon.status === 'ACTIVE');
const redeemLabel = computed(() => {
  if (!props.gifticon) return '사용하기';
  if (props.gifticon.status === 'EXPIRED') return '만료됨';
  if (props.gifticon.status === 'REDEEMED') return '사용됨';
  return '사용하기';
});

function formatDate(iso?: string) {
  if (!iso) return '';
  // 우선 안전하게 날짜 부분만 슬라이스
  if (iso.length >= 19) {
    // 2025-08-27T12:34:56 => 2025-08-27 12:34:56
    return iso.slice(0, 10) + ' ' + iso.slice(11, 19);
  }
  const d = new Date(iso);
  return isNaN(d.getTime()) ? '' : d.toLocaleString();
}
function onImgError(e: Event) { (e.target as HTMLImageElement).src = '/items/gifticon_default.png'; }

async function redeem() {
  if (!props.gifticon || !canRedeem.value) return;
  loading.value = true;
  try {
    await redeemPurchasedGifticon(props.gifticon.id);
    alert('사용 처리되었습니다.');
    emit('redeemed');
  } catch (e) {
    alert('사용 처리에 실패했습니다.');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped></style>
