<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="relative mb-6">
        <!-- 뒤로가기 버튼 (절대 위치) -->
        <router-link 
          to="/mascot" 
          class="absolute left-0 top-1/2 transform -translate-y-1/2 p-2 rounded-lg hover:bg-gray-100 transition-colors z-10"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
          </svg>
        </router-link>
        
        <!-- 중앙: 친구 목록 타이틀 (전체 화면 중앙) -->
        <h1 class="text-xl font-bold text-gray-800 text-center w-full">친구 목록</h1>
        
        <!-- 친구 추가 버튼 (오른쪽 절대 위치) -->
        <router-link 
          to="/friend/add" 
          class="absolute right-0 top-1/2 transform -translate-y-1/2 p-2 rounded-lg hover:bg-blue-600 transition-colors z-10 bg-blue-500"
        >
          <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
          </svg>
        </router-link>
      </div>

      <!-- 새로운 친구 요청 섹션 (요청이 있을 때만 표시) -->
      <div v-if="friendRequests.length > 0" class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold text-gray-800">새로운 친구 요청 {{ friendRequests.length }}</h3>
        </div>
        
        <div class="space-y-3">
          <div 
            v-for="request in friendRequests" 
            :key="request.requestId"
            class="bg-gradient-to-r from-yellow-50 to-orange-50 rounded-xl p-4 border border-yellow-200"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <!-- 사용자 아바타 -->
                <div class="w-12 h-12 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-full flex items-center justify-center">
                  <span class="text-white font-bold text-lg">{{ request.nickname?.charAt(0) || '?' }}</span>
                </div>
                
                <!-- 사용자 정보 -->
                <div>
                  <h4 class="font-semibold text-gray-800">{{ request.nickname }}</h4>
                  <p class="text-sm text-gray-600">{{ request.campus || '캠퍼스 정보 없음' }}</p>
                  <p class="text-xs text-gray-500">{{ request.email }}</p>
                  
                </div>
              </div>
              
              <!-- 수락/거절 버튼 -->
              <div class="flex space-x-2">
                <button
                  @click="acceptFriendRequest(request.requestId)"
                  :disabled="isProcessing"
                  class="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors text-sm"
                >
                  수락
                </button>
                <button
                  @click="rejectFriendRequest(request.userId)"
                  :disabled="isProcessing"
                  class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors text-sm"
                >
                  거절
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 내 친구 섹션 (친구가 있을 때만 표시) -->
      <div v-if="friends.length > 0">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold text-gray-800">내 친구 {{ friends.length }}</h3>
        </div>
        
        <div class="space-y-3">
          <div 
            v-for="friend in friends" 
            :key="friend.friendId"
            @click="viewFriendDetail(friend)"
            class="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-4 cursor-pointer hover:shadow-lg transition-all duration-200 border border-blue-100 hover:border-blue-300"
          >
            <div class="flex items-center space-x-3">
              <!-- 캐릭터 이미지 (기본 이미지 사용) -->
              <div class="w-12 h-12 bg-gradient-to-br from-blue-400 to-indigo-500 rounded-full flex items-center justify-center">
                <span class="text-white text-lg font-bold">{{ friend.nickname?.charAt(0) || '?' }}</span>
              </div>
              
              <!-- 친구 정보 -->
              <div class="flex-1">
                <h4 class="font-semibold text-gray-800">{{ friend.nickname }}</h4>
                <p class="text-sm text-gray-600">{{ friend.campus || '캠퍼스 정보 없음' }}</p>
                <p class="text-xs text-gray-500">{{ friend.email }}</p>
                <p class="text-xs text-blue-500">{{ friend.totalPoints || 0 }} 포인트</p>
              </div>
              
              <!-- 친구 삭제 버튼 -->
              <button
                @click="showDeleteConfirm(friend)"
                :disabled="isProcessing"
                class="text-red-500 hover:text-red-700 disabled:text-red-300 transition-colors p-1 rounded-lg hover:bg-red-50"
                title="친구 삭제"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 친구가 없고 요청도 없는 경우 -->
      <div v-if="friends.length === 0 && friendRequests.length === 0" class="text-center py-16">
        <div class="w-24 h-24 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
          </svg>
        </div>
        <h3 class="text-xl font-semibold text-gray-700 mb-2">친구가 없습니다</h3>
        <p class="text-gray-500 mb-6">새로운 친구를 추가하고 함께 쏠쏠Hey를 즐겨보세요!</p>
      </div>
    </div>

    <!-- 삭제 확인 팝업 -->
    <div v-if="showDeleteModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 shadow-xl max-w-md w-full">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">친구 삭제</h3>
        <p class="text-gray-600 mb-6">
          {{ friendToDelete?.nickname }} 친구를 정말로 삭제하시겠습니까?
          이 작업은 되돌릴 수 없습니다.
        </p>
        <div class="flex justify-end space-x-2">
          <button
            @click="closeDeleteModal"
            class="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors text-sm"
          >
            취소
          </button>
          <button
            @click="deleteFriend"
            :disabled="isProcessing"
            class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors text-sm"
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getFriendList, getFriendRequests, acceptFriendRequest as acceptRequest, rejectFriendRequest as rejectRequest, deleteFriend as deleteFriendRequest, type Friend, type PendingFriendRequest } from '../api/friend';

const router = useRouter();
const friends = ref<Friend[]>([]);
const friendRequests = ref<PendingFriendRequest[]>([]);
const isProcessing = ref(false);

// 삭제 확인 팝업 관련 상태
const showDeleteModal = ref(false);
const friendToDelete = ref<Friend | null>(null);

const fetchFriends = async () => {
  try {
    // api/friend.ts에서 이미 데이터를 추출해서 반환하므로, 바로 할당합니다.
    friends.value = await getFriendList() || [];
  } catch (error) {
    console.error('친구 목록 조회 실패:', error);
    friends.value = [];
  }
};

const fetchFriendRequests = async () => {
  try {
    console.log('친구 요청 목록 조회 시작...');
    // api/friend.ts에서 이미 데이터를 추출해서 반환하므로, 바로 할당합니다.
    const requests = await getFriendRequests() || [];
    console.log('받은 친구 요청 목록:', requests);
    friendRequests.value = requests;
    console.log('friendRequests.value에 할당됨:', friendRequests.value);
  } catch (error) {
    console.error('친구 요청 목록 조회 실패:', error);
    friendRequests.value = [];
  }
};

const loadData = () => {
  console.log('loadData 함수 호출됨');
  fetchFriends();
  fetchFriendRequests();
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  console.log('FriendList 컴포넌트 마운트됨');
  loadData();
});

// /friend 경로로 진입할 때마다 데이터를 새로고침합니다.
watch(
  () => router.currentRoute.value.path,
  (path) => {
    console.log('라우터 경로 변경 감지:', path);
    if (path === '/friend') {
      console.log('/friend 경로 감지됨, 데이터 로드 시작');
      loadData();
    }
  },
  { immediate: true }
);

const viewFriendDetail = (friend: Friend) => {
  console.log('친구 상세 화면으로 이동:', friend);
  // 상세 페이지 라우팅 로직 추가 가능
};

// 삭제 확인 팝업 표시
const showDeleteConfirm = (friend: Friend) => {
  friendToDelete.value = friend;
  showDeleteModal.value = true;
};

// 친구 삭제 실행
const deleteFriend = async () => {
  if (!friendToDelete.value) return;
  
  isProcessing.value = true;
  try {
    await deleteFriendRequest(friendToDelete.value.friendId);
    alert('친구를 삭제했습니다.');
    await loadData(); // 삭제 후 목록 새로고침
  } catch (error) {
    console.error('친구 삭제 실패:', error);
    alert('친구 삭제에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isProcessing.value = false;
    showDeleteModal.value = false;
    friendToDelete.value = null;
  }
};

// 삭제 확인 팝업 닫기
const closeDeleteModal = () => {
  showDeleteModal.value = false;
  friendToDelete.value = null;
};

const acceptFriendRequest = async (userId: number) => {
  isProcessing.value = true;
  try {
    await acceptRequest(userId);
    alert('친구 요청을 수락했습니다!');
    await loadData(); // 수락 후 목록 전체를 새로고침
  } catch (error) {
    console.error('친구 요청 수락 실패:', error);
    alert('친구 요청 수락에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isProcessing.value = false;
  }
};

const rejectFriendRequest = async (userId: number) => {
  isProcessing.value = true;
  try {
    await rejectRequest(userId);
    alert('친구 요청을 거절했습니다.');
    // 거절 후 요청 목록에서 즉시 제거
    friendRequests.value = friendRequests.value.filter(req => req.userId !== userId);
  } catch (error) {
    console.error('친구 요청 거절 실패:', error);
    alert('친구 요청 거절에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isProcessing.value = false;
  }
};
</script>
