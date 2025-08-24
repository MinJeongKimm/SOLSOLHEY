import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getUserInfo } from '../api/index';
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
      const user = auth.getUser();
      if (user && user.userId) {
        const userInfo = await getUserInfo(Number(user.userId));
        userPoints.value = userInfo.totalPoints;
      }
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
