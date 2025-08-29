<template>
  <div
    class="min-h-screen w-full flex flex-col justify-center items-center text-center"
    :style="gradientStyle"
  >
    <div class="w-full px-6 flex flex-col items-center">
      <!-- Title + Subtitle -->
      <section>
        <h1
          class="text-white tracking-tight leading-none select-none flex items-end justify-center"
          :style="titleStyle"
        >
          <span class="font-brand font-semibold">쏠쏠 </span>
          <img
            src="/icons/icon_hey.png"
            alt="Hey"
            class="inline-block ml-1"
            :style="heyStyle"
            draggable="false"
          />
        </h1>
        <p class="text-white/90 mt-3 font-medium" :class="subtitleSize">
          챌린지로 쏠쏠한 리워드 <br/> 마스코트 키우기
        </p>
      </section>

      <!-- Mascot: centered under subtitle -->
      <section class="mt-6 sm:mt-8">
        <img
          src="/mascot/shinhan_friends.png"
          alt="신한 프렌즈 마스코트"
          class="mx-auto animate-float select-none"
          :class="mascotSize"
          draggable="false"
        />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../api'

const router = useRouter()

// Background gradient style (exact brand colors)
const gradientStyle = computed(() => ({
  background: 'linear-gradient(to bottom, #6E72EF 0%, #E586E2 100%)',
}))

// Safe area for mobile (not forcing extra top gap for centering)
const safeAreaBottom = { paddingBottom: 'env(safe-area-inset-bottom)' }

// Responsive sizes: headline uses CSS clamp for smooth scaling
const titleStyle = { fontSize: 'clamp(54px, 14.4vw, 90px)', lineHeight: '1' }
// Make Hey image follow the text size (em-based)
const heyStyle = { height: '1.1em', width: 'auto' }
const subtitleSize = 'text-base sm:text-lg'
// Increase mascot size on mobile for better visibility
const mascotSize = 'w-11/12 max-w-md sm:max-w-lg'

onMounted(async () => {
  const isAuthenticated = await auth.isAuthenticatedAsync()
  if (isAuthenticated) {
    router.replace('/mascot')
    return
  }
  setTimeout(() => router.replace('/login'), 3000)
})
</script>

<style scoped>
/* Mascot.vue와 유사한 플로팅 효과 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.animate-float {
  animation: float 3s ease-in-out infinite;
}
</style>
