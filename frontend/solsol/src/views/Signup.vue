<template>
  <div class="min-h-screen flex flex-col justify-center items-center bg-gradient-to-b from-blue-100 to-blue-300 px-4">
    <div class="w-full max-w-sm bg-white rounded-2xl shadow-lg p-6 flex flex-col gap-6" role="main" aria-label="회원가입 폼">
      <h1 class="text-2xl font-bold text-center text-blue-600 mb-2" tabindex="0">회원가입</h1>
      <form v-if="!successMessage" @submit.prevent="onSubmit" class="flex flex-col gap-4" autocomplete="on" aria-live="polite">
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
          <label for="nickname" class="block text-sm font-medium text-gray-700 mb-1">닉네임</label>
          <input
            id="nickname"
            v-model="nickname"
            type="text"
            autocomplete="nickname"
            required
            aria-required="true"
            aria-invalid="true"
            aria-describedby="nicknameError"
            class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-400 focus:outline-none text-base min-h-[44px]"
            :class="{'border-red-400 ring-2 ring-red-300': nicknameError, 'focus:ring-blue-400': !nicknameError}"
            placeholder="닉네임"
            @input="nicknameError = validateNickname(nickname)"
          />
          <transition name="fade">
            <p v-if="nicknameError" id="nicknameError" class="text-xs text-red-500 mt-1 animate-shake">{{ nicknameError }}</p>
          </transition>
        </div>
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
          <input
            id="password"
            v-model="password"
            type="password"
            autocomplete="new-password"
            required
            minlength="6"
            aria-required="true"
            aria-invalid="true"
            aria-describedby="passwordError"
            class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-400 focus:outline-none text-base min-h-[44px]"
            :class="{'border-red-400 ring-2 ring-red-300': passwordError, 'focus:ring-blue-400': !passwordError}"
            placeholder="비밀번호 (6자 이상)"
            @input="passwordError = validatePassword(password)"
          />
          <transition name="fade">
            <p v-if="passwordError" id="passwordError" class="text-xs text-red-500 mt-1 animate-shake">{{ passwordError }}</p>
          </transition>
        </div>
        <div>
          <label for="password2" class="block text-sm font-medium text-gray-700 mb-1">비밀번호 확인</label>
          <input
            id="password2"
            v-model="password2"
            type="password"
            autocomplete="new-password"
            required
            aria-required="true"
            aria-invalid="true"
            aria-describedby="password2Error"
            class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-400 focus:outline-none text-base min-h-[44px]"
            :class="{'border-red-400 ring-2 ring-red-300': password2Error, 'focus:ring-blue-400': !password2Error}"
            placeholder="비밀번호 확인"
            @input="password2Error = validatePassword2(password2)"
          />
          <transition name="fade">
            <p v-if="password2Error" id="password2Error" class="text-xs text-red-500 mt-1 animate-shake">{{ password2Error }}</p>
          </transition>
        </div>
        <button
          type="submit"
          class="w-full py-3 mt-2 rounded-lg bg-blue-500 text-white font-semibold text-lg shadow-md hover:bg-blue-600 transition active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed min-h-[44px]"
          :disabled="loading"
          aria-busy="loading"
        >
          <span v-if="loading" class="flex items-center justify-center gap-2">
            <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path></svg>
            가입 중...
          </span>
          <span v-else>회원가입</span>
        </button>
        <transition name="fade">
          <p v-if="errorMessage" class="text-center text-sm text-red-500 mt-2 animate-shake" aria-live="assertive">{{ errorMessage }}</p>
        </transition>
      </form>
      <transition name="fade">
        <div v-if="successMessage" class="flex flex-col items-center gap-4">
          <p class="text-center text-green-600 text-base">{{ successMessage }}</p>
          <button @click="goLogin" class="w-full py-3 rounded-lg bg-blue-500 text-white font-semibold text-lg shadow-md hover:bg-blue-600 transition active:scale-95 min-h-[44px]">로그인 하러가기</button>
        </div>
      </transition>
      <div class="text-center text-xs text-gray-400 mt-2" tabindex="0">© 쏠쏠Hey</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { signup, handleApiError } from '../api/index';
import type { SignupRequest } from '../types/api';

const userId = ref('');
const nickname = ref('');
const password = ref('');
const password2 = ref('');
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const userIdError = ref('');
const nicknameError = ref('');
const passwordError = ref('');
const password2Error = ref('');
const router = useRouter();

function validateUserId(value: string) {
  if (!value) return '이메일을 입력하세요.';
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!re.test(value)) return '올바른 이메일 형식이 아닙니다.';
  return '';
}

function validateNickname(value: string) {
  if (!value) return '닉네임을 입력하세요.';
  if (value.length < 2) return '닉네임은 2자 이상이어야 합니다.';
  return '';
}

function validatePassword(value: string) {
  if (!value) return '비밀번호를 입력하세요.';
  if (value.length < 6) return '비밀번호는 6자 이상이어야 합니다.';
  return '';
}

function validatePassword2(value: string) {
  if (!value) return '비밀번호 확인을 입력하세요.';
  if (value !== password.value) return '비밀번호가 일치하지 않습니다.';
  return '';
}

function goLogin() {
  router.push('/');
}

async function onSubmit() {
  // 클라이언트 측 유효성 검사
  userIdError.value = validateUserId(userId.value);
  nicknameError.value = validateNickname(nickname.value);
  passwordError.value = validatePassword(password.value);
  password2Error.value = validatePassword2(password2.value);
  errorMessage.value = '';
  successMessage.value = '';
  
  if (userIdError.value || nicknameError.value || passwordError.value || password2Error.value) {
    return;
  }

  loading.value = true;
  
  try {
    const signupData: SignupRequest = {
      userId: userId.value,
      password: password.value,
      nickname: nickname.value,
    };

    const response = await signup(signupData);
    
    if (response.success) {
      successMessage.value = response.message || '회원가입이 완료되었습니다!';
      
      // 입력 필드 초기화
      userId.value = '';
      nickname.value = '';
      password.value = '';
      password2.value = '';
      
      // 1.5초 후 로그인 페이지로 이동
      setTimeout(() => {
        router.push('/');
      }, 1500);
    } else {
      // 서버에서 성공하지 않은 응답을 보낸 경우
      errorMessage.value = response.message || '회원가입에 실패했습니다.';
      
      // 서버 측 필드별 에러 처리
      if (response.errors) {
        if (response.errors.username) {
          userIdError.value = response.errors.username;
        }
        if (response.errors.email) {
          userIdError.value = response.errors.email;
        }
        if (response.errors.nickname) {
          nicknameError.value = response.errors.nickname;
        }
        if (response.errors.password) {
          passwordError.value = response.errors.password;
        }
      }
    }
  } catch (error: any) {
    errorMessage.value = handleApiError(error);
    console.error('회원가입 오류:', error);
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
