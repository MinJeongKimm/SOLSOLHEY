<template>
  <div
    class="relative inline-block bg-white text-gray-800 rounded-2xl shadow-lg px-4 py-3 border border-gray-200 horizontal-text font-bubble"
    role="status"
    :aria-live="ariaLive"
  >
    <slot>{{ text }}</slot>
    <!-- tail -->
    <span
      v-if="tail === 'left'"
      class="absolute -left-2 top-5 w-0 h-0 border-t-8 border-b-8 border-r-8 border-t-transparent border-b-transparent border-r-white drop-shadow"
      aria-hidden="true"
    />
    <span
      v-else-if="tail === 'right'"
      class="absolute -right-2 top-5 w-0 h-0 border-t-8 border-b-8 border-l-8 border-t-transparent border-b-transparent border-l-white drop-shadow"
      aria-hidden="true"
    />
  </div>
  
</template>

<script setup lang="ts">
interface Props {
  text?: string;
  tail?: 'left' | 'right';
  ariaLive?: 'polite' | 'assertive' | 'off';
}
const props = withDefaults(defineProps<Props>(), {
  text: '',
  tail: 'left',
  ariaLive: 'polite'
});
</script>

<style scoped>
.drop-shadow {
  filter: drop-shadow(0 1px 1px rgba(0,0,0,0.1));
}
/* 가로쓰기 강제 및 자연스러운 줄바꿈 */
.horizontal-text {
  writing-mode: horizontal-tb;
  text-orientation: mixed;
  white-space: pre-wrap; /* \n을 줄바꿈으로 반영 */
  overflow-wrap: normal; /* 단어 단위 줄바꿈 우선 */
  word-break: keep-all; /* 공백 기준 줄바꿈, 단어 중간 분리 지양 */
}
/* 말풍선 전용 폰트 */
.font-bubble {
  font-family: 'OngleipParkDahyeon', 'Apple SD Gothic Neo', 'Malgun Gothic', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
}
</style>
