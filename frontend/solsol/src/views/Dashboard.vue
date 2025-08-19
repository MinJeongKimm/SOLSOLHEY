<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-100 to-blue-300">
    <!-- Header가 이미 App.vue에서 표시됨 -->
    
    <div class="flex flex-col items-center justify-center min-h-screen pt-16">
      <div class="bg-white rounded-2xl shadow-lg p-8 w-full max-w-md flex flex-col items-center gap-4">
        <h2 class="text-2xl font-bold text-blue-600">쏠쏠Hey 대시보드</h2>
        <p class="text-gray-700 text-center">환영합니다!<br />이곳에서 마스코트, 챌린지, 포인트 등 주요 정보를 확인할 수 있습니다.</p>
        
        <!-- 사용자 정보 표시 -->
        <div v-if="user" class="mt-4 p-4 bg-blue-50 rounded-lg w-full">
          <p class="text-sm text-gray-600">로그인 사용자:</p>
          <p class="font-semibold text-blue-700">{{ user.nickname || user.username }}</p>
          <p class="text-sm text-gray-500">{{ user.userId }}</p>
        </div>

        <!-- 네비게이션 링크 -->
        <div class="flex gap-2 mt-4">
          <router-link 
            to="/challenge" 
            class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors"
          >
            챌린지
          </router-link>
          <router-link 
            to="/mascot" 
            class="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition-colors"
          >
            마스코트
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { auth } from '../api/index';

// 사용자 정보
const user = computed(() => auth.getUser());
</script>
