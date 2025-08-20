<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-100 to-blue-300">
    <div class="flex flex-col items-center justify-center min-h-screen pt-16">
      <div class="bg-white rounded-2xl shadow-lg p-8 w-full max-w-4xl">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-3xl font-bold text-blue-600">친구 목록</h2>
          <router-link 
            to="/friend/add" 
            class="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-semibold"
          >
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
                
                <!-- 화살표 아이콘 -->
                <div class="text-blue-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
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
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-700 mb-2">친구가 없습니다</h3>
          <p class="text-gray-500 mb-6">새로운 친구를 추가하고 함께 쏠쏠Hey를 즐겨보세요!</p>
          <router-link 
            to="/friend/add" 
            class="inline-flex items-center px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-semibold text-lg"
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
            </svg>
            친구 추가하러 가기
          </router-link>
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
