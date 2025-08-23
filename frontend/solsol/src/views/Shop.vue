<template>
  <div class="min-h-screen bg-gray-50">
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
          <span class="font-bold text-gray-800">{{ userPoints }}P</span>
        </div>
      </div>
    </div>

    <!-- 상점 컨텐츠 -->
    <div class="p-4">
      <!-- 탭 네비게이션 -->
      <div class="flex space-x-1 bg-gray-100 p-1 rounded-lg mb-6">
        <button 
          @click="activeTab = 'items'"
          :class="[
            'flex-1 py-3 px-4 rounded-md text-sm font-medium transition-colors',
            activeTab === 'items' 
              ? 'bg-white text-gray-900 shadow-sm' 
              : 'text-gray-600 hover:text-gray-900'
          ]"
        >
          🎨 꾸미기 아이템
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
          🎁 기프티콘
        </button>
      </div>

      <!-- 탭별 컨텐츠 -->
      <div v-if="activeTab === 'items'">
        <ItemShop :user-points="userPoints" @points-updated="loadUserPoints" />
      </div>
      <div v-else-if="activeTab === 'gifticons'">
        <GifticonShop :user-points="userPoints" @points-updated="loadUserPoints" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo, auth } from '../api/index';
import ItemShop from '../components/shop/ItemShop.vue';
import GifticonShop from '../components/shop/GifticonShop.vue';

const router = useRouter();

// 반응형 데이터
const activeTab = ref<'items' | 'gifticons'>('items');
const userPoints = ref(0);

// 라우터 함수
function goBack() {
  router.back();
}

// 사용자 포인트 로드
async function loadUserPoints() {
  try {
    const user = auth.getUser();
    if (user && user.userId) {
      const userInfo = await getUserInfo(Number(user.userId));
      userPoints.value = userInfo.totalPoints;
    }
  } catch (err) {
    console.error('사용자 포인트 로드 실패:', err);
    // 기본값 설정
    userPoints.value = 15000;
  }
}

// 컴포넌트 마운트
onMounted(async () => {
  await loadUserPoints();
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>
