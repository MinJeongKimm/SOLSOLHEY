<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-green-100 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8">
      <!-- 상단 헤더 -->
      <div class="relative mb-6">
        <!-- 뒤로가기 버튼 (절대 위치) -->
        <router-link 
          to="/friend" 
          class="absolute left-0 top-1/2 transform -translate-y-1/2 p-2 rounded-lg hover:bg-gray-100 transition-colors z-10"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
          </svg>
        </router-link>
        
        <!-- 중앙: 친구 추가 타이틀 (전체 화면 중앙) -->
        <h1 class="text-xl font-bold text-gray-800 text-center w-full">친구 추가</h1>
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
              <h4 class="font-semibold text-gray-800">{{ user.nickname }}</h4>
              <p class="text-sm text-gray-600">{{ user.campus || '캠퍼스 정보 없음' }}</p>
              <p class="text-xs text-gray-500">{{ user.email }}</p>
            </div>
          </div>
          
          <!-- 친구 추가 버튼 -->
          <button
            v-if="!user.isAlreadyFriend && !user.hasPendingRequest"
            @click="addFriend(user)"
            :disabled="isAdding"
            class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          >
            {{ isAdding ? '추가 중' : '추가' }}
          </button>
          
          <!-- 이미 친구인 경우 -->
          <button
            v-else-if="user.isAlreadyFriend"
            disabled
            class="px-4 py-2 bg-green-100 text-green-700 rounded-lg text-sm cursor-not-allowed"
          >
            친구
          </button>
          
          <!-- 친구 요청 대기 중인 경우 -->
          <button
            v-else-if="user.hasPendingRequest"
            disabled
            class="px-4 py-2 bg-yellow-100 text-yellow-700 rounded-lg text-sm cursor-not-allowed"
          >
            요청 중
          </button>
        </div>
      </div>

      <!-- 검색 결과가 없는 경우 -->
      <div v-else-if="searchQuery && !isSearching" class="text-center py-12">
        <div class="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4"></path>
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
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
          </svg>
        </div>
        <h3 class="text-xl font-semibold text-gray-700 mb-2">새로운 친구를 찾아보세요</h3>
        <p class="text-gray-500">닉네임이나 사용자명으로 검색하여 친구를 추가할 수 있습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { searchUsers as searchUsersApi, sendFriendRequest, type User } from '../api/friend';

const router = useRouter();
const searchQuery = ref('');
const searchResults = ref<User[]>([]);
const isSearching = ref(false);
const isAdding = ref(false);

// 사용자 검색 (디바운싱 적용)
let searchTimeout: ReturnType<typeof setTimeout>;
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
    const users = await searchUsersApi(searchQuery.value);
    // 백엔드에서 반환되는 친구 상태 정보를 올바르게 매핑
    searchResults.value = users.map((user: any) => ({
      ...user,
      isAlreadyFriend: user.isAlreadyFriend || false,
      hasPendingRequest: user.hasPendingRequest || false
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
    user.hasPendingRequest = true; // 요청 성공 시, UI를 즉시 '요청 중'으로 변경
    
    // 성공 메시지 표시
    alert(`${user.nickname}님에게 친구 요청을 보냈습니다.`);
  } catch (error) {
    console.error('친구 추가 실패:', error);
    alert('친구 추가에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isAdding.value = false;
  }
};
</script>
