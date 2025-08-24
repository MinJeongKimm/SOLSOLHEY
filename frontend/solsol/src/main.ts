import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@/assets/main.css'
import App from './App.vue'
import router from './router/index'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.mount('#app')
