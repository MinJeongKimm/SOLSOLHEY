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
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 items-center mb-3 text-sm text-gray-600">
        <div>미읽음: <span class="font-semibold">{{ unread }}</span></div>
        <div class="flex flex-wrap sm:justify-end gap-2">
          <button 
            class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-blue-100 text-blue-700 text-xs sm:text-sm hover:bg-blue-200 disabled:opacity-50"
            @click="markAllRead"
            :disabled="processingAll || loading"
          >모두 읽음</button>
          <button 
            class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-gray-100 text-gray-700 text-xs sm:text-sm hover:bg-gray-200 disabled:opacity-50"
            @click="clearInbox"
            :disabled="processingAll || loading"
          >알림함 비우기</button>
        </div>
      </div>

      <!-- 목록 영역: 친구 요청 -->
      <div v-if="visibleRequests.length" class="mb-2">
        <div class="text-xs font-semibold text-gray-500 mb-1">친구 요청</div>
        <div class="divide-y divide-gray-100">
          <div v-for="it in visibleRequests" :key="it.interactionId" class="py-3">
            <div class="flex items-start justify-between">
              <div>
                <div class="text-sm text-gray-800">
                  <router-link
                    class="font-semibold text-blue-600 hover:underline"
                    :to="`/friends/${it.fromUserId}`"
                  >
                    {{ it.fromUserNickname }}
                  </router-link>
                  <span class="ml-1">{{ it.message || '님이 친구 요청을 보냈습니다.' }}</span>
                </div>
                <div class="text-xs text-gray-500 mt-1">{{ formatDate(it.createdAt) }}</div>
              </div>
            </div>
            <div class="mt-2 flex gap-2">
              <template v-if="!it.isRead && !isAcceptedMessage(it.message)">
                <button
                  class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-green-500 text-white text-xs sm:text-sm hover:bg-green-600 disabled:opacity-60"
                  :disabled="processingId === it.interactionId"
                  @click="acceptRequestFromInbox(it)"
                >{{ processingId === it.interactionId ? '처리 중' : '수락' }}</button>
                <button
                  class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-red-500 text-white text-xs sm:text-sm hover:bg-red-600 disabled:opacity-60"
                  :disabled="processingId === it.interactionId"
                  @click="rejectRequestFromInbox(it)"
                >거절</button>
              </template>
              <span v-else class="text-xs text-gray-400 self-center">처리됨</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 목록 영역: 좋아요 -->
      <div class="divide-y divide-gray-100">
        <div v-for="it in visibleLikeOnly" :key="it.interactionId" class="py-3 flex items-start justify-between">
          <div>
            <div class="text-sm text-gray-800">
              <router-link
                class="font-semibold text-blue-600 hover:underline"
                :to="`/friends/${it.fromUserId}`"
              >
                {{ it.fromUserNickname }}
              </router-link>
              <span class="ml-1">님이 좋아요를 보냈습니다.</span>
            </div>
            <div class="text-xs text-gray-500 mt-1">{{ formatDate(it.createdAt) }}</div>
            <div class="mt-2 flex gap-2">
              <button
                class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-pink-500 text-white text-xs sm:text-sm hover:bg-pink-600 disabled:opacity-60"
                @click="sendBackLike(it)"
                :disabled="likingId === it.interactionId"
              >
                {{ likingId === it.interactionId ? '전송 중' : '좋아요 보내기' }}
              </button>
              <button
                v-if="!it.isRead"
                @click="markRead(it)"
                class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-blue-500 text-white text-xs sm:text-sm hover:bg-blue-600"
                :disabled="readingId === it.interactionId"
              >
                {{ readingId === it.interactionId ? '처리 중' : '읽음' }}
              </button>
              <span v-else class="text-xs text-gray-400 self-center">읽음</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 페이징 -->
      <div class="flex items-center justify-between mt-4 text-sm">
        <button 
          class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
          :disabled="page === 0 || loading"
          @click="prevPage"
        >이전</button>
        <div class="text-gray-600">{{ page + 1 }} / {{ totalPages }}</div>
        <button 
          class="inline-flex items-center whitespace-nowrap px-3 py-1 rounded-lg bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
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
import { onMounted, onActivated, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getInteractions, markInteractionRead, markAllInteractionsRead, getUnreadInteractionCount, sendLikeBack, acceptFriendRequest, rejectFriendRequest, type FriendInteraction, type PageResponse } from '../api/friend';

const router = useRouter();

const items = ref<FriendInteraction[]>([]);
const page = ref(0);
const size = ref(20);
const totalPages = ref(1);
const unread = ref(0);
const loading = ref(false);
const error = ref<string | null>(null);
const readingId = ref<number | null>(null);
const likingId = ref<number | null>(null);
const processingAll = ref(false);
const dismissedBefore = ref<string | null>(localStorage.getItem('notif:dismissedBefore'));
const processingId = ref<number | null>(null);

// 서버가 미읽음만 반환하므로 단순 필터만 유지
const likeOnly = computed(() => items.value.filter(i => i.interactionType === 'LIKE'));
const requestOnly = computed(() => items.value.filter(i => i.interactionType === 'FRIEND_REQUEST'));
const visibleLikeOnly = computed(() => likeOnly.value);
const visibleRequests = computed(() => requestOnly.value);

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
    // 미읽음만 보여주므로 읽음 처리 성공 시 즉시 제거
    items.value = items.value.filter(x => x.interactionId !== it.interactionId);
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
    // 미읽음만 노출이므로 모두 제거
    items.value = [];
    await refreshUnread();
  } catch (e) {
    // 실패 시 무시 또는 에러 표시
  } finally {
    processingAll.value = false;
  }
}

async function sendBackLike(it: FriendInteraction) {
  if (likingId.value) return;
  likingId.value = it.interactionId;
  try {
    await sendLikeBack(it.interactionId);
    // 서버에서 원본 읽음 처리 → 미읽음만 노출이므로 즉시 제거
    items.value = items.value.filter(x => x.interactionId !== it.interactionId);
  } catch (e) {
    // 필요 시 토스트/에러 처리 가능
  } finally {
    likingId.value = null;
    // 보낸 직후 읽음 갱신/뱃지 갱신을 위해 미읽음 카운트 리프레시
    await refreshUnread();
  }
}

async function acceptRequestFromInbox(it: FriendInteraction) {
  if (!it.referenceId) return;
  if (processingId.value) return;
  processingId.value = it.interactionId;
  try {
    await acceptFriendRequest(it.referenceId);
    // 서버에서 읽음/메시지 갱신되지만, 미읽음만 노출하므로 즉시 제거
    items.value = items.value.filter(x => x.interactionId !== it.interactionId);
    await refreshUnread();
  } catch (e) {
    // 실패 시 무시 또는 에러 표시
  } finally {
    processingId.value = null;
  }
}

async function rejectRequestFromInbox(it: FriendInteraction) {
  if (!it.referenceId) return;
  if (processingId.value) return;
  processingId.value = it.interactionId;
  try {
    await rejectFriendRequest(it.referenceId);
    items.value = items.value.filter(x => x.interactionId !== it.interactionId);
    await refreshUnread();
  } catch (e) {
    // 실패 시 무시 또는 에러 표시
  } finally {
    processingId.value = null;
  }
}

async function clearInbox() {
  if (processingAll.value) return;
  if (!confirm('알림함의 미읽음 알림을 모두 읽음 처리합니다. 계속할까요?')) return;
  processingAll.value = true;
  try {
    // 서버에서 모두 읽음 처리 -> 미읽음만 보여주므로 목록은 자동 비워짐
    await markAllInteractionsRead();
    items.value = [];
    page.value = 0;
    totalPages.value = 1;
    await refreshUnread();
  } finally {
    processingAll.value = false;
  }
}

function nextPage() { if (page.value + 1 < totalPages.value) { page.value++; load(); } }
function prevPage() { if (page.value > 0) { page.value--; load(); } }

onMounted(load);
// keep-alive로 재진입 시 최신 상태 재조회
onActivated(() => { page.value = 0; load(); });

// helper: 수락 메시지 여부 판별 (버튼 숨김용)
function isAcceptedMessage(msg?: string) {
  return !!msg && msg.includes('친구가 되었습니다');
}
</script>

<style scoped>
</style>
