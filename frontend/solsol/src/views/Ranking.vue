<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 p-2 sm:p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-6xl mx-auto p-4 sm:p-6 lg:p-8">
      <!-- 헤더 -->
      <div class="flex flex-col sm:flex-row items-center mb-6 sm:mb-8 relative">
        <!-- 뒤로가기 버튼 (모바일에서는 상단, 데스크톱에서는 왼쪽) -->
        <router-link 
          to="/mascot" 
          class="p-2 text-purple-500 hover:text-purple-700 transition-colors rounded-full hover:bg-purple-50 absolute left-0 top-0 sm:relative sm:left-auto sm:top-auto"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
          </svg>
        </router-link>
        
        <!-- 제목 (가운데) -->
        <h1 class="text-xl sm:text-2xl font-bold text-gray-800 text-center w-full mt-8 sm:mt-0">Ranking</h1>
      </div>

      <!-- 탭 네비게이션 -->
      <div class="border-b border-gray-200 mb-6 sm:mb-8">
        <nav class="-mb-px flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-8 justify-center">
          <button
            @click="activeTab = 'campus'"
            :class="[
              activeTab === 'campus'
                ? 'border-purple-500 text-purple-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
              'whitespace-nowrap py-2 px-1 border-b-2 sm:border-b-2 border-l-2 sm:border-l-0 font-medium text-sm'
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
              'whitespace-nowrap py-2 px-1 border-b-2 sm:border-b-2 border-l-2 sm:border-l-0 font-medium text-sm'
            ]"
          >
            전국 랭킹
          </button>
        </nav>
  </div>

  <!-- 전국 랭킹 참가 슬롯 섹션 -->
      <div v-if="activeTab === 'national'" class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4 sm:p-6 mb-6 border-2 border-blue-200">
        <div class="mb-4">
          <h2 class="text-base sm:text-lg font-bold text-gray-800 mb-2">전국 랭킹 참가 슬롯</h2>
          <p class="text-xs sm:text-sm text-gray-600">마스코트를 전국 랭킹에 등록하여 다른 사용자들과 경쟁해보세요!</p>
          
        </div>
        
        <!-- 슬롯 슬라이드 컨테이너 -->
        <div class="relative">
          <!-- 이전 버튼 -->
          <button 
            @click="slideToPrevious('national')"
            class="absolute left-0 top-1/2 transform -translate-y-1/2 z-10 bg-white/80 hover:bg-white text-gray-600 hover:text-gray-800 rounded-full p-2 shadow-lg transition-all duration-200"
            :class="{ 'opacity-50 cursor-not-allowed': currentSlideIndex.national === 0 }"
            :disabled="currentSlideIndex.national === 0"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>

          <!-- 슬롯 슬라이드 -->
          <div 
            ref="nationalSlotsContainer"
            class="flex overflow-x-auto snap-x snap-mandatory scrollbar-hide transition-transform duration-300 ease-in-out"
            @touchstart="handleTouchStart"
            @touchmove="handleTouchMove"
            @touchend="handleTouchEnd"
          >
            <div class="flex space-x-4 min-w-full">
              <div 
                v-for="(slot, index) in nationalRankingSlots" 
                :key="index"
                class="snap-start flex-shrink-0 w-full"
              >
                <RankingSlot
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
          </div>

          <!-- 다음 버튼 -->
          <button 
            @click="slideToNext('national')"
            class="absolute right-0 top-1/2 transform -translate-y-1/2 z-10 bg-white/80 hover:bg-white text-gray-600 hover:text-gray-800 rounded-full p-2 shadow-lg transition-all duration-200"
            :class="{ 'opacity-50 cursor-not-allowed': currentSlideIndex.national === nationalRankingSlots.length - 1 }"
            :disabled="currentSlideIndex.national === nationalRankingSlots.length - 1"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>
        </div>

        <!-- 슬라이드 인디케이터 -->
        <div class="flex justify-center mt-4 space-x-2">
          <button
            v-for="(slot, index) in nationalRankingSlots"
            :key="index"
            @click="slideToIndex('national', index)"
            class="w-2 h-2 rounded-full transition-all duration-200"
            :class="currentSlideIndex.national === index ? 'bg-blue-500 w-4' : 'bg-gray-300 hover:bg-gray-400'"
          ></button>
        </div>
  </div>

  <!-- 스냅샷 선택 모달 -->
  <SnapshotPickerModal
    :visible="showSnapshotPicker"
    :snapshots="snapshotList"
    @close="showSnapshotPicker = false"
    @select="onSnapshotPicked"
  />

  <!-- 교내 랭킹 참가 슬롯 섹션 -->
      <div v-if="activeTab === 'campus'" class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-4 sm:p-6 mb-6 border-2 border-purple-200">
        <div class="mb-4">
          <h2 class="text-base sm:text-lg font-bold text-gray-800 mb-2">교내 랭킹 참가 슬롯</h2>
          <p class="text-xs sm:text-sm text-gray-600">마스코트를 교내 랭킹에 등록하여 다른 사용자들과 경쟁해보세요!</p>
          
        </div>
        
        <!-- 슬롯 슬라이드 컨테이너 -->
        <div class="relative">
          <!-- 이전 버튼 -->
          <button 
            @click="slideToPrevious('campus')"
            class="absolute left-0 top-1/2 transform -translate-y-1/2 z-10 bg-white/80 hover:bg-white text-gray-600 hover:text-gray-800 rounded-full p-2 shadow-lg transition-all duration-200"
            :class="{ 'opacity-50 cursor-not-allowed': currentSlideIndex.campus === 0 }"
            :disabled="currentSlideIndex.campus === 0"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>

          <!-- 슬롯 슬라이드 -->
          <div 
            ref="campusSlotsContainer"
            class="flex overflow-x-auto snap-x snap-mandatory scrollbar-hide transition-transform duration-300 ease-in-out"
            @touchstart="handleTouchStart"
            @touchmove="handleTouchMove"
            @touchend="handleTouchEnd"
          >
            <div class="flex space-x-4 min-w-full">
              <div 
                v-for="(slot, index) in campusRankingSlots" 
                :key="index"
                class="snap-start flex-shrink-0 w-full"
              >
                <RankingSlot
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
          </div>

          <!-- 다음 버튼 -->
          <button 
            @click="slideToNext('campus')"
            class="absolute right-0 top-1/2 transform -translate-y-1/2 z-10 bg-white/80 hover:bg-white text-gray-600 hover:text-gray-800 rounded-full p-2 shadow-lg transition-all duration-200"
            :class="{ 'opacity-50 cursor-not-allowed': currentSlideIndex.campus === campusRankingSlots.length - 1 }"
            :disabled="currentSlideIndex.campus === campusRankingSlots.length - 1"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
            </svg>
          </button>
        </div>

        <!-- 슬라이드 인디케이터 -->
        <div class="flex justify-center mt-4 space-x-2">
          <button
            v-for="(slot, index) in campusRankingSlots"
            :key="index"
            @click="slideToIndex('campus', index)"
            class="w-2 h-2 rounded-full transition-all duration-200"
            :class="currentSlideIndex.campus === index ? 'bg-purple-500 w-4' : 'bg-gray-300 hover:bg-gray-400'"
          ></button>
        </div>
      </div>

      <!-- 교내 랭킹 -->
      <div v-if="activeTab === 'campus'" class="space-y-6">
        <!-- 교내 랭킹 필터 -->
        <div class="flex flex-col sm:flex-row justify-end space-y-2 sm:space-y-0 sm:space-x-4">
          <select
            v-model="campusFilters.sort"
            @change="loadCampusRankings"
            class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm w-full sm:w-auto"
          >
            <option value="votes_desc">득표순</option>
            <option value="newest">최신순</option>
          </select>
          
          <select
            v-model="campusFilters.period"
            @change="loadCampusRankings"
            class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm w-full sm:w-auto"
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
            class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-4 sm:p-6 hover:shadow-lg transition-all duration-200 border border-purple-100 hover:border-purple-300"
            >
            <div class="flex flex-col sm:flex-row items-start sm:items-center space-y-3 sm:space-y-0 sm:space-x-4">
              <!-- 순위와 이미지 (모바일에서는 가로 배치) -->
              <div class="flex items-center space-x-3 sm:space-x-4 w-full sm:w-auto">
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
                      :src="entryImageSrc(entry)"
                      :alt="`${entry.mascotName || '마스코트'} (${entry.ownerNickname})`"
                      class="w-12 h-12 rounded-lg object-cover"
                    />
                  </div>
                </div>

                <!-- 정보 -->
                <div class="flex-1 min-w-0">
                  <div class="flex flex-col sm:flex-row sm:items-center space-y-1 sm:space-y-0 sm:space-x-2 mb-1">
                    <span class="text-base sm:text-lg font-semibold text-gray-800">
                      {{ entry.entryTitle || entry.mascotName || '마스코트' }}
                    </span>
                    <span class="text-sm text-gray-600">({{ entry.ownerNickname }})</span>
                  </div>
                  <div class="flex flex-col sm:flex-row sm:items-center space-y-1 sm:space-y-0 sm:space-x-4 text-sm text-gray-600">
                    <span>득표: {{ entry.votes.toLocaleString() }}표</span>
                    <span v-if="entry.school?.name" class="text-gray-500">
                      학교: {{ entry.school.name }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 투표 버튼 (모바일에서는 전체 너비) -->
              <div class="flex-shrink-0 w-full sm:w-auto">
                <button
                  @click="voteForMascot(entry.entryId)"
                  :disabled="voting || !canVoteForMascot(entry.entryId)"
                  class="bg-purple-500 text-white px-3 py-2 sm:py-1.5 rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors w-full sm:w-auto"
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

          <!-- 페이지네이션 -->
          <div v-if="campusRankings.total > campusRankings.size" class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-4 sm:p-6 border border-purple-100">
            <div class="flex flex-col sm:flex-row items-center justify-between space-y-3 sm:space-y-0">
              <div class="text-sm text-gray-700 text-center sm:text-left">
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
        <div class="flex flex-col sm:flex-row justify-end space-y-2 sm:space-y-0 sm:space-x-4">
          <select
            v-model="nationalFilters.sort"
            @change="loadNationalRankings"
            class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm w-full sm:w-auto"
          >
            <option value="votes_desc">득표순</option>
            <option value="newest">최신순</option>
          </select>
          
          <select
            v-model="nationalFilters.period"
            @change="loadNationalRankings"
            class="rounded-lg border-gray-300 shadow-sm focus:border-purple-500 focus:ring-purple-500 text-sm w-full sm:w-auto"
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
              class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-4 sm:p-6 hover:shadow-lg transition-all duration-200 border border-purple-100 hover:border-purple-300"
            >
              <div class="flex flex-col sm:flex-row items-start sm:items-center space-y-3 sm:space-y-0 sm:space-x-4">
                <!-- 순위와 이미지 (모바일에서는 가로 배치) -->
                <div class="flex items-center space-x-3 sm:space-x-4 w-full sm:w-auto">
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
                      :src="entryImageSrc(entry)"
                      :alt="`${entry.mascotName || '마스코트'} (${entry.ownerNickname})`"
                      class="w-12 h-12 rounded-lg object-cover"
                    />
                  </div>

                  <!-- 정보 -->
                  <div class="flex-1 min-w-0">
                    <div class="flex flex-col sm:flex-row sm:items-center space-y-1 sm:space-y-0 sm:space-x-2 mb-1">
                      <span class="text-base sm:text-lg font-semibold text-gray-800">
                        {{ entry.entryTitle || entry.mascotName || '마스코트' }}
                      </span>
                      <span class="text-sm text-gray-600">({{ entry.ownerNickname }})</span>
                    </div>
                    <div class="flex flex-col sm:flex-row sm:items-center space-y-1 sm:space-y-0 sm:space-x-4 text-sm text-gray-600">
                      <span>득표: {{ entry.votes.toLocaleString() }}표</span>
                      <span v-if="entry.school?.name">학교: {{ entry.school.name }}</span>
                    </div>
                  </div>
                </div>

                <!-- 투표 버튼 (모바일에서는 전체 너비) -->
                <div class="flex-shrink-0 w-full sm:w-auto">
                  <button
                    @click="voteForMascot(entry.entryId)"
                    :disabled="voting || !canVoteForMascot(entry.entryId)"
                    class="bg-purple-500 text-white px-3 py-2 sm:py-1.5 rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors w-full sm:w-auto"
                  >
                    <span v-if="!canVoteForMascot(entry.entryId)">
                      <span v-if="currentUser && entry.ownerNickname === currentUser.nickname">내 마스코트</span>
                      <span v-else-if="nationalVotedEntries.has(entry.entryId)">이미 투표함</span>
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
  getUserEntries,
  getUserEntriesByType,
  createRankingEntry,
  deleteRankingEntry,
  getCurrentUserMascotSnapshot,
  getCurrentUserMascot,
  composeMascotImage,
  getCampusVoteableStatus,
  getNationalVoteableStatus,
  getUserSnapshots,
  type RankingResponse,
  type VoteRequest,
  type EntryResponse,
  type CreateEntryRequest
} from '../api/ranking';
import { bootstrapAuth, auth, getMascot, getMascotCustomization, getShopItems, saveMascotCustomization } from '../api/index';
import RankingSlot from '../components/RankingSlot.vue';
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

// 스냅샷 선택 모달 상태
const showSnapshotPicker = ref(false);
const snapshotList = ref<any[]>([]);

// 필터 설정
const campusFilters = ref({
  sort: 'votes_desc',
  period: 'weekly',
  size: 10, // 페이지 크기를 10개로 고정
  page: 0
});

// 프론트 측 이미지 URL 보정: 상대 경로('/uploads/...')를 백엔드 오리진으로 변환
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

// 배경 이미지 URL 조합 (import.meta 사용은 스크립트에서만)
const BASE_URL = (import.meta as any).env?.BASE_URL || '/';
function bgUrl(id?: string): string {
  const name = id || 'bg_base.png';
  return `${BASE_URL}backgrounds/${name}`;
}

// 랭킹 리스트/슬롯 공통 이미지 src 생성 (등록 이미지 우선, 없으면 배경)
function entryImageSrc(entry: any): string {
  return normalizeImageUrlClient(entry?.entryImageUrl) || bgUrl(entry?.backgroundId);
}

async function openSnapshotPicker(tab: 'national' | 'campus', slotIndex?: number) {
  try {
    const list = await getUserSnapshots();
    snapshotList.value = list || [];
    const slots = tab === 'national' ? nationalRankingSlots : campusRankingSlots;
    if (typeof slotIndex === 'number') {
      selectedSlotIndex.value = slotIndex;
    } else {
      const idx = slots.value.findIndex(s => s.isActive);
      selectedSlotIndex.value = idx >= 0 ? idx : 0;
    }
    showSnapshotPicker.value = true;
  } catch (e) {
    console.error('스냅샷 목록 로드 실패:', e);
    alert('스냅샷 목록을 불러오지 못했습니다.');
  }
}

function onSnapshotPicked(s: any) {
  const url = normalizeImageUrlClient(s?.imageUrl || '');
  currentMascot.value = { name: '스냅샷', imageUrl: url, snapshotId: s?.id || 0 };
  showSnapshotPicker.value = false;
  showRankingModal.value = true;
}

const nationalFilters = ref({
  sort: 'votes_desc',
  period: 'weekly',
  size: 10,
  page: 0
});

// 슬라이드 관련 상태
const currentSlideIndex = ref({
  national: 0,
  campus: 0
});

// 슬라이드 컨테이너 ref
const nationalSlotsContainer = ref<HTMLElement | null>(null);
const campusSlotsContainer = ref<HTMLElement | null>(null);

// 터치 관련 상태
const touchStartX = ref(0);
const touchEndX = ref(0);

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

// 투표 가능 여부 계산 (새로운 API 사용)
const voteableMascots = computed(() => {
  const result = new Map<number, boolean>();
  
  if (!currentUser.value) return result;
  
  // 교내 랭킹 투표 가능 여부
  if (campusRankings.value?.entries) {
    campusRankings.value.entries.forEach(entry => {
      const isOwnMascot = entry.ownerNickname === currentUser.value!.nickname;
      // 자기 자신의 마스코트는 투표 불가
      result.set(entry.entryId, !isOwnMascot);
    });
  }
  
  // 전국 랭킹 투표 가능 여부
  if (nationalRankings.value?.entries) {
    nationalRankings.value.entries.forEach(entry => {
      const isOwnMascot = entry.ownerNickname === currentUser.value!.nickname;
      // 자기 자신의 마스코트는 투표 불가
      result.set(entry.entryId, !isOwnMascot);
    });
  }
  
  return result;
});

// 투표 가능 여부 확인 (백엔드 로직과 일치)
const canVoteForMascot = (entryId: number) => {
  // 기본적으로 자기 자신의 마스코트는 투표 불가
  const entry = activeTab.value === 'campus' 
    ? campusRankings.value?.entries.find(e => e.entryId === entryId)
    : nationalRankings.value?.entries.find(e => e.entryId === entryId);
  
  if (!entry || !currentUser.value) {
    return false;
  }
  
  const isOwnMascot = entry.ownerNickname === currentUser.value.nickname;
  if (isOwnMascot) {
    return false;
  }
  
  // 백엔드에서 이미 투표한 경우를 확인하기 위해 voteableMascots 사용
  const canVote = voteableMascots.value.get(entryId) ?? false;
  console.log(`투표 가능 여부 확인 - entryId: ${entryId}, isOwnMascot: ${isOwnMascot}, canVote: ${canVote}`);
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

// 랭킹 데이터 로드 후 투표 가능 여부 새로고침
const refreshVoteableStatus = async (rankingType: 'campus' | 'national') => {
  try {
    console.log(`${rankingType} 랭킹 투표 가능 여부 새로고침 시작`);
    
    if (rankingType === 'campus' && campusRankings.value?.entries) {
      const entryIds = campusRankings.value.entries.map(entry => entry.entryId);
      const voteableStatus = await getCampusVoteableStatus(entryIds);
      
      // 투표 가능 여부 업데이트
      entryIds.forEach(entryId => {
        const isOwnMascot = campusRankings.value!.entries.find(e => e.entryId === entryId)?.ownerNickname === currentUser.value?.nickname;
        const canVote = voteableStatus[entryId] && !isOwnMascot;
        voteableMascots.value.set(entryId, canVote);
      });
      
      console.log('교내 랭킹 투표 가능 여부 새로고침 완료');
    } else if (rankingType === 'national' && nationalRankings.value?.entries) {
      const entryIds = nationalRankings.value.entries.map(entry => entry.entryId);
      const voteableStatus = await getNationalVoteableStatus(entryIds);
      
      // 투표 가능 여부 업데이트
      entryIds.forEach(entryId => {
        const isOwnMascot = nationalRankings.value!.entries.find(e => e.entryId === entryId)?.ownerNickname === currentUser.value?.nickname;
        const canVote = voteableStatus[entryId] && !isOwnMascot;
        voteableMascots.value.set(entryId, canVote);
      });
      
      console.log('전국 랭킹 투표 가능 여부 새로고침 완료');
    }
  } catch (error) {
    console.error(`${rankingType} 랭킹 투표 가능 여부 새로고침 실패:`, error);
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
    
    // 랭킹 데이터 로드 후 투표 가능 여부 새로고침
    await refreshVoteableStatus('campus');
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
    
    // 랭킹 데이터 로드 후 투표 가능 여부 새로고침
    await refreshVoteableStatus('national');
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
      // 투표 가능 여부 새로고침
      await refreshVoteableStatus('campus');
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
      // 투표 가능 여부 새로고침
      await refreshVoteableStatus('national');
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
        slot.entry = entries[index] || null;
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
        slot.entry = entries[index] || null;
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
  const currentSlots = activeTab.value === 'national' ? nationalRankingSlots : campusRankingSlots;
  if (!currentSlots.value[slotIndex].isActive) return;
  await openSnapshotPicker(activeTab.value, slotIndex);
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

    // Canvas에 그려 dataURL 생성(스냅샷 저장용)
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
    
    // 현재 활성 탭에 따라 랭킹 타입 결정
    const rankingType = activeTab.value === 'national' ? 'NATIONAL' : 'CAMPUS';
    
    // description이 필수이므로 빈 문자열 대신 기본값 설정
    const description = data.description && data.description.trim() !== '' ? data.description : '랭킹 참가';
    
    // 1) 스냅샷 저장(커스터마이징에 스냅샷만 추가 저장)
    try {
      const payload = { ...(customization as any), snapshotImageDataUrl: dataUrl };
      await saveMascotCustomization(payload);
    } catch (e) { console.warn('스냅샷 저장 실패(계속 진행):', e); }

    // 2) 최신 스냅샷 조회 → 스냅샷 기반 랭킹 등록
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
    const newEntry = await createRankingEntry(req);
    
    console.log('새로 생성된 엔트리:', newEntry);
    console.log('엔트리의 이미지 URL:', newEntry.imageUrl);
    
    // 현재 활성 탭에 따라 적절한 슬롯 배열 선택
    const currentSlots = activeTab.value === 'national' ? nationalRankingSlots : campusRankingSlots;
    const updateSlotActivation = activeTab.value === 'national' ? updateNationalSlotActivation : updateCampusSlotActivation;
    
    // 슬롯에 새 엔트리 할당
    currentSlots.value[selectedSlotIndex.value].entry = newEntry;
    currentSlots.value[selectedSlotIndex.value].mascotImageUrl = normalizeImageUrlClient(newEntry.imageUrl || '');
    
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
        // 등록된 슬롯의 경우 저장된 이미지 URL 사용 (상대 경로 보정)
        slot.mascotImageUrl = normalizeImageUrlClient(slot.entry.imageUrl || '');
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
    
    console.log('전국 랭킹 데이터:', nationalRankings.value);
    console.log('전국 랭킹 엔트리들:', nationalRankings.value?.entries);
    
    for (let i = 0; i < nationalRankingSlots.value.length; i++) {
      const slot = nationalRankingSlots.value[i];
      console.log(`전국 슬롯 ${i} 상태:`, slot);
      
      if (slot.entry && nationalRankings.value) {
        console.log(`전국 슬롯 ${i} 엔트리:`, slot.entry);
        console.log(`전국 슬롯 ${i} mascotId:`, slot.entry.mascotId, '타입:', typeof slot.entry.mascotId);
        
        // 실제 랭킹 데이터에서 해당 엔트리의 정보 찾기 (entryId 기준)
        const rankingEntry = nationalRankings.value.entries.find(entry => {
          console.log(`랭킹 엔트리 비교:`, { 
            rankingEntryId: entry.entryId, 
            rankingEntryIdType: typeof entry.entryId,
            slotEntryId: slot.entry!.entryId,
            slotEntryIdType: typeof slot.entry!.entryId,
            isMatch: entry.entryId === slot.entry!.entryId
          });
          return entry.entryId === slot.entry!.entryId;
        });
        
        console.log(`전국 슬롯 ${i} 매칭된 랭킹 엔트리:`, rankingEntry);
        
        if (rankingEntry) {
          slot.voteCount = rankingEntry.votes;
          slot.rank = rankingEntry.rank;
          console.log(`전국 슬롯 ${i} 랭킹 정보 업데이트 완료:`, { 
            voteCount: slot.voteCount, 
            rank: slot.rank 
          });
        } else {
          // 랭킹에 등록되지 않은 경우 기본값 설정
          slot.voteCount = 0;
          slot.rank = 0;
          console.log(`전국 슬롯 ${i} 랭킹 정보 없음 - 기본값 설정:`, { 
            voteCount: slot.voteCount, 
            rank: slot.rank 
          });
        }
      } else {
        console.log(`전국 슬롯 ${i} 엔트리 없음 또는 랭킹 데이터 없음`);
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

// (중복 제거) normalizeImageUrlClient는 상단에 정의되어 있습니다.

// 교내 랭킹 슬롯의 실제 랭킹 정보 업데이트 (득표수, 순위)
async function updateCampusSlotRankingInfo() {
  try {
    console.log('교내 랭킹 슬롯 랭킹 정보 업데이트 시작');
    
    // 교내 랭킹 데이터가 로드되어 있는지 확인
    if (!campusRankings.value) {
      await loadCampusRankings();
    }
    
    console.log('교내 랭킹 데이터:', campusRankings.value);
    console.log('교내 랭킹 엔트리들:', campusRankings.value?.entries);
    
    for (let i = 0; i < campusRankingSlots.value.length; i++) {
      const slot = campusRankingSlots.value[i];
      console.log(`교내 슬롯 ${i} 상태:`, slot);
      
      if (slot.entry && campusRankings.value) {
        console.log(`교내 슬롯 ${i} 엔트리:`, slot.entry);
        console.log(`교내 슬롯 ${i} mascotId:`, slot.entry.mascotId, '타입:', typeof slot.entry.mascotId);
        
        // 실제 랭킹 데이터에서 해당 엔트리의 정보 찾기 (entryId 기준)
        const rankingEntry = campusRankings.value.entries.find(entry => {
          console.log(`랭킹 엔트리 비교:`, { 
            rankingEntryId: entry.entryId, 
            rankingEntryIdType: typeof entry.entryId,
            slotEntryId: slot.entry!.entryId,
            slotEntryIdType: typeof slot.entry!.entryId,
            isMatch: entry.entryId === slot.entry!.entryId
          });
          return entry.entryId === slot.entry!.entryId;
        });
        
        console.log(`교내 슬롯 ${i} 매칭된 랭킹 엔트리:`, rankingEntry);
        
        if (rankingEntry) {
          slot.voteCount = rankingEntry.votes;
          slot.rank = rankingEntry.rank;
          console.log(`교내 슬롯 ${i} 랭킹 정보 업데이트 완료:`, { 
            voteCount: slot.voteCount, 
            rank: slot.rank 
          });
        } else {
          // 랭킹에 등록되지 않은 경우 기본값 설정
          slot.voteCount = 0;
          slot.rank = 0;
          console.log(`교내 슬롯 ${i} 랭킹 정보 없음 - 기본값 설정:`, { 
            voteCount: slot.voteCount, 
            rank: slot.rank 
          });
        }
      } else {
        console.log(`교내 슬롯 ${i} 엔트리 없음 또는 랭킹 데이터 없음`);
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

// 슬라이드 이동 함수
const slideToPrevious = (type: 'national' | 'campus') => {
  if (type === 'national' && nationalSlotsContainer.value) {
    if (currentSlideIndex.value.national > 0) {
      currentSlideIndex.value.national = currentSlideIndex.value.national - 1;
      const container = nationalSlotsContainer.value;
      const slotWidth = container.offsetWidth;
      container.scrollTo({ left: slotWidth * currentSlideIndex.value.national, behavior: 'smooth' });
    }
  } else if (type === 'campus' && campusSlotsContainer.value) {
    if (currentSlideIndex.value.campus > 0) {
      currentSlideIndex.value.campus = currentSlideIndex.value.campus - 1;
      const container = campusSlotsContainer.value;
      const slotWidth = container.offsetWidth;
      container.scrollTo({ left: slotWidth * currentSlideIndex.value.campus, behavior: 'smooth' });
    }
  }
};

const slideToNext = (type: 'national' | 'campus') => {
  if (type === 'national' && nationalSlotsContainer.value) {
    if (currentSlideIndex.value.national < nationalRankingSlots.value.length - 1) {
      currentSlideIndex.value.national = currentSlideIndex.value.national + 1;
      const container = nationalSlotsContainer.value;
      const slotWidth = container.offsetWidth;
      container.scrollTo({ left: slotWidth * currentSlideIndex.value.national, behavior: 'smooth' });
    }
  } else if (type === 'campus' && campusSlotsContainer.value) {
    if (currentSlideIndex.value.campus < campusRankingSlots.value.length - 1) {
      currentSlideIndex.value.campus = currentSlideIndex.value.campus + 1;
      const container = campusSlotsContainer.value;
      const slotWidth = container.offsetWidth;
      container.scrollTo({ left: slotWidth * currentSlideIndex.value.campus, behavior: 'smooth' });
    }
  }
};

const slideToIndex = (type: 'national' | 'campus', index: number) => {
  if (type === 'national' && nationalSlotsContainer.value) {
    if (index >= 0 && index < nationalRankingSlots.value.length) {
      currentSlideIndex.value.national = index;
      const container = nationalSlotsContainer.value;
      const slotWidth = container.offsetWidth;
      container.scrollTo({ left: slotWidth * index, behavior: 'smooth' });
    }
  } else if (type === 'campus' && campusSlotsContainer.value) {
    if (index >= 0 && index < campusRankingSlots.value.length) {
      currentSlideIndex.value.campus = index;
      const container = campusSlotsContainer.value;
      const slotWidth = container.offsetWidth;
      container.scrollTo({ left: slotWidth * index, behavior: 'smooth' });
    }
  }
};

// 터치 이벤트 핸들러
const handleTouchStart = (e: TouchEvent) => {
  touchStartX.value = e.changedTouches[0].screenX;
};

const handleTouchMove = (e: TouchEvent) => {
  touchEndX.value = e.changedTouches[0].screenX;
};

const handleTouchEnd = () => {
  const swipeThreshold = 50; // 스와이프 임계값
  const slideDirection = touchEndX.value - touchStartX.value;

  if (slideDirection > swipeThreshold) {
    // 오른쪽으로 스와이프 - 이전 슬롯
    if (activeTab.value === 'national') {
      slideToPrevious('national');
    } else if (activeTab.value === 'campus') {
      slideToPrevious('campus');
    }
  } else if (slideDirection < -swipeThreshold) {
    // 왼쪽으로 스와이프 - 다음 슬롯
    if (activeTab.value === 'national') {
      slideToNext('national');
    } else if (activeTab.value === 'campus') {
      slideToNext('campus');
    }
  }
};

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
/* 스크롤바 숨기기 */
.scrollbar-hide {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;  /* Chrome, Safari and Opera */
}

/* 슬롯 슬라이드 애니메이션 */
.slot-slide-enter-active,
.slot-slide-leave-active {
  transition: all 0.3s ease;
}

.slot-slide-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.slot-slide-leave-to {
  opacity: 0;
  transform: translateX(-100%);
}
</style>
