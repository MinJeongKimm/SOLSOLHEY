<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
      <!-- 모달 헤더 -->
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-xl font-bold text-gray-800">랭킹 참가</h3>
        <button 
          @click="$emit('close')"
          class="text-gray-400 hover:text-gray-600 text-2xl transition-colors"
        >
          ×
        </button>
      </div>

      <!-- 마스코트 이미지 및 확인 메시지 -->
      <div class="text-center mb-6">
        <div class="mascot-preview mb-4">
          <img 
            :src="imgSrc" 
            :alt="mascotName"
            class="w-32 h-32 object-cover rounded-xl mx-auto border-4 border-blue-200 shadow-lg"
            @error="handleImageError"
          />
        </div>
        
        <div class="confirmation-message">
          <h4 class="text-lg font-semibold text-gray-800 mb-2">
            {{ mascotName }}을(를) 랭킹에 등록하시겠습니까?
          </h4>
          <p class="text-sm text-gray-600">
            현재 모습으로 랭킹에 참가합니다
          </p>
        </div>
      </div>

      <!-- 등록 폼 -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <!-- 제목 입력 -->
        <div>
          <label for="title" class="block text-sm font-medium text-gray-700 mb-2">
            참가 제목 *
          </label>
          <input
            id="title"
            v-model="formData.title"
            type="text"
            maxlength="100"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
            placeholder="참가 제목을 입력하세요"
          />
          <div class="flex justify-between items-center mt-1">
            <span class="text-xs text-gray-500">
              {{ formData.title.length }}/100
            </span>
            <span v-if="formData.title.length === 0" class="text-xs text-red-500">
              제목을 입력해주세요
            </span>
          </div>
        </div>

        <!-- 설명 입력 (선택사항) -->
        <div>
          <label for="description" class="block text-sm font-medium text-gray-700 mb-2">
            참가 설명 <span class="text-gray-500">(선택사항)</span>
          </label>
          <textarea
            id="description"
            v-model="formData.description"
            rows="3"
            maxlength="500"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors resize-none"
            placeholder="참가 설명을 입력하세요 (선택사항)"
          ></textarea>
          <div class="text-right">
            <span class="text-xs text-gray-500">
              {{ formData.description.length }}/500
            </span>
          </div>
        </div>

        <!-- 액션 버튼 -->
        <div class="flex space-x-3 pt-4">
          <button
            type="button"
            @click="$emit('close')"
            class="flex-1 py-3 px-4 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300 transition-colors"
          >
            취소
          </button>
          <button
            type="submit"
            :disabled="!isFormValid || isSubmitting"
            :class="[
              'flex-1 py-3 px-4 rounded-lg font-medium transition-colors',
              isFormValid && !isSubmitting
                ? 'bg-blue-500 text-white hover:bg-blue-600'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            ]"
          >
            <span v-if="isSubmitting" class="flex items-center justify-center">
              <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              등록 중...
            </span>
            <span v-else>랭킹 참가</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { CreateEntryRequest } from '../api/ranking';

interface Props {
  mascotName: string;
  mascotImageUrl: string;
  mascotSnapshotId: number;
}

interface Emits {
  (e: 'close'): void;
  (e: 'submit', data: CreateEntryRequest): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// 폼 데이터
const formData = ref({
  title: props.mascotName, // 기본값으로 마스코트 이름 설정
  description: ''
});

// 제출 상태
const isSubmitting = ref(false);

// 폼 유효성 검사
const isFormValid = computed(() => {
  return formData.value.title.trim().length > 0 && 
         formData.value.title.trim().length <= 100;
});

// 마스코트 이름이 변경되면 제목도 업데이트
watch(() => props.mascotName, (newName) => {
  if (formData.value.title === props.mascotName || formData.value.title.trim().length === 0) {
    formData.value.title = newName;
  }
});

// 이미지 소스(안전 처리)
import { getApiOrigin } from '../api/index';
const BASE_URL = (import.meta as any).env?.BASE_URL || '/';
const FALLBACK_IMG = BASE_URL + 'mascot/pli.png';

function resolveSrc(val?: string): string {
  const v = (val || '').trim();
  if (!v) return FALLBACK_IMG;
  if (v.startsWith('/uploads/')) return (getApiOrigin() || '') + v;
  return v;
}

const imgSrc = ref<string>(resolveSrc(props.mascotImageUrl));
watch(() => props.mascotImageUrl, (val) => {
  imgSrc.value = resolveSrc(val);
});

// 이미지 로드 에러 처리
const handleImageError = () => {
  imgSrc.value = FALLBACK_IMG;
};

// 폼 제출 처리
const handleSubmit = async () => {
  if (!isFormValid.value || isSubmitting.value) return;
  
  isSubmitting.value = true;
  
  try {
    const requestData: CreateEntryRequest = {
      mascotSnapshotId: props.mascotSnapshotId,
      title: formData.value.title.trim(),
      description: formData.value.description.trim() || undefined
    };
    
    emit('submit', requestData);
  } catch (error) {
    console.error('폼 제출 실패:', error);
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.mascot-preview {
  @apply relative;
}

.confirmation-message {
  @apply px-4;
}

/* 입력 필드 포커스 효과 */
input:focus, textarea:focus {
  @apply outline-none;
}

/* 버튼 호버 효과 */
button:not(:disabled):hover {
  @apply transform scale-105;
}
</style>
