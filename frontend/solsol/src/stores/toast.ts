import { defineStore } from 'pinia';

export interface ToastItem {
  id: number;
  message: string;
  type?: 'info' | 'success' | 'error';
  duration?: number; // ms
}

export const useToastStore = defineStore('toast', {
  state: () => ({
    items: [] as ToastItem[],
    seq: 1,
  }),
  actions: {
    show(message: string, type: ToastItem['type'] = 'info', duration = 2000) {
      const id = this.seq++;
      const item: ToastItem = { id, message, type, duration };
      this.items.push(item);
      setTimeout(() => this.dismiss(id), duration);
    },
    dismiss(id: number) {
      this.items = this.items.filter(t => t.id !== id);
    },
    clear() {
      this.items = [];
    },
  },
});

