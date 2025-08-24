<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 상단 바 -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="flex items-center justify-between px-4 py-3">
        <!-- 뒤로가기 버튼 -->
        <button 
          @click="goBack"
          class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

                 <!-- 제목 -->
         <div class="text-center">
           <h1 class="text-xl font-bold text-gray-800">Challenge</h1>
         </div>

        <!-- 보유 포인트 배지 -->
        <div class="flex items-center space-x-2 bg-gray-100 px-3 py-2 rounded-full shadow-sm">
          <div class="w-6 h-6 bg-yellow-400 rounded-full flex items-center justify-center">
            <span class="text-white font-bold text-sm">$</span>
          </div>
          <span class="font-bold text-gray-800">{{ userPoints }}P</span>
        </div>
      </div>
    </div>

    <!-- 챌린지 목록 -->
    <div class="p-4 space-y-4">
      <!-- 카테고리 필터 탭 -->
      <div class="space-y-3">
        <!-- 보상 타입 탭 -->
        <div class="flex space-x-1 bg-gray-100 p-1 rounded-lg">
          <button 
            @click="selectedRewardType = 'all'"
            :class="[
              'flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors',
              selectedRewardType === 'all' 
                ? 'bg-white text-gray-900 shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            전체
          </button>
          <button 
            @click="selectedRewardType = 'points'"
            :class="[
              'flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors',
              selectedRewardType === 'points' 
                ? 'bg-white text-gray-900 shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            포인트
          </button>
          <button 
            @click="selectedRewardType = 'exp'"
            :class="[
              'flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors',
              selectedRewardType === 'exp' 
                ? 'bg-white text-gray-900 shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            경험치
          </button>
        </div>

        <!-- 카테고리 탭 -->
        <div class="flex flex-wrap gap-2">
          <button 
            @click="selectedCategory = 'all'"
            :class="[
              'px-3 py-2 rounded-full text-sm font-medium transition-colors',
              selectedCategory === 'all' 
                ? 'bg-blue-500 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            ]"
          >
            전체
          </button>
          <button 
            @click="selectedCategory = 'ACADEMIC'"
            :class="[
              'px-3 py-2 rounded-full text-sm font-medium transition-colors',
              selectedCategory === 'ACADEMIC' 
                ? 'bg-blue-500 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            ]"
          >
            학사
          </button>
          <button 
            @click="selectedCategory = 'FINANCE'"
            :class="[
              'px-3 py-2 rounded-full text-sm font-medium transition-colors',
              selectedCategory === 'FINANCE' 
                ? 'bg-blue-500 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            ]"
          >
            금융
          </button>
          <button 
            @click="selectedCategory = 'SOCIAL'"
            :class="[
              'px-3 py-2 rounded-full text-sm font-medium transition-colors',
              selectedCategory === 'SOCIAL' 
                ? 'bg-blue-500 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            ]"
          >
            소셜
          </button>
          <button 
            @click="selectedCategory = 'EVENT'"
            :class="[
              'px-3 py-2 rounded-full text-sm font-medium transition-colors',
              selectedCategory === 'EVENT' 
                ? 'bg-blue-500 text-white' 
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            ]"
          >
            이벤트
          </button>
        </div>
      </div>

      <!-- 로딩 상태 -->
      <div v-if="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>

      <!-- 에러 상태 -->
      <div v-else-if="error" class="text-center py-8">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button 
          @click="loadChallenges"
          class="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
        >
          다시 시도
        </button>
      </div>

      <!-- 챌린지 목록 -->
      <div v-else-if="filteredChallenges.length > 0" class="space-y-3">
        <div 
          v-for="challenge in filteredChallenges" 
          :key="challenge.challengeId"
          @click="selectChallenge(challenge)"
          class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 hover:shadow-md transition-all cursor-pointer"
        >
          <div class="flex items-center space-x-4">
            <!-- 챌린지 아이콘 -->
            <div class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0" 
                 :class="getRewardType(challenge) === 'points' ? 'bg-blue-500' : 'bg-green-500'">
              <span class="text-white font-bold text-lg">$</span>
            </div>

                         <!-- 챌린지 정보 -->
             <div class="flex-1 min-w-0">
               <h3 class="font-medium text-gray-800 text-base mb-1">
                 {{ challenge.challengeName }}
               </h3>
               <p class="text-sm text-gray-500">
                 <span v-if="getRewardType(challenge) === 'points'" class="text-blue-600">
                   {{ challenge.rewardPoints }}P
                 </span>
                 <span v-else class="text-green-600">
                   {{ challenge.rewardExp }}XP
                 </span>
               </p>
             </div>

            <!-- 화살표 아이콘 -->
            <div class="flex-shrink-0">
              <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else-if="challenges.length === 0" class="text-center py-8">
        <div class="text-6xl mb-4">🎯</div>
        <p class="text-gray-500 mb-2">현재 진행 가능한 챌린지가 없습니다</p>
        <p class="text-sm text-gray-400">새로운 챌린지가 추가될 때까지 기다려주세요!</p>
      </div>

      <!-- 필터링 결과 없음 -->
      <div v-else class="text-center py-8">
        <div class="text-6xl mb-4">🔍</div>
        <p class="text-gray-500 mb-2">선택한 필터에 맞는 챌린지가 없습니다</p>
        <p class="text-sm text-gray-400">다른 필터를 선택해보세요!</p>
      </div>
    </div>

    <!-- 챌린지 상세 모달 -->
    <div v-if="selectedChallenge" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
        <!-- 모달 헤더 -->
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-bold text-gray-800">챌린지 상세</h3>
          <button 
            @click="selectedChallenge = null"
            class="text-gray-400 hover:text-gray-600 text-2xl"
          >
            ×
          </button>
        </div>

        <!-- 챌린지 정보 -->
        <div class="space-y-4">
          <div>
            <h4 class="font-semibold text-gray-800 mb-2">{{ selectedChallenge.challengeName }}</h4>
            <p class="text-gray-600 text-sm">{{ selectedChallenge.description }}</p>
          </div>

                                <!-- 보상 정보 (챌린지 타입별로 하나만 표시) -->
           <div class="bg-gradient-to-r from-blue-50 to-green-50 p-4 rounded-lg border border-blue-100">
             <p class="text-xs text-gray-600 mb-2 text-center">보상</p>
             <div class="text-center">
               <span v-if="getRewardType(selectedChallenge) === 'points'" class="text-2xl font-bold text-blue-600">
                 {{ selectedChallenge.rewardPoints }}P
               </span>
               <span v-else class="text-2xl font-bold text-green-600">
                 {{ selectedChallenge.rewardExp }}XP
               </span>
               <p class="text-sm text-gray-500 mt-1">
                 {{ getRewardType(selectedChallenge) === 'points' ? '포인트' : '경험치' }} 획득
               </p>
             </div>
           </div>

           <!-- 카테고리 및 달성 방법 -->
           <div class="space-y-3">
             <!-- 카테고리 -->
             <div class="flex items-center space-x-2">
               <div class="w-3 h-3 rounded-full" :class="getCategoryColor(selectedChallenge.categoryName)"></div>
               <span class="text-sm font-medium text-gray-700">{{ selectedChallenge.categoryDisplayName }}</span>
             </div>
             
             <!-- 달성 방법 -->
             <div class="mb-6">
               <h5 class="text-sm font-medium text-gray-800 mb-2">🎯 달성 방법</h5>
               <p class="text-sm text-gray-600 leading-relaxed">
                 {{ getAchievementGuide(selectedChallenge) }}
               </p>
             </div>

             <!-- 진행도 표시 섹션 -->
             <div v-if="selectedChallenge.isJoined" class="mb-6">
               <h5 class="text-sm font-medium text-gray-800 mb-3">📊 진행도</h5>
               
               <!-- 현재 진행도 표시 -->
               <div class="bg-gray-50 rounded-lg p-4 mb-4">
                 <div class="flex justify-between items-center mb-2">
                   <span class="text-sm font-medium text-gray-700">현재 진행도</span>
                   <span class="text-sm text-gray-600">{{ currentProgress }}/{{ selectedChallenge.targetCount }}</span>
                 </div>
                 
                 <!-- 프로그레스 바 -->
                 <div class="w-full bg-gray-200 rounded-full h-2.5">
                   <div 
                     class="bg-blue-600 h-2.5 rounded-full transition-all duration-300"
                     :style="{ width: `${(currentProgress / selectedChallenge.targetCount) * 100}%` }"
                   ></div>
                 </div>
                 
                 <!-- 완료 상태 표시 -->
                 <div v-if="isCompleted" class="mt-2 text-center">
                   <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
                     🎉 챌린지 완료!
                   </span>
                   <div v-if="rewardPoints > 0" class="mt-2 text-sm text-green-600">
                     +{{ rewardPoints }}P 획득!
                   </div>
                 </div>
               </div>

                <!-- 진행도 업데이트 폼 -->
                <div v-if="!isCompleted" class="bg-blue-50 rounded-lg p-4">
                  <h6 class="text-sm font-medium text-blue-800 mb-3">진행 완료</h6>
                  
                  <div class="text-center">
                    <button
                      @click="completeChallenge"
                      :disabled="updatingProgress"
                      :class="[
                        'px-6 py-3 rounded-lg text-sm font-medium transition-colors',
                        updatingProgress
                          ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                          : 'bg-green-500 text-white hover:bg-green-600'
                      ]"
                    >
                      <span v-if="updatingProgress" class="flex items-center justify-center">
                        <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                        완료 처리 중...
                      </span>
                      <span v-else>🎯 챌린지 완료하기</span>
                    </button>
                  </div>
                  
                  <p class="text-xs text-blue-600 mt-2 text-center">
                    챌린지 목표를 달성했다면 이 버튼을 눌러주세요!
                  </p>
                </div>
             </div>
           </div>          
        </div>

                 <!-- 액션 버튼 -->
         <div class="flex space-x-3 mt-6">
           <button 
             @click="joinSelectedChallenge"
             :disabled="selectedChallenge.isJoined"
             :class="[
               'flex-1 py-3 px-4 rounded-lg font-medium transition-colors',
               selectedChallenge.isJoined
                 ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                 : 'bg-blue-500 text-white hover:bg-blue-600'
             ]"
           >
             {{ selectedChallenge.isJoined ? '이미 참여중' : '챌린지 참여' }}
           </button>
           <button 
             @click="selectedChallenge = null"
             class="flex-1 py-3 px-4 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300 transition-colors"
           >
             닫기
           </button>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getChallenges, joinChallenge, getUserInfo, auth, updateChallengeProgress } from '../api/index';
import type { Challenge } from '../types/api';

const router = useRouter();

// 반응형 데이터
const challenges = ref<Challenge[]>([]);
const selectedChallenge = ref<Challenge | null>(null);
const loading = ref(false);
const error = ref('');
const userPoints = ref(0);
const selectedRewardType = ref<'all' | 'points' | 'exp'>('all');
const selectedCategory = ref<'all' | 'ACADEMIC' | 'FINANCE' | 'SOCIAL' | 'EVENT'>('all');

// 진행도 관련 반응형 데이터
const currentProgress = ref(0);
const isCompleted = ref(false);
const rewardPoints = ref(0);
const progressStep = ref<number | null>(null);
const updatingProgress = ref(false);

// 필터링된 챌린지 목록
const filteredChallenges = computed(() => {
  return challenges.value.filter(challenge => {
    // 보상 타입 필터링
    if (selectedRewardType.value !== 'all') {
      if (getRewardType(challenge) !== selectedRewardType.value) {
        return false;
      }
    }
    
    // 카테고리 필터링
    if (selectedCategory.value !== 'all') {
      if (challenge.categoryName !== selectedCategory.value) {
        return false;
      }
    }
    
    return true;
  });
});

// 라우터 함수
function goBack() {
  router.back();
}

// 챌린지 목록 로드
async function loadChallenges() {
  loading.value = true;
  error.value = '';
  
  try {
    const challengesData = await getChallenges();
    challenges.value = challengesData;
  } catch (err: any) {
    console.error('챌린지 목록 로드 실패:', err);
    error.value = '챌린지 목록을 불러오는데 실패했습니다.';
  } finally {
    loading.value = false;
  }
}

// 사용자 포인트 로드
async function loadUserPoints() {
  try {
    const user = auth.getUser();
    if (user && user.userId) {
      const userInfo = await getUserInfo(Number(user.userId));
      userPoints.value = userInfo.totalPoints;
    }
  } catch (err) {
    console.error('사용자 포인트 로드 실패:', err);
    // 기본값 설정
    userPoints.value = 15000;
  }
}

// 챌린지 선택
function selectChallenge(challenge: Challenge) {
  selectedChallenge.value = challenge;
  
  // 진행도 초기화
  if (challenge.isJoined) {
    // 참여 중인 챌린지인 경우 진행도 로드
    loadChallengeProgress(challenge.challengeId);
  } else {
    // 참여하지 않은 챌린지인 경우 진행도 초기화
    currentProgress.value = 0;
    isCompleted.value = false;
    rewardPoints.value = 0;
    progressStep.value = null;
  }
}

// 챌린지 진행도 로드
async function loadChallengeProgress(challengeId: number) {
  try {
    // 현재는 진행도 조회 API가 없으므로 기본값으로 설정
    // 실제로는 백엔드에서 진행도 조회 API를 구현해야 함
    currentProgress.value = 0; // 기본값
    isCompleted.value = false;
    rewardPoints.value = 0;
    progressStep.value = null;
  } catch (err) {
    console.error('진행도 로드 실패:', err);
    // 에러 발생 시 기본값 설정
    currentProgress.value = 0;
    isCompleted.value = false;
    rewardPoints.value = 0;
    progressStep.value = null;
  }
}

// 챌린지 타입별 보상 결정 (백엔드 타입에 맞춤)
function getRewardType(challenge: Challenge): 'points' | 'exp' {
  // 챌린지 타입에 따라 보상 결정
  switch (challenge.challengeType) {
    case 'DAILY':
    case 'WEEKLY':
      return 'points'; // 일일/주간 챌린지는 포인트 보상
    case 'MONTHLY':
    case 'SPECIAL':
      return 'exp';    // 월간/특별 챌린지는 경험치 보상
    default:
      return 'points'; // 기본값은 포인트
  }
}

// 카테고리별 색상 결정 (백엔드 카테고리에 맞춤)
function getCategoryColor(categoryName: string): string {
  switch (categoryName) {
    case 'FINANCE':
      return 'bg-green-500'; // 금융 - 초록색
    case 'ACADEMIC':
      return 'bg-purple-500'; // 학사 - 보라색
    case 'SOCIAL':
      return 'bg-pink-500';   // 소셜 - 분홍색
    case 'EVENT':
      return 'bg-orange-500'; // 이벤트 - 주황색
    default:
      return 'bg-gray-500';   // 기본 - 회색
  }
}

// 챌린지별 달성 방법 가이드 생성
function getAchievementGuide(challenge: Challenge): string {
  const challengeName = challenge.challengeName.toLowerCase();
  
  if (challengeName.includes('적금') || challengeName.includes('저축')) {
    return '신한은행 앱에서 적금 상품을 선택하고 가입하세요. 가입 완료 후 인증서를 업로드하면 챌린지가 완료됩니다.';
  }
  
  if (challengeName.includes('sol 페이') || challengeName.includes('결제')) {
    return '신한 sol 페이로 온라인 또는 오프라인에서 결제를 진행하세요. 결제 완료 후 자동으로 챌린지가 완료됩니다.';
  }
  
  if (challengeName.includes('친구') || challengeName.includes('초대')) {
    return '친구에게 앱을 추천하고 초대 링크를 공유하세요. 친구가 가입을 완료하면 챌린지가 달성됩니다.';
  }
  
  if (challengeName.includes('출석') || challengeName.includes('도서관')) {
    return '도서관에 방문하여 체크인을 하거나 사진을 찍어 인증하세요. 매일 출석하면 연속 달성 보너스를 받을 수 있습니다.';
  }
  
  if (challengeName.includes('과제') || challengeName.includes('제출')) {
    return '지정된 기간 내에 과제를 완성하고 제출하세요. 제출 완료 시점에 챌린지가 달성됩니다.';
  }
  
  // 기본 가이드
  return '챌린지 설명을 참고하여 목표를 달성하세요. 완료 조건을 만족하면 자동으로 보상이 지급됩니다.';
}

// 챌린지 참여
async function joinSelectedChallenge() {
  if (!selectedChallenge.value) return;
  
  try {
    await joinChallenge(selectedChallenge.value.challengeId);
    
    // 참여 상태 업데이트
    selectedChallenge.value.isJoined = true;
    
    // 진행도 초기화 및 로드
    await loadChallengeProgress(selectedChallenge.value.challengeId);
    
    // 모달 닫기
    selectedChallenge.value = null;
    
    // 챌린지 목록 새로고침
    await loadChallenges();
    
  } catch (err: any) {
    console.error('챌린지 참여 실패:', err);
    alert('챌린지 참여에 실패했습니다.');
  }
}

// 진행도 업데이트
async function updateProgress() {
  if (!selectedChallenge.value || !progressStep.value || progressStep.value < 1 || progressStep.value > selectedChallenge.value.targetCount) {
    alert('유효한 진행도를 입력해주세요.');
    return;
  }

  updatingProgress.value = true;
  try {
    const response = await updateChallengeProgress(selectedChallenge.value.challengeId, {
      step: progressStep.value,
      payload: `진행도 업데이트: ${progressStep.value}`
    });
    
    if (response.success && response.data) {
      // 진행도 업데이트
      currentProgress.value = response.data.currentStep;
      isCompleted.value = response.data.isCompleted;
      
      // 보상 지급 확인
      if (response.data.rewardPoints && response.data.rewardPoints > 0) {
        rewardPoints.value = response.data.rewardPoints;
        // 사용자 포인트 새로고침
        await loadUserPoints();
        alert(`진행도가 업데이트되었습니다! +${rewardPoints.value}P 획득!`);
      } else {
        alert('진행도가 업데이트되었습니다!');
      }
      
      // 진행도 입력 필드 초기화
      progressStep.value = null;
    } else {
      alert(response.message || '진행도 업데이트에 실패했습니다.');
    }
  } catch (err: any) {
    console.error('진행도 업데이트 실패:', err);
    alert('진행도 업데이트에 실패했습니다.');
  } finally {
    updatingProgress.value = false;
  }
}

// 챌린지 완료 처리 (임시)
async function completeChallenge() {
  if (!selectedChallenge.value) return;
  
  updatingProgress.value = true;
  try {
    // 목표 진행도로 바로 완료 처리
    const response = await updateChallengeProgress(selectedChallenge.value.challengeId, {
      step: selectedChallenge.value.targetCount,
      payload: '챌린지 완료'
    });
    
    if (response.success && response.data) {
      // 진행도 업데이트
      currentProgress.value = response.data.currentStep;
      isCompleted.value = response.data.isCompleted;
      
      // 보상 지급 확인
      if (response.data.rewardPoints && response.data.rewardPoints > 0) {
        rewardPoints.value = response.data.rewardPoints;
        // 사용자 포인트 새로고침
        await loadUserPoints();
        alert(`🎉 챌린지 완료! +${rewardPoints.value}P 획득!`);
      } else {
        alert('🎉 챌린지가 완료되었습니다!');
      }
    } else {
      alert(response.message || '챌린지 완료 처리에 실패했습니다.');
    }
  } catch (err: any) {
    console.error('챌린지 완료 처리 실패:', err);
    alert('챌린지 완료 처리에 실패했습니다.');
  } finally {
    updatingProgress.value = false;
  }
}

// 컴포넌트 마운트
onMounted(async () => {
  await Promise.all([
    loadChallenges(),
    loadUserPoints()
  ]);
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>


