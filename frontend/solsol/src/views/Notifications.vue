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
        <h1 class="text-lg font-bold text-gray-800">알림</h1>
        <div class="w-8"></div>
      </div>

      <!-- 요약 -->
      <div class="flex items-center justify-between mb-3 text-sm text-gray-600">
        <div>미읽음: <span class="font-semibold">{{ unread }}</span></div>
        <div class="flex items-center space-x-2">
          <button 
            class="px-2 py-0.5 rounded bg-blue-100 text-blue-700 text-xs hover:bg-blue-200 disabled:opacity-50"
            @click="markAllRead"
            :disabled="processingAll || loading"
          >모두 읽음</button>
          <button 
            class="px-2 py-0.5 rounded bg-gray-100 text-gray-700 text-xs hover:bg-gray-200 disabled:opacity-50"
            @click="clearInbox"
            :disabled="processingAll || loading"
          >알림함 비우기</button>
          <span class="px-2 py-0.5 rounded-full bg-pink-100 text-pink-700 text-xs">좋아요만 표시</span>
        </div>
      </div>

      <!-- 목록 영역 -->
      <div class="divide-y divide-gray-100">
        <div v-for="it in visibleLikeOnly" :key="it.interactionId" class="py-3 flex items-start justify-between">
          <div>
            <div class="text-sm text-gray-800">
              <span class="font-semibold">{{ it.fromUserNickname }}</span>
              <span class="ml-1">님이 좋아요를 보냈습니다.</span>
            </div>
            <div class="text-xs text-gray-500 mt-1">{{ formatDate(it.createdAt) }}</div>
          </div>
          <div class="ml-3">
            <button
              v-if="!it.isRead"
              @click="markRead(it)"
              class="px-3 py-1 rounded-lg bg-blue-500 text-white text-xs hover:bg-blue-600"
              :disabled="readingId === it.interactionId"
            >
              {{ readingId === it.interactionId ? '처리 중' : '읽음' }}
            </button>
            <span v-else class="text-xs text-gray-400">읽음</span>
          </div>
        </div>
      </div>

      <!-- 페이징 -->
      <div class="flex items-center justify-between mt-4 text-sm">
        <button 
          class="px-3 py-1 rounded-lg bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
          :disabled="page === 0 || loading"
          @click="prevPage"
        >이전</button>
        <div class="text-gray-600">{{ page + 1 }} / {{ totalPages }}</div>
        <button 
          class="px-3 py-1 rounded-lg bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
          :disabled="page + 1 >= totalPages || loading"
          @click="nextPage"
        >다음</button>
      </div>

      <!-- 상태 -->
      <div v-if="loading" class="text-center text-gray-500 text-sm mt-3">불러오는 중...</div>
      <div v-if="error" class="text-center text-red-500 text-sm mt-3">{{ error }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getInteractions, markInteractionRead, markAllInteractionsRead, getUnreadInteractionCount, type FriendInteraction, type PageResponse } from '../api/friend';

const router = useRouter();

const items = ref<FriendInteraction[]>([]);
const page = ref(0);
const size = ref(20);
const totalPages = ref(1);
const unread = ref(0);
const loading = ref(false);
const error = ref<string | null>(null);
const readingId = ref<number | null>(null);
const processingAll = ref(false);
const dismissedBefore = ref<string | null>(localStorage.getItem('notif:dismissedBefore'));

const likeOnly = computed(() => items.value.filter(i => i.interactionType === 'LIKE'));
const visibleLikeOnly = computed(() => {
  if (!dismissedBefore.value) return likeOnly.value;
  const threshold = new Date(dismissedBefore.value).getTime();
  return likeOnly.value.filter(i => {
    const t = new Date(i.createdAt as any).getTime();
    // 생성 시간이 없거나 파싱 불가하면 숨김 처리
    return Number.isFinite(t) ? t >= threshold : false;
  });
});

function formatDate(iso: string): string {
  try {
    return new Date(iso).toLocaleString();
  } catch { return iso; }
}

function goBack() {
  router.back();
}

async function refreshUnread() {
  try { unread.value = await getUnreadInteractionCount(); } catch { /* ignore */ }
}

async function load() {
  loading.value = true;
  error.value = null;
  try {
    const res: PageResponse<FriendInteraction> = await getInteractions(page.value, size.value);
    items.value = res.content || [];
    totalPages.value = Math.max(1, res.totalPages || 1);
    await refreshUnread();
  } catch (e: any) {
    error.value = e?.message || '불러오기 실패';
  } finally {
    loading.value = false;
  }
}

async function markRead(it: FriendInteraction) {
  if (readingId.value) return;
  readingId.value = it.interactionId;
  try {
    await markInteractionRead(it.interactionId);
    it.isRead = true as any; // 가벼운 낙관적 반영
    await refreshUnread();
  } catch (e) {
    // 실패 시 무시 또는 토스트 처리 가능
  } finally {
    readingId.value = null;
  }
}

async function markAllRead() {
  if (processingAll.value) return;
  processingAll.value = true;
  try {
    await markAllInteractionsRead();
    // 로컬 반영
    items.value.forEach(i => { (i as any).isRead = true; });
    await refreshUnread();
  } catch (e) {
    // 실패 시 무시 또는 에러 표시
  } finally {
    processingAll.value = false;
  }
}

async function clearInbox() {
  if (processingAll.value) return;
  if (!confirm('알림을 화면에서 비웁니다. 계속할까요?')) return;
  processingAll.value = true;
  try {
    const now = new Date().toISOString();
    dismissedBefore.value = now;
    localStorage.setItem('notif:dismissedBefore', now);
    // 즉시 화면 비우기 효과와 페이징 리셋
    items.value = [];
    page.value = 0;
    totalPages.value = 1;
  } finally {
    processingAll.value = false;
  }
}

function nextPage() { if (page.value + 1 < totalPages.value) { page.value++; load(); } }
function prevPage() { if (page.value > 0) { page.value--; load(); } }

onMounted(load);
</script>

<style scoped>
</style>

