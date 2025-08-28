import { defineStore } from 'pinia';
import { ref } from 'vue';
import { auth } from '../api/index';

export const usePointStore = defineStore('point', () => {
  // 상태
  const userPoints = ref(0);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  // 액션
  const loadPoints = async () => {
    isLoading.value = true;
    error.value = null;
    
    try {
      // 최신 사용자 정보 동기화 후 캐시에서 포인트 읽기
      const user: any = await auth.fetchUser();
      userPoints.value = user?.totalPoints ?? 0;
    } catch (err: any) {
      console.error('포인트 로드 실패:', err);
      error.value = '포인트를 불러오는데 실패했습니다.';
      // 에러 발생 시에도 기본값은 0으로 유지
      userPoints.value = 0;
    } finally {
      isLoading.value = false;
    }
  };

  const updatePoints = (amount: number) => {
    userPoints.value += amount;
  };

  const setPoints = (amount: number) => {
    userPoints.value = amount;
  };

  const resetPoints = () => {
    userPoints.value = 0;
  };

  // 게터
  const getCurrentPoints = () => userPoints.value;
  const getIsLoading = () => isLoading.value;
  const getError = () => error.value;

  return {
    // 상태
    userPoints,
    isLoading,
    error,
    
    // 액션
    loadPoints,
    updatePoints,
    setPoints,
    resetPoints,
    
    // 게터
    getCurrentPoints,
    getIsLoading,
    getError,
  };
});
