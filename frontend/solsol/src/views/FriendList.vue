<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center space-x-4">
          <router-link 
            to="/mascot" 
            class="p-2 text-blue-500 hover:text-blue-700 transition-colors rounded-full hover:bg-blue-50"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </router-link>
          <h2 class="text-3xl font-bold text-blue-600">친구 목록</h2>
        </div>
        <router-link 
          to="/friend/add" 
          class="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-semibold inline-flex items-center"
        >
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
          </svg>
          친구 추가
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
                  <span class="text-white font-bold text-lg">{{ request.nickname?.charAt(0) || request.username?.charAt(0) || '?' }}</span>
                </div>
                
                <!-- 사용자 정보 -->
                <div>
                  <h4 class="font-semibold text-gray-800">{{ request.nickname || request.username }}</h4>
                  <p class="text-sm text-gray-600">{{ request.campus || '캠퍼스 정보 없음' }}</p>
                  <p class="text-xs text-gray-500">@{{ request.username }}</p>
                </div>
              </div>
              
              <!-- 수락/거절 버튼 -->
              <div class="flex space-x-2">
                <button
                  @click="acceptFriendRequest(request.userId)"
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
                <span class="text-white text-lg font-bold">{{ friend.nickname?.charAt(0) || friend.username?.charAt(0) || '?' }}</span>
              </div>
              
              <!-- 친구 정보 -->
              <div class="flex-1">
                <h4 class="font-semibold text-gray-800">{{ friend.nickname || friend.username }}</h4>
                <p class="text-sm text-gray-600">{{ friend.campus || '캠퍼스 정보 없음' }}</p>
                <p class="text-xs text-blue-500">{{ friend.totalPoints || 0 }} 포인트</p>
              </div>
              
              <!-- 상세 보기 아이콘 -->
              <div class="text-blue-400">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getFriendList, getFriendRequests, acceptFriendRequest as acceptRequest, rejectFriendRequest as rejectRequest, type Friend, type PendingFriendRequest } from '../api/friend';

const router = useRouter();
const friends = ref<Friend[]>([]);
const friendRequests = ref<PendingFriendRequest[]>([]);
const isProcessing = ref(false);

// 친구 목록과 요청 목록 조회
onMounted(() => {
  fetchFriends();
  fetchFriendRequests();
});

// 친구 상세 화면으로 이동
const viewFriendDetail = (friend: Friend) => {
  // TODO: 친구 상세 화면으로 이동
  console.log('친구 상세 화면으로 이동:', friend);
  // router.push(`/friend/${friend.friendId}`);
};

// 친구 목록 조회 함수
const fetchFriends = async () => {
  try {
    const friendsList = await getFriendList();
    friends.value = friendsList;
  } catch (error) {
    console.error('친구 목록 조회 실패:', error);
  }
};

// 친구 요청 목록 조회 함수
const fetchFriendRequests = async () => {
  try {
    const requests = await getFriendRequests();
    friendRequests.value = requests;
  } catch (error) {
    console.error('친구 요청 목록 조회 실패:', error);
  }
};

// 친구 요청 수락
const acceptFriendRequest = async (userId: number) => {
  isProcessing.value = true;
  try {
    await acceptRequest(userId);
    // 요청 목록에서 제거하고 친구 목록 새로고침
    friendRequests.value = friendRequests.value.filter(req => req.userId !== userId);
    await fetchFriends();
    alert('친구 요청을 수락했습니다!');
  } catch (error) {
    console.error('친구 요청 수락 실패:', error);
    alert('친구 요청 수락에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isProcessing.value = false;
  }
};

// 친구 요청 거절
const rejectFriendRequest = async (userId: number) => {
  isProcessing.value = true;
  try {
    await rejectRequest(userId);
    // 요청 목록에서 제거
    friendRequests.value = friendRequests.value.filter(req => req.userId !== userId);
    alert('친구 요청을 거절했습니다.');
  } catch (error) {
    console.error('친구 요청 거절 실패:', error);
    alert('친구 요청 거절에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isProcessing.value = false;
  }
};
</script>
