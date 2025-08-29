<template>
  <div
    class="relative inline-block max-w-xs sm:max-w-sm md:max-w-md bg-white text-gray-800 rounded-2xl shadow-lg px-4 py-3 border border-gray-200"
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
</style>

