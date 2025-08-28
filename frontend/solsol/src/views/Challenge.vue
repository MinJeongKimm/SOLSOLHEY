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
        
        <!-- 챌린지 상태 필터 - 우측에 작은 드롭다운 -->
        <div class="flex justify-end">
          <div class="w-32">
            <Dropdown
              v-model="selectedStatus"
              :options="statusOptions"
              placeholder="상태 선택"
            />
          </div>
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
          :class="[
            'rounded-xl p-4 shadow-sm border transition-all cursor-pointer',
            challenge.isJoined && challenge.userStatus === 'COMPLETED'
              ? 'bg-gray-100 border-gray-200 opacity-60' // 완료된 챌린지: 회색, 투명도 낮춤
              : 'bg-white border-gray-100 hover:shadow-md' // 진행 중인 챌린지: 흰색, 호버 효과
          ]"
        >
          <div class="flex items-center space-x-4">
            <!-- 챌린지 아이콘 -->
            <div class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0 relative" 
                 :class="getRewardType(challenge) === 'points' ? 'bg-blue-500' : 'bg-green-500'">
              <span class="text-white font-bold text-lg">$</span>
              
              <!-- 완료 상태 표시 -->
              <div v-if="challenge.isJoined && challenge.userStatus === 'COMPLETED'" 
                   class="absolute -top-1 -right-1 w-6 h-6 bg-green-500 rounded-full flex items-center justify-center border-2 border-white">
                <span class="text-white text-xs">✓</span>
              </div>
            </div>

            <!-- 챌린지 정보 -->
            <div class="flex-1 min-w-0">
              <h3 class="font-medium text-base mb-1"
                  :class="challenge.isJoined && challenge.userStatus === 'COMPLETED' ? 'text-gray-500' : 'text-gray-800'">
                {{ challenge.challengeName }}
              </h3>
              <p class="text-sm"
                 :class="challenge.isJoined && challenge.userStatus === 'COMPLETED' ? 'text-gray-400' : 'text-gray-500'">
                <span v-if="getRewardType(challenge) === 'points'" 
                      :class="challenge.isJoined && challenge.userStatus === 'COMPLETED' ? 'text-gray-400' : 'text-blue-600'">
                  {{ challenge.rewardPoints }}P
                </span>
                
                <!-- 완료 상태 텍스트 -->
                <span v-if="challenge.isJoined && challenge.userStatus === 'COMPLETED'" 
                      class="ml-2 text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full">
                  완료됨
                </span>
                <!-- 참여중 배지 -->
                <span v-else-if="challenge.isJoined" 
                      class="ml-2 text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded-full">
                  참여중
                </span>
              </p>
            </div>

            <!-- 화살표 아이콘 -->
            <div class="flex-shrink-0">
              <svg class="w-5 h-5" 
                   :class="challenge.isJoined && challenge.userStatus === 'COMPLETED' ? 'text-gray-300' : 'text-gray-400'"
                   fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
        <div class="flex space-x-2 mt-6">
          <button 
            @click="joinSelectedChallenge"
            :disabled="selectedChallenge.isJoined"
            :class="[
              'flex-1 sm:flex-none py-2 sm:py-3 px-3 sm:px-4 rounded-lg font-medium transition-colors whitespace-nowrap text-xs sm:text-sm',
              selectedChallenge.isJoined
                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                : 'bg-blue-500 text-white hover:bg-blue-600'
            ]"
          >
            {{ selectedChallenge.isJoined ? '이미 참여중' : '챌린지 참여' }}
          </button>
          
          <!-- 완료된 챌린지인 경우 완료 상태 표시 -->
          <div v-if="selectedChallenge.isJoined && selectedChallenge.userStatus === 'COMPLETED'" 
                class="flex-1 sm:flex-none py-2 sm:py-3 px-3 sm:px-4 bg-green-100 text-green-700 rounded-lg font-medium flex items-center justify-center whitespace-nowrap text-xs sm:text-sm">
            <span class="mr-2">🎉</span>
            챌린지 완료!
          </div>
          
          <button 
            @click="selectedChallenge = null"
            class="flex-1 sm:flex-none py-2 sm:py-3 px-3 sm:px-4 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300 transition-colors whitespace-nowrap text-xs sm:text-sm"
          >
            닫기
          </button>
        </div>
      </div>
    </div>

    <!-- 금융 API 팝업 -->
    <FinanceApiModal
      :visible="showFinanceModal"
      :title="financeModalTitle"
      :defaultTab="financeDefaultTab"
      :onlyTab="financeDefaultTab"
      :challengeId="financeChallengeId"
      :targetCount="financeTargetCount"
      @close="closeFinanceModal"
      @succeeded="onFinanceSucceeded"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getChallenges, joinChallenge, updateChallengeProgress } from '../api/index';
import { usePointStore } from '../stores/point';
import type { Challenge } from '../types/api';
import Dropdown from '../components/Dropdown.vue';
import FinanceApiModal from '../components/FinanceApiModal.vue';

const router = useRouter();
const pointStore = usePointStore();

// 반응형 데이터
const challenges = ref<Challenge[]>([]);
const selectedChallenge = ref<Challenge | null>(null);
const loading = ref(false);
const error = ref('');
const selectedCategory = ref<'all' | 'ACADEMIC' | 'FINANCE' | 'SOCIAL' | 'EVENT'>('all');
const selectedStatus = ref<'all' | 'available' | 'completed'>('all');

// 챌린지 상태 드롭다운 옵션 데이터
const statusOptions = [
  { value: 'all', label: '전체 상태' },
  { value: 'available', label: '진행 가능' },
  { value: 'completed', label: '완료됨' }
];

// 진행도 관련 반응형 데이터
const currentProgress = ref(0);
const isCompleted = ref(false);
const rewardPoints = ref(0);
const progressStep = ref<number | null>(null);
const updatingProgress = ref(false);

// 포인트 상태는 Store에서 관리
const userPoints = computed(() => pointStore.userPoints);

// 리스트 내 특정 챌린지 상태 동기화 헬퍼
function syncChallengeStatusInList(challengeId: number, updates: Partial<Challenge>) {
  const idx = challenges.value.findIndex(c => c.challengeId === challengeId);
  if (idx >= 0) {
    challenges.value[idx] = { ...challenges.value[idx], ...updates } as Challenge;
  }
}

// 필터링된 챌린지 목록
const filteredChallenges = computed(() => {
  return challenges.value.filter(challenge => {
    // 카테고리 필터링
    if (selectedCategory.value !== 'all') {
      if (challenge.categoryName !== selectedCategory.value) {
        return false;
      }
    }
    
    // 챌린지 상태 필터링
    if (selectedStatus.value !== 'all') {
      if (selectedStatus.value === 'available') {
        // 진행 가능한 챌린지: 참여하지 않았거나 진행 중인 상태
        if (challenge.isJoined && challenge.userStatus === 'COMPLETED') {
          return false;
        }
      } else if (selectedStatus.value === 'completed') {
        // 완료된 챌린지: 참여했고 완료된 상태
        if (!challenge.isJoined || challenge.userStatus !== 'COMPLETED') {
          return false;
        }
      }
    }
    
    return true;
  });
});

// 완료 버튼은 기존 로직 유지

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

// 챌린지 선택
function selectChallenge(challenge: Challenge) {
  // 금융 챌린지는 외부 조회 성공 전에는 항상 '참여' 단계로 보이도록 처리
  // - 보류 중(pending) 이거나
  // - 재로그인 등으로 로컬 상태가 사라졌더라도 서버 상태가 미완료라면
  //   상세 화면에서는 '참여' 버튼이 보이게 한다.
  let ch: Challenge = challenge;
  const isFinance = challenge.categoryName === 'FINANCE';
  const notCompleted = challenge.userStatus !== 'COMPLETED';
  const isPending = pendingFinance.value.has(challenge.challengeId);
  const notSucceeded = !succeededFinance.value.has(challenge.challengeId);
  if (isFinance && notCompleted && (isPending || notSucceeded)) {
    ch = { ...challenge, isJoined: false, userStatus: 'NOT_JOINED' } as any;
  }
  selectedChallenge.value = ch;
  
  // 진행도 초기화
  if (challenge.isJoined) {
    // 참여 중인 챌린지인 경우 진행도 로드
    loadChallengeProgress(challenge.challengeId);
    
    // 완료된 챌린지인 경우 알림
    if (challenge.userStatus === 'COMPLETED') {
      console.log('완료된 챌린지 선택됨:', challenge.challengeName);
    }
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
    
    // 챌린지 상태에 따른 진행도 설정
    const userChallenge = challenges.value.find(c => c.challengeId === challengeId);
    if (userChallenge && userChallenge.isJoined) {
      // 참여 중인 챌린지인 경우 상태 확인
      if (userChallenge.userStatus === 'COMPLETED') {
        currentProgress.value = userChallenge.targetCount || 0;
        isCompleted.value = true;
        rewardPoints.value = userChallenge.rewardPoints || 0;
      } else {
        currentProgress.value = 0;
        isCompleted.value = false;
        rewardPoints.value = 0;
      }
    } else {
      currentProgress.value = 0;
      isCompleted.value = false;
      rewardPoints.value = 0;
    }
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

// 금융 팝업 상태 및 유틸
type FinanceTab = 'EXCHANGE_RATES' | 'SINGLE_RATE' | 'ESTIMATE' | 'TX_HISTORY';
const showFinanceModal = ref(false);
const financeModalTitle = ref('');
const financeDefaultTab = ref<FinanceTab>('EXCHANGE_RATES');
const financeChallengeId = ref<number | null>(null);
const financeTargetCount = ref<number | null>(null);
// 금융 챌린지 '참여 후 외부 액션 미수행' 상태를 로컬로 관리
const pendingFinance = ref<Set<number>>(new Set());
// 금융 챌린지 액션 성공 후(아직 완료 버튼 미누름) 상태
const succeededFinance = ref<Set<number>>(new Set());

function inferFinanceTabByName(name: string): FinanceTab {
  const n = (name || '').toLowerCase();
  if (n.includes('환율 전체') || n.includes('전체 환율') || n.includes('환율전체')) return 'EXCHANGE_RATES';
  if (n.includes('환율 확인') || n.includes('단건') || n.includes('환율확인')) return 'SINGLE_RATE';
  if (n.includes('환전') || n.includes('예상') || n.includes('환전예상')) return 'ESTIMATE';
  if (n.includes('거래내역')) return 'TX_HISTORY';
  return 'EXCHANGE_RATES';
}

function isRecognizedFinanceAction(name: string): boolean {
  const n = (name || '').toLowerCase();
  return (
    n.includes('환율 전체') || n.includes('전체 환율') || n.includes('환율전체') ||
    n.includes('환율 확인') || n.includes('단건') || n.includes('환율확인') ||
    n.includes('환전') || n.includes('예상') || n.includes('환전예상') ||
    n.includes('거래내역')
  );
}

function openFinanceModalFor(ch: Challenge) {
  financeModalTitle.value = ch.challengeName;
  financeDefaultTab.value = inferFinanceTabByName(ch.challengeName);
  financeChallengeId.value = ch.challengeId;
  financeTargetCount.value = ch.targetCount || 1;
  showFinanceModal.value = true;
}

function onFinanceCompleted() {
  if (financeChallengeId.value != null) {
    pendingFinance.value.delete(financeChallengeId.value);
  }
  showFinanceModal.value = false;
  loadChallenges();
}

function closeFinanceModal() {
  showFinanceModal.value = false;
  // 닫기 시: 보류 중이면 '처음 화면'으로, 성공이면 '완료하기 버튼' 화면으로
  if (financeChallengeId.value != null && pendingFinance.value.has(financeChallengeId.value)) {
    const id = financeChallengeId.value;
    const found = challenges.value.find(c => c.challengeId === id);
    if (found) {
      const ch: Challenge = { ...found, isJoined: false, userStatus: 'NOT_JOINED' } as any;
      selectedChallenge.value = ch;
    }
  } else if (financeChallengeId.value != null && succeededFinance.value.has(financeChallengeId.value)) {
    const id = financeChallengeId.value;
    const found = challenges.value.find(c => c.challengeId === id);
    if (found) {
      // 참여중 + 미완료 상태로 상세 모달 열기 → '챌린지 완료하기' 버튼 노출
      const ch: Challenge = { ...found, isJoined: true, userStatus: 'IN_PROGRESS' } as any;
      selectedChallenge.value = ch;
      // 진행도 초기화 표시
      currentProgress.value = 0;
      isCompleted.value = false;
      rewardPoints.value = 0;
      progressStep.value = null;
    }
  }
  // 목록도 동기화
  loadChallenges();
}

function onFinanceSucceeded() {
  if (financeChallengeId.value != null) {
    pendingFinance.value.delete(financeChallengeId.value);
    succeededFinance.value.add(financeChallengeId.value);
  }
}

// 챌린지 타입별 보상 결정 (백엔드 타입에 맞춤)
function getRewardType(challenge: Challenge): 'points' | 'exp' {
  // 정책 변경: 챌린지는 포인트만 지급
  return 'points';
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
    const cid = selectedChallenge.value.challengeId;
    const isFinance = selectedChallenge.value.categoryName === 'FINANCE';
    // 보류 중이거나(로컬) 이미 서버에서 참여 상태인 금융 챌린지는 재참여 API를 건너뛰고 모달만 연다
    const original = challenges.value.find(c => c.challengeId === cid);
    const alreadyJoinedOnServer = !!original?.isJoined;
    if (isFinance && (pendingFinance.value.has(cid) || alreadyJoinedOnServer)) {
      const joined = selectedChallenge.value;
      selectedChallenge.value = null;
      openFinanceModalFor(joined);
      return;
    }

    await joinChallenge(cid);
    
    // 참여 상태 업데이트
    selectedChallenge.value.isJoined = true;
    // 목록에도 즉시 반영
    syncChallengeStatusInList(selectedChallenge.value.challengeId, { isJoined: true });
    // 진행도 초기화 및 로드 (비금융의 경우 유지)
    if (!isFinance) {
      await loadChallengeProgress(cid);
    }
    
    // 모달 닫기 및 다음 동작
    const joined = selectedChallenge.value;
    selectedChallenge.value = null;

    if (isFinance && joined && isRecognizedFinanceAction(joined.challengeName)) {
      // 금융은 외부 액션 성공 전까지 로컬로 보류 처리하여 다음에 눌렀을 때 '처음 화면'으로 보이게 함
      pendingFinance.value.add(joined.challengeId);
      // 금융 챌린지는 팝업으로 진행
      openFinanceModalFor(joined);
    } else {
      // 비금융은 목록 새로고침 유지
      await loadChallenges();
    }
    
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
    
    if (response.success && response.userChallenge) {
      // 진행도 업데이트 - 백엔드 응답 구조에 맞게 수정
      currentProgress.value = response.userChallenge.progressCount;
      isCompleted.value = response.isCompleted || false;
      
      // 보상 지급 확인 - 백엔드 응답 구조에 맞게 수정
      if (response.rewardPoints && response.rewardPoints > 0) {
        rewardPoints.value = response.rewardPoints;
        // 사용자 포인트 실시간 업데이트
        pointStore.updatePoints(response.rewardPoints);
        // 백엔드 응답에 이미 포인트 정보가 있으므로 별도 API 호출 불필요
        alert(`진행도가 업데이트되었습니다! +${rewardPoints.value}P 획득!`);
      } else {
        alert('진행도가 업데이트되었습니다!');
      }

      // 완료 상태 동기화 (isCompleted 플래그 true인 경우)
      if (response.isCompleted) {
        if (selectedChallenge.value) {
          selectedChallenge.value.isJoined = true;
          selectedChallenge.value.userStatus = 'COMPLETED';
          syncChallengeStatusInList(selectedChallenge.value.challengeId, { isJoined: true, userStatus: 'COMPLETED' as any });
        }
        await loadChallenges();
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
  const ch = selectedChallenge.value;
  const cid = ch.challengeId;
  const isFinance = ch.categoryName === 'FINANCE';
  const isFinanceAction = isFinance && isRecognizedFinanceAction(ch.challengeName);
  let payload = '챌린지 완료';
  if (isFinanceAction) {
    // 금융은 조회 성공 후에만 완료 가능
    if (!succeededFinance.value.has(cid)) {
      alert('외부 조회를 완료한 뒤에 완료할 수 있습니다.');
      updatingProgress.value = false;
      return;
    }
    const action = inferFinanceTabByName(ch.challengeName);
    payload = `FINANCE_${action}_SUCCESS`;
  }
    // 목표 진행도로 바로 완료 처리
    const response = await updateChallengeProgress(cid, {
      step: ch.targetCount,
      payload,
    });
    
    if (response.success && response.userChallenge) {
      // 진행도 업데이트 - 백엔드 응답 구조에 맞게 수정
      currentProgress.value = response.userChallenge.progressCount;
      isCompleted.value = response.isCompleted || false;
      
      // 보상 지급 확인 - 백엔드 응답 구조에 맞게 수정
      if (response.rewardPoints && response.rewardPoints > 0) {
        rewardPoints.value = response.rewardPoints;
        
        // 사용자 포인트 실시간 업데이트
        pointStore.updatePoints(response.rewardPoints);
        
        // 백엔드 응답에 이미 포인트 정보가 있으므로 별도 API 호출 불필요
        alert(`🎉 챌린지 완료! +${rewardPoints.value}P 획득!`);
      } else {
        alert('🎉 챌린지가 완료되었습니다!');
      }

      // 완료 상태 동기화
      if (selectedChallenge.value) {
        selectedChallenge.value.isJoined = true;
        selectedChallenge.value.userStatus = 'COMPLETED';
        syncChallengeStatusInList(selectedChallenge.value.challengeId, { isJoined: true, userStatus: 'COMPLETED' as any });
      }

      await loadChallenges();
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
    pointStore.loadPoints() // Store에서 포인트 로드
  ]);
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>
