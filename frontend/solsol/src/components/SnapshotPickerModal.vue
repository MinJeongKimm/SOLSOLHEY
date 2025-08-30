<template>
  <div v-if="visible" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-2xl shadow-xl max-w-3xl w-full p-4 sm:p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-bold text-gray-800">과거 스냅샷 선택</h3>
        <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700">×</button>
      </div>

      <div v-if="!snapshots || snapshots.length === 0" class="text-center text-gray-500 py-8">
        저장된 스냅샷이 없어요.
      </div>

      <div v-else class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-3">
        <button
          v-for="s in snapshots"
          :key="s.id"
          class="border rounded-lg overflow-hidden hover:shadow focus:outline-none focus:ring-2 focus:ring-purple-400"
          @click="select(s)"
          title="스냅샷 선택"
        >
          <img :src="resolveSrc(s.imageUrl)" class="w-full h-24 object-cover" :alt="'snapshot ' + s.id" />
          <div class="text-[11px] text-gray-500 p-1 text-center truncate">
            {{ formatDate(s.createdAt) }}
          </div>
        </button>
      </div>

      <div class="mt-4 text-right">
        <button @click="$emit('close')" class="px-3 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg text-sm text-gray-700">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getApiOrigin } from '../api/index';

interface Snapshot { id: number; imageUrl: string; createdAt: string | any }

const props = defineProps<{ visible: boolean; snapshots: Snapshot[] }>();
const emit = defineEmits<{ (e: 'close'): void; (e: 'select', s: Snapshot): void }>();

function resolveSrc(url?: string): string {
  const v = (url || '').trim();
  if (!v) return '';
  if (v.startsWith('/uploads/')) return (getApiOrigin() || '') + v;
  return v;
}

function formatDate(d: any): string {
  try {
    const date = new Date(Array.isArray(d) ? d.join('-') : d);
    if (isNaN(date.getTime())) return '';
    return date.toLocaleString('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
  } catch { return ''; }
}

function select(s: Snapshot) {
  emit('select', s);
}
</script>

<style scoped>
</style>

