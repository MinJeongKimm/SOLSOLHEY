<template>
  <div class="fixed left-1/2 -translate-x-1/2 bottom-4 z-50 space-y-2 w-[90%] max-w-sm">
    <div
      v-for="t in toasts"
      :key="t.id"
      class="flex items-center px-4 py-2 rounded-lg shadow text-sm"
      :class="toastClass(t.type)"
      role="status"
    >
      <span class="truncate">{{ t.message }}</span>
      <button class="ml-3 text-xs opacity-70 hover:opacity-100" @click="dismiss(t.id)">닫기</button>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { useToastStore } from '../stores/toast';

const toast = useToastStore();
const { items: toasts } = storeToRefs(toast);

function dismiss(id: number) {
  toast.dismiss(id);
}

function toastClass(type?: 'info'|'success'|'error') {
  switch (type) {
    case 'success':
      return 'bg-green-600 text-white';
    case 'error':
      return 'bg-red-600 text-white';
    default:
      return 'bg-gray-900 text-white';
  }
}
</script>

