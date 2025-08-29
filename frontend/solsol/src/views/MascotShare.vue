<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center p-4">
    <!-- 디버깅 정보 (개발 중에만 표시) -->
    <div class="absolute top-3 right-3 bg-white bg-opacity-90 p-2 rounded-lg text-xs">
      <div>로그인: {{ isLoggedIn }}</div>
      <div>권한: {{ permissions?.canSendFriendRequest ? '친구추가 가능' : '친구추가 불가' }}</div>
      <div>좋아요: {{ permissions?.canCheer ? '좋아요 가능' : '좋아요 불가' }}</div>
      <div class="mt-2 p-1 bg-gray-100 rounded">
        <div>permissions 전체: {{ JSON.stringify(permissions) }}</div>
        <div>canSendFriendRequest: {{ permissions?.canSendFriendRequest }}</div>
        <div>canCheer: {{ permissions?.canCheer }}</div>
      </div>
      <div class="mt-2 p-1 bg-blue-100 rounded">
        <div>친구 관계 상태:</div>
        <div>isFriend: {{ friendshipStatus?.isFriend }}</div>
        <div>hasPendingRequest: {{ friendshipStatus?.hasPendingRequest }}</div>
        <div>hasSentRequest: {{ friendshipStatus?.hasSentRequest }}</div>
      </div>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <div class="text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
        <p class="text-gray-600">마스코트 정보를 불러오는 중...</p>
      </div>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="errorMessage" class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <div class="text-center">
        <div class="text-red-500 text-6xl mb-4">⚠️</div>
        <h1 class="text-xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h1>
        <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
        <button @click="retryLoad" class="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600 transition-all">
          다시 시도
        </button>
      </div>
    </div>

    <!-- 메인 카드 -->
    <div v-else-if="currentMascot" class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <h1 class="text-xl font-bold text-gray-800 text-center mb-6">{{ pageTitle }}</h1>
      
      <!-- 메인 캔버스 -->
      <div class="relative">
        <div ref="canvasEl" class="w-full h-80 rounded-xl shadow-lg relative overflow-hidden" :style="roomBackgroundStyle">
          <div class="absolute inset-0 z-0 overflow-hidden pointer-events-none">
            <img v-for="bg in backgroundEquippedItems" :key="bg.key" :src="bg.imageUrl" alt="배경 아이템" class="absolute inset-0 w-full h-full object-cover pointer-events-none" />
          </div>
          <div class="absolute inset-0 z-10 flex items-center justify-center">
            <div class="relative animate-float">
              <img ref="mascotEl" :src="getMascotImageUrl(currentMascot.type)" :alt="currentMascot.name" class="w-48 h-48 object-contain" @load="updateRects" @error="handleImageError" />
            </div>
          </div>
          <div class="absolute inset-0 z-20 animate-float pointer-events-none">
            <img v-for="ri in foregroundEquippedItems" :key="ri.key" :src="ri.imageUrl" class="absolute object-contain pointer-events-none" :style="styleForItem(ri)" />
          </div>
          <div class="absolute top-3 left-3">
            <div class="bg-white bg-opacity-90 px-2 py-1 rounded-full">
              <span class="text-xs font-medium text-gray-800">{{ currentMascot.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 레벨 정보 -->
      <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4 mt-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <span class="text-xl">⭐</span>
            <span class="text-lg font-bold text-gray-800">Lv.{{ currentMascot.level }}</span>
          </div>
          <span class="text-sm text-gray-500">{{ currentMascot.exp || 0 }} / {{ getNextLevelExp() }} XP</span>
        </div>
      </div>

      <!-- 공유 정보 -->
      <div class="bg-gradient-to-r from-green-50 to-blue-50 rounded-xl p-4 mt-4">
        <div class="text-center">
          <p class="text-sm text-gray-600 mb-2">{{ shareLink.title }}</p>
          <p class="text-xs text-gray-500">{{ shareLink.description }}</p>
        </div>
      </div>

      <!-- 액션 버튼 영역 -->
      <div class="mt-4">
        <!-- 비로그인 상태: 로그인 버튼 -->
        <div v-if="!isLoggedIn">
          <button @click="handleLoginClick" class="w-full bg-blue-500 text-white py-3 rounded-lg font-bold hover:bg-blue-600 transition-all text-base">
            🔐 로그인하고 친구 맺기
          </button>
        </div>
        
        <!-- 로그인 상태: 액션 버튼들 -->
        <template v-else-if="canShowActionButtons">
          <!-- 버튼들을 중앙에 배치하는 컨테이너 -->
          <div class="flex justify-center items-center w-full">
            <!-- 친구가 아닌 경우: 친구추가 버튼만 표시 -->
            <button v-if="!friendshipStatus?.isFriend && !friendshipStatus?.hasSentRequest" 
                    @click="addFriend" 
                    :disabled="!permissions?.canSendFriendRequest" 
                    class="w-1/3 max-w-48 bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed">
              <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                <img src="/icons/icon_friends.png" alt="친구추가" class="w-6 h-6" />
              </div>
              <span class="text-xs font-medium text-gray-700">친구추가</span>
            </button>
            
            <!-- 친구 요청 대기 중: "요청됨" 상태 -->
            <div v-else-if="friendshipStatus?.hasSentRequest" 
                 class="w-1/3 max-w-48 bg-gradient-to-br from-gray-50 to-gray-100 rounded-xl p-3 flex flex-col items-center space-y-1 opacity-50">
              <div class="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center">
                <img src="/icons/icon_friends.png" alt="친구추가" class="w-6 h-6" />
              </div>
              <span class="text-xs font-medium text-gray-700">요청됨</span>
            </div>
            
            <!-- 친구인 경우: 좋아요 버튼만 표시 -->
            <button v-else-if="friendshipStatus?.isFriend" 
                    @click="cheerMascot" 
                    :disabled="!permissions?.canCheer" 
                    class="w-1/3 max-w-48 bg-gradient-to-br from-pink-50 to-pink-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed">
              <div class="w-12 h-12 bg-pink-100 rounded-full flex items-center justify-center">
                <img src="/icons/icon_like.png" alt="좋아요" class="w-6 h-6" />
              </div>
              <span class="text-sm font-medium text-gray-700">{{ permissions?.canCheer ? '좋아요' : '완료' }}</span>
            </button>
          </div>
        </template>
      </div>
    </div>

    <!-- 마스코트가 없는 경우 -->
    <div v-else class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <div class="text-center">
        <div class="text-6xl mb-4">🤔</div>
        <h1 class="text-xl font-bold text-gray-800 mb-4">마스코트 정보를 찾을 수 없습니다</h1>
        <p class="text-gray-600 mb-4">마스코트가 삭제되었거나 비공개로 설정되었을 수 있습니다.</p>
        <div class="space-y-2">
          <p class="text-xs text-gray-400">가능한 원인:</p>
          <ul class="text-xs text-gray-400 space-y-1">
            <li>• 해당 사용자가 마스코트를 생성하지 않음</li>
            <li>• 마스코트가 삭제됨</li>
            <li>• 공유 링크가 만료됨</li>
            <li>• 잘못된 링크</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { auth, apiRequest, handleApiError, getShopItems, getPublicMascot, getPublicShopItems, sendFriendRequest, sendInteraction, checkFriendship } from '../api/index';
import type { Mascot, MascotViewResponse, Permissions, ShopItem } from '../types/api';
import { levelExperience, mascotTypes } from '../data/mockData';
import { toAbsoluteFromMascot } from '../utils/coordinates';

const route = useRoute();
const router = useRouter();

const isLoading = ref(true);
const currentMascot = ref<Mascot | null>(null);
const permissions = ref<Permissions | null>(null);
const viewMode = ref<'SELF' | 'OTHER' | 'UNKNOWN'>('UNKNOWN');
const ownerNickname = ref('');
const shopItems = ref<ShopItem[]>([]);
const shareLink = ref<any>(null);
const isLoggedIn = ref(false);
const errorMessage = ref('');

// 친구 관계 상태
const friendshipStatus = ref<{
  isFriend: boolean;
  hasPendingRequest: boolean;
  hasSentRequest: boolean;
} | null>(null);

// 친구 관계 확인
const checkFriendshipStatus = async () => {
  if (!isLoggedIn.value || !shareLink.value?.userId) {
    return;
  }

  try {
    const response = await checkFriendship(shareLink.value.userId);
    if (response && response.data) {
      friendshipStatus.value = response.data;
      console.log('✅ 친구 관계 상태 확인 완료:', friendshipStatus.value);
    }
  } catch (error) {
    console.error('❌ 친구 관계 확인 실패:', error);
  }
};

const pageTitle = computed(() => {
  if (ownerNickname.value) return `${ownerNickname.value}님의 Room`;
  return 'Mascot Room';
});

const isLoggedInVisitor = computed(() => {
  // 로그인되어 있고, 다른 사용자의 마스코트를 보고 있는 경우
  return isLoggedIn.value && viewMode.value === 'OTHER';
});

const canShowActionButtons = computed(() => {
  // 로그인되어 있고, 마스코트 데이터가 있는 경우
  return isLoggedIn.value && currentMascot.value && permissions.value;
});

async function checkLoginStatus() {
  try {
    const isAuthenticated = await auth.isAuthenticatedAsync();
    isLoggedIn.value = isAuthenticated;
    console.log('🔐 로그인 상태 확인:', isAuthenticated);
    console.log('🔐 isLoggedIn.value:', isLoggedIn.value);
  } catch (error) {
    console.error('로그인 상태 확인 실패:', error);
    isLoggedIn.value = false;
  }
}

async function fetchShareLinkData() {
  const linkCode = route.params.linkCode as string;
  
  if (!linkCode) {
    isLoading.value = false;
    return;
  }

  try {
    // 로그인 상태 먼저 확인
    await checkLoginStatus();
    console.log('🔐 로그인 상태 확인 완료:', isLoggedIn.value);
    
    // 공유 링크 정보 조회
    const shareResponse = await apiRequest<any>(`/shares/links/${linkCode}`);
    
    if (shareResponse && shareResponse.data) {
      shareLink.value = shareResponse.data;
      
      // 디버깅: 공유 링크 데이터 확인
      console.log('🔗 공유 링크 데이터:', shareResponse.data);
      console.log('📸 마스코트 스냅샷:', shareResponse.data.mascotSnapshot);
      
      // shareLink에서 직접 userId 사용
      const ownerId = shareResponse.data.userId;
      
      if (ownerId) {
        await fetchMascotData(ownerId);
      } else {
        currentMascot.value = null;
      }
    } else {
      shareLink.value = null;
    }
  } catch (error) {
    console.error('공유 링크 조회 실패:', error);
    shareLink.value = null;
  } finally {
    isLoading.value = false;
  }
}

const fetchMascotData = async () => {
  try {
    console.log('🔍 마스코트 데이터 조회 시작...');
    console.log('🔍 shareLink.value:', shareLink.value);
    
    if (!shareLink.value?.userId) {
      console.error('❌ userId가 없음');
      errorMessage.value = '공유 링크 정보를 찾을 수 없습니다.';
      return;
    }

    console.log('🔍 getPublicMascot 호출:', shareLink.value.userId);
    const mascotViewResponse = await getPublicMascot(shareLink.value.userId);
    console.log('🔍 getPublicMascot 응답:', mascotViewResponse);

    if (mascotViewResponse && mascotViewResponse.data) {
      const mascot = mascotViewResponse.data.mascot;
      const permissionsData = mascotViewResponse.data.permissions;
      console.log('🔍 파싱된 마스코트:', mascot);
      console.log('🔍 파싱된 권한:', permissionsData);

      if (mascot) {
        currentMascot.value = {
          ...mascot,
          equippedLayout: mascot?.equippedLayout || null
        };
        
        // 권한 설정
        if (permissionsData) {
          // 백엔드에서 받은 권한을 기반으로 하되, 로그인 상태에 따라 조정
          permissions.value = {
            ...permissionsData,
            canSendFriendRequest: isLoggedIn.value && permissionsData.canSendFriendRequest,
            canCheer: isLoggedIn.value && permissionsData.canCheer
          };
        } else {
          // 기본 권한 설정 (로그인 상태에 따라)
          permissions.value = {
            canSendFriendRequest: isLoggedIn.value,
            canCheer: isLoggedIn.value,
            canViewDetails: true
          };
        }
        
        // viewMode 설정 (다른 사용자의 마스코트를 보고 있음)
        viewMode.value = 'OTHER';
        
        console.log('✅ currentMascot 설정 완료:', currentMascot.value);
        console.log('✅ permissions 설정 완료:', permissions.value);
        console.log('✅ viewMode 설정 완료:', viewMode.value);
      } else {
        console.error('❌ 마스코트 정보가 없음');
        errorMessage.value = '마스코트 정보를 찾을 수 없습니다.';
      }
    } else {
      console.error('❌ 응답 데이터가 없음');
      errorMessage.value = '마스코트 데이터를 불러올 수 없습니다.';
    }
  } catch (error: any) {
    console.error('❌ 마스코트 데이터 로드 실패:', error);
    
    // 더 구체적인 에러 메시지 제공
    let errorMsg = '마스코트 정보를 불러올 수 없습니다.';
    if (error.message === 'Failed to fetch') {
      errorMsg = '서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.';
    } else if (error.response?.status === 404) {
      errorMsg = '마스코트 정보를 찾을 수 없습니다.';
    } else if (error.response?.status === 403) {
      errorMsg = '접근 권한이 없습니다.';
    }
    
    errorMessage.value = errorMsg;
  }
};

async function handleLoginClick() {
  // 로그인 페이지로 이동하면서 현재 페이지를 리다이렉트 URL로 설정
  router.push('/login?redirect=' + encodeURIComponent(route.fullPath));
}

async function addFriend() {
  if (!isLoggedIn.value) {
    handleLoginClick();
    return;
  }

  if (!permissions.value?.canSendFriendRequest) {
    alert('친구추가 권한이 없습니다.');
    return;
  }

  try {
    const response = await sendFriendRequest(shareLink.value.userId);
    if (response && response.data) {
      alert('친구 요청이 성공적으로 전송되었습니다!');
      // 친구 요청 성공 시 권한 업데이트
      permissions.value = {
        ...permissions.value,
        canSendFriendRequest: false, // 친구 요청 전송 후 권한 제거
        canCheer: false // 좋아요 권한도 제거
      };
      console.log('✅ 친구 요청 전송 완료:', response.data);
    } else {
      console.error('❌ 친구 요청 전송 실패:', response);
      alert('친구 요청 전송에 실패했습니다.');
    }
  } catch (error: any) {
    console.error('❌ 친구 요청 전송 중 오류:', error);
    alert('친구 요청 전송 중 오류가 발생했습니다.');
  }
}

async function cheerMascot() {
  if (!isLoggedIn.value) {
    handleLoginClick();
    return;
  }

  if (!permissions.value?.canCheer) {
    alert('좋아요 권한이 없습니다.');
    return;
  }

  try {
    const response = await sendInteraction(shareLink.value.userId, 'CHEER');
    if (response && response.data) {
      alert('좋아요가 성공적으로 전송되었습니다!');
      // 좋아요 성공 시 권한 업데이트
      permissions.value = {
        ...permissions.value,
        canCheer: false // 좋아요 권한 제거
      };
      console.log('✅ 좋아요 전송 완료:', response.data);
    } else {
      console.error('❌ 좋아요 전송 실패:', response);
      alert('좋아요 전송에 실패했습니다.');
    }
  } catch (error: any) {
    console.error('❌ 좋아요 전송 중 오류:', error);
    alert('좋아요 전송 중 오류가 발생했습니다.');
  }
}

function retryLoad() {
  errorMessage.value = '';
  fetchShareLinkData();
  fetchMascotData();
}

function goToMascot() {
  if (isLoggedIn.value) {
    // 로그인된 사용자는 마스코트 메인 페이지로 이동
    router.push('/mascot');
  } else {
    // 비로그인 사용자는 로그인 페이지로 이동
    handleLoginClick();
  }
}

// --- 렌더링 로직 from Mascot.vue ---
const canvasEl = ref<HTMLElement>();
const mascotEl = ref<HTMLElement>();
const canvasRect = ref<DOMRect | null>(null);
const mascotRect = ref<DOMRect | null>(null);

function updateRects() {
  if (canvasEl.value) canvasRect.value = canvasEl.value.getBoundingClientRect();
  if (mascotEl.value) mascotRect.value = mascotEl.value.getBoundingClientRect();
}

const roomBackgroundStyle = computed(() => {
  const color = currentMascot.value?.backgroundColor;
  const pattern = currentMascot.value?.backgroundPattern;
  if (!color && !pattern) return { background: 'linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)' };
  const style: Record<string, string> = { backgroundColor: color || '#f5f7ff' };
  if (pattern === 'dots') {
    style.backgroundImage = 'radial-gradient(circle, rgba(0,0,0,0.10) 1px, transparent 1px)';
    style.backgroundSize = '12px 12px';
  } else if (pattern === 'stripes') {
    style.backgroundImage = 'repeating-linear-gradient(45deg, rgba(0,0,0,0.06) 0 10px, transparent 10px 20px)';
  }
  return style;
});

const backgroundEquippedItems = computed(() => {
  if (!currentMascot.value?.equippedLayout) {
    console.log('❌ equippedLayout이 없음');
    return [];
  }

  try {
    // equippedLayout JSON을 파싱
    console.log('🔍 JSON 파싱 시작:', currentMascot.value.equippedLayout);
    const equippedLayoutData = JSON.parse(currentMascot.value.equippedLayout);
    console.log('🔍 파싱된 equippedLayoutData:', equippedLayoutData);
    
    // equippedLayoutData.equippedItems 배열 추출
    const equippedItems = equippedLayoutData?.equippedItems;
    console.log('🔍 추출된 equippedItems:', equippedItems);
    
    if (!equippedItems || !Array.isArray(equippedItems)) {
      console.log('❌ equippedItems가 배열이 아님');
      return [];
    }

    // 배경 아이템만 필터링 (itemId === 31)
    const backgroundItems = equippedItems.filter(item => item.itemId === 31);
    console.log('🔍 배경 아이템 31 필터링:', backgroundItems);

    // shopItems에서 실제 아이템 정보 찾기
    return backgroundItems.map(item => {
      const shopItem = shopItems.value.find(shopItem => shopItem.id === item.itemId);
      console.log('🔍 배경 아이템 31 변환 결과:', { 
        item, 
        shopItem,
        shopItemId: shopItem?.id,
        shopItemImageUrl: shopItem?.imageUrl,
        shopItemName: shopItem?.name
      });
      
      return {
        key: `${item.itemId}-bg`,
        ...item,
        imageUrl: shopItem?.imageUrl || '/items/item_placeholder.png',
        name: shopItem?.name || 'Unknown Item'
      };
    });
  } catch (error) {
    console.error('❌ equippedLayout 파싱 오류:', error);
    return [];
  }
});

const foregroundEquippedItems = computed(() => {
  if (!currentMascot.value?.equippedLayout) {
    console.log('❌ equippedLayout이 없음');
    return [];
  }

  try {
    // equippedLayout JSON을 파싱
    console.log('🔍 JSON 파싱 시작:', currentMascot.value.equippedLayout);
    const equippedLayoutData = JSON.parse(currentMascot.value.equippedLayout);
    console.log('🔍 파싱된 equippedLayoutData:', equippedLayoutData);
    
    // equippedLayoutData.equippedItems 배열 추출
    const equippedItems = equippedLayoutData?.equippedItems;
    console.log('🔍 추출된 equippedItems:', equippedItems);
    
    if (!equippedItems || !Array.isArray(equippedItems)) {
      console.log('❌ equippedItems가 배열이 아님');
      return [];
    }

    // 전경 아이템만 필터링 (itemId !== 31)
    const foregroundItems = equippedItems.filter(item => item.itemId !== 31);
    console.log('🔍 전경 아이템 필터링:', foregroundItems);

    // shopItems에서 실제 아이템 정보 찾기
    return foregroundItems.map(item => {
      const shopItem = shopItems.value.find(shopItem => shopItem.id === item.itemId);
      console.log('🔍 전경 아이템 변환 결과:', { 
        item, 
        shopItem,
        shopItemId: shopItem?.id,
        shopItemImageUrl: shopItem?.imageUrl,
        shopItemName: shopItem?.name
      });
      
      return {
        key: `${item.itemId}-fg`,
        ...item,
        imageUrl: shopItem?.imageUrl || '/items/item_placeholder.png',
        name: shopItem?.name || 'Unknown Item'
      };
    });
  } catch (error) {
    console.error('❌ equippedLayout 파싱 오류:', error);
    return [];
  }
});

const BASE_ITEM_SIZE = 120;
function styleForItem(e: any) {
  if (!canvasRect.value || !mascotRect.value) return {};
  const center = toAbsoluteFromMascot(e.relativePosition, mascotRect.value);
  const left = center.x - canvasRect.value.left;
  const top = center.y - canvasRect.value.top;
  const size = Math.max(12, BASE_ITEM_SIZE * (e.scale ?? 1));
  return { position: 'absolute', left: `${left}px`, top: `${top}px`, width: `${size}px`, height: `${size}px`, transform: `translate(-50%, -50%) rotate(${e.rotation}deg)` };
}

function getMascotImageUrl(type: string) {
  const typeObj = mascotTypes.find(t => t.id === type);
  return typeObj ? typeObj.imageUrl : '/mascot/soll.png';
}

function handleImageError(event: Event) {
  (event.target as HTMLImageElement).src = '/mascot/soll.png';
}

function getNextLevelExp(): number {
  if (!currentMascot.value) return 0;
  const nextLevel = currentMascot.value.level + 1;
  return levelExperience.find(l => l.level === nextLevel)?.requiredExp ?? 9999;
}

// currentMascot 변경 감지
watch(currentMascot, (newValue, oldValue) => {
  console.log('🚨 currentMascot 변경됨:', {
    oldValue,
    newValue,
    oldName: oldValue?.name,
    newName: newValue?.name,
    oldType: oldValue?.type,
    newType: newValue?.type
  });
}, { deep: true });

// 로그인 상태 변경 감지
watch(isLoggedIn, (newValue) => {
  console.log('🔐 로그인 상태 변경 감지:', newValue);
  
  // 로그인 상태가 변경되면 권한 업데이트
  if (permissions.value) {
    permissions.value = {
      ...permissions.value,
      canSendFriendRequest: newValue,
      canCheer: newValue
    };
    console.log('✅ 권한 업데이트 완료:', permissions.value);
  }
});

// 컴포넌트 마운트
onMounted(async () => {
  try {
    console.log('🚀 MascotShare 컴포넌트 마운트됨');
    
    // 공개 상품 목록 조회 (인증 불필요)
    console.log('📡 getPublicShopItems() API 호출 시작...');
    const items = await getPublicShopItems();
    console.log('📡 getPublicShopItems() API 응답:', items);
    
    if (items && Array.isArray(items)) {
      shopItems.value = items;
      console.log('✅ shopItems 설정 완료:', shopItems.value);
      console.log('🔍 shopItems 상세 정보:', {
        length: shopItems.value.length,
        firstItem: shopItems.value[0],
        itemIds: shopItems.value.map(item => item.id),
        imageUrls: shopItems.value.map(item => item.imageUrl)
      });
    } else {
      console.warn('⚠️ shopItems 데이터가 없음:', items);
      shopItems.value = [];
    }
    
    // 마스코트 데이터 조회
    await fetchShareLinkData();
    await fetchMascotData();
    
    // 로그인된 사용자인 경우 친구 관계 확인
    if (isLoggedIn.value) {
      await checkFriendshipStatus();
    }
    
  } catch (error) {
    console.error('❌ 컴포넌트 마운트 중 오류:', error);
    errorMessage.value = '데이터를 불러오는 중 오류가 발생했습니다.';
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', updateRects);
});

</script>

<style scoped>
.animate-float {
  animation: float 3s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}
</style>
