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

      <!-- 전국 랭킹 참가 슬롯 섹션 -->
      <div v-if="activeTab === 'national'" class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-6 mb-6 border-2 border-blue-200">
        <div class="mb-4">
          <h2 class="text-lg font-bold text-gray-800 mb-2">전국 랭킹 참가 슬롯</h2>
          <p class="text-sm text-gray-600">마스코트를 전국 랭킹에 등록하여 다른 사용자들과 경쟁해보세요!</p>
        </div>
        
        <!-- 3개 슬롯 가로 배치 -->
        <div class="grid grid-cols-3 gap-4 max-w-4xl mx-auto">
          <RankingSlot
            v-for="(slot, index) in nationalRankingSlots"
            :key="index"
            :entry="slot.entry"
            :is-active="slot.isActive"
            :mascot-image-url="slot.mascotImageUrl"
            :vote-count="slot.voteCount"
            :rank="slot.rank"
            @slot-click="handleSlotClick(index)"
            @delete="handleSlotDelete"
          />
        </div>
      </div>

      <!-- 교내 랭킹 참가 슬롯 섹션 -->
      <div v-if="activeTab === 'campus'" class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 mb-6 border-2 border-purple-200">
        <div class="mb-4">
          <h2 class="text-lg font-bold text-gray-800 mb-2">교내 랭킹 참가 슬롯</h2>
          <p class="text-sm text-gray-600">마스코트를 교내 랭킹에 등록하여 다른 사용자들과 경쟁해보세요!</p>
        </div>
        
        <!-- 3개 슬롯 가로 배치 -->
        <div class="grid grid-cols-3 gap-4 max-w-4xl mx-auto">
          <RankingSlot
            v-for="(slot, index) in campusRankingSlots"
            :key="index"
            :entry="slot.entry"
            :is-active="slot.isActive"
            :mascot-image-url="slot.mascotImageUrl"
            :vote-count="slot.voteCount"
            :rank="slot.rank"
            @slot-click="handleSlotClick(index)"
            @delete="handleSlotDelete"
          />
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
                  'w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-sm',
                  entry.rank === 1 ? 'bg-yellow-500' : 
                  entry.rank === 2 ? 'bg-gray-400' : 
                  entry.rank === 3 ? 'bg-orange-500' : 'bg-purple-500'
                ]"
                  >
                    {{ entry.rank }}
                  </div>
                </div>

                <!-- 마스코트 이미지 (등록한 이미지 우선, 없으면 배경) -->
                <div class="flex-shrink-0">
                  <img
                    :src="entry.entryImageUrl || `/backgrounds/${entry.backgroundId || 'bg_base.png'}`"
                    :alt="`${entry.mascotName || '마스코트'} (${entry.ownerNickname})`"
                    class="w-12 h-12 rounded-lg object-cover"
                  />
                </div>

                <!-- 정보 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center space-x-2 mb-1">
                    <span class="text-lg font-semibold text-gray-800">
                      {{ entry.entryTitle || entry.mascotName || '마스코트' }}
                    </span>
                    <span class="text-sm text-gray-600">({{ entry.ownerNickname }})</span>
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
                      @click="voteForMascot(entry.entryId)"
                      :disabled="voting || !canVoteForMascot(entry.entryId)"
                      class="bg-purple-500 text-white px-3 py-1.5 rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                                          <span v-if="!canVoteForMascot(entry.entryId)">
                        <span v-if="currentUser && entry.ownerNickname === currentUser.nickname">내 마스코트</span>
                        <span v-else-if="votedEntries.has(entry.entryId)">이미 투표함</span>
                        <span v-else>투표 불가</span>
                      </span>
                    <span v-else>👍 투표</span>
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
                      'w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-sm',
                      entry.rank === 1 ? 'bg-yellow-500' : 
                      entry.rank === 2 ? 'bg-gray-400' : 
                      entry.rank === 3 ? 'bg-orange-500' : 'bg-purple-500'
                    ]"
                  >
                    {{ entry.rank }}
                  </div>
                </div>

                <!-- 마스코트 이미지 (등록한 이미지 우선, 없으면 배경) -->
                <div class="flex-shrink-0">
                  <img
                    :src="entry.entryImageUrl || `/backgrounds/${entry.backgroundId || 'bg_base.png'}`"
                    :alt="`${entry.mascotName || '마스코트'} (${entry.ownerNickname})`"
                    class="w-12 h-12 rounded-lg object-cover"
                  />
                </div>

                <!-- 정보 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center space-x-2 mb-1">
                    <span class="text-lg font-semibold text-gray-800">
                      {{ entry.entryTitle || entry.mascotName || '마스코트' }}
                    </span>
                    <span class="text-sm text-gray-600">({{ entry.ownerNickname }})</span>
                  </div>
                  <div class="flex items-center space-x-4 text-sm text-gray-600">
                    <span>득표: {{ entry.votes.toLocaleString() }}표</span>
                    <span v-if="entry.school?.name">학교: {{ entry.school.name }}</span>
                  </div>
                </div>

                <!-- 투표 버튼 -->
                <div class="flex-shrink-0">
                                      <button
                      @click="voteForNationalMascot(entry.entryId)"
                      :disabled="voting || !canVoteForMascot(entry.entryId)"
                      class="bg-purple-500 text-white px-3 py-1.5 rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                                          <span v-if="!canVoteForMascot(entry.entryId)">
                        <span v-if="currentUser && entry.ownerNickname === currentUser.nickname">내 마스코트</span>
                        <span v-else-if="nationalVotedEntries.has(entry.entryId)">투표 불가</span>
                        <span v-else>투표 불가</span>
                      </span>
                    <span v-else>👍 투표</span>
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

    <!-- 랭킹 등록 모달 -->
    <RankingEntryModal
      v-if="showRankingModal && currentMascot"
      :mascot-name="currentMascot.name"
      :mascot-image-url="currentMascot.imageUrl"
      :mascot-snapshot-id="currentMascot.snapshotId"
      @close="showRankingModal = false"
      @submit="handleRankingSubmit"
    />
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
  getUserCampusVotedMascotIds,
  getUserNationalVotedMascotIds,
  getUserEntries,
  getUserEntriesByType,
  createRankingEntry,
  createRankingEntryWithImage,
  deleteRankingEntry,
  getCurrentUserMascotSnapshot,
  getCurrentUserMascot,
  composeMascotImage,
  type RankingResponse,
  type VoteRequest,
  type EntryResponse,
  type CreateEntryRequest
} from '../api/ranking';
import { bootstrapAuth, auth, getMascot, getMascotCustomization, getShopItems } from '../api/index';
import RankingSlot from '../components/RankingSlot.vue';
import RankingEntryModal from '../components/RankingEntryModal.vue';

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
const votedEntries = ref<Set<number>>(new Set()); // 교내 랭킹 투표한 엔트리 ID들
const nationalVotedEntries = ref<Set<number>>(new Set()); // 전국 랭킹 투표한 엔트리 ID들

// 랭킹 슬롯 관련 상태 (전국/교내 분리)
const nationalRankingSlots = ref([
  { entry: null as EntryResponse | null, isActive: true, mascotImageUrl: '', voteCount: 0, rank: 0 },
  { entry: null as EntryResponse | null, isActive: false, mascotImageUrl: '', voteCount: 0, rank: 0 },
  { entry: null as EntryResponse | null, isActive: false, mascotImageUrl: '', voteCount: 0, rank: 0 }
]);

const campusRankingSlots = ref([
  { entry: null as EntryResponse | null, isActive: true, mascotImageUrl: '', voteCount: 0, rank: 0 },
  { entry: null as EntryResponse | null, isActive: false, mascotImageUrl: '', voteCount: 0, rank: 0 },
  { entry: null as EntryResponse | null, isActive: false, mascotImageUrl: '', voteCount: 0, rank: 0 }
]);

const showRankingModal = ref(false);
const selectedSlotIndex = ref(-1);
const currentMascot = ref<{ name: string; imageUrl: string; snapshotId: number } | null>(null);
const loadingRanking = ref(false);

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
    // 전국 랭킹에서 내 순위 찾기
    if (nationalRankings.value) {
      const myEntry = nationalRankings.value.entries.find(entry => 
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
  }
};

// 투표 가능 여부를 미리 계산하여 저장
const voteableMascots = computed(() => {
  const result = new Map<number, boolean>();
  
  console.log('voteableMascots 계산 시작');
  console.log('currentUser:', currentUser.value);
  console.log('campusRankings entries:', campusRankings.value?.entries);
  console.log('nationalRankings entries:', nationalRankings.value?.entries);
  console.log('votedEntries:', Array.from(votedEntries.value));
  console.log('nationalVotedEntries:', Array.from(nationalVotedEntries.value));
  
  if (!currentUser.value) return result;
  
  // 교내 랭킹 투표 가능 여부
  if (campusRankings.value?.entries) {
    campusRankings.value.entries.forEach(entry => {
      const isOwnMascot = entry.ownerNickname === currentUser.value!.nickname;
      // 엔트리별로 투표 제한
      const alreadyVoted = votedEntries.value.has(entry.entryId);
      const canVote = !isOwnMascot && !alreadyVoted;
      result.set(entry.entryId, canVote);
      console.log(`교내 랭킹 entryId ${entry.entryId}: isOwnMascot=${isOwnMascot}, alreadyVoted=${alreadyVoted}, canVote=${canVote}`);
    });
  }
  
  // 전국 랭킹 투표 가능 여부
  if (nationalRankings.value?.entries) {
    nationalRankings.value.entries.forEach(entry => {
      const isOwnMascot = entry.ownerNickname === currentUser.value!.nickname;
      // 엔트리별로 투표 제한
      const alreadyVoted = nationalVotedEntries.value.has(entry.entryId);
      const canVote = !isOwnMascot && !alreadyVoted;
      result.set(entry.entryId, canVote);
      console.log(`전국 랭킹 entryId ${entry.entryId}: isOwnMascot=${isOwnMascot}, alreadyVoted=${alreadyVoted}, canVote=${canVote}`);
    });
  }
  
  console.log('voteableMascots 최종 결과:', Object.fromEntries(result));
  return result;
});

// 투표 가능 여부 확인 (캐시된 결과 사용)
const canVoteForMascot = (entryId: number) => {
  const canVote = voteableMascots.value.get(entryId) ?? false;
  console.log(`투표 가능 여부 확인 - entryId: ${entryId}, canVote: ${canVote}`);
  console.log(`votedEntries:`, Array.from(votedEntries.value));
  console.log(`nationalVotedEntries:`, Array.from(nationalVotedEntries.value));
  return canVote;
};

// 투표 후 상태 업데이트 (엔트리별로 제한)
const updateVoteStatus = (entryId: number) => {
  console.log('투표 상태 업데이트 시작 - entryId:', entryId, 'activeTab:', activeTab.value);
  
  if (activeTab.value === 'campus') {
    votedEntries.value.add(entryId);
    console.log('교내 랭킹 투표 상태 업데이트 완료 - votedEntries:', Array.from(votedEntries.value));
  } else {
    nationalVotedEntries.value.add(entryId);
    console.log('전국 랭킹 투표 상태 업데이트 완료 - nationalVotedEntries:', Array.from(nationalVotedEntries.value));
  }
};

// 랭킹 데이터 로드 후 투표 히스토리 변환
const updateVoteHistoryAfterRankingLoad = async (rankingType: 'campus' | 'national') => {
  try {
    console.log(`${rankingType} 랭킹 데이터 로드 후 투표 히스토리 변환 시작`);
    
    if (rankingType === 'campus') {
      // 교내 랭킹 투표 히스토리 변환
      const votedMascotIds = await getUserCampusVotedMascotIds();
      console.log('교내 랭킹 투표한 마스코트 ID:', votedMascotIds);
      
      // 마스코트 ID를 엔트리 ID로 변환
      const votedEntryIds = new Set<number>();
      if (campusRankings.value?.entries) {
        votedMascotIds.forEach(mascotId => {
          const entry = campusRankings.value!.entries.find(e => e.mascotId === mascotId);
          if (entry && entry.entryId) {
            votedEntryIds.add(entry.entryId);
            console.log(`교내 랭킹 마스코트 ID ${mascotId} → 엔트리 ID ${entry.entryId}`);
          }
        });
      }
      votedEntries.value = votedEntryIds;
      console.log('교내 랭킹 투표 히스토리 변환 완료:', Array.from(votedEntries.value));
    } else {
      // 전국 랭킹 투표 히스토리 변환
      const nationalVotedMascotIds = await getUserNationalVotedMascotIds();
      console.log('전국 랭킹 투표한 마스코트 ID:', nationalVotedMascotIds);
      
      // 마스코트 ID를 엔트리 ID로 변환
      const votedEntryIds = new Set<number>();
      if (nationalRankings.value?.entries) {
        nationalVotedMascotIds.forEach(mascotId => {
          const entry = nationalRankings.value!.entries.find(e => e.mascotId === mascotId);
          if (entry && entry.entryId) {
            votedEntryIds.add(entry.entryId);
            console.log(`전국 랭킹 마스코트 ID ${mascotId} → 엔트리 ID ${entry.entryId}`);
          }
        });
      }
      nationalVotedEntries.value = votedEntryIds;
      console.log('전국 랭킹 투표 히스토리 변환 완료:', Array.from(nationalVotedEntries.value));
    }
  } catch (error) {
    console.error(`${rankingType} 랭킹 투표 히스토리 변환 실패:`, error);
  }
};

// 투표 히스토리 새로고침
const refreshVoteHistory = async () => {
  try {
    console.log('투표 히스토리 새로고침 시작');
    
    if (activeTab.value === 'campus') {
      // 임시로 기존 API 사용
      const votedMascotIds = await getUserCampusVotedMascotIds();
      console.log('교내 랭킹 투표한 마스코트 ID:', votedMascotIds);
      
      // 마스코트 ID를 엔트리 ID로 변환
      const votedEntryIds = new Set<number>();
      if (campusRankings.value?.entries) {
        votedMascotIds.forEach(mascotId => {
          const entry = campusRankings.value!.entries.find(e => e.mascotId === mascotId);
          if (entry && entry.entryId) {
            votedEntryIds.add(entry.entryId);
          }
        });
      }
      votedEntries.value = votedEntryIds;
      console.log('교내 랭킹 투표 히스토리 새로고침 완료:', Array.from(votedEntries.value));
    } else {
      // 임시로 기존 API 사용
      const nationalVotedMascotIds = await getUserNationalVotedMascotIds();
      console.log('전국 랭킹 투표한 마스코트 ID:', nationalVotedMascotIds);
      
      // 마스코트 ID를 엔트리 ID로 변환
      const votedEntryIds = new Set<number>();
      if (nationalRankings.value?.entries) {
        nationalVotedMascotIds.forEach(mascotId => {
          const entry = nationalRankings.value!.entries.find(e => e.mascotId === mascotId);
          if (entry && entry.entryId) {
            votedEntryIds.add(entry.entryId);
          }
        });
      }
      nationalVotedEntries.value = votedEntryIds;
      console.log('전국 랭킹 투표 히스토리 새로고침 완료:', Array.from(nationalVotedEntries.value));
    }
  } catch (error) {
    console.error('투표 히스토리 새로고침 실패:', error);
  }
};

// 정렬 기준을 사용자 친화적인 텍스트로 변환
const getSortDisplayName = (sort: string) => {
  switch (sort) {
    case 'votes_desc':
      return '득표순';
    case 'newest':
      return '최신순';
    case 'trending':
      return '트렌딩순';
    default:
      return '득표순';
  }
};

// 기간을 사용자 친화적인 텍스트로 변환
const getPeriodDisplayName = (period: string) => {
  switch (period) {
    case 'daily':
      return '일간';
    case 'weekly':
      return '주간';
    case 'monthly':
      return '월간';
    case 'all':
      return '전체';
    default:
      return '주간';
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
    
    // 랭킹 데이터 로드 후 투표 히스토리 변환
    await updateVoteHistoryAfterRankingLoad('campus');
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
    
    // 랭킹 데이터 로드 후 투표 히스토리 변환
    await updateVoteHistoryAfterRankingLoad('national');
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
const voteForMascot = async (entryId: number) => {
  try {
    console.log('교내 랭킹 투표 시작 - entryId:', entryId);
    voting.value = true;
    
    // 랭킹 데이터 확인
    console.log('campusRankings.value:', campusRankings.value);
    console.log('campusRankings.entries:', campusRankings.value?.entries);
    
    // 해당 엔트리 찾기
    const entry = campusRankings.value?.entries.find(e => e.entryId === entryId);
    console.log('찾은 entry:', entry);
    
    if (!entry) {
      console.error('엔트리 정보를 찾을 수 없음 - entryId:', entryId);
      error.value = '엔트리 정보를 찾을 수 없습니다.';
      return;
    }
    
    console.log('entry.ownerNickname:', entry.ownerNickname);
    
    const voteData: VoteRequest = {
      weight: 1, // 기본 투표 가중치
      campusId: currentUser.value?.campusId
    };
    
    console.log('투표 데이터:', voteData);
    const response = await voteForCampus(entryId, voteData);
    console.log('투표 API 응답:', response);
    
    if (response.success) {
      // 투표 성공 시 상태 업데이트 (엔트리별로 제한)
      console.log('투표 성공, 상태 업데이트 시작 - entryId:', entryId);
      updateVoteStatus(entryId);
      // 투표 성공 시 랭킹 새로고침
      await loadCampusRankings();
      // 투표 히스토리 새로고침
      await refreshVoteHistory();
    } else {
      console.error('투표 실패 - message:', response.message);
      error.value = response.message;
    }
  } catch (err: any) {
    console.error('투표 처리 중 오류 발생:', err);
    error.value = err.message || '투표에 실패했습니다.';
  } finally {
    voting.value = false;
  }
};

// 전국 랭킹 투표 처리
const voteForNationalMascot = async (entryId: number) => {
  try {
    console.log('전국 랭킹 투표 시작 - entryId:', entryId);
    console.log('현재 사용자 정보:', currentUser.value);
    console.log('현재 사용자 ID:', currentUser.value?.id);
    voting.value = true;
    
    // 랭킹 데이터 확인
    console.log('nationalRankings.value:', nationalRankings.value);
    console.log('nationalRankings.entries:', nationalRankings.value?.entries);
    
    // 해당 엔트리 찾기
    const entry = nationalRankings.value?.entries.find(e => e.entryId === entryId);
    console.log('찾은 entry:', entry);
    
    if (!entry) {
      console.error('엔트리 정보를 찾을 수 없음 - entryId:', entryId);
      error.value = '엔트리 정보를 찾을 수 없습니다.';
      return;
    }
    
    console.log('entry.ownerNickname:', entry.ownerNickname);
    
    const voteData: VoteRequest = {
      weight: 1, // 기본 투표 가중치
    };
    
    console.log('투표 데이터:', voteData);
    const response = await voteForNational(entryId, voteData);
    console.log('투표 API 응답:', response);
    
    if (response.success) {
      // 투표 성공 시 상태 업데이트 (엔트리별로 제한)
      console.log('투표 성공, 상태 업데이트 시작 - entryId:', entryId);
      updateVoteStatus(entryId);
      // 투표 성공 시 랭킹 새로고침
      await loadNationalRankings();
      // 투표 히스토리 새로고침
      await refreshVoteHistory();
    } else {
      console.error('투표 실패 - message:', response.message);
      error.value = response.message;
    }
  } catch (err: any) {
    console.error('전국 랭킹 투표 처리 중 오류 발생:', err);
    error.value = err.message || '전국 랭킹 투표에 실패했습니다.';
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
    // 교내 랭킹 탭으로 변경 시 교내 랭킹 투표 히스토리 로드
    try {
      const votedMascotIds = await getUserCampusVotedMascotIds();
      console.log('교내 랭킹 탭 변경 시 투표한 마스코트 ID:', votedMascotIds);
      // 마스코트 ID를 엔트리 ID로 변환은 랭킹 데이터 로드 후에 수행
      votedEntries.value = new Set<number>();
    } catch (error) {
      console.error('교내 랭킹 투표 히스토리 로드 실패:', error);
    }
    await loadCampusRankings();
    await loadCampusRankingEntries(); // 교내 랭킹 슬롯 로드
    await updateCampusSlotRankingInfo(); // 교내 랭킹 슬롯 정보 업데이트
  } else {
    // 전국 랭킹 탭으로 변경 시 전국 랭킹 투표 히스토리 로드
    try {
      const nationalVotedMascotIds = await getUserNationalVotedMascotIds();
      console.log('전국 랭킹 탭 변경 시 투표한 마스코트 ID:', nationalVotedMascotIds);
      // 마스코트 ID를 엔트리 ID로 변환은 랭킹 데이터 로드 후에 수행
      nationalVotedEntries.value = new Set<number>();
    } catch (error) {
      console.error('전국 랭킹 투표 히스토리 로드 실패:', error);
    }
    await loadNationalRankings();
    await loadNationalRankingEntries(); // 전국 랭킹 슬롯 로드
    await updateNationalSlotRankingInfo(); // 전국 랭킹 슬롯 정보 업데이트
  }
}, { immediate: true }); // immediate: true로 설정하여 컴포넌트 마운트 시 첫 번째 탭 로드

// 랭킹 슬롯 관련 함수들 (전국/교내 분리)
async function loadNationalRankingEntries() {
  try {
    console.log('전국 랭킹 엔트리 로드 시작');
    loadingRanking.value = true;
    
    const entries = await getUserEntriesByType('NATIONAL');
    console.log('전국 랭킹 엔트리 API 응답:', entries);
    
    // 전국 랭킹 슬롯에 엔트리 할당
    nationalRankingSlots.value.forEach((slot, index) => {
      if (index < entries.length) {
        slot.entry = entries[index];
        slot.isActive = false; // 등록된 슬롯은 비활성화
        console.log(`전국 슬롯 ${index}에 엔트리 할당:`, entries[index]);
      } else {
        slot.entry = null;
        console.log(`전국 슬롯 ${index}는 비어있음`);
      }
    });
    
    console.log('전국 슬롯 상태:', nationalRankingSlots.value);
    
    // 다음 슬롯 활성화
    updateNationalSlotActivation();
    
    // 등록된 슬롯의 저장된 마스코트 이미지 업데이트
    await updateNationalSlotMascotImages();
    
    // 등록된 슬롯의 실제 랭킹 정보 업데이트 (득표수, 순위)
    await updateNationalSlotRankingInfo();
    
  } catch (error) {
    console.error('전국 랭킹 엔트리 로드 실패:', error);
  } finally {
    loadingRanking.value = false;
  }
}

async function loadCampusRankingEntries() {
  try {
    console.log('교내 랭킹 엔트리 로드 시작');
    loadingRanking.value = true;
    
    const entries = await getUserEntriesByType('CAMPUS');
    console.log('교내 랭킹 엔트리 API 응답:', entries);
    
    // 교내 랭킹 슬롯에 엔트리 할당
    campusRankingSlots.value.forEach((slot, index) => {
      if (index < entries.length) {
        slot.entry = entries[index];
        slot.isActive = false; // 등록된 슬롯은 비활성화
        console.log(`교내 슬롯 ${index}에 엔트리 할당:`, entries[index]);
      } else {
        slot.entry = null;
        console.log(`교내 슬롯 ${index}는 비어있음`);
      }
    });
    
    console.log('교내 슬롯 상태:', campusRankingSlots.value);
    
    // 다음 슬롯 활성화
    updateCampusSlotActivation();
    
    // 등록된 슬롯의 저장된 마스코트 이미지 업데이트
    await updateCampusSlotMascotImages();
    
    // 등록된 슬롯의 실제 랭킹 정보 업데이트 (득표수, 순위)
    await updateCampusSlotRankingInfo();
    
  } catch (error) {
    console.error('교내 랭킹 엔트리 로드 실패:', error);
  } finally {
    loadingRanking.value = false;
  }
}

function updateNationalSlotActivation() {
  const registeredCount = nationalRankingSlots.value.filter(slot => slot.entry !== null).length;
  console.log('전국 슬롯 활성화 - 등록된 엔트리 수:', registeredCount);
  
  nationalRankingSlots.value.forEach((slot, index) => {
    if (index === 0) {
      slot.isActive = registeredCount === 0; // 첫 번째 슬롯은 등록된 것이 없을 때만 활성화
      console.log(`전국 슬롯 ${index} 활성화 상태:`, slot.isActive, '(등록된 것이 없을 때만)');
    } else if (index === 1) {
      slot.isActive = registeredCount >= 1 && registeredCount < 3; // 두 번째 슬롯은 1개 이상 등록되었을 때 활성화
      console.log(`전국 슬롯 ${index} 활성화 상태:`, slot.isActive, '(1개 이상, 3개 미만일 때)');
    } else if (index === 2) {
      slot.isActive = registeredCount >= 2 && registeredCount < 3; // 세 번째 슬롯은 2개 이상 등록되었을 때 활성화
      console.log(`전국 슬롯 ${index} 활성화 상태:`, slot.isActive, '(2개 이상, 3개 미만일 때)');
    }
  });
  
  console.log('전국 슬롯 최종 상태:', nationalRankingSlots.value.map((slot, index) => ({ index, isActive: slot.isActive, hasEntry: slot.entry !== null })));
}

function updateCampusSlotActivation() {
  const registeredCount = campusRankingSlots.value.filter(slot => slot.entry !== null).length;
  console.log('교내 슬롯 활성화 - 등록된 엔트리 수:', registeredCount);
  
  campusRankingSlots.value.forEach((slot, index) => {
    if (index === 0) {
      slot.isActive = registeredCount === 0; // 첫 번째 슬롯은 등록된 것이 없을 때만 활성화
      console.log(`교내 슬롯 ${index} 활성화 상태:`, slot.isActive, '(등록된 것이 없을 때만)');
    } else if (index === 1) {
      slot.isActive = registeredCount >= 1 && registeredCount < 3; // 두 번째 슬롯은 1개 이상 등록되었을 때 활성화
      console.log(`교내 슬롯 ${index} 활성화 상태:`, slot.isActive, '(1개 이상, 3개 미만일 때)');
    } else if (index === 2) {
      slot.isActive = registeredCount >= 2 && registeredCount < 3; // 세 번째 슬롯은 2개 이상 등록되었을 때 활성화
      console.log(`교내 슬롯 ${index} 활성화 상태:`, slot.isActive, '(2개 이상, 3개 미만일 때)');
    }
  });
  
  console.log('교내 슬롯 최종 상태:', campusRankingSlots.value.map((slot, index) => ({ index, isActive: slot.isActive, hasEntry: slot.entry !== null })));
}

async function handleSlotClick(slotIndex: number) {
  // 현재 활성 탭에 따라 적절한 슬롯 배열 선택
  const currentSlots = activeTab.value === 'national' ? nationalRankingSlots : campusRankingSlots;
  
  if (!currentSlots.value[slotIndex].isActive) return;
  
  try {
    // 현재 마스코트 정보와 커스터마이징, 상점 아이템 로드
    const [mascot, customization, shopItems] = await Promise.all([
      getCurrentUserMascot(),
      getMascotCustomization(),
      getShopItems()
    ]);
    
    if (!mascot) {
      alert('마스코트 정보를 불러올 수 없습니다.');
      return;
    }
    
    // 실시간 마스코트 이미지 합성
    const realtimeImageUrl = await composeMascotImage(mascot, customization, shopItems);
    
    currentMascot.value = {
      name: mascot.name,
      imageUrl: realtimeImageUrl,
      snapshotId: 0 // 실시간 이미지이므로 0으로 설정
    };
    
    selectedSlotIndex.value = slotIndex;
    showRankingModal.value = true;
    
  } catch (error) {
    console.error('마스코트 정보 로드 실패:', error);
    alert('마스코트 정보를 불러올 수 없습니다.');
  }
}

async function handleRankingSubmit(data: CreateEntryRequest) {
  try {
    // 현재 마스코트 상태로 이미지 생성
    const [mascot, customization, shopItems] = await Promise.all([
      getCurrentUserMascot(),
      getMascotCustomization(),
      getShopItems()
    ]);
    
    if (!mascot) {
      alert('마스코트 정보를 불러올 수 없습니다.');
      return;
    }
    
    // 실시간 마스코트 이미지 생성
    const realtimeImageUrl = await composeMascotImage(mascot, customization, shopItems);
    
    // Canvas를 Blob으로 변환
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
    
    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob((b) => b ? resolve(b) : reject(new Error('Canvas toBlob failed')), 'image/png');
    });
    
    // 현재 활성 탭에 따라 랭킹 타입 결정
    const rankingType = activeTab.value === 'national' ? 'NATIONAL' : 'CAMPUS';
    
    // description이 필수이므로 빈 문자열 대신 기본값 설정
    const description = data.description && data.description.trim() !== '' ? data.description : '랭킹 참가';
    
    // 이미지와 함께 랭킹 등록
    const newEntry = await createRankingEntryWithImage(data.title, description, blob, rankingType);
    
    console.log('새로 생성된 엔트리:', newEntry);
    console.log('엔트리의 이미지 URL:', newEntry.imageUrl);
    
    // 현재 활성 탭에 따라 적절한 슬롯 배열 선택
    const currentSlots = activeTab.value === 'national' ? nationalRankingSlots : campusRankingSlots;
    const updateSlotActivation = activeTab.value === 'national' ? updateNationalSlotActivation : updateCampusSlotActivation;
    
    // 슬롯에 새 엔트리 할당
    currentSlots.value[selectedSlotIndex.value].entry = newEntry;
    currentSlots.value[selectedSlotIndex.value].mascotImageUrl = newEntry.imageUrl || '';
    
    console.log('슬롯에 설정된 이미지 URL:', currentSlots.value[selectedSlotIndex.value].mascotImageUrl);
    
    // 슬롯 활성화 상태 업데이트
    updateSlotActivation();
    
    // 랭킹 목록 새로고침 (득표수, 순위 업데이트를 위해)
    if (activeTab.value === 'national') {
      await loadNationalRankings();
      await loadNationalRankingEntries(); // 전국 랭킹 슬롯 엔트리 새로고침
      await updateNationalSlotRankingInfo();
    } else {
      await loadCampusRankings();
      await loadCampusRankingEntries(); // 교내 랭킹 슬롯 엔트리 새로고침
      await updateCampusSlotRankingInfo();
    }
    
    // 모달 닫기
    showRankingModal.value = false;
    selectedSlotIndex.value = -1;
    currentMascot.value = null;
    
    alert('랭킹 참가가 완료되었습니다!');
    
  } catch (error) {
    console.error('랭킹 참가 실패:', error);
    alert('랭킹 참가에 실패했습니다.');
  }
}

// 등록된 슬롯의 저장된 마스코트 이미지 업데이트 (전국/교내 분리)
async function updateNationalSlotMascotImages() {
  try {
    console.log('전국 랭킹 슬롯 마스코트 이미지 업데이트 시작');
    for (let i = 0; i < nationalRankingSlots.value.length; i++) {
      const slot = nationalRankingSlots.value[i];
      if (slot.entry) {
        console.log(`전국 슬롯 ${i} 엔트리:`, slot.entry);
        console.log(`전국 슬롯 ${i} 이미지 URL:`, slot.entry.imageUrl);
        // 등록된 슬롯의 경우 저장된 이미지 URL 사용
        slot.mascotImageUrl = slot.entry.imageUrl || '';
        console.log(`전국 슬롯 ${i} 설정된 이미지 URL:`, slot.mascotImageUrl);
      }
    }
  } catch (error) {
    console.error('전국 슬롯 마스코트 이미지 업데이트 실패:', error);
  }
}

// 전국 랭킹 슬롯의 실제 랭킹 정보 업데이트 (득표수, 순위)
async function updateNationalSlotRankingInfo() {
  try {
    console.log('전국 랭킹 슬롯 랭킹 정보 업데이트 시작');
    
    // 전국 랭킹 데이터가 로드되어 있는지 확인
    if (!nationalRankings.value) {
      await loadNationalRankings();
    }
    
    for (let i = 0; i < nationalRankingSlots.value.length; i++) {
      const slot = nationalRankingSlots.value[i];
      if (slot.entry && nationalRankings.value) {
        // 실제 랭킹 데이터에서 해당 마스코트의 정보 찾기
        const rankingEntry = nationalRankings.value.entries.find(entry => 
          entry.mascotId === slot.entry!.mascotId
        );
        
        if (rankingEntry) {
          slot.voteCount = rankingEntry.votes;
          slot.rank = rankingEntry.rank;
          console.log(`전국 슬롯 ${i} 랭킹 정보 업데이트:`, { 
            voteCount: slot.voteCount, 
            rank: slot.rank 
          });
        } else {
          // 랭킹에 등록되지 않은 경우 기본값 설정
          slot.voteCount = 0;
          slot.rank = 0;
        }
      }
    }
  } catch (error) {
    console.error('전국 슬롯 랭킹 정보 업데이트 실패:', error);
  }
}

async function updateCampusSlotMascotImages() {
  try {
    console.log('교내 랭킹 슬롯 마스코트 이미지 업데이트 시작');
    for (let i = 0; i < campusRankingSlots.value.length; i++) {
      const slot = campusRankingSlots.value[i];
      if (slot.entry) {
        console.log(`교내 슬롯 ${i} 엔트리:`, slot.entry);
        console.log(`교내 슬롯 ${i} 이미지 URL:`, slot.entry.imageUrl);
        // 등록된 슬롯의 경우 저장된 이미지 URL 사용
        slot.mascotImageUrl = slot.entry.imageUrl || '';
        console.log(`교내 슬롯 ${i} 설정된 이미지 URL:`, slot.mascotImageUrl);
      }
    }
  } catch (error) {
    console.error('교내 슬롯 마스코트 이미지 업데이트 실패:', error);
  }
}

// 교내 랭킹 슬롯의 실제 랭킹 정보 업데이트 (득표수, 순위)
async function updateCampusSlotRankingInfo() {
  try {
    console.log('교내 랭킹 슬롯 랭킹 정보 업데이트 시작');
    
    // 교내 랭킹 데이터가 로드되어 있는지 확인
    if (!campusRankings.value) {
      await loadCampusRankings();
    }
    
    for (let i = 0; i < campusRankingSlots.value.length; i++) {
      const slot = campusRankingSlots.value[i];
      if (slot.entry && campusRankings.value) {
        // 실제 랭킹 데이터에서 해당 마스코트의 정보 찾기
        const rankingEntry = campusRankings.value.entries.find(entry => 
          entry.mascotId === slot.entry!.mascotId
        );
        
        if (rankingEntry) {
          slot.voteCount = rankingEntry.votes;
          slot.rank = rankingEntry.rank;
          console.log(`교내 슬롯 ${i} 랭킹 정보 업데이트:`, { 
            voteCount: slot.voteCount, 
            rank: slot.rank 
          });
        } else {
          // 랭킹에 등록되지 않은 경우 기본값 설정
          slot.voteCount = 0;
          slot.rank = 0;
        }
      }
    }
  } catch (error) {
    console.error('교내 슬롯 랭킹 정보 업데이트 실패:', error);
  }
}

async function handleSlotDelete(entryId: number) {
  if (!confirm('정말로 랭킹 참가를 취소하시겠습니까?')) return;
  
  try {
    await deleteRankingEntry(entryId);
    
    // 현재 활성 탭에 따라 적절한 슬롯 배열 선택
    const currentSlots = activeTab.value === 'national' ? nationalRankingSlots : campusRankingSlots;
    const updateSlotActivation = activeTab.value === 'national' ? updateNationalSlotActivation : updateCampusSlotActivation;
    
    // 슬롯에서 엔트리 제거
    const slotIndex = currentSlots.value.findIndex(slot => slot.entry?.entryId === entryId);
    if (slotIndex !== -1) {
      currentSlots.value[slotIndex].entry = null;
      currentSlots.value[slotIndex].mascotImageUrl = '';
      currentSlots.value[slotIndex].voteCount = 0;
      currentSlots.value[slotIndex].rank = 0;
    }
    
    // 슬롯 활성화 상태 업데이트
    updateSlotActivation();
    
    alert('랭킹 참가가 취소되었습니다.');
    
  } catch (error) {
    console.error('랭킹 참가 취소 실패:', error);
    alert('랭킹 참가 취소에 실패했습니다.');
  }
}

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
    
    // 4. 사용자의 투표 히스토리 가져오기 (임시로 기존 API 사용)
    try {
      // 현재 활성 탭에 따라 투표 히스토리 로드
      if (activeTab.value === 'campus') {
        const votedMascotIds = await getUserCampusVotedMascotIds();
        console.log('초기 교내 랭킹 투표한 마스코트 ID:', votedMascotIds);
        // 마스코트 ID를 엔트리 ID로 변환은 랭킹 데이터 로드 후에 수행
        votedEntries.value = new Set<number>();
      } else {
        const nationalVotedMascotIds = await getUserNationalVotedMascotIds();
        console.log('초기 전국 랭킹 투표한 마스코트 ID:', nationalVotedMascotIds);
        // 마스코트 ID를 엔트리 ID로 변환은 랭킹 데이터 로드 후에 수행
        nationalVotedEntries.value = new Set<number>();
      }
    } catch (error) {
      console.error('초기 투표 히스토리 로드 실패:', error);
      // 실패 시 빈 Set으로 초기화
      votedEntries.value = new Set<number>();
      nationalVotedEntries.value = new Set<number>();
    }

    // 5. 랭킹 엔트리 로드는 탭 변경 시 자동으로 로드됨
    
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
