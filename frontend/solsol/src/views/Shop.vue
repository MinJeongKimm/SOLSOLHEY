<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- 상단 헤더 -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="flex items-center justify-between px-4 py-3">
        <!-- 뒤로가기 버튼 -->
        <button 
          @click="goBack"
          class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <!-- 제목 -->
        <div class="text-center">
          <h1 class="text-xl font-bold text-gray-800">상점</h1>
        </div>

        <!-- 보유 포인트 배지 -->
        <div class="flex items-center space-x-2 bg-gray-100 px-3 py-2 rounded-full shadow-sm">
          <div class="w-6 h-6 bg-yellow-400 rounded-full flex items-center justify-center">
            <span class="text-white font-bold text-sm">$</span>
          </div>
          <span class="font-bold text-gray-800">{{ validUserPoints }}P</span>
        </div>
      </div>
    </div>

    <!-- 상점 컨텐츠 (상단 필터는 고정, 아이템 영역만 스크롤) -->
    <div class="p-4 flex-1 flex flex-col overflow-hidden">
      <!-- 탭 네비게이션 -->
      <div class="flex space-x-1 bg-gray-100 p-1 rounded-lg mb-6 sticky top-0 z-20">
        <button 
          @click="activeTab = 'items'"
          :class="[
            'flex-1 py-3 px-4 rounded-md text-sm font-medium transition-colors',
            activeTab === 'items' 
              ? 'bg-white text-gray-900 shadow-sm' 
              : 'text-gray-600 hover:text-gray-900'
          ]"
        >
          꾸미기 아이템
        </button>
        <button 
          @click="activeTab = 'gifticons'"
          :class="[
            'flex-1 py-3 px-4 rounded-md text-sm font-medium transition-colors',
            activeTab === 'gifticons' 
              ? 'bg-white text-gray-900 shadow-sm' 
              : 'text-gray-600 hover:text-gray-900'
          ]"
        >
          기프티콘
        </button>
      </div>

      <!-- 스크롤 영역: 아이템 리스트만 스크롤 -->
      <div class="flex-1 overflow-y-auto">
        <!-- 로딩 상태일 때 -->
        <div v-if="isLoading" class="flex justify-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
        </div>
        
        <!-- 로딩 완료 후 탭 컨텐츠 표시 -->
        <div v-else>
          <div v-if="activeTab === 'items'">
            <ItemShop :user-points="validUserPoints" @points-updated="handlePointsUpdated" />
          </div>
          <div v-else-if="activeTab === 'gifticons'">
            <GifticonShop :user-points="validUserPoints" @points-updated="handlePointsUpdated" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { usePointStore } from '../stores/point';
import ItemShop from '../components/shop/ItemShop.vue';
import GifticonShop from '../components/shop/GifticonShop.vue';

const router = useRouter();
const route = useRoute();
const pointStore = usePointStore();

// 반응형 데이터
const activeTab = ref<'items' | 'gifticons'>('items');
const isLoading = ref(true); // 로딩 상태

// 포인트 스토어에서 포인트를 가져오는 computed 속성
const validUserPoints = computed(() => {
  return pointStore.userPoints;
});

// 라우터 함수
function goBack() {
  router.back();
}

// 포인트 업데이트 이벤트 처리 - 포인트 스토어에서 포인트를 다시 로드
async function handlePointsUpdated() {
  await pointStore.loadPoints();
}

// 컴포넌트 마운트
onMounted(async () => {
  try {
    isLoading.value = true;
    // 포인트 스토어에서 포인트 로드
    await pointStore.loadPoints();
    // 쿼리 파라미터로 탭 제어 (?tab=gifticons)
    const tab = String(route.query.tab || '').toLowerCase();
    if (tab === 'gifticons') {
      activeTab.value = 'gifticons';
    }
  } catch (err) {
    console.error('포인트 로드 실패:', err);
  } finally {
    isLoading.value = false;
  }
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>
