<template>
  <div class="relative">
    <!-- 드롭다운 버튼 -->
    <button
      @click="toggleDropdown"
      @blur="handleBlur"
      @keydown="handleKeydown"
      :aria-expanded="isOpen"
      :aria-haspopup="true"
      class="w-full flex items-center justify-between px-4 py-2 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
      :class="buttonClass"
    >
      <span class="text-sm font-medium text-gray-700">{{ selectedLabel }}</span>
      <svg 
        class="w-4 h-4 text-gray-400 transition-transform duration-200"
        :class="{ 'rotate-180': isOpen }"
        fill="none" 
        stroke="currentColor" 
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>

    <!-- 드롭다운 메뉴 -->
    <div
      v-show="isOpen"
      class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-auto"
    >
      <div class="py-1">
        <button
          v-for="option in options"
          :key="option.value"
          @click="selectOption(option)"
          @mousedown.prevent
          class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 focus:bg-gray-100 focus:outline-none transition-colors"
          :class="{ 'bg-blue-50 text-blue-700': option.value === modelValue }"
        >
          {{ option.label }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

interface DropdownOption {
  value: string | number;
  label: string;
}

interface Props {
  modelValue: string | number;
  options: DropdownOption[];
  placeholder?: string;
  buttonClass?: string;
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '선택하세요',
  buttonClass: ''
});

const emit = defineEmits<{
  'update:modelValue': [value: string | number];
}>();

const isOpen = ref(false);

// 현재 선택된 옵션의 라벨
const selectedLabel = computed(() => {
  const selectedOption = props.options.find(option => option.value === props.modelValue);
  return selectedOption ? selectedOption.label : props.placeholder;
});

// 드롭다운 토글
function toggleDropdown() {
  isOpen.value = !isOpen.value;
}

// 옵션 선택
function selectOption(option: DropdownOption) {
  emit('update:modelValue', option.value);
  isOpen.value = false;
}

// 포커스 아웃 처리
function handleBlur() {
  // 약간의 지연을 두어 클릭 이벤트가 처리될 수 있도록 함
  setTimeout(() => {
    isOpen.value = false;
  }, 150);
}

// 키보드 네비게이션
function handleKeydown(event: KeyboardEvent) {
  switch (event.key) {
    case 'Enter':
    case ' ':
      event.preventDefault();
      toggleDropdown();
      break;
    case 'Escape':
      isOpen.value = false;
      break;
    case 'ArrowDown':
      event.preventDefault();
      if (!isOpen.value) {
        isOpen.value = true;
      }
      break;
    case 'ArrowUp':
      event.preventDefault();
      if (!isOpen.value) {
        isOpen.value = true;
      }
      break;
  }
}

// 외부 클릭 감지
function handleClickOutside(event: Event) {
  const target = event.target as Element;
  if (!target.closest('.relative')) {
    isOpen.value = false;
  }
}

// 컴포넌트 마운트 시 이벤트 리스너 추가
import { onMounted, onUnmounted } from 'vue';

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/* 추가 스타일이 필요한 경우 여기에 작성 */
</style>
