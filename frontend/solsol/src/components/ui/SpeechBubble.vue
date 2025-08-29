<template>
  <div
    class="relative inline-block bg-white text-gray-800 rounded-2xl shadow-lg px-4 py-3 border border-gray-200 horizontal-text font-bubble"
    role="status"
    :aria-live="ariaLive"
  >
    <slot>{{ text }}</slot>
  </div>
  
</template>

<script setup lang="ts">
interface Props {
  text?: string;
  tail?: 'top' | 'left' | 'right';
  ariaLive?: 'polite' | 'assertive' | 'off';
}
const props = withDefaults(defineProps<Props>(), {
  text: '',
  tail: 'top',
  ariaLive: 'polite'
});
</script>

<style scoped>
.drop-shadow {
  filter: drop-shadow(0 1px 1px rgba(0,0,0,0.1));
}
/* 가로쓰기 강제 및 자연스러운 줄바꿈 */
.horizontal-text {
  writing-mode: horizontal-tb !important;
  text-orientation: mixed;
  white-space: pre-wrap; /* \n을 줄바꿈으로 반영 */
  overflow-wrap: break-word; /* Safari/구형 브라우저 호환 줄바꿈 */
  word-break: break-word; /* 단어 중간 분리 허용(필요 시) */
  line-break: anywhere;
  line-height: 1.35;
}
/* 말풍선 전용 폰트 */
.font-bubble {
  font-family: 'OngleipParkDahyeon', 'Apple SD Gothic Neo', 'Malgun Gothic', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
}
</style>
