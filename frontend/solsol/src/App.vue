<template>
  <div id="app">
    <!-- 인증이 필요한 페이지에만 Header 표시 -->
    <Header v-if="showHeader" />
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Header from './components/Header.vue';

const route = useRoute();

// 인증이 필요한 페이지에서만 Header 표시
const showHeader = computed(() => {
  const authPages = ['/dashboard', '/challenge', '/mascot'];
  return authPages.includes(route.path);
});
</script>
