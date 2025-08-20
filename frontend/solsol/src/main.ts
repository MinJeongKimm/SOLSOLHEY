import { createApp } from 'vue'
import '@/assets/main.css'
import App from './App.vue'
import router from './router/index'
import './style.css'

createApp(App).use(router).mount('#app')
