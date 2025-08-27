<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-6xl mx-auto p-8">
      <!-- 헤더 -->
      <div class="flex items-center mb-8 relative">
        <!-- 뒤로가기 버튼 (왼쪽) -->
        <router-link 
          to="/mascot" 
          class="p-2 text-purple-500 hover:text-purple-700 transition-colors rounded-full hover:bg-purple-50 absolute left-0"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
          </svg>
        </router-link>
        
        <!-- 제목 (가운데) -->
        <h1 class="text-2xl font-bold text-gray-800 text-center w-full">Ranking</h1>
      </div>

      <!-- 탭 네비게이션 -->
      <div class="border-b border-gray-200 mb-8">
        <nav class="-mb-px flex space-x-8 justify-center">
          <button
            @click="activeTab = 'campus'"
            :class="[
              activeTab === 'campus'
                ? 'border-purple-500 text-purple-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
              'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm'
            ]"
          >
            교내 랭킹
          </button>
          <button
            @click="activeTab = 'national'"
            :class="[
              activeTab === 'national'
                ? 'border-purple-500 text-purple-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
              'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm'
            ]"
          >
            전국 랭킹
          </button>
        </nav>
      </div>

      <!-- 내 순위 섹션 (탭 아래, 고정 위치) -->
      <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-6 mb-6 border-2 border-blue-200">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center">
              <span class="text-white text-xl">👤</span>
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-800">
                {{ activeTab === 'campus' ? '교내' : '전국' }} 내 순위
              </h2>
              <!-- 마스코트가 있는 경우 -->
              <div v-if="hasMascot" class="flex items-center space-x-2">
                <span class="text-2xl font-bold text-blue-600">{{ myRank || '계산 중...' }}위</span>
                <span class="text-sm text-gray-600">• {{ myRank === 1 ? '🥇' : myRank === 2 ? '🥈' : myRank === 3 ? '🥉' : '🏅' }}</span>
              </div>
              <!-- 마스코트가 없는 경우 -->
              <div v-else class="flex items-center space-x-2">
                <span class="text-lg text-gray-600">마스코트가 없습니다</span>
              </div>
            </div>
          </div>
          
          <!-- 마스코트가 없는 경우 마스코트 생성 안내 -->
          <div v-if="!hasMascot">
            <router-link
              to="/mascot-create"
              class="bg-gradient-to-r from-blue-500 to-purple-500 hover:from-blue-600 hover:to-purple-600 text-white px-6 py-3 rounded-lg font-medium transition-all transform hover:scale-105 shadow-lg"
            >
              마스코트 만들기
            </router-link>
          </div>
          

        </div>
        
        <!-- 마스코트가 없는 경우 안내 메시지 -->
        <div v-if="!hasMascot" class="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
          <div class="flex items-center space-x-2">
            <span class="text-blue-600">💡</span>
            <span class="text-blue-700 text-sm">
              {{ activeTab === 'campus' ? '교내' : '전국' }} 랭킹에서 내 순위를 확인하려면 마스코트를 생성하세요! 마스코트를 만들면 자동으로 랭킹에 참가됩니다.
            </span>
          </div>
        </div>
      </div>

      <!-- 교내 랭킹 -->
      <div v-if="activeTab === 'campus'" class="space-y-6">
        <!-- 교내 랭킹 필터 -->
        <div class="flex justify-end">
          <div class="flex items-center space-x-4">
            <select
              v-model="campusFilters.sort"
              @change="loadCampusRankings"
              class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm"
            >
              <option value="votes_desc">득표순</option>
              <option value="trending">트렌딩순</option>
              <option value="newest">최신순</option>
            </select>
            
            <select
              v-model="campusFilters.period"
              @change="loadCampusRankings"
              class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm"
            >
              <option value="daily">일간</option>
              <option value="weekly">주간</option>
              <option value="monthly">월간</option>
              <option value="all">전체</option>
            </select>
          </div>
        </div>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex justify-center items-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
        </div>

        <!-- 에러 상태 -->
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
          <div class="text-red-600 text-lg font-medium mb-2">오류가 발생했습니다</div>
          <div class="text-red-500 mb-4">{{ error }}</div>
          <button
            @click="loadCampusRankings"
            class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            다시 시도
          </button>
        </div>

        <!-- 랭킹 목록 -->
        <div v-else-if="campusRankings" class="space-y-4">
          <!-- 랭킹 정보 -->
          <div class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 border border-purple-100">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-xl font-semibold text-gray-800">
                {{ campusRankings.campus?.campusName || '교내' }} 랭킹
              </h2>
              <div class="text-sm text-gray-600">
                총 {{ campusRankings.total }}개 • {{ campusRankings.period }} 기준
              </div>
            </div>
          </div>

          <!-- 랭킹 엔트리들 -->
          <div class="space-y-3">
            <div
            v-for="entry in campusRankings.entries"
            :key="entry.mascotId"
            class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 hover:shadow-lg transition-all duration-200 border border-purple-100 hover:border-purple-300"
            >
            <div class="flex items-center space-x-4">
              <!-- 순위 -->
              <div class="flex-shrink-0">
                <div
                :class="[
                  'w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-lg',
                  entry.rank === 1 ? 'bg-yellow-500' : 
                  entry.rank === 2 ? 'bg-gray-400' : 
                  entry.rank === 3 ? 'bg-orange-500' : 'bg-purple-500'
                ]"
                  >
                    {{ entry.rank }}
                  </div>
                </div>

                <!-- 마스코트 배경 -->
                <div class="flex-shrink-0">
                  <img
                    :src="`/backgrounds/${entry.backgroundId || 'bg_base.png'}`"
                    :alt="`${entry.ownerNickname}의 마스코트`"
                    class="w-16 h-16 rounded-lg object-cover"
                  />
                </div>

                <!-- 정보 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center space-x-2 mb-1">
                    <span class="text-lg font-semibold text-gray-800">
                      {{ entry.ownerNickname }}
                    </span>
                    <span class="text-sm text-gray-600">님의 마스코트</span>
                  </div>
                  <div class="flex items-center space-x-4 text-sm text-gray-600">
                    <span>득표: {{ entry.votes.toLocaleString() }}표</span>
                    <span v-if="entry.school?.name" class="text-gray-500">
                      학교: {{ entry.school.name }}
                    </span>
                  </div>
                </div>

                <!-- 투표 버튼 -->
                <div class="flex-shrink-0">
                  <button
                    @click="voteForMascot(entry.mascotId)"
                    :disabled="voting"
                    class="bg-purple-500 text-white px-4 py-2 rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    👍 투표
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 페이지네이션 -->
          <div v-if="campusRankings.total > campusRankings.size" class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 border border-purple-100">
            <div class="flex items-center justify-between">
              <div class="text-sm text-gray-700">
                {{ (campusRankings.page * campusRankings.size) + 1 }} - 
                {{ Math.min((campusRankings.page + 1) * campusRankings.size, campusRankings.total) }} / 
                {{ campusRankings.total }}개
              </div>
              <div class="flex space-x-2">
                <button
                  @click="changePage(campusRankings.page - 1)"
                  :disabled="campusRankings.page === 0"
                  class="px-3 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  이전
                </button>
                <button
                  @click="changePage(campusRankings.page + 1)"
                  :disabled="(campusRankings.page + 1) * campusRankings.size >= campusRankings.total"
                  class="px-3 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  다음
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 빈 상태 -->
        <div v-else class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-12 text-center border border-purple-100">
          <div class="text-gray-600 text-lg">랭킹 데이터가 없습니다</div>
        </div>
      </div>

      <!-- 전국 랭킹 -->
      <div v-else class="space-y-6">
        <!-- 전국 랭킹 필터 -->
        <div class="flex justify-end">
          <div class="flex items-center space-x-4">
            <select
              v-model="nationalFilters.sort"
              @change="loadNationalRankings"
              class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm"
            >
              <option value="votes_desc">득표순</option>
              <option value="trending">트렌딩순</option>
              <option value="newest">최신순</option>
            </select>
            
            <select
              v-model="nationalFilters.period"
              @change="loadNationalRankings"
              class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm"
            >
              <option value="daily">일간</option>
              <option value="weekly">주간</option>
              <option value="monthly">월간</option>
              <option value="all">전체</option>
            </select>
          </div>
        </div>
        
        <!-- 로딩 상태 -->
        <div v-if="loading" class="flex justify-center items-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
        </div>

        <!-- 에러 상태 -->
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
          <div class="text-red-600 text-lg font-medium mb-2">오류가 발생했습니다</div>
          <div class="text-red-500 mb-4">{{ error }}</div>
          <button
            @click="loadNationalRankings"
            class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            다시 시도
          </button>
        </div>

        <!-- 전국 랭킹 목록 -->
        <div v-else-if="nationalRankings" class="space-y-4">
          <!-- 랭킹 정보 -->
          <div class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 border border-purple-100">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-xl font-semibold text-gray-800">전국 랭킹</h2>
              <div class="text-sm text-gray-600">
                총 {{ nationalRankings.total }}개 • {{ nationalRankings.period }} 기준
              </div>
            </div>
          </div>

          <!-- 랭킹 엔트리들 -->
          <div class="space-y-3">
            <div
              v-for="entry in nationalRankings.entries"
              :key="entry.mascotId"
              class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 hover:shadow-lg transition-all duration-200 border border-purple-100 hover:border-purple-300"
            >
              <div class="flex items-center space-x-4">
                <!-- 순위 -->
                <div class="flex-shrink-0">
                  <div
                    :class="[
                      'w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-lg',
                      entry.rank === 1 ? 'bg-yellow-500' : 
                      entry.rank === 2 ? 'bg-gray-400' : 
                      entry.rank === 3 ? 'bg-orange-500' : 'bg-purple-500'
                    ]"
                  >
                    {{ entry.rank }}
                  </div>
                </div>

                <!-- 마스코트 배경 -->
                <div class="flex-shrink-0">
                  <img
                    :src="`/backgrounds/${entry.backgroundId || 'bg_base.png'}`"
                    :alt="`${entry.ownerNickname}의 마스코트`"
                    class="w-16 h-16 rounded-lg object-cover"
                  />
                </div>

                <!-- 정보 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center space-x-2 mb-1">
                    <span class="text-lg font-semibold text-gray-800">
                      {{ entry.ownerNickname }}
                    </span>
                    <span class="text-sm text-gray-600">님의 마스코트</span>
                  </div>
                  <div class="flex items-center space-x-4 text-sm text-gray-600">
                    <span>득표: {{ entry.votes.toLocaleString() }}표</span>
                    <span v-if="entry.school?.name">학교: {{ entry.school.name }}</span>
                  </div>
                </div>

                <!-- 투표 버튼 -->
                <div class="flex-shrink-0">
                  <button
                    @click="voteForNationalMascot(entry.mascotId)"
                    :disabled="voting"
                    class="bg-purple-500 text-white px-4 py-2 rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    👍 투표
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 빈 상태 -->
        <div v-else class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-12 text-center border border-purple-100">
          <div class="text-gray-600 text-lg">전국 랭킹 데이터가 없습니다</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { 
  getCampusRankings, 
  getNationalRankings,
  getCurrentUser, 
  voteForCampus,
  voteForNational,
  type RankingResponse,
  type VoteRequest
} from '../api/ranking';
import { bootstrapAuth, auth, getMascot } from '../api/index';

// Router 인스턴스
const router = useRouter();

// 상태 관리
const activeTab = ref<'campus' | 'national'>('campus');
const loading = ref(false);
const error = ref<string | null>(null);
const voting = ref(false);
const campusRankings = ref<RankingResponse | null>(null);
const nationalRankings = ref<RankingResponse | null>(null);
const currentUser = ref<any>(null);
const myRank = ref<number | null>(null);
const hasMascot = ref<boolean>(false);

// 필터 설정
const campusFilters = ref({
  sort: 'votes_desc',
  period: 'weekly',
  size: 10, // 페이지 크기를 10개로 고정
  page: 0
});

const nationalFilters = ref({
  sort: 'votes_desc',
  period: 'weekly',
  size: 10,
  page: 0
});

// 마스코트 존재 여부 확인
const checkMascotExists = async () => {
  try {
    const mascot = await getMascot();
    hasMascot.value = !!mascot;
  } catch (error) {
    hasMascot.value = false;
  }
};

// 현재 사용자의 순위 찾기
const findMyRank = () => {
  if (!currentUser.value || !hasMascot.value) {
    myRank.value = null;
    return;
  }
  
  if (activeTab.value === 'campus') {
    // 교내 랭킹에서 내 순위 찾기
    if (campusRankings.value) {
      const myEntry = campusRankings.value.entries.find(entry => 
        entry.ownerNickname === currentUser.value.nickname
      );
      
      if (myEntry) {
        myRank.value = myEntry.rank;
      } else {
        myRank.value = 0; // 랭킹에 등록되지 않음
      }
    } else {
      myRank.value = null;
    }
  } else {
    // 전국 랭킹에서 내 순위 찾기 (아직 구현되지 않음)
    // TODO: 전국 랭킹 데이터가 로드되면 내 순위 찾기
    myRank.value = null;
  }
};

// 교내 랭킹 로드
const loadCampusRankings = async () => {
  try {
    loading.value = true;
    error.value = null;
    
    // 사용자 정보에서 campus 정보 가져오기
    if (!currentUser.value) {
      currentUser.value = await getCurrentUser();
    }
    
    const response = await getCampusRankings(
      undefined, // campusId는 백엔드에서 사용자 정보로 자동 처리
      campusFilters.value.sort,
      campusFilters.value.period,
      campusFilters.value.page,
      campusFilters.value.size
    );
    
    console.log('교내 랭킹 API 응답:', response);
    console.log('교내 랭킹 entries:', response.entries);
    
    campusRankings.value = response;
    findMyRank(); // 랭킹 로드 후 내 순위 갱신
  } catch (err: any) {
    console.error('교내 랭킹 로드 실패:', err);
    error.value = err.message || '랭킹을 불러오는데 실패했습니다.';
    
    // 인증 에러 시 로그인 페이지로 이동
    if (err.message === '로그인이 필요합니다.') {
      router.push('/login');
    }
  } finally {
    loading.value = false;
  }
};

// 전국 랭킹 로드
const loadNationalRankings = async () => {
  try {
    loading.value = true;
    error.value = null;
    
    const response = await getNationalRankings(
      nationalFilters.value.sort,
      nationalFilters.value.period,
      undefined, // region 제거
      undefined, // schoolId는 선택사항
      nationalFilters.value.page,
      nationalFilters.value.size
    );
    
    console.log('전국 랭킹 API 응답:', response);
    console.log('전국 랭킹 entries:', response.entries);
    
    nationalRankings.value = response;
    findMyRank(); // 랭킹 로드 후 내 순위 갱신
  } catch (err: any) {
    console.error('전국 랭킹 로드 실패:', err);
    error.value = err.message || '전국 랭킹을 불러오는데 실패했습니다.';
    
    // 인증 에러 시 로그인 페이지로 이동
    if (err.message === '로그인이 필요합니다.') {
      router.push('/login');
    }
  } finally {
    loading.value = false;
  }
};

// 투표 처리
const voteForMascot = async (mascotId: number) => {
  try {
    voting.value = true;
    
    const voteData: VoteRequest = {
      weight: 1, // 기본 투표 가중치
      campusId: currentUser.value?.campusId
    };
    
    const response = await voteForCampus(mascotId, voteData);
    
    if (response.success) {
      // 투표 성공 시 랭킹 새로고침
      await loadCampusRankings();
    } else {
      error.value = response.message;
    }
  } catch (err: any) {
    console.error('투표 실패:', err);
    error.value = err.message || '투표에 실패했습니다.';
  } finally {
    voting.value = false;
  }
};

// 전국 랭킹 투표 처리
const voteForNationalMascot = async (mascotId: number) => {
  try {
    voting.value = true;
    
    const voteData: VoteRequest = {
      weight: 1, // 기본 투표 가중치
    };
    
    const response = await voteForNational(mascotId, voteData);
    
    if (response.success) {
      // 투표 성공 시 랭킹 새로고침
      await loadNationalRankings();
    } else {
      error.value = response.message;
    }
  } catch (err: any) {
    console.error('전국 랭킹 투표 실패:', err);
    error.value = err.message || '투표에 실패했습니다.';
  } finally {
    voting.value = false;
  }
};

// 페이지 변경
const changePage = (newPage: number) => {
  campusFilters.value.page = newPage;
  loadCampusRankings();
};

// 탭 변경 시 랭킹 로드
watch(activeTab, async (newTab) => {
  if (newTab === 'campus') {
    await loadCampusRankings();
  } else {
    await loadNationalRankings();
  }
}, { immediate: true }); // immediate: true로 설정하여 컴포넌트 마운트 시 첫 번째 탭 로드

// 컴포넌트 마운트 시 인증 확인 후 랭킹 로드
onMounted(async () => {
  try {
    // 1. 인증 상태 강제 동기화
    await bootstrapAuth();
    
    // 2. 인증 상태 재확인 (더 엄격하게)
    const isAuth = await auth.isAuthenticatedAsync();
    if (!isAuth) {
      console.log('인증 실패, 로그인 페이지로 이동');
      router.push('/login');
      return;
    }
    
    console.log('인증 성공, 랭킹 데이터 로드 시작');
    
    // 3. 마스코트 존재 여부 확인
    await checkMascotExists();
    
    // 4. 랭킹 데이터 로드 (탭 변경 시 로드되므로 여기서는 필요 없음)
    // await loadCampusRankings(); 
    
  } catch (error) {
    console.error('랭킹 페이지 초기화 실패:', error);
    // 인증 실패 시 로그인 페이지로 이동
    router.push('/login');
  }
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>
