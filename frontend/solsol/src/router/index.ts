import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Signup from '../views/Signup.vue'

const routes = [
  { path: '/', component: Login },
  { path: '/dashboard', component: Dashboard },
  { path: '/signup', component: Signup }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
