<template>
  <div>
    <!-- 사이드바 -->
    <div
      class="fixed inset-y-0 left-0 w-64 bg-white border-r border-gray-200 shadow-lg z-50 transform transition-transform duration-300"
      :class="isOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <!-- 헤더 영역 -->
      <div class="h-16 px-4 flex items-center justify-between border-b border-gray-200">
        <div class="flex items-center space-x-2">
          <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
            <span class="text-white font-bold text-sm">쏠</span>
          </div>
          <span class="text-lg font-bold text-blue-600">쏠쏠Hey</span>
        </div>
        <button @click="close" aria-label="닫기" class="p-2 rounded-md text-gray-500 hover:bg-gray-100">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 네비게이션 -->
      <nav class="p-4 space-y-2">
        <router-link
          to="/mascot"
          @click="close"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/mascot' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v8a2 2 0 002 2h10a2 2 0 002-2v-8" />
          </svg>
          <span>My Page</span>
        </router-link>

        <router-link
          :to="{ path: '/shop', query: { tab: 'items' } }"
          @click="close"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/shop' && $route.query.tab !== 'gifticons' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2m0 0h13.2l-1.68 8.39a2 2 0 01-1.97 1.61H8.53a2 2 0 01-1.97-1.61L5.4 5m0 0L5 3m0 0H3" />
          </svg>
          <span>Item Shop</span>
        </router-link>

        <router-link
          to="/locker"
          @click="close"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/locker' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
          </svg>
          <span>My Gifticons</span>
        </router-link>
      </nav>
    </div>

    <!-- 오버레이 (모바일에서만 표시해도 되지만 간단히 isOpen 기준 표시) -->
    <div
      v-if="isOpen"
      class="fixed inset-0 bg-black/40 z-40"
      @click="close"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const isOpen = ref(false);

function open() { isOpen.value = true; }
function close() { isOpen.value = false; }

function toggle() { isOpen.value = !isOpen.value; }

function handleToggle() { toggle(); }

onMounted(() => {
  window.addEventListener('toggleLeftSidebar', handleToggle as EventListener);
});

onUnmounted(() => {
  window.removeEventListener('toggleLeftSidebar', handleToggle as EventListener);
});

defineExpose({ open, close, toggle });
</script>

<style scoped></style>

