<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-100 to-blue-300">
    <div class="flex flex-col items-center justify-center min-h-screen pt-16">
      <div class="bg-white rounded-2xl shadow-lg p-8 w-full max-w-2xl">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-3xl font-bold text-blue-600">친구 추가</h2>
          <router-link 
            to="/friend" 
            class="px-4 py-2 text-blue-500 hover:text-blue-700 transition-colors font-semibold"
          >
            ← 목록으로
          </router-link>
        </div>

        <!-- 사용자 검색 -->
        <div class="mb-8">
          <div class="relative">
            <input
              v-model="searchQuery"
              @input="searchUsers"
              type="text"
              placeholder="닉네임이나 사용자명으로 검색하세요"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent pr-12"
            />
            <div class="absolute inset-y-0 right-0 flex items-center pr-3">
              <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <!-- 검색 결과 -->
        <div v-if="searchResults.length > 0" class="space-y-3">
          <h3 class="text-lg font-semibold text-gray-700 mb-4">검색 결과</h3>
          <div 
            v-for="user in searchResults" 
            :key="user.userId"
            class="bg-gray-50 rounded-lg p-4 flex items-center justify-between"
          >
            <div class="flex items-center space-x-3">
              <!-- 사용자 아바타 -->
              <div class="w-12 h-12 bg-gradient-to-br from-green-400 to-blue-500 rounded-full flex items-center justify-center">
                <span class="text-white font-bold text-lg">{{ user.nickname?.charAt(0) || '?' }}</span>
              </div>
              
              <!-- 사용자 정보 -->
              <div>
                <h4 class="font-semibold text-gray-800">{{ user.nickname || '닉네임 없음' }}</h4>
                <p class="text-sm text-gray-600">{{ user.campus || '캠퍼스 정보 없음' }}</p>
                <p class="text-xs text-gray-500">@{{ user.username }}</p>
              </div>
            </div>
            
            <!-- 친구 추가 버튼 -->
            <button
              v-if="!user.isFriend && !user.isRequested"
              @click="addFriend(user)"
              :disabled="isAdding"
              class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
            >
              {{ isAdding ? '추가 중...' : '친구 추가' }}
            </button>
            
            <!-- 이미 친구인 경우 -->
            <span v-else-if="user.isFriend" class="px-4 py-2 bg-green-100 text-green-700 rounded-lg text-sm">
              이미 친구
            </span>
            
            <!-- 친구 요청 대기 중인 경우 -->
            <span v-else-if="user.isRequested" class="px-4 py-2 bg-yellow-100 text-yellow-700 rounded-lg text-sm">
              요청 대기 중
            </span>
          </div>
        </div>

        <!-- 검색 결과가 없는 경우 -->
        <div v-else-if="searchQuery && !isSearching" class="text-center py-12">
          <div class="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.47-.881-6.08-2.33"></path>
            </svg>
          </div>
          <p class="text-gray-500">검색 결과가 없습니다.</p>
          <p class="text-sm text-gray-400 mt-2">다른 검색어를 시도해보세요.</p>
        </div>

        <!-- 검색 중인 경우 -->
        <div v-if="isSearching" class="text-center py-12">
          <div class="animate-spin w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p class="text-gray-500">검색 중...</p>
        </div>

        <!-- 검색 전 안내 -->
        <div v-if="!searchQuery && !isSearching" class="text-center py-12">
          <div class="w-20 h-20 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-10 h-10 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-700 mb-2">새로운 친구를 찾아보세요</h3>
          <p class="text-gray-500">닉네임이나 사용자명으로 검색하여 친구를 추가할 수 있습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { searchUsers, sendFriendRequest, type User } from '../api/friend';



const router = useRouter();
const searchQuery = ref('');
const searchResults = ref<User[]>([]);
const isSearching = ref(false);
const isAdding = ref(false);

// 사용자 검색 (디바운싱 적용)
let searchTimeout: NodeJS.Timeout;
const searchUsers = () => {
  clearTimeout(searchTimeout);
  
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    return;
  }
  
  searchTimeout = setTimeout(async () => {
    await performSearch();
  }, 300);
};

// 실제 검색 수행
const performSearch = async () => {
  if (!searchQuery.value.trim()) return;
  
  isSearching.value = true;
  try {
    const users = await searchUsers(searchQuery.value);
    searchResults.value = users.map((user: any) => ({
      ...user,
      isFriend: false, // TODO: 친구 여부 확인 API 연동 필요
      isRequested: false // TODO: 친구 요청 여부 확인 API 연동 필요
    }));
  } catch (error) {
    console.error('사용자 검색 실패:', error);
    searchResults.value = [];
  } finally {
    isSearching.value = false;
  }
};

// 친구 추가
const addFriend = async (user: User) => {
  isAdding.value = true;
  try {
    await sendFriendRequest(user.userId);
    user.isRequested = true;
    
    // 성공 메시지 표시
    alert(`${user.nickname || user.username}님에게 친구 요청을 보냈습니다.`);
  } catch (error) {
    console.error('친구 추가 실패:', error);
    alert('친구 추가에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isAdding.value = false;
  }
};
</script>
