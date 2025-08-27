<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 헤더 -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="flex items-center justify-between px-4 py-3">
        <button @click="goBack" class="p-2 rounded-lg hover:bg-gray-100 transition-colors">
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 class="text-xl font-bold text-gray-800">보관함</h1>
        <div class="w-8"></div>
      </div>
    </div>

    <!-- 컨텐츠 -->
    <div class="p-4 flex-1 overflow-y-auto">
      <div v-if="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>
      <div v-else-if="error" class="text-center py-8 text-red-500">{{ error }}</div>

      <div v-else>
        <div v-if="gifticons.length > 0" class="grid grid-cols-2 gap-4">
          <div v-for="g in gifticons" :key="g.id" class="bg-white border border-gray-200 rounded-xl p-3 hover:shadow cursor-pointer" @click="openDetail(g.id)">
            <div class="w-full h-24 bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden mb-2">
              <img :src="g.imageUrl" :alt="g.name" class="w-full h-full object-contain" @error="onImgError" />
            </div>
            <div class="flex items-center justify-between">
              <div>
                <div class="text-sm font-semibold text-gray-800 line-clamp-1">{{ g.name }}</div>
                <div class="text-xs text-gray-500" v-if="g.expiresAt">만료: {{ formatDate(g.expiresAt) }}</div>
              </div>
              <span class="text-xs px-2 py-1 rounded-full"
                    :class="statusClass(g.status)">{{ statusLabel(g.status) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-12">
          <div class="text-6xl mb-3">📦</div>
          <div class="text-gray-600">보관함에 기프티콘이 없어요</div>
          <div class="text-sm text-gray-400">샵에서 기프티콘을 구매해보세요</div>
        </div>
      </div>
    </div>

    <!-- 상세 모달 -->
    <GifticonDetailModal
      :open="detailOpen"
      :gifticon="detail"
      @close="detailOpen = false"
      @redeemed="handleRedeemed"
    />
  </div>
  
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getPurchasedGifticons, getPurchasedGifticonDetail } from '../api/index';
import type { PurchasedGifticon, PurchasedGifticonDetail } from '../types/api';
import GifticonDetailModal from '../components/gifticon/GifticonDetailModal.vue';

const router = useRouter();
const loading = ref(true);
const error = ref('');
const gifticons = ref<PurchasedGifticon[]>([]);
const detailOpen = ref(false);
const detail = ref<PurchasedGifticonDetail | null>(null);

function goBack() { router.back(); }
function onImgError(e: Event) { (e.target as HTMLImageElement).src = '/items/gifticon_default.png'; }

function statusLabel(s: PurchasedGifticon['status']): string {
  return s === 'ACTIVE' ? '사용가능' : s === 'REDEEMED' ? '사용됨' : '만료';
}
function statusClass(s: PurchasedGifticon['status']) {
  return s === 'ACTIVE' ? 'bg-green-100 text-green-700' : s === 'REDEEMED' ? 'bg-gray-100 text-gray-600' : 'bg-red-100 text-red-600';
}
function formatDate(iso?: string) { return iso ? new Date(iso).toLocaleDateString() : ''; }

async function loadList() {
  loading.value = true; error.value = '';
  try { gifticons.value = await getPurchasedGifticons(); }
  catch (e) { console.error(e); error.value = '목록을 불러오지 못했습니다.'; }
  finally { loading.value = false; }
}

async function openDetail(id: number) {
  try {
    detail.value = await getPurchasedGifticonDetail(id);
    detailOpen.value = true;
  } catch (e) {
    alert('상세 정보를 불러오지 못했습니다.');
  }
}

function handleRedeemed() {
  detailOpen.value = false;
  loadList();
}

onMounted(loadList);
</script>

<style scoped>
.line-clamp-1 { display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
</style>

