<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-6">
      <!-- 헤더 -->
      <div class="flex items-center justify-between mb-4">
        <button @click="goBack" class="p-2 rounded-lg hover:bg-gray-100">
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 class="text-lg font-bold text-gray-800">{{ titleText }}</h1>
        <!-- 친구 홈에서는 코인/좋아요 숨김 -->
        <div class="w-8"></div>
      </div>

      <!-- 로딩/에러/콘텐츠 -->
      <div v-if="loading" class="py-16 text-center text-gray-500">불러오는 중...</div>
      <div v-else-if="error" class="py-16 text-center text-red-500">{{ error }}</div>

      <div v-else class="space-y-4">
        <!-- 마스코트 간단 표시 -->
        <div class="relative">
          <div
            ref="canvasEl"
            class="w-full h-72 rounded-xl shadow-lg relative overflow-hidden flex items-center justify-center"
            :style="roomBackgroundStyle"
          >
            <!-- 레이어 1: 배경 아이템 풀커버 -->
            <div class="absolute inset-0 z-0 overflow-hidden pointer-events-none">
              <img
                v-for="bg in backgroundEquippedItems"
                :key="bg.id"
                :src="bg.imageUrl"
                alt="배경 아이템"
                class="absolute inset-0 w-full h-full object-cover pointer-events-none"
              />
            </div>

            <!-- 레이어 2: 마스코트 -->
            <div class="absolute inset-0 z-10 flex items-center justify-center">
              <div class="relative animate-float">
                <img
                  ref="mascotEl"
                  :src="mascotImageUrl"
                  :alt="friendHome?.mascot?.name || 'mascot'"
                  class="w-44 h-44 object-contain"
                />
              </div>
            </div>

            <!-- 레이어 3: 전경 아이템 (간이 배치: 중앙 정렬, 약간씩 좌우 오프셋) -->
            <div class="absolute inset-0 z-20 pointer-events-none">
              <img
                v-for="(it, idx) in foregroundEquippedItems"
                :key="`${it.id}-${idx}`"
                :src="it.imageUrl"
                class="absolute object-contain"
                :style="styleForSimpleItem(idx)"
                alt="장착 아이템"
              />
            </div>

            <!-- 마스코트 이름 -->
            <div class="absolute top-3 left-3">
              <div class="bg-white bg-opacity-90 px-2 py-1 rounded-full">
                <span class="text-xs font-medium text-gray-800">{{ friendHome?.mascot?.name || '친구' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 레벨/좋아요 요약 (XP 바 없음) -->
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-2">
              <span class="text-xl">⭐</span>
              <span class="text-lg font-bold text-gray-800">Lv.{{ friendHome?.level || 1 }}</span>
            </div>
            <div class="flex items-center space-x-2">
              <img src="/icons/icon_like.png" alt="좋아요" class="w-5 h-5" />
              <span class="text-sm font-semibold text-gray-900">{{ friendHome?.likeCount ?? 0 }}</span>
            </div>
          </div>
          <p class="text-xs text-gray-500 mt-2">
            오늘 보낸: {{ friendHome?.likeSentToday ?? 0 }} · 받은: {{ friendHome?.likeReceivedToday ?? 0 }}
            · 가능: {{ friendHome?.likeRemainingToday ?? 0 }}
          </p>
        </div>

        <!-- 좋아요 버튼 (친구 홈 전용) -->
        <div class="flex">
          <button
            class="flex-1 py-3 rounded-xl font-semibold transition-colors"
            :class="friendHome?.canLikeNow ? 'bg-pink-500 text-white hover:bg-pink-600' : 'bg-gray-200 text-gray-500 cursor-not-allowed'"
            :disabled="!friendHome?.canLikeNow || likeSending"
            @click="handleLike"
          >
            {{ likeSending ? '전송 중...' : '좋아요 보내기' }}
          </button>
        </div>
      </div>

      <!-- 전역 토스트 사용으로 로컬 토스트 제거 -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getFriendHome, sendLike, type FriendHomeResponse } from '../api/friend';
import { useToastStore } from '../stores/toast';
import { mascotTypes } from '../data/mockData';
import { getShopItems } from '../api';
import type { ShopItem } from '../types/api';

const route = useRoute();
const router = useRouter();

const friendHome = ref<FriendHomeResponse | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const likeSending = ref(false);
const toastStore = useToastStore();
const shopItems = ref<ShopItem[]>([]);

const friendId = computed(() => {
  const raw = route.params.id as string | undefined;
  return raw ? parseInt(raw, 10) : NaN;
});

const titleText = computed(() => friendHome.value?.nickname ? `${friendHome.value.nickname}의 방` : '친구 홈');

function getMascotImageUrl(type?: string): string {
  const t = mascotTypes.find(v => v.id === type);
  return t?.imageUrl || '/mascot/soll.png';
}

const mascotImageUrl = computed(() => getMascotImageUrl(friendHome.value?.mascot?.type));

const roomBackgroundStyle = computed(() => {
  const bg = friendHome.value?.mascot?.backgroundId;
  if (bg) {
    return {
      backgroundImage: `url(/backgrounds/${bg})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat',
    } as Record<string, string>;
  }
  return { background: 'linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)' } as Record<string, string>;
});

// equippedItem 문자열에 포함된 아이템 추출 (이름 기반, 간이 매칭)
const equippedItemsByName = computed(() => {
  const eq = friendHome.value?.mascot?.equippedItem || '';
  if (!eq || !shopItems.value.length) return [] as ShopItem[];
  return shopItems.value.filter(item => eq.includes(item.name));
});

const backgroundEquippedItems = computed(() => equippedItemsByName.value.filter(i => String(i.type).toLowerCase() === 'background'));
const foregroundEquippedItems = computed(() => equippedItemsByName.value.filter(i => String(i.type).toLowerCase() !== 'background'));

async function fetchHome() {
  loading.value = true;
  error.value = null;
  try {
    if (!friendId.value || Number.isNaN(friendId.value)) throw new Error('잘못된 친구 ID');
    const [home, catalog] = await Promise.all([
      getFriendHome(friendId.value),
      getShopItems().catch(() => [] as ShopItem[])
    ]);
    friendHome.value = home;
    shopItems.value = catalog as any;
  } catch (e: any) {
    error.value = e?.message || '불러오기 실패';
  } finally {
    loading.value = false;
  }
}

async function handleLike() {
  if (!friendHome.value?.canLikeNow || likeSending.value) return;
  likeSending.value = true;
  try {
    await sendLike(friendHome.value.ownerId);
    await fetchHome();
  } catch (e: any) {
    const msg = e?.body?.message || e?.message || '좋아요 전송 실패';
    toastStore.show(msg, 'error');
  } finally {
    likeSending.value = false;
  }
}

function goBack() {
  router.back();
}

onMounted(fetchHome);

// 간이 전경 아이템 배치: 중앙 기준으로 좌우로 퍼뜨림
const canvasEl = ref<HTMLElement>();
const mascotEl = ref<HTMLElement>();
function styleForSimpleItem(idx: number) {
  // 기준 박스 크기와 위치
  const baseSize = 64; // px
  const centerX = '50%';
  const centerY = '50%';
  const offset = (idx - Math.floor(foregroundEquippedItems.value.length / 2)) * 28; // 좌우 간격
  return {
    left: `calc(${centerX} + ${offset}px)`,
    top: centerY,
    width: `${baseSize}px`,
    height: `${baseSize}px`,
    transform: 'translate(-50%, -50%)',
    pointerEvents: 'none',
  } as Record<string, string>;
}
</script>

<style scoped>
.animate-float {
  animation: float 3s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}
</style>
