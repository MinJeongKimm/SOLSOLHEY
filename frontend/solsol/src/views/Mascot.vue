<template>
  <div class="min-h-screen bg-gradient-to-br from-purple-50 to-pink-50 p-6">
    <div class="max-w-md mx-auto">
      <!-- 헤더 -->
      <div class="text-center mb-8">
        <div class="text-6xl mb-4">🐾</div>
        <h1 class="text-3xl font-bold text-gray-800 mb-2">나만의 마스코트 만들기</h1>
        <p class="text-gray-600">특별한 마스코트를 만들어보세요!</p>
      </div>

      <!-- 마스코트 생성 폼 -->
      <div class="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 마스코트 이름 -->
          <div>
            <label for="name" class="block text-sm font-semibold text-gray-700 mb-2">
              마스코트 이름 <span class="text-red-500">*</span>
            </label>
            <input
              id="name"
              v-model="formData.name"
              type="text"
              placeholder="마스코트의 이름을 입력해주세요"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition duration-200"
              :class="{ 'border-red-300 bg-red-50': errors.name }"
              maxlength="50"
            />
            <div class="mt-1 flex justify-between">
              <span v-if="errors.name" class="text-red-500 text-sm">{{ errors.name }}</span>
              <span class="text-gray-400 text-sm ml-auto">{{ formData.name.length }}/50</span>
            </div>
          </div>

          <!-- 마스코트 타입 -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-3">
              마스코트 타입 <span class="text-red-500">*</span>
            </label>
            <div class="grid grid-cols-2 gap-3">
              <button
                v-for="type in mascotTypes"
                :key="type.value"
                type="button"
                @click="formData.mascotType = type.value"
                class="p-4 rounded-xl border-2 transition duration-200 hover:shadow-md"
                :class="formData.mascotType === type.value 
                  ? 'border-purple-500 bg-purple-50 text-purple-700' 
                  : 'border-gray-200 hover:border-gray-300'"
              >
                <div class="text-2xl mb-1">{{ type.icon }}</div>
                <div class="text-sm font-medium">{{ type.label }}</div>
              </button>
            </div>
            <span v-if="errors.mascotType" class="text-red-500 text-sm mt-1 block">{{ errors.mascotType }}</span>
          </div>

          <!-- 마스코트 진화 단계 정보 -->
          <div class="bg-blue-50 rounded-xl p-4 border border-blue-200">
            <h3 class="text-sm font-semibold text-blue-800 mb-2">🌟 진화 시스템</h3>
            <div class="text-xs text-blue-700 space-y-1">
              <div>• 1단계 (Lv.1) → 2단계 (Lv.10)</div>
              <div>• 2단계 (Lv.10) → 3단계 (Lv.30)</div>
              <div>• 3단계 (Lv.30) → 4단계 (Lv.60)</div>
              <div class="mt-2 text-blue-600">챌린지를 완료해서 경험치를 얻어보세요!</div>
            </div>
          </div>

          <!-- 에러 메시지 -->
          <div v-if="apiError" class="bg-red-50 border border-red-200 rounded-xl p-4">
            <div class="flex items-center">
              <span class="text-red-500 text-xl mr-2">⚠️</span>
              <span class="text-red-700 text-sm">{{ apiError }}</span>
            </div>
          </div>

          <!-- 성공 메시지 -->
          <div v-if="successMessage" class="bg-green-50 border border-green-200 rounded-xl p-4">
            <div class="flex items-center">
              <span class="text-green-500 text-xl mr-2">✅</span>
              <span class="text-green-700 text-sm">{{ successMessage }}</span>
            </div>
          </div>

          <!-- 생성된 마스코트 정보 표시 -->
          <div v-if="createdMascot" class="bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl p-4 border border-purple-200">
            <h3 class="text-sm font-semibold text-purple-800 mb-2">🎉 생성된 마스코트</h3>
            <div class="space-y-1 text-sm text-purple-700">
              <div><span class="font-medium">이름:</span> {{ createdMascot.name }}</div>
              <div><span class="font-medium">타입:</span> {{ getMascotTypeLabel(createdMascot.mascotType) }}</div>
              <div><span class="font-medium">레벨:</span> {{ createdMascot.level }}</div>
              <div><span class="font-medium">진화 단계:</span> {{ createdMascot.evolutionStage }}단계</div>
              <div><span class="font-medium">경험치:</span> {{ createdMascot.experiencePoint }}XP</div>
            </div>
          </div>

          <!-- 제출 버튼 -->
          <button
            type="submit"
            :disabled="isLoading || !isFormValid || !!createdMascot"
            class="w-full py-3 px-6 rounded-xl font-semibold text-white transition duration-200 transform hover:scale-105"
            :class="isLoading || !isFormValid || !!createdMascot
              ? 'bg-gray-300 cursor-not-allowed' 
              : 'bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600 shadow-lg'"
          >
            <span v-if="isLoading" class="flex items-center justify-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              생성 중...
            </span>
            <span v-else-if="createdMascot">🎉 마스코트가 생성되었습니다!</span>
            <span v-else>🌟 마스코트 만들기</span>
          </button>

          <!-- 새로 만들기 버튼 -->
          <button
            v-if="createdMascot"
            type="button"
            @click="resetForm"
            class="w-full py-2 px-4 rounded-xl font-medium text-purple-600 border border-purple-300 hover:bg-purple-50 transition duration-200"
          >
            다른 마스코트 만들기
          </button>
        </form>
      </div>

      <!-- 도움말 -->
      <div class="mt-6 text-center text-sm text-gray-500">
        <p>💡 팁: 마스코트는 챌린지를 완료할 때마다 경험치를 얻어 성장해요!</p>
        <p class="mt-1">✨ 레벨이 올라가면 새로운 모습으로 진화합니다!</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { createMascot, handleApiError } from '../api'
import type { MascotCreateRequest, MascotResponse } from '../types/api'

// 폼 데이터
const formData = ref<MascotCreateRequest>({
  name: '',
  mascotType: ''
})

// 상태 관리
const isLoading = ref(false)
const apiError = ref('')
const successMessage = ref('')
const createdMascot = ref<MascotResponse | null>(null)

// 에러 상태
const errors = ref({
  name: '',
  mascotType: ''
})

// 마스코트 타입 옵션
const mascotTypes = [
  { value: 'cat', label: '고양이', icon: '🐱' },
  { value: 'dog', label: '강아지', icon: '🐶' },
  { value: 'rabbit', label: '토끼', icon: '🐰' },
  { value: 'bear', label: '곰', icon: '🐻' },
  { value: 'fox', label: '여우', icon: '🦊' },
  { value: 'panda', label: '판다', icon: '🐼' },
  { value: 'penguin', label: '펭귄', icon: '🐧' },
  { value: 'lion', label: '사자', icon: '🦁' }
]

// 폼 유효성 검사
const isFormValid = computed(() => {
  return formData.value.name.trim().length > 0 && 
         formData.value.mascotType.length > 0 && 
         !errors.value.name && 
         !errors.value.mascotType
})

// 마스코트 타입 레이블 가져오기
const getMascotTypeLabel = (type: string) => {
  const mascotType = mascotTypes.find(t => t.value === type)
  return mascotType ? `${mascotType.icon} ${mascotType.label}` : type
}

// 유효성 검사 함수
const validateForm = () => {
  errors.value = { name: '', mascotType: '' }
  
  // 이름 검사
  if (!formData.value.name.trim()) {
    errors.value.name = '마스코트 이름은 필수입니다'
  } else if (formData.value.name.length > 50) {
    errors.value.name = '마스코트 이름은 50자 이하여야 합니다'
  }
  
  // 타입 검사
  if (!formData.value.mascotType) {
    errors.value.mascotType = '마스코트 타입을 선택해주세요'
  }
  
  return !errors.value.name && !errors.value.mascotType
}

// 폼 초기화
const resetForm = () => {
  formData.value = { name: '', mascotType: '' }
  errors.value = { name: '', mascotType: '' }
  apiError.value = ''
  successMessage.value = ''
  createdMascot.value = null
}

// 폼 제출 처리
const handleSubmit = async () => {
  // 메시지 초기화
  apiError.value = ''
  successMessage.value = ''
  
  // 유효성 검사
  if (!validateForm()) {
    return
  }
  
  isLoading.value = true
  
  try {
    const requestData: MascotCreateRequest = {
      name: formData.value.name.trim(),
      mascotType: formData.value.mascotType
    }
    
    const response = await createMascot(requestData)
    
    if (response.success && response.data) {
      successMessage.value = '🎉 마스코트가 성공적으로 생성되었습니다!'
      createdMascot.value = response.data
    } else {
      apiError.value = response.message || '마스코트 생성에 실패했습니다.'
    }
  } catch (error) {
    apiError.value = handleApiError(error)
  } finally {
    isLoading.value = false
  }
}
</script>


