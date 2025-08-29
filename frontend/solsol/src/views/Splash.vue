<template>
  <div
    class="min-h-screen w-full flex flex-col justify-center items-center text-center"
    :style="gradientStyle"
  >
    <div class="w-full px-6 flex flex-col items-center">
      <!-- Title + Subtitle -->
      <section>
        <h1 class="text-white tracking-tight leading-none select-none">
          <span class="align-middle font-brand font-semibold" :class="titleSize">쏠쏠</span>
          <img
            src="/icons/icon_hey.png"
            alt="Hey"
            class="inline-block align-middle ml-2"
            :class="heySize"
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

// Responsive sizes (tweak here to adjust scale)
const titleSize = 'text-5xl sm:text-6xl'
const heySize = 'h-12 sm:h-48 w-auto'
const subtitleSize = 'text-base sm:text-lg'
const mascotSize = 'w-8/12 max-w-sm sm:max-w-md'

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
