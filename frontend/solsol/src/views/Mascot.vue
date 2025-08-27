<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 메인 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="flex justify-between items-start mb-6">
        <!-- 좌측: My Room 타이틀 -->
        <h1 class="text-xl font-bold text-gray-800">My Room</h1>
        
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

      <!-- 마스코트가 없는 경우 생성 버튼 -->
      <div v-if="!currentMascot" class="text-center py-8">
        <div class="text-6xl mb-4">🥚</div>
        <p class="text-gray-600 mb-4">아직 마스코트가 없습니다</p>
        <button 
          @click="goToCreate"
          class="bg-purple-500 hover:bg-purple-600 text-white px-6 py-3 rounded-lg font-medium transition-colors"
        >
          마스코트 생성하기
        </button>
      </div>

      <!-- 마스코트가 있는 경우 메인 영역 -->
      <div v-else class="space-y-4">
        <!-- 메인 캔버스: 방 배경 + 레이어링(배경/마스코트/전경) -->
        <div class="relative">
          <!-- 방 배경 컨테이너 -->
          <div 
            class="w-full h-80 rounded-xl shadow-lg relative overflow-hidden"
            :style="roomBackgroundStyle"
          >
            <!-- 레이어 1: 배경 아이템 (마스코트 뒤, 캔버스 전체 채움) -->
            <div class="absolute inset-0 z-0 overflow-hidden">
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
                  :src="getMascotImageUrl(currentMascot.type)" 
                  :alt="currentMascot.name" 
                  class="w-32 h-32 object-contain"
                  @error="handleImageError"
                />
              </div>
            </div>

            <!-- 레이어 3: 전경 아이템 (마스코트 앞) -->
            <div class="absolute inset-0 z-20 animate-float">
              <img
                v-for="ri in foregroundEquippedItems"
                :key="ri.key"
                :src="ri.src"
                class="absolute object-contain pointer-events-none"
                :style="{
                  left: ri.leftPct + '%',
                  top: ri.topPct + '%',
                  width: ri.sizePx + 'px',
                  height: ri.sizePx + 'px',
                  transform: `translate(-50%, -50%) rotate(${ri.rotation}deg)`,
                }"
              />
            </div>

            <!-- 마스코트 이름 -->
            <div class="absolute top-3 left-3">
              <div class="bg-white bg-opacity-90 px-2 py-1 rounded-full">
                <span class="text-xs font-medium text-gray-800">{{ currentMascot.name }}</span>
              </div>
            </div>
            
            <!-- 공유 버튼 -->
            <div class="absolute top-3 right-3">
              <button 
                @click="showSharePopup"
                class="bg-white bg-opacity-90 p-1 rounded-lg hover:bg-opacity-100 transition-all flex items-center justify-center w-8 h-8"
              >
                <img src="/icons/icon_share.png" alt="공유" class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        <!-- 레벨 카드 -->
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4">
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
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { auth, createShareLink, getAvailableTemplates, getMascot, getMascotCustomization, getShopItems, handleApiError, ShareType, type MascotCustomization, type ShareLinkCreateRequest } from '../api/index';
import { levelExperience, mascotTypes } from '../data/mockData';
import { usePointStore } from '../stores/point';
import type { Mascot, ShopItem } from '../types/api';

const router = useRouter();

// Pinia Store 사용
const pointStore = usePointStore();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(null);
// 포인트 상태는 Store에서 관리
const userCoins = computed(() => pointStore.userPoints);
const userLikes = ref(151);

// 서버 커스터마이징 + 아이템 카탈로그 (동기 렌더)
const customization = ref<MascotCustomization | null>(null);
const shopItems = ref<ShopItem[]>([]);
// 과거 폴백 제거됨

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
    leftPct: number;
    topPct: number;
    sizePx: number;
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
        leftPct: e.relativePosition.x * 100,
        topPct: e.relativePosition.y * 100,
        sizePx: Math.max(24, BASE_ITEM_SIZE * (e.scale ?? 1)),
        rotation: ((e.rotation ?? 0) % 360 + 360) % 360,
      };
    })
    .filter(Boolean) as any[];
});

const backgroundEquippedItems = computed(() => joinedItems.value.filter(i => i.type === 'background'));
const foregroundEquippedItems = computed(() => joinedItems.value.filter(i => i.type !== 'background'));

// 토스트 알림
const showToast = ref(false);
const toastMessage = ref('');

// 공유 팝업 관련 데이터
const showShare = ref(false);
const shareType = ref<'link' | 'image'>('link');
const shareLinkData = ref({
  message: ''
});
const shareImageData = ref({
  message: ''
});

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
  const canvasSize = 800; // 출력 품질
  const canvas = document.createElement('canvas');
  canvas.width = canvasSize * DPR;
  canvas.height = canvasSize * DPR;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Canvas context unavailable');
  ctx.scale(DPR, DPR);
  ctx.imageSmoothingEnabled = true;
  // 배경
  const bgUrl = '/backgrounds/base/bg_blue.png';
  const bgImg = await loadImage(bgUrl);
  ctx.drawImage(bgImg, 0, 0, canvasSize, canvasSize);

  // 마스코트
  const mascotUrl = currentMascot.value ? getMascotImageUrl(currentMascot.value.type) : '/mascot/soll.png';
  const mascotImg = await loadImage(mascotUrl);
  const mascotBoxSize = Math.floor(canvasSize * 0.5); // 중앙 50%
  const mascotX = (canvasSize - mascotBoxSize) / 2;
  const mascotY = (canvasSize - mascotBoxSize) / 2;
  ctx.drawImage(mascotImg, mascotX, mascotY, mascotBoxSize, mascotBoxSize);

  // 아이템들(커스터마이징)
  if (customization.value && customization.value.equippedItems?.length) {
    const byId = new Map<number, ShopItem>(shopItems.value.map(s => [s.id, s]));
    // UI 기준과 동일 비율 유지(아이템 기본 크기: BASE_ITEM_SIZE / UI_MASCOT_PX * mascotBoxSize)
    const UI_MASCOT_PX = 128;
    const baseItemSize = (BASE_ITEM_SIZE / UI_MASCOT_PX) * mascotBoxSize; // 약 0.9375 * mascotBoxSize

    for (const e of customization.value.equippedItems) {
      const si = byId.get(e.itemId);
      if (!si) continue;
      const img = await loadImage(si.imageUrl);
      const centerX = mascotX + (e.relativePosition.x * mascotBoxSize);
      const centerY = mascotY + (e.relativePosition.y * mascotBoxSize);
      const size = Math.max(12, baseItemSize * (e.scale ?? 1));
      const rot = (((e.rotation ?? 0) % 360) + 360) % 360;

      ctx.save();
      ctx.translate(centerX, centerY);
      ctx.rotate((rot * Math.PI) / 180);
      ctx.drawImage(img, -size / 2, -size / 2, size, size);
      ctx.restore();
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
function showSharePopup() {
  // 토큰 상태 확인
  if (!auth.isAuthenticated()) {
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

// 공유 팝업 닫기
function closeSharePopup() {
  showShare.value = false;
}

// 공유 처리
async function handleShare() {
  try {
    console.log('공유 시작:', { shareType: shareType.value, currentMascot: currentMascot.value });
    
    if (shareType.value === 'link') {
      const message = shareLinkData.value.message || '나의 마스코트와 함께한 이야기를 적어보세요!';
      
      const shareUrl = `${window.location.origin}/mascot/${currentMascot.value?.id}`;
      const userNickname = auth.getUser()?.nickname || auth.getUser()?.username || '나의';
      const mascotName = currentMascot.value?.name || '마스코트';
      const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;
      
      console.log('링크 공유 시도:', { shareTitle, message, shareUrl });
      
      try {
        // 백엔드 API로 공유 링크 생성 (새로운 ShareLinkCreateRequest 구조)
        // 백엔드 validation 조건에 맞춰 데이터 준비
        const thumbnailUrl = currentMascot.value 
          ? `${window.location.origin}${getMascotImageUrl(currentMascot.value.type)}`
          : undefined;
        
        // validation 조건: title max 100자, description max 500자
        const validTitle = shareTitle.length > 100 ? shareTitle.substring(0, 100) : shareTitle;
        const validDescription = message.length > 500 ? message.substring(0, 500) : message;
        
        const shareLinkRequest: ShareLinkCreateRequest = {
          title: validTitle,
          description: validDescription || undefined,  // 빈 문자열이면 undefined로
          targetUrl: shareUrl,
          shareType: ShareType.GENERAL,  // 마스코트 공유는 GENERAL 타입 사용
          thumbnailUrl: thumbnailUrl
        };
        
        console.log('백엔드로 전송할 데이터:', shareLinkRequest);
        
        const response = await createShareLink(shareLinkRequest);
        
        if (response.success) {
          // 생성된 공유 링크로 공유
          const shareUrl = response.data?.shareUrl || `${window.location.origin}/mascot/${currentMascot.value?.id}`;
          await navigator.share({
            title: shareTitle,
            text: message,
            url: shareUrl
          });
          showToastMessage('마스코트 링크가 생성되어 공유되었습니다!');
        } else {
          showToastMessage('링크 생성에 실패했습니다. 기본 링크로 공유합니다.');
          await navigator.share({
            title: shareTitle,
            text: message,
            url: shareUrl
          });
          showToastMessage('마스코트 링크를 공유했습니다!');
        }
      } catch (error) {
        console.error('링크 생성 실패:', error);
        
        // 토큰 만료 체크
        if (error instanceof Error && 
            (error.message.includes('401') || error.message.includes('토큰이 만료되었습니다'))) {
          showToastMessage('로그인이 만료되었습니다. 로그인 페이지로 이동합니다.');
          // 토큰 만료 시 로그인 페이지로 이동
          setTimeout(() => {
            auth.clearAuth();
            router.push('/');
          }, 2000);
          return;
        }
        
        showToastMessage('링크 생성에 실패했습니다. 기본 링크로 공유합니다.');
        
        // 에러 발생 시 기본 링크 공유로 fallback
        await navigator.share({
          title: shareTitle,
          text: message,
          url: shareUrl
        });
        showToastMessage('마스코트 링크를 공유했습니다!');
      }
  } else {
      const message = shareImageData.value.message || '나의 마스코트와 함께한 이야기를 적어보세요!';
      try {
        const blob = await composeShareImageBlob();
        const mascotName = currentMascot.value?.name || 'mascot';
        const file = new File([blob], `${mascotName}_share.png`, { type: blob.type || 'image/png' });
        const userNickname = auth.getUser()?.nickname || auth.getUser()?.username || '나의';
        const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;

        if (navigator.canShare && navigator.canShare({ files: [file] })) {
          await navigator.share({ title: shareTitle, text: message, files: [file] });
          showToastMessage('이미지로 공유했습니다!');
        } else {
          // 데스크톱 등: 다운로드로 폴백
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `${mascotName}_share.png`;
          document.body.appendChild(a);
          a.click();
          a.remove();
          URL.revokeObjectURL(url);
          showToastMessage('이미지를 다운로드했습니다.');
        }
      } catch (err) {
        console.error('이미지 합성/공유 실패:', err);
        showToastMessage('이미지 합성에 실패했습니다.');
      }
  }
    closeSharePopup();
  } catch (error) {
    console.error('공유 실패:', error);
    
    // 더 구체적인 에러 메시지 표시
    if (error instanceof Error) {
      if (error.name === 'AbortError') {
        showToastMessage('공유가 취소되었습니다.');
      } else if (error.name === 'NotAllowedError') {
        showToastMessage('공유 권한이 거부되었습니다.');
      } else {
        showToastMessage(`공유 실패: ${error.message}`);
      }
    } else {
      showToastMessage('공유에 실패했습니다.');
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
  } catch (err) {
    console.error('메인화면 데이터 로드 실패:', err);
    handleApiError(err);
  }
});
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
