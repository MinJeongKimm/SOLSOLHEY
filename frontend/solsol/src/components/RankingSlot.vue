<template>
  <div class="ranking-slot">
    <!-- 빈 슬롯 (등록되지 않은 상태) -->
    <div 
      v-if="!entry" 
      @click="$emit('slot-click')"
      :class="[
        'empty-slot cursor-pointer transition-all duration-300 hover:scale-105',
        isActive ? 'bg-blue-50 border-blue-300 hover:bg-blue-100' : 'bg-gray-50 border-gray-200'
      ]"
    >
      <div class="flex flex-col items-center justify-center h-full p-4">
        <div class="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mb-3">
          <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
        </div>
        <p class="text-sm text-gray-500 text-center">
          {{ isActive ? '클릭하여 랭킹에 참가' : '잠금 해제 필요' }}
        </p>
      </div>
    </div>

    <!-- 등록된 슬롯 (참가 완료 상태) -->
    <div 
      v-else 
      class="filled-slot bg-white border-2 border-green-200 shadow-md"
    >
      <!-- 삭제 아이콘 -->
      <div class="absolute top-2 right-2">
        <button 
          @click="$emit('delete', entry.entryId)"
          class="w-6 h-6 bg-red-500 hover:bg-red-600 text-white rounded-full flex items-center justify-center transition-colors duration-200"
          title="참가 취소"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 마스코트 이미지 -->
      <div class="mascot-image-container mb-3">
        <img 
          :src="mascotImageUrl" 
          :alt="entry.title"
          class="w-20 h-20 object-cover rounded-lg mx-auto border-2 border-gray-200"
          @error="handleImageError"
        />
      </div>

      <!-- 제목 -->
      <h3 class="text-sm font-medium text-gray-800 text-center mb-2 px-2 line-clamp-2">
        {{ entry.title }}
      </h3>

      <!-- 순위와 득표수 -->
      <div class="stats-container text-center">
        <div class="flex items-center justify-center space-x-3 mb-2">
          <div class="flex items-center space-x-1">
            <svg class="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
            <span class="text-sm font-medium text-gray-700">{{ rank }}위</span>
          </div>
          <div class="flex items-center space-x-1">
            <svg class="w-4 h-4 text-blue-500" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            <span class="text-sm font-medium text-gray-700">{{ voteCount }}표</span>
          </div>
        </div>
        
        <!-- 참가 일시 -->
        <p class="text-xs text-gray-500">
          {{ formatDate(entry.createdAt) }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { EntryResponse } from '../api/ranking';

interface Props {
  entry?: EntryResponse;
  isActive: boolean;
  mascotImageUrl?: string;
  voteCount?: number;
  rank?: number;
}

interface Emits {
  (e: 'slot-click'): void;
  (e: 'delete', entryId: number): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// 이미지 로드 에러 처리
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = '/mascot/pli.png'; // 기본 마스코트 이미지로 대체
};

// 날짜 포맷팅
const formatDate = (dateString: string): string => {
  try {
    let date: Date;
    
    // Java LocalDateTime 배열 형태 처리 (예: [2025, 8, 27, 21, 12, 14, 323184000])
    if (Array.isArray(dateString)) {
      const [year, month, day, hour, minute, second, nano] = dateString;
      // Java의 월은 0부터 시작하므로 -1, 나노초는 밀리초로 변환
      date = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1000000));
    }
    // ISO 형식 (2024-01-15T10:30:00.000Z)
    else if (typeof dateString === 'string' && dateString.includes('T')) {
      date = new Date(dateString);
    } 
    // 타임스탬프 형식 (1705297800000)
    else if (typeof dateString === 'string' && /^\d+$/.test(dateString)) {
      date = new Date(parseInt(dateString));
    }
    // 일반 날짜 형식 (2024-01-15)
    else if (typeof dateString === 'string') {
      date = new Date(dateString);
    }
    // 기타 경우
    else {
      date = new Date(dateString);
    }
    
    // 유효한 날짜인지 확인
    if (isNaN(date.getTime())) {
      return '날짜 없음';
    }
    
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 1) return '오늘';
    if (diffDays === 2) return '어제';
    if (diffDays <= 7) return `${diffDays - 1}일 전`;
    
    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
  } catch (error) {
    console.error('날짜 포맷팅 오류:', error, dateString);
    return '날짜 오류';
  }
};
</script>

<style scoped>
.ranking-slot {
  @apply relative w-full h-48 rounded-xl border-2 transition-all duration-300;
}

.empty-slot {
  @apply flex flex-col items-center justify-center;
}

.filled-slot {
  @apply flex flex-col items-center justify-center p-4;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mascot-image-container {
  @apply relative;
}

.stats-container {
  @apply w-full;
}
</style>

