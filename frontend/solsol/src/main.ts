import { createApp } from 'vue'
import '@/assets/main.css'
import App from './App.vue'
import router from './router/index'
import './style.css'
import { bootstrapAuth } from './api/index'

// 앱 마운트 전에 인증 상태 초기화 (성공/실패 모두 마운트)
const mount = () => createApp(App).use(router).mount('#app');
bootstrapAuth().finally(mount);
