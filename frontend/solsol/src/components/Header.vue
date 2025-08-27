<template>
  <header class="bg-white shadow-sm border-b border-gray-200 fixed top-0 left-0 right-0 z-40">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <!-- 좌측: 좌측 사이드바 토글 + 로고 -->
        <div class="flex items-center space-x-2">
          <button
            @click="toggleLeftSidebar"
            aria-label="사이드바 열기"
            class="inline-flex items-center justify-center w-9 h-9 rounded-full text-gray-600 hover:text-blue-600 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500 transition-colors"
          >
            <svg class="h-5 w-5" stroke="currentColor" fill="none" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h10M4 18h8" />
            </svg>
          </button>
          <router-link to="/mascot" class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
              <span class="text-white font-bold text-sm">쏠</span>
            </div>
            <span class="text-xl font-bold text-blue-600">쏠쏠Hey</span>
          </router-link>
        </div>

        <!-- 우측 메뉴 버튼 -->
        <div class="flex items-center">
          <button
            @click="toggleSlideMenu"
            class="inline-flex items-center justify-center p-2 rounded-md text-gray-600 hover:text-blue-600 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500 transition-colors"
          >
            <svg class="h-6 w-6" stroke="currentColor" fill="none" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

// 슬라이드 메뉴 토글 이벤트 발생
function toggleSlideMenu() {
  // 부모 컴포넌트에 이벤트 전달
  // 실제로는 emit을 사용하거나 전역 상태 관리 사용
  window.dispatchEvent(new CustomEvent('toggleSlideMenu'));
}

function toggleLeftSidebar() {
  window.dispatchEvent(new CustomEvent('toggleLeftSidebar'));
}
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>



