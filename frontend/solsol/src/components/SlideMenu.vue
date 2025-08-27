<template>
  <div>
    <!-- 슬라이드 메뉴 -->
    <div 
      class="fixed inset-y-0 right-0 w-64 bg-white shadow-lg z-50 transform transition-transform duration-300"
      :class="isOpen ? 'translate-x-0' : 'translate-x-full'"
    >
      <!-- 메뉴 헤더 -->
      <div class="flex items-center justify-between p-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-800">메뉴</h2>
        <button 
          @click="closeMenu"
          class="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 사용자 정보 -->
      <div v-if="user" class="p-4 border-b border-gray-200">
        <div class="flex items-center space-x-3">
          <div class="w-12 h-12 bg-gradient-to-br from-blue-400 to-purple-500 rounded-full flex items-center justify-center">
            <span class="text-white font-bold text-lg">{{ getInitial(user.nickname || user.username) }}</span>
          </div>
          <div>
            <p class="font-semibold text-gray-800">{{ user.nickname || user.username }}</p>
            <p class="text-sm text-gray-500">{{ user.email || '사용자' }}</p>
          </div>
        </div>
      </div>

      <!-- 메뉴 아이템들 -->
      <nav class="p-4 space-y-2">
        <!-- 홈 -->
        <router-link 
          to="/mascot" 
          @click="closeMenu"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/mascot' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
          <span>홈</span>
        </router-link>



        <!-- 출석체크 -->
        <router-link 
          to="/attendance" 
          @click="closeMenu"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/attendance' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span>출석체크</span>
        </router-link>

        <!-- 보관함 -->
        <router-link 
          to="/locker" 
          @click="closeMenu"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/locker' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V7a2 2 0 00-2-2H6a2 2 0 00-2 2v6m16 0v4a2 2 0 01-2 2H6a2 2 0 01-2-2v-4m16 0H4" />
          </svg>
          <span>보관함</span>
        </router-link>

        <!-- 친구 -->
        <router-link 
          to="/friend" 
          @click="closeMenu"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/friend' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <span>친구</span>
        </router-link>

        <!-- 랭킹 -->
        <router-link 
          to="/ranking" 
          @click="closeMenu"
          class="flex items-center space-x-3 p-3 rounded-lg text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition-colors"
          :class="{ 'bg-blue-50 text-blue-600': $route.path === '/ranking' }"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <span>랭킹</span>
        </router-link>


        <!-- 로그아웃 -->
        <button 
          @click="handleLogout"
          :disabled="isLoggingOut"
          class="w-full flex items-center space-x-3 p-3 rounded-lg text-red-600 hover:bg-red-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          <span v-if="isLoggingOut">로그아웃 중...</span>
          <span v-else>로그아웃</span>
        </button>
      </nav>
    </div>

    <!-- 오버레이 -->
    <div 
      v-if="isOpen"
      @click="closeMenu"
      class="fixed inset-0 bg-black bg-opacity-50 z-40 transition-opacity duration-300"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { logout, auth, handleApiError } from '../api/index';

const router = useRouter();
const isOpen = ref(false);
const isLoggingOut = ref(false);

// 사용자 정보
const user = computed(() => auth.getUser());

// 사용자 이름의 첫 글자 추출
function getInitial(name: string): string {
  return name ? name.charAt(0).toUpperCase() : '?';
}

// 메뉴 열기
function openMenu() {
  isOpen.value = true;
}

// 메뉴 닫기
function closeMenu() {
  isOpen.value = false;
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
    
    // 메뉴 닫기
    closeMenu();
    
    // 로그인 페이지로 리다이렉트
    router.push('/login');
  } catch (error: any) {
    console.error('로그아웃 오류:', error);
    
    // 서버 오류가 있어도 로컬 정보는 삭제
    auth.clearAuth();
    router.push('/login');
    
    // 사용자에게 알림
    alert('로그아웃 처리 중 오류가 발생했지만, 로그아웃되었습니다.');
  } finally {
    isLoggingOut.value = false;
  }
}

// 전역 이벤트 리스너 등록
window.addEventListener('toggleSlideMenu', () => {
  isOpen.value = !isOpen.value;
});

// 컴포넌트가 언마운트될 때 이벤트 리스너 제거
import { onUnmounted } from 'vue';
onUnmounted(() => {
  window.removeEventListener('toggleSlideMenu', () => {});
});

// 외부에서 메뉴 상태를 제어할 수 있도록 expose
defineExpose({
  openMenu,
  closeMenu
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>
