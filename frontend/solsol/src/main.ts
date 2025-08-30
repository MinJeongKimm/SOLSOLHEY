import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/assets/main.css'
import App from './App.vue'
import router from './router/index'
import './style.css'
import './styles/bubble-font.css'
import { bootstrapAuth } from './api/index'

// Pinia 스토어 생성
const pinia = createPinia()

// 앱 생성 및 플러그인 등록
const app = createApp(App)
app.use(router)
app.use(pinia)

// 앱 마운트 전에 인증 상태 초기화 (성공/실패 모두 마운트)
bootstrapAuth().finally(() => {
  app.mount('#app')
})
