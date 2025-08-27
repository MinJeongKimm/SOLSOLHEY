<template>
  <div id="app">
    <!-- 인증이 필요한 페이지에만 Header 표시 -->
    <Header v-if="showHeader" />
    
    <!-- 좌측 사이드바 + 기존 슬라이드 메뉴 -->
    <SidebarLeft v-if="showHeader" />
    <SlideMenu v-if="showHeader" />
    
    <!-- 메인 콘텐츠 (헤더가 고정되어 있으므로 상단 여백 추가) -->
    <main :class="{ 'pt-16': showHeader }">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Header from './components/Header.vue';
import SlideMenu from './components/SlideMenu.vue';
import SidebarLeft from './components/SidebarLeft.vue';

const route = useRoute();

// 인증이 필요한 페이지에서만 Header/Sidebar 표시 (라우터 메타 기반)
const showHeader = computed(() => route.matched.some(r => (r.meta as any)?.requiresAuth));
</script>
