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
                  @load="updateRects"
                />
              </div>
            </div>

            <!-- 레이어 3: 전경 아이템 (간이 배치: 중앙 정렬, 약간씩 좌우 오프셋) -->
            <div class="absolute inset-0 z-20 pointer-events-none">
              <img
                v-for="it in foregroundEquippedItems"
                :key="it.key"
                :src="it.imageUrl"
                class="absolute object-contain pointer-events-none"
                :style="styleForItem(it)"
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
import { onMounted, ref, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getFriendHome, sendLike, type FriendHomeResponse } from '../api/friend';
import { useToastStore } from '../stores/toast';
import { mascotTypes } from '../data/mockData';
import { getShopItems } from '../api';
import type { ShopItem } from '../types/api';
import { toAbsoluteFromMascot } from '../utils/coordinates';
import { CSSProperties } from 'vue';

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
  // backgroundColor와 backgroundPattern을 우선적으로 확인
  const color = friendHome.value?.mascot?.backgroundColor;
  const pattern = friendHome.value?.mascot?.backgroundPattern;
  
  // 커스터마이징된 배경이 있으면 해당 스타일 적용
  if (color || pattern) {
    const style: Record<string, string> = { backgroundColor: color || '#f5f7ff' };
    
    if (pattern === 'dots') {
      style.backgroundImage = 'radial-gradient(circle, rgba(0,0,0,0.10) 1px, transparent 1px)';
      style.backgroundSize = '12px 12px';
    } else if (pattern === 'stripes') {
      style.backgroundImage = 'repeating-linear-gradient(45deg, rgba(0,0,0,0.06) 0 10px, transparent 10px 20px)';
    }
    
    return style;
  }
  
  // 커스터마이징된 배경이 없으면 backgroundId 사용
  const bg = friendHome.value?.mascot?.backgroundId;
  if (bg) {
    return {
      backgroundImage: `url(/backgrounds/${bg})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat',
    } as Record<string, string>;
  }
  
  // 아무것도 없으면 기본 그라데이션 사용
  return { background: 'linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)' } as Record<string, string>;
});

// equippedLayout JSON을 파싱해서 equippedItems 배열 추출
const equippedItemsFromLayout = computed(() => {
  if (!friendHome.value?.mascot?.equippedLayout) {
    console.log('❌ equippedLayout이 없음');
    return [];
  }

  try {
    // equippedLayout JSON을 파싱
    console.log('🔍 JSON 파싱 시작:', friendHome.value.mascot.equippedLayout);
    const equippedLayoutData = JSON.parse(friendHome.value.mascot.equippedLayout);
    console.log('🔍 파싱된 equippedLayoutData:', equippedLayoutData);
    
    // equippedLayoutData.equippedItems 배열 추출
    const equippedItems = equippedLayoutData?.equippedItems;
    console.log('🔍 추출된 equippedItems:', equippedItems);
    
    if (!equippedItems || !Array.isArray(equippedItems)) {
      console.log('❌ equippedItems가 배열이 아님');
      return [];
    }

    return equippedItems;
  } catch (error) {
    console.error('❌ equippedLayout 파싱 오류:', error);
    return [];
  }
});

const backgroundEquippedItems = computed(() => {
  if (!equippedItemsFromLayout.value.length || !shopItems.value.length) {
    return [];
  }

  // 배경 아이템만 필터링 (itemId === 31)
  const backgroundItems = equippedItemsFromLayout.value.filter(item => item.itemId === 31);
  console.log('🔍 배경 아이템 31 필터링:', backgroundItems);

  // shopItems에서 실제 아이템 정보 찾기
  return backgroundItems.map(item => {
    const shopItem = shopItems.value.find(shopItem => shopItem.id === item.itemId);
    console.log('🔍 배경 아이템 31 변환 결과:', { 
      item, 
      shopItem,
      shopItemId: shopItem?.id,
      shopItemImageUrl: shopItem?.imageUrl,
      shopItemName: shopItem?.name
    });
    
    return {
      key: `${item.itemId}-bg`,
      ...item,
      imageUrl: shopItem?.imageUrl || '/items/item_placeholder.png',
      name: shopItem?.name || 'Unknown Item'
    };
  });
});

const foregroundEquippedItems = computed(() => {
  if (!equippedItemsFromLayout.value.length || !shopItems.value.length) {
    return [];
  }

  // 전경 아이템만 필터링 (itemId !== 31)
  const foregroundItems = equippedItemsFromLayout.value.filter(item => item.itemId !== 31);
  console.log('🔍 전경 아이템 필터링:', foregroundItems);

  // shopItems에서 실제 아이템 정보 찾기
  return foregroundItems.map(item => {
    const shopItem = shopItems.value.find(shopItem => shopItem.id === item.itemId);
    console.log('🔍 전경 아이템 변환 결과:', { 
      item, 
      shopItem,
      shopItemId: shopItem?.id,
      shopItemImageUrl: shopItem?.imageUrl,
      shopItemName: shopItem?.name
    });
    
    return {
      key: `${item.itemId}-fg`,
      ...item,
      imageUrl: shopItem?.imageUrl || '/items/item_placeholder.png',
      name: shopItem?.name || 'Unknown Item'
    };
  });
});

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
    
    // 데이터 로드 완료 후 rect 정보 업데이트
    nextTick(() => {
      updateRects();
    });
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

// 정확한 좌표, 스케일, 회전 정보를 이용한 아이템 렌더링
const canvasEl = ref<HTMLElement>();
const mascotEl = ref<HTMLElement>();
const canvasRect = ref<DOMRect | null>(null);
const mascotRect = ref<DOMRect | null>(null);

function updateRects() {
  if (canvasEl.value) canvasRect.value = canvasEl.value.getBoundingClientRect();
  if (mascotEl.value) mascotRect.value = mascotEl.value.getBoundingClientRect();
}

const BASE_ITEM_SIZE = 120;
function styleForItem(e: any): CSSProperties {
  if (!canvasRect.value || !mascotRect.value) return {};
  const center = toAbsoluteFromMascot(e.relativePosition, mascotRect.value);
  const left = center.x - canvasRect.value.left;
  const top = center.y - canvasRect.value.top;
  const size = Math.max(12, BASE_ITEM_SIZE * (e.scale ?? 1));
  return { 
    position: 'absolute', 
    left: `${left}px`, 
    top: `${top}px`, 
    width: `${size}px`, 
    height: `${size}px`, 
    transform: `translate(-50%, -50%) rotate(${e.rotation || 0}deg)` 
  };
}

onMounted(() => {
  fetchHome();
  // 컴포넌트 마운트 후 rect 정보 업데이트
  nextTick(() => {
    updateRects();
  });
});
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
