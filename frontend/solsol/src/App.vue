<template>
  <div id="app">
    <!-- 인증이 필요한 페이지에만 Header 표시 -->
    <Header v-if="showHeader" />
    
    <!-- 슬라이드 메뉴 -->
    <SlideMenu v-if="showHeader" />
    
    <!-- 메인 콘텐츠 (헤더가 고정되어 있으므로 상단 여백 추가) -->
    <main :class="{ 'pt-16': showHeader }">
      <router-view />
    </main>

    <!-- Global Toasts -->
    <ToastContainer />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Header from './components/Header.vue';
import SlideMenu from './components/SlideMenu.vue';
import ToastContainer from './components/ToastContainer.vue';

const route = useRoute();

// 인증이 필요한 페이지에서만 Header 표시
const showHeader = computed(() => {
  const authPages = ['/challenge', '/mascot', '/friend', '/ranking', '/attendance', '/shop'];
  return authPages.includes(route.path);
});
</script>
