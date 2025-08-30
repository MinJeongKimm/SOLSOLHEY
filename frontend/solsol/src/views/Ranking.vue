<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 p-2 sm:p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-6xl mx-auto p-4 sm:p-6 lg:p-8">
      <!-- 헤더 -->
      <div class="flex items-center justify-between mb-4 sm:mb-6">
        <!-- 뒤로가기 버튼 -->
        <router-link 
          to="/mascot" 
          class="p-2 text-purple-500 hover:text-purple-700 transition-colors rounded-full hover:bg-purple-50"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
          </svg>
        </router-link>
        
        <!-- 제목 -->
        <h1 class="text-xl sm:text-2xl font-bold text-gray-800">랭킹</h1>
        
        <!-- 빈 공간 (가운데 정렬을 위해) -->
        <div class="w-10 h-10"></div>
      </div>

      <!-- 배너 이미지 -->
      <div class="mb-4 sm:mb-6">
        <img 
          src="/icons/banner.png" 
          alt="랭킹 배너" 
          class="w-full h-32 sm:h-40 object-contain rounded-xl"
        />
      </div>

      <!-- 탭 네비게이션 -->
      <div class="mb-6 sm:mb-8">
        <nav class="flex justify-center">
          <div class="flex bg-gray-200 rounded-lg p-0.5 w-64 max-w-full">
          <button
            @click="activeTab = 'campus'"
            :class="[
              activeTab === 'campus'
                  ? 'bg-white text-purple-600 shadow-md'
                  : 'text-gray-600 hover:text-purple-500 hover:bg-gray-100',
                'flex-1 py-2 px-3 rounded-md font-medium text-sm transition-all duration-200'
            ]"
          >
            교내 랭킹
          </button>
          <button
            @click="activeTab = 'national'"
            :class="[
              activeTab === 'national'
                  ? 'bg-white text-purple-600 shadow-md'
                  : 'text-gray-500 hover:text-purple-500 hover:bg-gray-100',
                'flex-1 py-2 px-3 rounded-md font-medium text-sm transition-all duration-200'
            ]"
          >
            전국 랭킹
          </button>
          </div>
        </nav>
  </div>

      <!-- 랭킹 정보 섹션 -->
      <div class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-4 sm:p-6 mb-6 border border-purple-100">
        <div class="flex items-center justify-between">
          <h2 class="text-lg sm:text-xl font-semibold text-gray-800">
            {{ activeTab === 'campus' ? (campusRankings?.campus?.campusName || '교내') : '전국' }} 랭킹
          </h2>
          <div class="text-sm text-gray-600">
            총 {{ getCurrentRankings()?.total || 0 }}개 • {{ getCurrentFilters()?.period || 'weekly' }} 기준
              </div>
            </div>
          </div>

      <!-- 필터 막대 -->
      <div class="flex justify-end space-x-2 mb-6">
          <select
          v-model="getCurrentFilters().sort"
          @change="loadCurrentRankings"
          class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm w-auto min-w-[100px]"
          >
            <option value="votes_desc">득표순</option>
            <option value="newest">최신순</option>
          </select>
          
          <select
          v-model="getCurrentFilters().period"
          @change="loadCurrentRankings"
          class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm w-auto min-w-[100px]"
          >
            <option value="daily">일간</option>
            <option value="weekly">주간</option>
            <option value="monthly">월간</option>
            <option value="all">전체</option>
          </select>
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
          @click="loadCurrentRankings"
            class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
          >
            다시 시도
          </button>
        </div>

      <!-- 랭킹 목록 (인스타그램 피드 스타일) -->
      <div v-else-if="getCurrentRankings()?.entries" class="space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div
            v-for="entry in getCurrentRankings()?.entries"
            :key="entry.entryId"
            class="bg-white rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 border border-gray-100 overflow-hidden"
          >
            <!-- 마스코트 이미지 -->
            <div class="relative">
              <img
                :src="entryImageSrc(entry)"
                :alt="`${entry.entryTitle || entry.mascotName || '마스코트'} (${entry.ownerNickname})`"
                class="w-full h-64 object-cover"
              />
              <!-- 순위 배지 -->
              <div class="absolute top-3 left-3">
                  <div
                  :class="[
                    'w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-lg shadow-lg',
                    entry.rank === 1 ? 'bg-yellow-500' : 
                    entry.rank === 2 ? 'bg-gray-400' : 
                    entry.rank === 3 ? 'bg-orange-500' : 'bg-purple-500'
                  ]"
                    >
                      {{ entry.rank }}
                    </div>
                  </div>
                </div>

            <!-- 카드 내용 -->
            <div class="p-4 sm:p-6">
              <!-- 등록 시 설정한 이름과 참가자 정보 -->
              <div class="flex items-center justify-between mb-2">
                <h3 class="text-lg sm:text-xl font-bold text-gray-800">
                  {{ entry.entryTitle || entry.mascotName || '마스코트' }}
                </h3>
                <div class="text-gray-500 text-sm">
                  {{ entry.ownerNickname }}
                  <span v-if="entry.school?.name" class="ml-2">• {{ entry.school.name }}</span>
                </div>
              </div>

              <!-- 득표수 -->
              <div class="text-purple-600 font-semibold mb-3">
                {{ entry.votes.toLocaleString() }}표
              </div>

              <!-- 등록 시 설정한 상세 설명 -->
              <p v-if="entry.description" class="text-gray-600 text-sm mb-4 line-clamp-2">
                {{ entry.description }}
              </p>

              <!-- 투표 버튼 -->
                <button
                  @click="voteForMascot(entry.entryId)"
                  :disabled="voting || !canVoteForMascot(entry.entryId)"
                class="w-full bg-purple-500 text-white py-2 px-4 rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
                >
                  <span v-if="!canVoteForMascot(entry.entryId)">
                    <span v-if="currentUser && entry.ownerNickname === currentUser.nickname">내 마스코트</span>
                  <span v-else-if="getVotedEntries().has(entry.entryId)">이미 투표함</span>
                    <span v-else>투표 불가</span>
                  </span>
                <span v-else>👍 투표하기</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 페이지네이션 -->
        <div v-if="getCurrentRankings()?.total > getCurrentFilters()?.size" class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-4 sm:p-6 border border-purple-100">
            <div class="flex flex-col sm:flex-row items-center justify-between space-y-3 sm:space-y-0">
              <div class="text-sm text-gray-700 text-center sm:text-left">
              {{ (getCurrentFilters()?.page * getCurrentFilters()?.size) + 1 }} - 
              {{ Math.min((getCurrentFilters()?.page + 1) * getCurrentFilters()?.size, getCurrentRankings()?.total) }} / 
              {{ getCurrentRankings()?.total }}개
              </div>
              <div class="flex space-x-2">
                <button
                @click="changePage(getCurrentFilters()?.page - 1)"
                :disabled="getCurrentFilters()?.page === 0"
                  class="px-3 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  이전
                </button>
                <button
                @click="changePage(getCurrentFilters()?.page + 1)"
                :disabled="(getCurrentFilters()?.page + 1) * getCurrentFilters()?.size >= getCurrentRankings()?.total"
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

    <!-- 플로팅 액션 버튼 (랭킹 참가) -->
          <button
      @click="openRankingModal"
      class="fixed bottom-6 right-6 w-14 h-14 bg-purple-500 text-white rounded-full shadow-lg hover:bg-purple-600 hover:shadow-xl transition-all duration-200 flex items-center justify-center z-50"
          >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
      </svg>
          </button>

    <!-- 랭킹 등록 모달 -->
    <RankingEntryModal
      v-if="showRankingModal && currentMascot"
      :mascot-name="currentMascot.name"
      :mascot-image-url="currentMascot.imageUrl"
      :mascot-snapshot-id="currentMascot.snapshotId"
      @close="showRankingModal = false"
      @submit="handleRankingSubmit"
    />

    <!-- 스냅샷 선택 모달 -->
    <SnapshotPickerModal
      :visible="showSnapshotPicker"
      :snapshots="snapshotList"
      @close="showSnapshotPicker = false"
      @select="onSnapshotPicked"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { getApiOrigin } from '../api/index';
import { useRouter } from 'vue-router';
import {
  getCampusRankings,
  getNationalRankings,
  getCurrentUser,
  voteForCampus,
  voteForNational,
  getUserCampusVotedMascotIds,
  getUserNationalVotedMascotIds,
  getCurrentUserMascotSnapshot,
  getCurrentUserMascot,
  composeMascotImage,
  getCampusVoteableStatus,
  getNationalVoteableStatus,
  getUserSnapshots,
  createRankingEntry,
  type RankingResponse,
  type VoteRequest,
  type CreateEntryRequest
} from '../api/ranking';
import { bootstrapAuth, auth, getMascot, getMascotCustomization, getShopItems, saveMascotCustomization } from '../api/index';
import RankingEntryModal from '../components/RankingEntryModal.vue';
import SnapshotPickerModal from '../components/SnapshotPickerModal.vue';

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
const votedEntries = ref<Set<number>>(new Set());
const nationalVotedEntries = ref<Set<number>>(new Set());

// 랭킹 등록 모달 상태
const showRankingModal = ref(false);
const currentMascot = ref<{ name: string; imageUrl: string; snapshotId: number } | null>(null);

// 스냅샷 선택 모달 상태
const showSnapshotPicker = ref(false);
const snapshotList = ref<any[]>([]);

// 필터 설정
const campusFilters = ref({
  sort: 'votes_desc',
  period: 'weekly',
  size: 10,
  page: 0
});

const nationalFilters = ref({
  sort: 'votes_desc',
  period: 'weekly',
  size: 10,
  page: 0
});

// 현재 활성 탭에 따른 데이터 반환 함수들
const getCurrentRankings = () => {
  return activeTab.value === 'campus' ? campusRankings.value : nationalRankings.value;
};

const getCurrentFilters = () => {
  return activeTab.value === 'campus' ? campusFilters.value : nationalFilters.value;
};

const getVotedEntries = () => {
  return activeTab.value === 'campus' ? votedEntries.value : nationalVotedEntries.value;
};

// 프론트 측 이미지 URL 보정
function normalizeImageUrlClient(url?: string): string {
  const v = (url || '').trim();
  if (!v) return '';
  if (v.startsWith('/uploads/')) {
    try {
      const origin = getApiOrigin();
      return (origin || '') + v;
    } catch {
      return v;
    }
  }
  return v;
}

// 배경 이미지 URL 조합
const BASE_URL = (import.meta as any).env?.BASE_URL || '/';
function bgUrl(id?: string): string {
  const name = id || 'bg_base.png';
  return `${BASE_URL}backgrounds/${name}`;
}

// 랭킹 리스트 공통 이미지 src 생성
function entryImageSrc(entry: any): string {
  return normalizeImageUrlClient(entry?.entryImageUrl) || bgUrl(entry?.backgroundId);
}

// 투표 가능 여부 계산
const voteableMascots = computed(() => {
  const result = new Map<number, boolean>();
  
  if (!currentUser.value) return result;
  
  const currentRankings = getCurrentRankings();
  if (currentRankings?.entries) {
    currentRankings.entries.forEach(entry => {
      const isOwnMascot = entry.ownerNickname === currentUser.value!.nickname;
      result.set(entry.entryId, !isOwnMascot);
    });
  }
  
  return result;
});

// 투표 가능 여부 확인
const canVoteForMascot = (entryId: number) => {
  const entry = getCurrentRankings()?.entries.find(e => e.entryId === entryId);
  
  if (!entry || !currentUser.value) {
    return false;
  }
  
  const isOwnMascot = entry.ownerNickname === currentUser.value.nickname;
  if (isOwnMascot) {
    return false;
  }
  
  const canVote = voteableMascots.value.get(entryId) ?? false;
  return canVote;
};

// 투표 후 상태 업데이트
const updateVoteStatus = (entryId: number) => {
  if (activeTab.value === 'campus') {
    votedEntries.value.add(entryId);
  } else {
    nationalVotedEntries.value.add(entryId);
  }
};

// 랭킹 데이터 로드 후 투표 가능 여부 새로고침
const refreshVoteableStatus = async (rankingType: 'campus' | 'national') => {
  try {
    const currentRankings = getCurrentRankings();
    if (currentRankings?.entries) {
      const entryIds = currentRankings.entries.map(entry => entry.entryId);
      const voteableStatus = rankingType === 'campus' 
        ? await getCampusVoteableStatus(entryIds)
        : await getNationalVoteableStatus(entryIds);
      
      entryIds.forEach(entryId => {
        const entry = currentRankings.entries.find(e => e.entryId === entryId);
        const isOwnMascot = entry?.ownerNickname === currentUser.value?.nickname;
        const canVote = voteableStatus[entryId] && !isOwnMascot;
        voteableMascots.value.set(entryId, canVote);
      });
    }
  } catch (error) {
    console.error(`${rankingType} 랭킹 투표 가능 여부 새로고침 실패:`, error);
  }
};

// 교내 랭킹 로드
const loadCampusRankings = async () => {
  try {
    loading.value = true;
    error.value = null;
    
    if (!currentUser.value) {
      currentUser.value = await getCurrentUser();
    }
    
    const response = await getCampusRankings(
      undefined,
      campusFilters.value.sort,
      campusFilters.value.period,
      campusFilters.value.page,
      campusFilters.value.size
    );
    
    campusRankings.value = response;
    await refreshVoteableStatus('campus');
  } catch (err: any) {
    console.error('교내 랭킹 로드 실패:', err);
    error.value = err.message || '랭킹을 불러오는데 실패했습니다.';
    
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
      undefined,
      undefined,
      nationalFilters.value.page,
      nationalFilters.value.size
    );
    
    nationalRankings.value = response;
    await refreshVoteableStatus('national');
  } catch (err: any) {
    console.error('전국 랭킹 로드 실패:', err);
    error.value = err.message || '전국 랭킹을 불러오는데 실패했습니다.';
    
    if (err.message === '로그인이 필요합니다.') {
      router.push('/login');
    }
  } finally {
    loading.value = false;
  }
};

// 현재 활성 탭에 따른 랭킹 로드
const loadCurrentRankings = () => {
  if (activeTab.value === 'campus') {
    return loadCampusRankings();
  } else {
    return loadNationalRankings();
  }
};

// 투표 처리
const voteForMascot = async (entryId: number) => {
  try {
    voting.value = true;
    
    const entry = getCurrentRankings()?.entries.find(e => e.entryId === entryId);
    if (!entry) {
      error.value = '엔트리 정보를 찾을 수 없습니다.';
      return;
    }
    
    const voteData: VoteRequest = {
      weight: 1,
      campusId: currentUser.value?.campusId
    };
    
    let response;
    if (activeTab.value === 'campus') {
      response = await voteForCampus(entryId, voteData);
    } else {
      response = await voteForNational(entryId, voteData);
    }
    
    if (response.success) {
      updateVoteStatus(entryId);
      await loadCurrentRankings();
      await refreshVoteableStatus(activeTab.value);
    } else {
      error.value = response.message;
    }
  } catch (err: any) {
    console.error('투표 처리 중 오류 발생:', err);
    error.value = err.message || '투표에 실패했습니다.';
  } finally {
    voting.value = false;
  }
};

// 페이지 변경
const changePage = (newPage: number) => {
  const currentFilters = getCurrentFilters();
  currentFilters.page = newPage;
  loadCurrentRankings();
};

// 탭 변경 시 랭킹 로드
watch(activeTab, async (newTab) => {
  if (newTab === 'campus') {
    try {
      const votedMascotIds = await getUserCampusVotedMascotIds();
      votedEntries.value = new Set<number>();
    } catch (error) {
      console.error('교내 랭킹 투표 히스토리 로드 실패:', error);
    }
    await loadCampusRankings();
  } else {
    try {
      const nationalVotedMascotIds = await getUserNationalVotedMascotIds();
      nationalVotedEntries.value = new Set<number>();
    } catch (error) {
      console.error('전국 랭킹 투표 히스토리 로드 실패:', error);
    }
    await loadNationalRankings();
  }
}, { immediate: true });

// 랭킹 등록 모달 열기
const openRankingModal = async () => {
  try {
    const list = await getUserSnapshots();
    snapshotList.value = list || [];
    showSnapshotPicker.value = true;
  } catch (e) {
    console.error('스냅샷 목록 로드 실패:', e);
    alert('스냅샷 목록을 불러오지 못했습니다.');
  }
};

// 스냅샷 선택 처리
const onSnapshotPicked = (s: any) => {
  const url = normalizeImageUrlClient(s?.imageUrl || '');
  currentMascot.value = { name: '스냅샷', imageUrl: url, snapshotId: s?.id || 0 };
  showSnapshotPicker.value = false;
  showRankingModal.value = true;
};

// 랭킹 등록 제출 처리
const handleRankingSubmit = async (data: CreateEntryRequest) => {
  try {
    const [mascot, customization, shopItems] = await Promise.all([
      getCurrentUserMascot(),
      getMascotCustomization(),
      getShopItems()
    ]);
    
    if (!mascot) {
      alert('마스코트 정보를 불러올 수 없습니다.');
      return;
    }
    
    const realtimeImageUrl = await composeMascotImage(mascot, customization, shopItems);

    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    if (!ctx) throw new Error('Canvas context unavailable');
    
    const img = new Image();
    img.crossOrigin = 'anonymous';
    await new Promise((resolve, reject) => {
      img.onload = resolve;
      img.onerror = reject;
      img.src = realtimeImageUrl;
    });
    
    canvas.width = img.width;
    canvas.height = img.height;
    ctx.drawImage(img, 0, 0);
    
    const dataUrl = canvas.toDataURL('image/png');
    
    const rankingType = activeTab.value === 'national' ? 'NATIONAL' : 'CAMPUS';
    const description = data.description && data.description.trim() !== '' ? data.description : undefined;
    
    try {
      const payload = { ...(customization as any), snapshotImageDataUrl: dataUrl };
      await saveMascotCustomization(payload);
    } catch (e) { console.warn('스냅샷 저장 실패(계속 진행):', e); }

    const latestSnapshot = await getCurrentUserMascotSnapshot();
    if (!latestSnapshot) {
      alert('스냅샷 생성에 실패했습니다. 다시 시도해주세요.');
      return;
    }
    
    const req: any = {
      mascotId: mascot.id,
      mascotSnapshotId: latestSnapshot.id,
      title: data.title,
      description,
      rankingType
    };
    
    await createRankingEntry(req);
    
    showRankingModal.value = false;
    currentMascot.value = null;
    
    alert('랭킹 참가가 완료되었습니다!');
    
    // 랭킹 목록 새로고침
    await loadCurrentRankings();
    
  } catch (error) {
    console.error('랭킹 참가 실패:', error);
    alert('랭킹 참가에 실패했습니다.');
  }
};

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  try {
    await bootstrapAuth();
    
    const isAuth = await auth.isAuthenticatedAsync();
    if (!isAuth) {
      console.log('인증 실패, 로그인 페이지로 이동');
      router.push('/login');
      return;
    }
    
    console.log('인증 성공, 랭킹 데이터 로드 시작');
    
    try {
      if (activeTab.value === 'campus') {
        const votedMascotIds = await getUserCampusVotedMascotIds();
        votedEntries.value = new Set<number>();
      } else {
        const nationalVotedMascotIds = await getUserNationalVotedMascotIds();
        nationalVotedEntries.value = new Set<number>();
      }
    } catch (error) {
      console.error('초기 투표 히스토리 로드 실패:', error);
      votedEntries.value = new Set<number>();
      nationalVotedEntries.value = new Set<number>();
    }
    
  } catch (error) {
    console.error('랭킹 페이지 초기화 실패:', error);
    router.push('/login');
  }
});
</script>

<style scoped>
/* 텍스트 줄 제한 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
