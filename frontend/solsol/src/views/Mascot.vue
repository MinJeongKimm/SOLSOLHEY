<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <!-- 메인 카드 -->
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="flex justify-between items-center mb-6">
        <!-- 좌측: My Room 타이틀 -->
        <h1 class="text-xl font-bold text-gray-800">My Room</h1>
        
        <!-- 우측: 4개 아이콘 버튼들 -->
        <div class="flex space-x-2">
          <button 
            @click="showNotReady('출석체크')"
            class="hover:opacity-70 transition-opacity p-1 rounded-lg hover:bg-gray-100"
          >
            <img src="/icons/icon_attendance.png" alt="출석" class="w-7 h-7" />
          </button>
          <button 
            @click="showNotReady('챌린지')"
            class="hover:opacity-70 transition-opacity p-1 rounded-lg hover:bg-gray-100"
          >
            <img src="/icons/icon_challenge.png" alt="챌린지" class="w-7 h-7" />
          </button>
          <button 
            @click="showNotReady('랭킹')"
            class="hover:opacity-70 transition-opacity p-1 rounded-lg hover:bg-gray-100"
          >
            <img src="/icons/icon_ranking.png" alt="랭킹" class="w-7 h-7" />
          </button>
          <button 
            @click="goToFriends"
            class="hover:opacity-70 transition-opacity p-1 rounded-lg hover:bg-gray-100"
          >
            <img src="/icons/icon_friends.png" alt="친구" class="w-7 h-7" />
          </button>
          <button 
            @click="showSharePopup"
            class="hover:opacity-70 transition-opacity p-1 rounded-lg hover:bg-gray-100"
          >
            <img src="/icons/icon_share.png" alt="공유" class="w-7 h-7" />
          </button>
        </div>
      </div>

      <!-- 포인트 & 좋아요 + 친구 썸네일 리스트 -->
      <div class="pb-4 mb-6 border-b border-gray-100">
        <div class="flex items-center justify-between">
          <!-- 좌측: 포인트 & 좋아요 (위아래 배치) -->
          <div class="flex flex-col space-y-2">
            <!-- 포인트 -->
            <div class="flex items-center space-x-2">
              <img src="/icons/icon_point.png" alt="포인트" class="w-5 h-5" />
              <span class="font-bold text-gray-900">{{ userCoins }}P</span>
            </div>
            <!-- 좋아요 -->
            <div class="flex items-center space-x-2">
              <img src="/icons/icon_like.png" alt="좋아요" class="w-5 h-5" />
              <span class="font-bold text-gray-900">{{ userLikes }}</span>
            </div>
          </div>
          
          <!-- 우측: 친구 썸네일 리스트 -->
          <div class="flex space-x-2">
            <div 
              v-for="friend in friends" 
              :key="friend.id"
              class="flex flex-col items-center"
            >
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white font-bold text-xs mb-1">
                {{ friend.name.charAt(0) }}
              </div>
              <span class="text-xs text-gray-600 truncate w-10 text-center">{{ friend.name }}</span>
            </div>
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
      <div v-else class="space-y-6">
        <!-- 메인 캔버스: 방 배경 + 마스코트 -->
        <div class="relative">
          <!-- 방 배경 -->
          <div 
            class="w-full h-80 rounded-xl shadow-lg relative overflow-hidden flex items-center justify-center"
            style="background: linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%)"
          >
            <!-- 배경 이미지 (크기 조정) -->
            <img 
              src="/backgrounds/bg_base.png" 
              alt="방 배경" 
              class="w-3/4 h-3/4 object-contain"
            />
            
            <!-- 마스코트 -->
            <div class="absolute inset-0 flex items-center justify-center">
              <div class="relative">
                <!-- 마스코트 이미지 (크기 키움) -->
                <img 
                  :src="getMascotImageUrl(currentMascot.type)" 
                  :alt="currentMascot.name" 
                  class="w-32 h-32 object-contain animate-float"
                  @error="handleImageError"
                />
                
                <!-- 장착된 아이템들 -->
                <div class="absolute inset-0">
                  <!-- 머리 아이템 -->
                  <img 
                    v-if="currentMascot.equippedItems.head" 
                    :src="currentMascot.equippedItems.head.imageUrl" 
                    :alt="currentMascot.equippedItems.head.name"
                    class="item-head absolute"
                  />
                  <!-- 액세서리 -->
                  <img 
                    v-if="currentMascot.equippedItems.accessory" 
                    :src="currentMascot.equippedItems.accessory.imageUrl" 
                    :alt="currentMascot.equippedItems.accessory.name"
                    class="item-accessory absolute"
                  />
                </div>
              </div>
            </div>
            
            <!-- 마스코트 이름 -->
            <div class="absolute top-3 left-3">
              <div class="bg-white bg-opacity-90 px-2 py-1 rounded-full">
                <span class="text-xs font-medium text-gray-800">{{ currentMascot.name }}</span>
              </div>
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
            <span class="text-sm text-gray-500">{{ currentMascot.experiencePoint }} / {{ getNextLevelExp() }} XP</span>
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
          
          <!-- 밥주기 -->
          <button 
            @click="showNotReady('밥주기')"
            class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-3 flex flex-col items-center space-y-1 hover:shadow-md transition-all transform hover:scale-105"
          >
            <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
              <img src="/action/action_feed.png" alt="밥주기" class="w-6 h-6" />
            </div>
            <span class="text-xs font-medium text-gray-700">밥주기</span>
          </button>
          
          <!-- 쇼핑하기 -->
          <button 
            @click="showNotReady('쇼핑하기')"
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
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { createMascot as createMascotApi, handleApiError, mascot, auth, createShareLink, createShareImage, ShareType, ImageType, type ShareLinkCreateRequest, type ShareImageCreateRequest } from '../api/index';
import { mockMascot, mascotTypes, levelExperience } from '../data/mockData';
import type { Mascot } from '../types/api';

const router = useRouter();

// 반응형 데이터
const currentMascot = ref<Mascot | null>(null);
const userCoins = ref(15000);
const userLikes = ref(151);

// 친구 목록 데이터
const friends = ref([
  { id: 1, name: 'Danne' },
  { id: 2, name: 'Joan Co' },
  { id: 3, name: 'Jerome' }
]);

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
  
  const currentExp = currentMascot.value.experiencePoint - currentLevel.requiredExp;
  const totalExp = nextLevel.requiredExp - currentLevel.requiredExp;
  
  return Math.min(100, (currentExp / totalExp) * 100);
}

// 꾸미기 화면으로 이동
function goToCustomize() {
  router.push('/mascot/customize');
}

// 마스코트 생성 화면으로 이동
function goToCreate() {
  router.push('/mascot/create');
}

// 친구 목록 화면으로 이동
function goToFriends() {
  router.push('/friend');
}

// 준비중 알림
function showNotReady(feature: string) {
  showToastMessage(`${feature} 기능은 준비중입니다! 🚧`);
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
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'}/shares/templates`, {
      headers: {
        'Authorization': `Bearer ${auth.getToken()}`
      }
    });
    console.log('백엔드 연결 상태:', response.status, response.ok);
    
    // 토큰 만료 체크
    if (response.status === 401) {
      console.log('토큰이 만료되었습니다. 로그인 페이지로 이동합니다.');
      showToastMessage('로그인이 만료되었습니다. 로그인 페이지로 이동합니다.');
      setTimeout(() => {
        auth.clearAuth();
        router.push('/');
      }, 2000);
    }
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
      const message = shareLinkData.value.message || '나의 마스코트와 함께 즐거운 시간을 보내보세요!';
      
      const shareUrl = `${window.location.origin}/mascot/${currentMascot.value?.id}`;
      const userNickname = auth.getUser()?.nickname || auth.getUser()?.username || '나의';
      const mascotName = currentMascot.value?.name || '마스코트';
      const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;
      
      console.log('링크 공유 시도:', { shareTitle, message, shareUrl });
      
      try {
        // 백엔드 API로 공유 링크 생성 (새로운 ShareLinkCreateRequest 구조)
        const shareLinkRequest: ShareLinkCreateRequest = {
          title: shareTitle,
          description: message,
          targetUrl: shareUrl,
          shareType: ShareType.MASCOT,
          thumbnailUrl: currentMascot.value ? getMascotImageUrl(currentMascot.value.type) : undefined
        };
        
        const response = await createShareLink(shareLinkRequest);
        
        if (response.success) {
          // 생성된 공유 링크로 공유
          const generatedShareUrl = response.data?.shareUrl || shareUrl;
          await navigator.share({
            title: shareTitle,
            text: message,
            url: generatedShareUrl
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
      const message = shareImageData.value.message || '나의 마스코트와 함께 즐거운 시간을 보내보세요!';
      
      console.log('이미지 공유 시도:', { message });
      
      try {
        // 마스코트 이미지 URL 준비
        const mascotImageUrl = currentMascot.value 
          ? `${window.location.origin}${getMascotImageUrl(currentMascot.value.type)}`
          : `${window.location.origin}/mascot/soll.png`;
        
        console.log('백엔드 API 호출 시작:', {
          imageUrl: mascotImageUrl,
          imageType: ImageType.MASCOT_SHARE,
          isPublic: true
        });
        
        // 백엔드 API로 공유 이미지 생성 (새로운 ShareImageCreateRequest 구조)
        const shareImageRequest: ShareImageCreateRequest = {
          imageUrl: mascotImageUrl,
          imageType: ImageType.MASCOT_SHARE,
          originalFilename: `mascot_${currentMascot.value?.name || 'unknown'}_share.png`,
          isPublic: true,
          width: 320,  // 마스코트 이미지 기본 크기
          height: 320
        };
        
        const response = await createShareImage(shareImageRequest);
        
        console.log('백엔드 API 응답:', response);
        
        if (response.success) {
          // 생성된 공유 이미지로 공유
          const shareUrl = response.data?.imageUrl || `${window.location.origin}/mascot/${currentMascot.value?.id}`;
          const userNickname = auth.getUser()?.nickname || auth.getUser()?.username || '나의';
          const mascotName = currentMascot.value?.name || '마스코트';
          const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;
          
          await navigator.share({
            title: shareTitle,
            text: message,
            url: shareUrl
          });
          showToastMessage('마스코트 이미지가 생성되어 공유되었습니다!');
        } else {
          showToastMessage('이미지 생성에 실패했습니다. 링크로 공유합니다.');
          // 이미지 생성 실패 시 링크 공유로 fallback
          const shareUrl = `${window.location.origin}/mascot/${currentMascot.value?.id}`;
          const userNickname = auth.getUser()?.nickname || auth.getUser()?.username || '나의';
          const mascotName = currentMascot.value?.name || '마스코트';
          const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;
          
          await navigator.share({
            title: shareTitle,
            text: message,
            url: shareUrl
          });
          showToastMessage('마스코트 링크를 공유했습니다!');
        }
      } catch (error) {
        console.error('이미지 생성 실패:', error);
        
        // 더 구체적인 에러 정보 로깅
        if (error instanceof Error) {
          console.error('에러 타입:', error.name);
          console.error('에러 메시지:', error.message);
          console.error('에러 스택:', error.stack);
        }
        
        // 에러 종류에 따른 메시지 표시
        let errorMessage = '이미지 생성에 실패했습니다.';
        if (error instanceof Error) {
          if (error.message.includes('Failed to fetch')) {
            errorMessage = '백엔드 서버에 연결할 수 없습니다.';
          } else if (error.message.includes('401') || error.message.includes('토큰이 만료되었습니다')) {
            errorMessage = '로그인이 만료되었습니다.';
            // 토큰 만료 시 로그인 페이지로 이동
            setTimeout(() => {
              auth.clearAuth();
              router.push('/');
            }, 2000);
          } else if (error.message.includes('404')) {
            errorMessage = '이미지 생성 API를 찾을 수 없습니다.';
          }
        }
        
        showToastMessage(errorMessage + ' 링크로 공유합니다.');
        
        // 에러 발생 시 링크 공유로 fallback
        const shareUrl = `${window.location.origin}/mascot/${currentMascot.value?.id}`;
        const userNickname = auth.getUser()?.nickname || auth.getUser()?.username || '나의';
        const mascotName = currentMascot.value?.name || '마스코트';
        const shareTitle = `${userNickname}의 마스코트 '${mascotName}'`;
        
        await navigator.share({
          title: shareTitle,
          text: message,
          url: shareUrl
        });
        showToastMessage('마스코트 링크를 공유했습니다!');
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
function loadMascotData() {
  const mascotData = mascot.getMascot();
  console.log('로드된 마스코트 데이터:', mascotData); // 디버깅용
  if (mascotData) {
    currentMascot.value = mascotData;
    console.log('currentMascot 설정 완료:', currentMascot.value); // 디버깅용
  } else {
    console.log('마스코트 데이터가 없습니다. 생성 페이지로 이동합니다.'); // 디버깅용
    // 마스코트가 없으면 생성 페이지로 이동
    router.push('/mascot/create');
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

// 컴포넌트 마운트
onMounted(() => {
  console.log('Mascot 컴포넌트 마운트됨');
  loadMascotData();
});
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

/* 아이템별 기본 스타일 */
.item-head {
  width: 60%;
  height: 60%;
  top: -15%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  object-fit: contain;
}

.item-accessory {
  width: 30%;
  height: 30%;
  top: 25%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  object-fit: contain;
}

.item-clothing {
  width: 80%;
  height: 80%;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  object-fit: contain;
}
</style>