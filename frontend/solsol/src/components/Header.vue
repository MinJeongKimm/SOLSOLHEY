<template>
  <header class="bg-white shadow-sm border-b border-gray-200">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <!-- 로고 -->
        <div class="flex items-center">
          <router-link to="/dashboard" class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
              <span class="text-white font-bold text-sm">쏠</span>
            </div>
            <span class="text-xl font-bold text-blue-600">쏠쏠Hey</span>
          </router-link>
        </div>

        <!-- 네비게이션 메뉴 -->
        <nav class="hidden md:flex items-center space-x-8">
          <router-link 
            to="/dashboard" 
            class="text-gray-600 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            :class="{ 'text-blue-600 font-semibold': $route.path === '/dashboard' }"
          >
            대시보드
          </router-link>
          <router-link 
            to="/challenge" 
            class="text-gray-600 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            :class="{ 'text-blue-600 font-semibold': $route.path === '/challenge' }"
          >
            챌린지
          </router-link>
          <router-link 
            to="/mascot" 
            class="text-gray-600 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            :class="{ 'text-blue-600 font-semibold': $route.path === '/mascot' }"
          >
            마스코트
          </router-link>
        </nav>

        <!-- 사용자 정보 및 로그아웃 -->
        <div class="flex items-center space-x-4">
          <!-- 사용자 정보 -->
          <div v-if="user" class="hidden sm:flex items-center space-x-2">
            <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
              <span class="text-gray-600 font-medium text-sm">{{ getInitial(user.nickname || user.username) }}</span>
            </div>
            <span class="text-sm font-medium text-gray-700">{{ user.nickname || user.username }}</span>
          </div>

          <!-- 로그아웃 버튼 -->
          <button
            @click="handleLogout"
            :disabled="isLoggingOut"
            class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span v-if="isLoggingOut" class="flex items-center">
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-gray-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path>
              </svg>
              로그아웃 중...
            </span>
            <span v-else>로그아웃</span>
          </button>

          <!-- 모바일 메뉴 버튼 -->
          <button
            @click="isMobileMenuOpen = !isMobileMenuOpen"
            class="md:hidden inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500"
          >
            <svg class="h-6 w-6" :class="{ 'hidden': isMobileMenuOpen, 'block': !isMobileMenuOpen }" stroke="currentColor" fill="none" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            <svg class="h-6 w-6" :class="{ 'block': isMobileMenuOpen, 'hidden': !isMobileMenuOpen }" stroke="currentColor" fill="none" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 모바일 메뉴 -->
      <div class="md:hidden" :class="{ 'block': isMobileMenuOpen, 'hidden': !isMobileMenuOpen }">
        <div class="px-2 pt-2 pb-3 space-y-1 border-t border-gray-200">
          <router-link 
            to="/dashboard" 
            @click="isMobileMenuOpen = false"
            class="block px-3 py-2 rounded-md text-base font-medium text-gray-600 hover:text-blue-600 hover:bg-gray-50 transition-colors"
            :class="{ 'text-blue-600 bg-blue-50': $route.path === '/dashboard' }"
          >
            대시보드
          </router-link>
          <router-link 
            to="/challenge" 
            @click="isMobileMenuOpen = false"
            class="block px-3 py-2 rounded-md text-base font-medium text-gray-600 hover:text-blue-600 hover:bg-gray-50 transition-colors"
            :class="{ 'text-blue-600 bg-blue-50': $route.path === '/challenge' }"
          >
            챌린지
          </router-link>
          <router-link 
            to="/mascot" 
            @click="isMobileMenuOpen = false"
            class="block px-3 py-2 rounded-md text-base font-medium text-gray-600 hover:text-blue-600 hover:bg-gray-50 transition-colors"
            :class="{ 'text-blue-600 bg-blue-50': $route.path === '/mascot' }"
          >
            마스코트
          </router-link>
          
          <!-- 모바일 사용자 정보 -->
          <div v-if="user" class="px-3 py-2 border-t border-gray-200 mt-2 pt-4">
            <div class="flex items-center space-x-2 mb-2">
              <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                <span class="text-gray-600 font-medium text-sm">{{ getInitial(user.nickname || user.username) }}</span>
              </div>
              <span class="text-sm font-medium text-gray-700">{{ user.nickname || user.username }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { logout, auth, handleApiError } from '../api/index';

const router = useRouter();
const isMobileMenuOpen = ref(false);
const isLoggingOut = ref(false);

// 사용자 정보
const user = computed(() => auth.getUser());

// 사용자 이름의 첫 글자 추출
function getInitial(name: string): string {
  return name ? name.charAt(0).toUpperCase() : '?';
}

// 로그아웃 처리
async function handleLogout() {
  if (isLoggingOut.value) return;
  
  isLoggingOut.value = true;
  
  try {
    // 서버에 로그아웃 요청
    await logout();
    
    // 로컬 인증 정보 삭제
    auth.clearAuth();
    
    // 모바일 메뉴 닫기
    isMobileMenuOpen.value = false;
    
    // 로그인 페이지로 리다이렉트
    router.push('/');
  } catch (error: any) {
    console.error('로그아웃 오류:', error);
    
    // 서버 오류가 있어도 로컬 정보는 삭제
    auth.clearAuth();
    router.push('/');
    
    // 사용자에게 알림 (선택사항)
    alert('로그아웃 처리 중 오류가 발생했지만, 로그아웃되었습니다.');
  } finally {
    isLoggingOut.value = false;
  }
}

// 인증 상태 확인
onMounted(() => {
  // 로그인되지 않은 상태에서 헤더에 접근하는 경우 로그인 페이지로 리다이렉트
  if (!auth.isAuthenticated()) {
    router.push('/');
  }
});

// 페이지 새로고침 시 사용자 정보 다시 로드
window.addEventListener('storage', () => {
  // localStorage 변경 감지 (다른 탭에서 로그아웃 등)
  if (!auth.isAuthenticated()) {
    router.push('/');
  }
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>




