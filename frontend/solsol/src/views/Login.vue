<template>
  <div class="min-h-screen flex flex-col justify-center items-center bg-gradient-to-b from-blue-100 to-blue-300 px-4">
    <div class="w-full max-w-sm bg-white rounded-2xl shadow-lg p-6 flex flex-col gap-6" role="main" aria-label="로그인 폼">
      <h1 class="text-2xl font-bold text-center text-blue-600 mb-2" tabindex="0">쏠쏠Hey 로그인</h1>
      <form @submit.prevent="onSubmit" class="flex flex-col gap-4" autocomplete="on" aria-live="polite">
        <div>
          <label for="userId" class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
          <input
            id="userId"
            v-model="userId"
            type="email"
            autocomplete="email"
            required
            aria-required="true"
            aria-invalid="true"
            aria-describedby="userIdError"
            class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-400 focus:outline-none text-base min-h-[44px]"
            :class="{'border-red-400 ring-2 ring-red-300': userIdError, 'focus:ring-blue-400': !userIdError}"
            placeholder="example@email.com"
            @input="userIdError = validateUserId(userId)"
          />
          <transition name="fade">
            <p v-if="userIdError" id="userIdError" class="text-xs text-red-500 mt-1 animate-shake">{{ userIdError }}</p>
          </transition>
        </div>
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
          <input
            id="password"
            v-model="password"
            type="password"
            autocomplete="current-password"
            required
            minlength="8"
            aria-required="true"
            aria-invalid="true"
            aria-describedby="passwordError"
            class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-400 focus:outline-none text-base min-h-[44px]"
            :class="{'border-red-400 ring-2 ring-red-300': passwordError, 'focus:ring-blue-400': !passwordError}"
            placeholder="비밀번호"
            @input="passwordError = validatePassword(password)"
            @keyup.enter="onSubmit"
          />
          <transition name="fade">
            <p v-if="passwordError" id="passwordError" class="text-xs text-red-500 mt-1 animate-shake">{{ passwordError }}</p>
          </transition>
        </div>
        <button
          type="submit"
          class="w-full py-3 mt-2 rounded-lg bg-blue-500 text-white font-semibold text-lg shadow-md hover:bg-blue-600 transition active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed min-h-[44px]"
          :disabled="loading"
          :aria-busy="loading"
        >
          <span v-if="loading" class="flex items-center justify-center gap-2">
            <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path></svg>
            로그인 중...
          </span>
          <span v-else>로그인</span>
        </button>
        <transition name="fade">
          <p v-if="errorMessage" class="text-center text-sm text-red-500 mt-2 animate-shake" aria-live="assertive">{{ errorMessage }}</p>
        </transition>
      </form>
      <div class="text-center text-xs text-gray-400 mt-2" tabindex="0">© 쏠쏠Hey</div>
      <div class="text-center mt-2">
        <router-link to="/signup" class="text-blue-500 hover:underline text-sm">회원가입</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginAndBootstrap, handleApiError, getMascot } from '../api/index';
import type { LoginRequest } from '../types/api';

const userId = ref('');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');
const userIdError = ref('');
const passwordError = ref('');
const router = useRouter();

function validateUserId(value: string) {
  if (!value) return '이메일을 입력하세요.';
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!re.test(value)) return '올바른 이메일 형식이 아닙니다.';
  return '';
}

function validatePassword(value: string) {
  if (!value) return '비밀번호를 입력하세요.';
  if (value.length < 8) return '비밀번호는 8자 이상이어야 합니다.';
  return '';
}

async function onSubmit() {
  // 클라이언트 측 유효성 검사
  userIdError.value = validateUserId(userId.value);
  passwordError.value = validatePassword(password.value);
  errorMessage.value = '';
  
  if (userIdError.value || passwordError.value) {
    return;
  }

  loading.value = true;
  
  try {
    const loginData: LoginRequest = {
      userId: userId.value,
      password: password.value,
    };

    const response = await loginAndBootstrap(loginData);
    
    if (response.success && response.data) {
      // loginAndBootstrap()에서 이미 bootstrapAuth()가 호출됨
      // 사용자 정보가 캐시에 반영되어 있음
      
      // 마스코트 존재 여부 확인
      try {
        const mascotData = await getMascot();
        if (mascotData) {
          // 마스코트가 있으면 마스코트 메인 화면으로 리다이렉트
          router.push('/mascot');
        } else {
          // 마스코트가 없으면 마스코트 생성 화면으로 리다이렉트
          router.push('/mascot/create');
        }
      } catch (error) {
        // 마스코트 조회 실패 시 마스코트 생성 화면으로 이동
        console.warn('마스코트 조회 실패, 생성 화면으로 이동:', error);
        router.push('/mascot/create');
      }
    } else {
      // 서버에서 성공하지 않은 응답을 보낸 경우
      errorMessage.value = response.message || '로그인에 실패했습니다.';
    }
  } catch (error: any) {
    errorMessage.value = handleApiError(error);
    console.error('로그인 오류:', error);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
@keyframes shake {
  10%, 90% { transform: translateX(-2px); }
  20%, 80% { transform: translateX(4px); }
  30%, 50%, 70% { transform: translateX(-8px); }
  40%, 60% { transform: translateX(8px); }
}
.animate-shake {
  animation: shake 0.4s cubic-bezier(.36,.07,.19,.97) both;
}
</style>
