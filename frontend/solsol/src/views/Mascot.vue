<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 메인 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="flex justify-between items-start mb-6">
       <!-- 좌측: 마스코트 이름의 방 -->
       <h1 class="text-xl font-bold text-gray-800">{{ (currentMascot && currentMascot.name) || '마스코트' }}의 방</h1>
        
        <!-- 우측: 포인트 & 좋아요 -->
        <div class="flex flex-col space-y-1">
          <!-- 포인트 -->
          <div class="flex items-center justify-end">
            <img src="/icons/icon_point.png" alt="포인트" class="w-5 h-5 mr-2" />
            <span class="font-bold text-gray-900 min-w-[60px] text-center">{{ userCoins }}P</span>
          </div>
          <!-- 좋아요 -->
          <div class="flex items-center justify-end">
            <img src="/icons/icon_like.png" alt="좋아요" class="w-5 h-5 mr-2" />
            <span class="font-bold text-gray-900 min-w-[60px] text-center">{{ userLikes }}</span>
          </div>
          </div>
      </div>

      <!-- 마스코트가 있는 경우에만 메인 영역 렌더 (없으면 라우터가 생성 페이지로 이동) -->
      <div v-if="currentMascot" class="space-y-4">
        <!-- 메인 캔버스: 방 배경 + 레이어링(배경/마스코트/전경) -->
        <div class="relative">
          <!-- 방 배경 컨테이너 -->
          <div 
            ref="canvasEl"
            class="w-full h-80 rounded-xl shadow-lg relative overflow-hidden"
            :style="roomBackgroundStyle"
          >
            <!-- 레이어 1: 배경 아이템 (마스코트 뒤, 캔버스 전체 채움) -->
            <div class="absolute inset-0 z-0 overflow-hidden pointer-events-none">
              <img
                v-for="bg in backgroundEquippedItems"
                :key="bg.key"
                :src="bg.src"
                alt="배경 아이템"
                class="absolute inset-0 w-full h-full object-cover pointer-events-none"
              />
            </div>

            <!-- 레이어 2: 마스코트 (중간) -->
            <div class="absolute inset-0 z-10 flex items-center justify-center">
              <div class="relative animate-float">
                <img 
                  ref="mascotEl"
                  :src="getMascotImageUrl(currentMascot.type)" 
                  :alt="currentMascot.name" 
                  class="w-48 h-48 object-contain cursor-pointer"
                  @click="onMascotClick"
                  @load="updateRects"
                  @error="handleImageError"
                />
                <!-- (말풍선은 이 래퍼 밖에서 렌더링하여 떠다니지 않도록 함) -->
              </div>
            </div>

            <!-- 고정 말풍선(마스코트 아래, 움직임 없음) -->
            <div
              v-if="showBubble"
              class="absolute bottom-5 left-1/2 -translate-x-1/2 z-30 cursor-pointer select-none"
              role="button"
              tabindex="0"
              @click.stop="onBubbleClick"
              @keydown.enter.prevent="onBubbleClick"
            >
              <SpeechBubble
                :text="bubbleDisplayText"
                tail="top"
                aria-live="polite"
                class="leading-snug whitespace-nowrap"
                :style="{ whiteSpace: 'nowrap' }"
              />
            </div>

            <!-- 레이어 3: 전경 아이템 (마스코트 앞) -->
            <div class="absolute inset-0 z-20 animate-float pointer-events-none">
              <img
                v-for="ri in foregroundEquippedItems"
                :key="ri.key"
                :src="ri.src"
                class="absolute object-contain pointer-events-none"
                :style="styleForItem(ri)"
              />
            </div>

           
            <!-- 공유 버튼 -->
            <div class="absolute top-3 right-3 z-30 pointer-events-auto">
              <button 
                @click="showSharePopup"
                class="bg-white bg-opacity-90 p-1 rounded-lg hover:bg-opacity-100 transition-all flex items-center justify-center w-8 h-8"
              >
                <img src="/icons/icon_share.png" alt="공유" class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        <!-- 레벨 카드 (클릭 시 EXP 요약 표시) -->
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4 cursor-pointer" @click="openExpSummary">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center space-x-2">
              <span class="text-xl">⭐</span>
              <span class="text-lg font-bold text-gray-800">Lv.{{ currentMascot.level }}</span>
            </div>
            <span class="text-sm text-gray-500">{{ currentMascot.exp }} / {{ getNextLevelExp() }} XP</span>
          </div>
          
          <!-- 경험치 진행바 -->
          <div class="w-full bg-gray-200 rounded-full h-2">
            <div 
              class="h-2 rounded-full transition-all duration-500"
              :style="{ 
                width: getExpPercentage() + '%',
                background: 'linear-gradient(90deg, #0046FF 0%, #4A90E2 100%)'
              }"
            ></div>
          </div>
        </div>

        <!-- 퀵 액션 버튼들 -->
        <div class="grid grid-cols-3 gap-3">
          <!-- 꾸미기 -->
          <button 
            @click="goToCustomize"
            class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105"
          >
            <div class="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
              <img src="/action/action_customize.png" alt="꾸미기" class="w-6 h-6" />
            </div>
            <span class="text-xs font-medium text-gray-700">꾸미기</span>
          </button>
          
          <!-- 챌린지 -->
          <button 
            @click="goToChallenge"
            class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105"
          >
            <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <span class="text-xs font-medium text-gray-700">챌린지</span>
          </button>
          
          <!-- 쇼핑하기 -->
          <button 
            @click="goToShop"
            class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105"
          >
            <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
              <img src="/action/action_shop.png" alt="쇼핑하기" class="w-6 h-6" />
            </div>
            <span class="text-xs font-medium text-gray-700">쇼핑하기</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 공유 팝업 -->
    <div v-if="showShare" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
        <!-- 팝업 헤더 -->
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-bold text-gray-800">마스코트 공유하기</h3>
          <button 
            @click="closeSharePopup"
            class="text-gray-400 hover:text-gray-600 text-2xl"
          >
            ×
          </button>
        </div>

        <!-- 공유 타입 선택 -->
        <div class="mb-4">
          <div class="flex space-x-2 mb-3">
            <button 
              @click="shareType = 'link'"
              :class="[
                'flex-1 py-2 px-4 rounded-lg font-medium transition-colors',
                shareType === 'link' 
                  ? 'bg-blue-500 text-white' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              링크 공유
            </button>
            <button 
              @click="shareType = 'image'"
              :class="[
                'flex-1 py-2 px-4 rounded-lg font-medium transition-colors',
                shareType === 'image' 
                  ? 'bg-blue-500 text-white' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              이미지 공유
            </button>
          </div>
        </div>

        <!-- 링크 공유 폼 -->
        <div v-if="shareType === 'link'" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">메시지 (선택사항)</label>
            <textarea 
              v-model="shareLinkData.message" 
              placeholder="마스코트와 함께한 이야기를 적어보세요!"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              rows="3"
            ></textarea>
          </div>
        </div>

        <!-- 이미지 공유 폼 -->
        <div v-if="shareType === 'image'" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">메시지 (선택사항)</label>
            <textarea 
              v-model="shareImageData.message" 
              placeholder="마스코트와 함께한 이야기를 적어보세요!"
              class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              rows="3"
            ></textarea>
          </div>
          <div class="flex items-center justify-between">
            <div class="text-xs text-gray-600">스냅샷을 선택해서 이미지를 공유하세요.</div>
            <button @click="openSnapshotPickerForShare" class="text-xs px-3 py-2 bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg">
              스냅샷 선택
            </button>
          </div>
          <div v-if="selectedSnapshotForShare.url" class="border rounded-lg overflow-hidden">
            <img :src="selectedSnapshotForShare.url" class="w-full h-32 object-cover" alt="선택된 스냅샷 미리보기" />
            <div class="text-[11px] text-gray-500 p-1 text-center truncate">선택된 스냅샷</div>
          </div>
        </div>

        <!-- 공유 버튼 -->
        <div class="flex space-x-3 mt-6">
          <button 
            @click="closeSharePopup"
            class="flex-1 py-3 px-4 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300 transition-colors"
          >
            취소
          </button>
          <button 
            @click="handleShare"
            :disabled="!canShare"
            :class="[
              'flex-1 py-3 px-4 rounded-lg font-medium transition-colors',
              canShare 
                ? 'bg-blue-500 text-white hover:bg-blue-600' 
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            ]"
          >
            공유하기
          </button>
        </div>
      </div>
    </div>

    <!-- 스냅샷 선택 모달 (공유용) -->
    <SnapshotPickerModal
      :visible="showSnapshotPickerShare"
      :snapshots="snapshotListShare"
      @close="showSnapshotPickerShare = false"
      @select="onSnapshotPickedForShare"
    />

    <!-- EXP 요약 모달 -->
    <div v-if="showExpSummary" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-xl max-w-sm w-full max-h-[90vh] overflow-y-auto">
        <div class="flex justify-between items-center mb-4 p-6 pb-4">
          <h3 class="text-lg font-bold text-gray-800">오늘의 활동 요약</h3>
          <button @click="closeExpSummary" class="text-gray-400 hover:text-gray-600 text-2xl p-1">×</button>
        </div>
        
        <div v-if="expSummary" class="px-6 pb-6 space-y-4">
          <!-- 오늘 상태 헤더 -->
          <div class="text-center">
            <p class="text-xs text-gray-500 font-medium">오늘 상태 (KST)</p>
          </div>
          
          <!-- 출석 체크 -->
          <div class="bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl p-4 border border-green-100">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <span class="font-medium text-gray-700">출석 체크</span>
              </div>
              <div class="flex items-center space-x-2">
                <span v-if="expSummary.today?.attendance" class="text-green-600 text-sm font-medium">완료</span>
                <span v-else class="text-red-500 text-sm font-medium">미완료</span>
                <div v-if="expSummary.today?.attendance" class="w-6 h-6 bg-green-500 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div v-else class="w-6 h-6 bg-red-500 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- 7연속 보너스 -->
          <div class="bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl p-4 border border-purple-100">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <span class="font-medium text-gray-700">7연속 보너스</span>
              </div>
              <div class="flex items-center space-x-2">
                <span v-if="expSummary.today?.streak7" class="text-purple-600 text-sm font-medium">지급</span>
                <span v-else class="text-gray-500 text-sm font-medium">미지급</span>
                <div v-if="expSummary.today?.streak7" class="w-6 h-6 bg-purple-500 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div v-else class="w-6 h-6 bg-gray-400 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- 친구 방문 (내가) -->
          <div class="bg-gradient-to-r from-blue-50 to-cyan-50 rounded-xl p-4 border border-blue-100">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <div>
                  <span class="font-medium text-gray-700">친구 방문 (내가)</span>
                  <p class="text-xs text-gray-500 mt-1">{{ expSummary.today?.friend?.active?.count || 0 }}회 방문</p>
                </div>
              </div>
              <div class="text-right">
                <div class="text-sm font-medium text-blue-600">{{ expSummary.today?.friend?.active?.remainingTop3 || 0 }}회 남음</div>
                <div class="text-xs text-gray-500">+3 보너스</div>
              </div>
            </div>
          </div>

          <!-- 친구 방문 (나에게) -->
          <div class="bg-gradient-to-r from-indigo-50 to-blue-50 rounded-xl p-4 border border-indigo-100">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <span class="font-medium text-gray-700">친구 방문 (나에게)</span>
              </div>
              <div class="text-right">
                <div class="text-lg font-bold text-indigo-600">{{ expSummary.today?.friend?.passive?.count || 0 }}회</div>
                <div class="text-xs text-gray-500">받은 방문</div>
              </div>
            </div>
          </div>

          <!-- 챌린지 카테고리 -->
          <div class="bg-gradient-to-r from-orange-50 to-yellow-50 rounded-xl p-4 border border-orange-100">
            <div class="space-y-3">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <span class="font-medium text-gray-700">챌린지 카테고리</span>
              </div>
              <div class="grid grid-cols-2 gap-2">
                <div class="flex items-center justify-between p-2 bg-white rounded-lg">
                  <span class="text-sm text-gray-600">학사</span>
                  <div v-if="expSummary.today?.categories?.ACADEMIC" class="w-5 h-5 bg-green-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div v-else class="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </div>
                </div>
                <div class="flex items-center justify-between p-2 bg-white rounded-lg">
                  <span class="text-sm text-gray-600">금융</span>
                  <div v-if="expSummary.today?.categories?.FINANCE" class="w-5 h-5 bg-green-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div v-else class="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </div>
                </div>
                <div class="flex items-center justify-between p-2 bg-white rounded-lg">
                  <span class="text-sm text-gray-600">소셜</span>
                  <div v-if="expSummary.today?.categories?.SOCIAL" class="w-5 h-5 bg-green-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div v-else class="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </div>
                </div>
                <div class="flex items-center justify-between p-2 bg-white rounded-lg">
                  <span class="text-sm text-gray-600">이벤트</span>
                  <div v-if="expSummary.today?.categories?.EVENT" class="w-5 h-5 bg-green-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div v-else class="w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                    <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="text-center text-gray-500 text-sm p-6">요약을 불러오는 중...</div>
      </div>
    </div>

    <!-- 알림 토스트 -->
    <div 
      v-if="showToast" 
      class="fixed bottom-4 left-1/2 transform -translate-x-1/2 bg-gray-900 text-white px-4 py-2 rounded-lg shadow-lg z-50 transition-all"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, onActivated, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getFriendHome } from '../api/friend';
import { apiRequest, auth, createShareLink, getAiSpeech, getAvailableTemplates, getMascot, getMascotCustomization, getShopItems, handleApiError, ShareType, type MascotCustomization, type ShareLinkCreateRequest } from '../api/index';
import SpeechBubble from '../components/ui/SpeechBubble.vue';
import { levelExperience, mascotTypes } from '../data/mockData';
import { usePointStore } from '../stores/point';
import type { Mascot, ShopItem } from '../types/api';
import { toAbsoluteFromMascot } from '../utils/coordinates';

const router = useRouter();

// Pinia Store 사용
const pointStore = usePointStore();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(null);
// 포인트/좋아요
const userCoins = computed(() => pointStore.userPoints);
const userLikes = ref(0);
const userNickname = ref<string>('나의');

// 서버 커스터마이징 + 아이템 카탈로그 (동기 렌더)
const customization = ref<MascotCustomization | null>(null);
const shopItems = ref<ShopItem[]>([]);

// AI 말풍선 상태
const showBubble = ref(false);
const bubbleText = ref('');
const bubbleKind = ref<'ACADEMIC' | 'CHALLENGE' | null>(null);
const bubbleLocked = ref(false); // 여러 번 클릭 방지 락
// 모바일 일부 브라우저에서 줄바꿈 이슈가 있어, 강제 개행을 제거하고 브라우저 줄바꿈에 맡깁니다.
const bubbleDisplayText = computed(() => (bubbleText.value || ''));
function chunkByWords(text: string, maxCharsPerLine: number): string[] {
  const raw = (text || '').trim();
  if (!raw) return [''];
  const words = raw.split(/\s+/);
  const lines: string[] = [];
  let current = '';
  for (const w of words) {
    if (current.length === 0) {
      current = w;
      continue;
    }
    // 다음 단어를 추가했을 때 길이 검사(공백 1 포함)
    if ((current.length + 1 + w.length) <= maxCharsPerLine) {
      current += ' ' + w;
    } else {
      lines.push(current);
      current = w;
    }
  }
  if (current.length) lines.push(current);
  return lines;
}
let bubbleTimer: number | null = null;

const bubblePlaceholders = [
  '불렀어?',
  '나 여기 있어!',
  '응, 듣고 있어!',
  '짜자잔 ~',
  '뿅! 나타났어'
];

async function onMascotClick() {
  // 여러 번 클릭 방지: 말풍선 라이프사이클 종료 전엔 무시
  if (bubbleLocked.value) return;
  bubbleLocked.value = true;

  // 즉시 표시: 지연 없이 말풍선 노출 (귀여운 반응)
  if (bubbleTimer) window.clearTimeout(bubbleTimer);
  bubbleText.value = bubblePlaceholders[Math.floor(Math.random() * bubblePlaceholders.length)];
  showBubble.value = true;
  await nextTick();
  updateRects(); // 표시 직후 가용 너비 재계산
  try {
    const res = await getAiSpeech();
    bubbleText.value = res.message || '오늘은 가벼운 챌린지 어때?';
    bubbleKind.value = (res as any).kind || null;
  } catch (e) {
    console.warn('AI 말풍선 실패:', e);
    bubbleText.value = '오늘은 가벼운 챌린지 어때?';
    bubbleKind.value = null;
  }
  // 최종 문구 기준으로 5초 유지하고 락 해제
  if (bubbleTimer) window.clearTimeout(bubbleTimer);
  bubbleTimer = window.setTimeout(() => {
    showBubble.value = false;
    bubbleLocked.value = false;
  }, 5000);
}
// 과거 폴백 제거됨

// 리사이즈 핸들러 불필요(항상 오른쪽, 고정 10자 줄바꿈)

// 타입 표준화 유틸 (Customize.vue와 동일 컨셉)
function normalizeType(val: unknown): string {
  return (val ?? '').toString().toLowerCase();
}

// 렌더링용 파생: 커스터마이징 + 아이템 메타데이터 조인 후 레이어 분리
const BASE_ITEM_SIZE = 120; // Customize.vue와 동일 기준
// NOTE: 과거의 문자열 기반 currentMascot.equippedItem 의존성은 제거되었습니다.
//       서버 커스터마이징(getMascotCustomization) + 아이템 메타(getShopItems)만 사용합니다.
const joinedItems = computed(() => {
  if (!customization.value || !customization.value.equippedItems?.length) return [] as Array<{
    key: string;
    src: string;
    type: string;
    relativePosition: { x: number; y: number };
    scale: number;
    rotation: number;
  }>;
  const byId = new Map<number, ShopItem>(shopItems.value.map(s => [s.id, s]));
  return customization.value.equippedItems
    .map((e, idx) => {
      const si = byId.get(e.itemId);
      if (!si) return null;
      const t = normalizeType((si as any).type || (si as any).category);
      return {
        key: `${e.itemId}-${idx}`,
        src: si.imageUrl,
        type: t,
        relativePosition: { x: e.relativePosition.x, y: e.relativePosition.y },
        scale: e.scale,
        rotation: ((e.rotation ?? 0) % 360 + 360) % 360,
      };
    })
    .filter(Boolean) as any[];
});

const backgroundEquippedItems = computed(() => joinedItems.value.filter(i => i.type === 'background'));
const foregroundEquippedItems = computed(() => joinedItems.value.filter(i => i.type !== 'background'));

// 캔버스/마스코트 DOMRect 추적 (커스터마이즈 화면과 동일 좌표계로 렌더)
const canvasEl = ref<HTMLElement>();
const mascotEl = ref<HTMLElement>();
const canvasRect = ref<DOMRect | null>(null);
const mascotRect = ref<DOMRect | null>(null);
// 말풍선 크기 계산 제거: 자연 크기 사용

function updateRects() {
  if (canvasEl.value) {
    canvasRect.value = canvasEl.value.getBoundingClientRect();
  }
  if (mascotEl.value) {
    mascotRect.value = mascotEl.value.getBoundingClientRect();
  }
  // 말풍선 크기 계산 제거(자연 크기 사용)
}

function styleForItem(e: { relativePosition: { x: number; y: number }; scale: number; rotation: number }) {
  if (!canvasRect.value || !mascotRect.value) return {} as Record<string, string>;
  const center = toAbsoluteFromMascot(e.relativePosition, mascotRect.value);
  const left = center.x - canvasRect.value.left;
  const top = center.y - canvasRect.value.top;
  const size = Math.max(12, BASE_ITEM_SIZE * (e.scale ?? 1));
  return {
    position: 'absolute',
    left: `${left}px`,
    top: `${top}px`,
    width: `${size}px`,
    height: `${size}px`,
    transform: `translate(-50%, -50%) rotate(${e.rotation}deg)`,
    pointerEvents: 'none',
  } as Record<string, string>;
}

// 말풍선은 항상 오른쪽에 표시 (요청사항)

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 공유 팝업 관련 데이터
const showShare = ref(false);
const shareType = ref<'link' | 'image'>('link');
const shareLinkData = ref({
  message: ''
});
const shareImageData = ref({ message: '' });

// 공유용 스냅샷 선택 상태
import SnapshotPickerModal from '../components/SnapshotPickerModal.vue';
import { getUserSnapshots } from '../api/ranking';
import { createShareImage, ImageType, getApiOrigin } from '../api/index';
const showSnapshotPickerShare = ref(false);
const snapshotListShare = ref<any[]>([]);
const selectedSnapshotForShare = ref<{ id?: number; url?: string }>({});

// EXP 요약
const showExpSummary = ref(false);
const expSummary = ref<any | null>(null);

// (스냅샷 모달 제거)

// 공유 가능 여부 계산
const canShare = computed(() => {
  if (shareType.value === 'link') {
    return true; // 링크 공유는 항상 가능
  } else {
    return true; // 이미지 공유도 항상 가능 (템플릿 선택은 선택사항)
  }
});

// 유틸리티 함수들
function getMascotImageUrl(type: string): string {
  console.log('getMascotImageUrl 호출됨:', { type });
  const typeObj = mascotTypes.find(t => t.id === type);
  const imageUrl = typeObj ? typeObj.imageUrl : '/mascot/soll.png';
  console.log('결정된 이미지 URL:', imageUrl);
  return imageUrl;
}

function handleImageError(event: Event) {
  const target = event.target as HTMLImageElement;
  target.src = '/mascot/soll.png'; // 기본 이미지로 대체
  console.error('이미지 로드 실패:', target.src);
}

// 저장된 배경색/패턴을 뷰에 반영
const roomBackgroundStyle = computed(() => {
  const color = currentMascot.value?.backgroundColor;
  const pattern = currentMascot.value?.backgroundPattern;
  if (!color && !pattern) {
    return { background: 'linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)' } as Record<string, string>;
  }
  const style: Record<string, string> = {
    backgroundColor: color || '#f5f7ff',
  };
  if (pattern === 'dots') {
    style.backgroundImage = 'radial-gradient(circle, rgba(0,0,0,0.10) 1px, transparent 1px)';
    style.backgroundSize = '12px 12px';
  } else if (pattern === 'stripes') {
    style.backgroundImage = 'repeating-linear-gradient(45deg, rgba(0,0,0,0.06) 0 10px, transparent 10px 20px)';
  } else {
    style.backgroundImage = 'none';
  }
  return style;
});

// 캔버스 합성: 배경 → 마스코트 → 아이템(위치/스케일/회전)
async function composeShareImageBlob(): Promise<Blob> {
  // 캔버스 설정
  const DPR = Math.max(1, Math.min(3, Math.floor(window.devicePixelRatio || 1)));
  const canvasSize = 800; // 출력 품질 (원래대로 복원)
  const canvas = document.createElement('canvas');
  canvas.width = canvasSize * DPR;
  canvas.height = canvasSize * DPR;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Canvas context unavailable');
  ctx.scale(DPR, DPR);
  ctx.imageSmoothingEnabled = true;
  // 배경 생성 (꾸미기 화면과 동일한 방식)
  const bgColor = currentMascot.value?.backgroundColor || '#ffffff';
  ctx.fillStyle = bgColor;
  ctx.fillRect(0, 0, canvasSize, canvasSize);
  
  // 배경 패턴 그리기
  if (currentMascot.value?.backgroundPattern === 'dots') {
    ctx.fillStyle = 'rgba(0,0,0,0.12)';
    for (let x = 6; x < canvasSize; x += 12) {
      for (let y = 6; y < canvasSize; y += 12) {
        ctx.beginPath();
        ctx.arc(x, y, 1, 0, 2 * Math.PI);
        ctx.fill();
      }
    }
  } else if (currentMascot.value?.backgroundPattern === 'stripes') {
    // 꾸미기 화면과 동일하게 반대 대각선 방향으로 스트라이프 그리기
    ctx.fillStyle = 'rgba(0,0,0,0.08)';
    const stripeWidth = 10;
    const stripeGap = 20;
    
    // 반대 대각선 방향으로 스트라이프 그리기 (-45도, 오른쪽 위에서 왼쪽 아래)
    // 전체 캔버스를 완전히 덮도록 범위 확장
    for (let offset = -canvasSize * 1.5; offset < canvasSize * 2.5; offset += stripeGap) {
      ctx.save();
      ctx.translate(offset, 0);
      ctx.rotate(-Math.PI / 4); // -45도 회전 (반대 대각선)
      ctx.fillRect(0, -canvasSize * 1.5, stripeWidth, canvasSize * 3);
      ctx.restore();
    }
  }

  // 마스코트
  const mascotUrl = currentMascot.value ? getMascotImageUrl(currentMascot.value.type) : '/mascot/soll.png';
  const mascotImg = await loadImage(mascotUrl);
  const mascotBoxSize = Math.floor(canvasSize * 0.6); // 메인화면과 동일한 60% 비율
  const mascotX = (canvasSize - mascotBoxSize) / 2;
  const mascotY = (canvasSize - mascotBoxSize) / 2;
  ctx.drawImage(mascotImg, mascotX, mascotY, mascotBoxSize, mascotBoxSize);

      // 아이템들을 배경/일반 아이템으로 분리하여 처리
    if (customization.value && customization.value.equippedItems?.length) {
      const byId = new Map<number, ShopItem>(shopItems.value.map(s => [s.id, s]));
      
      // 아이템을 타입별로 분리 (배경 아이템을 맨 뒤로 보내기 위해)
      const backgroundItems: any[] = [];
      const normalItems: any[] = [];
      
      for (const e of customization.value.equippedItems) {
        const si = byId.get(e.itemId);
        if (!si) continue;
        
        // 배경 아이템인지 확인 (ranking.ts와 동일한 로직)
        if (si.type === 'BACKGROUND') {
          backgroundItems.push({ ...e, shopItem: si });
        } else {
          normalItems.push({ ...e, shopItem: si });
        }
      }

    // 1. 배경 아이템을 먼저 그리기 (맨뒤)
    for (const e of backgroundItems) {
      try {
        const img = await loadImage(e.shopItem.imageUrl);
        
        // 배경 아이템은 캔버스 전체를 덮도록 크기 조정
        const bgSize = canvasSize; // 캔버스 전체 크기
        const bgX = 0;
        const bgY = 0;
        
        ctx.save();
        ctx.drawImage(img, bgX, bgY, bgSize, bgSize);
        ctx.restore();
      } catch (error) {
        console.warn('배경 아이템 이미지 로드 실패:', e.itemId, error);
      }
    }

    // 2. 마스코트 다시 그리기 (배경 아이템 위에)
    ctx.drawImage(mascotImg, mascotX, mascotY, mascotBoxSize, mascotBoxSize);

    // 3. 일반 아이템 그리기 (마스코트 위에) - DraggableItem과 동일한 타입별 z-index 적용
    const zIndexFor = (cat: string) => ({
      background: 1,
      clothing: 5,
      head: 10,
      accessory: 15,
    } as Record<string, number>)[(cat || '').toLowerCase()] ?? 5;
    const sortedForeground = normalItems
      .map((it: any, idx: number) => ({ it, idx, z: zIndexFor(it.shopItem?.category) }))
      .sort((a, b) => (a.z - b.z) || (a.idx - b.idx));

    for (const { it: e } of sortedForeground) {
      try {
        const img = await loadImage(e.shopItem.imageUrl);
        
        // 이미지 공유 시에만 아이템 크기를 줄임 (일반 화면과 구분)
        const UI_MASCOT_PX = 128;
        const SHARE_ITEM_SIZE = 80; // 이미지 공유용 아이템 크기 (유지)
        const baseItemSize = (SHARE_ITEM_SIZE / UI_MASCOT_PX) * mascotBoxSize; // 약 0.625 * mascotBoxSize
        
        const centerX = mascotX + (e.relativePosition.x * mascotBoxSize);
        const centerY = mascotY + (e.relativePosition.y * mascotBoxSize);
        const size = Math.max(12, baseItemSize * (e.scale ?? 1));
        const rot = (((e.rotation ?? 0) % 360) + 360) % 360;

        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.rotate((rot * Math.PI) / 180);
        ctx.drawImage(img, -size / 2, -size / 2, size, size);
        ctx.restore();
      } catch (error) {
        console.warn('일반 아이템 이미지 로드 실패:', e.itemId, error);
      }
    }
  }

  return await new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Canvas toBlob failed'))), 'image/png');
  });
}

function getMascotTypeDisplay(type: string): string {
  const typeObj = mascotTypes.find(t => t.id === type);
  return typeObj ? typeObj.name : type;
}

function getNextLevelExp(): number {
  if (!currentMascot.value) return 0;
  const nextLevel = currentMascot.value.level + 1;
  const levelData = levelExperience.find(l => l.level === nextLevel);
  return levelData ? levelData.requiredExp : 9999;
}

function getExpPercentage(): number {
  if (!currentMascot.value) return 0;
  const currentLevel = levelExperience.find(l => l.level === currentMascot.value!.level);
  const nextLevel = levelExperience.find(l => l.level === currentMascot.value!.level + 1);
  
  if (!currentLevel || !nextLevel) return 100;
  
  const currentExp = currentMascot.value.exp - currentLevel.requiredExp;
  const totalExp = nextLevel.requiredExp - currentLevel.requiredExp;
  
  return Math.min(100, (currentExp / totalExp) * 100);
}

// 장착된 아이템의 이미지 URL 가져오기 (로컬 저장소 포함)

// 꾸미기 화면으로 이동
function goToCustomize() {
  router.push('/mascot/customize');
}

// 챌린지 화면으로 이동
function goToChallenge() {
  router.push('/challenge');
}

function onBubbleClick() {
  if (bubbleKind.value === 'CHALLENGE') {
    router.push('/challenge');
  }
}

// 길이 제한 유틸 (유니코드 안전)
// (길이 트림 제거) AI가 25~35자 내로 생성하도록 백엔드 프롬프트 조정됨

// 마스코트 생성 화면으로 이동
function goToCreate() {
  router.push('/mascot/create');
}

// 상점 화면으로 이동
function goToShop() {
  router.push('/shop');
}

// 토스트 메시지 표시
function showToastMessage(message: string) {
  toastMessage.value = message;
  showToast.value = true;
  
  setTimeout(() => {
    showToast.value = false;
  }, 3000);
}

// 공유 팝업 표시
async function showSharePopup() {
  // 토큰 상태 확인
  const ok = await auth.isAuthenticatedAsync();
  if (!ok) {
    showToastMessage('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
    setTimeout(() => {
      router.push('/');
    }, 2000);
    return;
  }
  
  showShare.value = true;
  shareType.value = 'link'; // 기본값 설정
  shareLinkData.value = { message: '' };
  shareImageData.value = { message: '' };
  selectedSnapshotForShare.value = {};
  
  // 백엔드 연결 상태 확인
  checkBackendStatus();
}

// 백엔드 연결 상태 확인
async function checkBackendStatus() {
  try {
    // 공유 템플릿 목록을 호출하여 백엔드 연결 확인
    await getAvailableTemplates();
    console.log('백엔드 연결 상태: OK');
  } catch (error) {
    console.error('백엔드 연결 실패:', error);
  }
}

async function openExpSummary() {
  try {
    showExpSummary.value = true;
    expSummary.value = null;
    const res = await apiRequest<any>('/exp/summary', { method: 'GET' });
    expSummary.value = res?.data || null;
  } catch (e) {
    expSummary.value = { totalExp: currentMascot.value?.exp ?? 0, level: currentMascot.value?.level ?? 1, today: null };
  }
}

function closeExpSummary() {
  showExpSummary.value = false;
}

// 공유 팝업 닫기
function closeSharePopup() {
  showShare.value = false;
}

async function openSnapshotPickerForShare() {
  try {
    const list = await getUserSnapshots();
    snapshotListShare.value = list || [];
    showSnapshotPickerShare.value = true;
  } catch (e) {
    console.error('스냅샷 목록 로드 실패:', e);
    showToastMessage('스냅샷 목록을 불러오지 못했습니다.');
  }
}

function onSnapshotPickedForShare(s: any) {
  const apiOrigin = (getApiOrigin && getApiOrigin()) || '';
  const url = (s?.imageUrl || '').startsWith('/uploads/') ? apiOrigin + s.imageUrl : s?.imageUrl || '';
  selectedSnapshotForShare.value = { id: s?.id, url };
  showSnapshotPickerShare.value = false;
}

// 공유 처리
async function handleShare() {
  try {
    if (shareType.value === 'link') {
      const message = shareLinkData.value.message || '나의 마스코트와 함께한 이야기를 적어보세요!';
      
      // 현재 로그인한 사용자의 ID 사용
      const u: any = await auth.fetchUser();
      const ownerId = u?.userId || 0;
      const targetUrl = `${window.location.origin}/mascot/view/${ownerId}`;

      const userNickname = u?.nickname || '나의';
      const mascotName = currentMascot.value?.name || '마스코트';
      const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;

      const shareLinkRequest: ShareLinkCreateRequest = {
        title: shareTitle,
        description: message,
        targetUrl: 'https://example.com', // 검증 통과를 위한 더미 URL
        shareType: ShareType.GENERAL,
        thumbnailUrl: undefined // 검증 통과를 위해 undefined로 설정
      };

      // 디버깅: 실제 전송되는 데이터 확인
      console.log('공유 링크 생성 요청 데이터:', shareLinkRequest);
      console.log('JSON.stringify 결과:', JSON.stringify(shareLinkRequest));

      const response = await createShareLink(shareLinkRequest);

      if (response.success && response.data) {
        // 서버 응답에서 linkCode를 받아서 원하는 형태의 링크 구성
        const shareUrl = `${window.location.origin}/mascot/share/${response.data.linkCode}`;
        
        await navigator.share({
          title: shareTitle,
          text: message,
          url: shareUrl // 새로 구성한 링크 사용
        });
        showToastMessage('마스코트 링크가 생성되어 공유되었습니다!');
      } else {
        throw new Error(response.message || '링크 생성에 실패했습니다.');
      }
    } else {
      // 이미지 공유 로직 (기존과 동일)
      const message = shareImageData.value.message || '나의 마스코트와 함께한 이야기를 적어보세요!';
      try {
        if (!selectedSnapshotForShare.value.url) {
          showToastMessage('먼저 스냅샷을 선택해 주세요.');
          return;
        }
        const u2: any = await auth.fetchUser();
        const userNickname = u2?.nickname || '나의';
        const mascotName = currentMascot.value?.name || 'mascot';
        const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;

        // 1) 스냅샷 원본 이미지를 Blob으로 다운로드
        const res = await fetch(selectedSnapshotForShare.value.url!, { mode: 'cors' });
        if (!res.ok) throw new Error('이미지 다운로드 실패');
        const blob = await res.blob();

        // 2) 네이티브 파일 공유 지원 시 파일로 공유
        const fileName = (() => {
          try {
            const u = new URL(selectedSnapshotForShare.value.url!);
            const base = u.pathname.split('/').pop() || `${mascotName}_snapshot.png`;
            return base.endsWith('.png') || base.endsWith('.jpg') || base.endsWith('.jpeg') ? base : `${mascotName}_snapshot.png`;
          } catch { return `${mascotName}_snapshot.png`; }
        })();

        const file = new File([blob], fileName, { type: blob.type || 'image/png' });

        if ((navigator as any).canShare && (navigator as any).canShare({ files: [file] })) {
          await (navigator as any).share({ title: shareTitle, text: message, files: [file] });
          showToastMessage('이미지로 공유했습니다!');
        } else {
          // 3) 데스크톱 등: 파일 다운로드로 폴백
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
          a.remove();
          URL.revokeObjectURL(url);
          showToastMessage('이미지를 다운로드했습니다.');
        }
      } catch (err) {
        console.error('스냅샷 공유 실패:', err);
        showToastMessage('스냅샷 공유에 실패했습니다.');
      }
    }
    closeSharePopup();
  } catch (error) {
    console.error('공유 실패:', error);
    if (error instanceof Error && error.name === 'AbortError') {
      showToastMessage('공유가 취소되었습니다.');
    } else {
      showToastMessage(String(error) || '공유에 실패했습니다.');
    }
  }
}

// 마스코트 데이터 로드
async function loadMascotData() {
  try {
    console.log('백엔드에서 마스코트 데이터를 로드합니다...'); // 디버깅용
    
    const mascotData = await getMascot();
    console.log('로드된 마스코트 데이터:', mascotData); // 디버깅용
    
    if (mascotData) {
      currentMascot.value = mascotData;
      console.log('currentMascot 설정 완료:', currentMascot.value); // 디버깅용
      // 서버 커스터마이징/아이템 카탈로그 로드
      await Promise.all([
        (async () => { try { customization.value = await getMascotCustomization(); } catch { customization.value = null; } })(),
        (async () => { try { shopItems.value = await getShopItems(); } catch { shopItems.value = []; } })(),
      ]);
      await nextTick();
    } else {
      console.log('마스코트 데이터가 없습니다. 생성 페이지로 이동합니다.'); // 디버깅용
      // 마스코트가 없으면 생성 페이지로 이동
      router.push('/mascot/create');
    }
  } catch (error) {
    console.error('마스코트 데이터 로드 실패:', error);
    
    // 에러 메시지 표시
    const errorMessage = handleApiError(error);
    showToastMessage(`마스코트 로드 실패: ${errorMessage}`);
    
    // 에러 발생 시 생성 페이지로 이동
    setTimeout(() => {
      router.push('/mascot/create');
    }, 2000);
  }
}

// currentMascot 변경 감지
watch(currentMascot, (newValue, oldValue) => {
  console.log('currentMascot 변경됨:', {
    oldValue,
    newValue,
    type: newValue?.type,
    name: newValue?.name
  });
}, { deep: true });

// (폴백 제거됨)

// 컴포넌트 마운트
onMounted(async () => {
  try {
    // 포인트 로드
    await pointStore.loadPoints();

    // 마스코트 정보 로드
    const mascotData = await getMascot();
    if (!mascotData) {
      router.push('/mascot/create');
      return;
    }
    currentMascot.value = mascotData;

    // 커스터마이징 + 아이템 카탈로그 동시 로드
    const [cust, items] = await Promise.all([
      getMascotCustomization().catch(() => null),
      getShopItems().catch(() => []),
    ]);
    customization.value = cust;
    shopItems.value = items as any;
    await nextTick();
    updateRects();
    window.addEventListener('resize', updateRects);

    // 내 홈 요약(좋아요 누적) 로드
    await reloadLikes();
  } catch (err) {
    console.error('메인화면 데이터 로드 실패:', err);
    handleApiError(err);
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', updateRects);
  if (bubbleTimer) {
    window.clearTimeout(bubbleTimer);
    bubbleTimer = null as unknown as number;
  }
  bubbleLocked.value = false;
});

// 라우트 복귀 시 최신 좋아요 수 재조회
onActivated(() => {
  reloadLikes();
});

async function reloadLikes() {
  try {
    const u: any = await auth.fetchUser();
    const uid = (u as any)?.userId as number | undefined;
    userNickname.value = (u as any)?.nickname || '나의';
    if (uid) {
      const myHome = await getFriendHome(uid);
      userLikes.value = Number(myHome?.likeCount ?? 0);
    }
  } catch (e) {
    // 무시: 좋아요 수는 보조 정보
    userLikes.value = 0;
  }
}
</script>

<script lang="ts">
// 보조 함수: 이미지 로드
export async function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = (e) => reject(e);
    img.src = src;
  });
}
</script>

<style scoped>
/* 플로팅 애니메이션 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

/* 스무스 전환 */
.transition-all {
  transition: all 0.3s ease;
}

/* (구) 타입별 절대 포지셔닝 스타일 제거됨: 실제 커스텀 좌표/스케일/회전 사용 */
</style>
