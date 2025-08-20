<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-100 to-blue-300">
    <div class="flex flex-col items-center justify-center min-h-screen pt-16">
      <div class="bg-white rounded-2xl shadow-lg p-8 w-full max-w-lg">
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

        <!-- 친구가 있는 경우 -->
        <div v-if="friends.length > 0" class="space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div 
              v-for="friend in friends" 
              :key="friend.friendId"
              @click="viewFriendDetail(friend)"
              class="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-6 cursor-pointer hover:shadow-lg transition-all duration-200 border border-blue-100 hover:border-blue-300"
            >
              <div class="flex items-center space-x-4">
                <!-- 캐릭터 이미지 (기본 이미지 사용) -->
                <div class="w-16 h-16 bg-gradient-to-br from-blue-400 to-indigo-500 rounded-full flex items-center justify-center">
                  <span class="text-white text-2xl font-bold">{{ friend.nickname?.charAt(0) || '?' }}</span>
                </div>
                
                <!-- 친구 정보 -->
                <div class="flex-1">
                  <h3 class="text-lg font-semibold text-gray-800">{{ friend.nickname || '닉네임 없음' }}</h3>
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

        <!-- 친구가 없는 경우 -->
        <div v-else class="text-center py-16">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getFriendList, type Friend } from '../api/friend';



const router = useRouter();
const friends = ref<Friend[]>([]);

// 친구 목록 조회
onMounted(() => {
  fetchFriends();
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
    // 에러 처리 (사용자에게 알림 등)
  }
};
</script>
