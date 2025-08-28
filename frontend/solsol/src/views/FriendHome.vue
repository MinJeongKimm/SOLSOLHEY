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
          <div class="w-full h-72 rounded-xl shadow-lg relative overflow-hidden flex items-center justify-center"
               :style="roomBackgroundStyle">
            <div class="relative animate-float">
              <img :src="mascotImageUrl" :alt="friendHome?.mascot?.name || 'mascot'" class="w-44 h-44 object-contain" />
            </div>
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

const route = useRoute();
const router = useRouter();

const friendHome = ref<FriendHomeResponse | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const likeSending = ref(false);
const toastStore = useToastStore();

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
  // 간단한 배경 처리(실제 배경 이미지는 프로젝트 자산 정책에 맞춰 확장 가능)
  return { background: 'linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)' } as Record<string, string>;
});

async function fetchHome() {
  loading.value = true;
  error.value = null;
  try {
    if (!friendId.value || Number.isNaN(friendId.value)) throw new Error('잘못된 친구 ID');
    friendHome.value = await getFriendHome(friendId.value);
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
