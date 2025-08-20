import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Signup from '../views/Signup.vue'
import Challenge from '../views/Challenge.vue'
import Mascot from '../views/Mascot.vue'
import MascotCreate from '../views/MascotCreate.vue'
import MascotCustomize from '../views/MascotCustomize.vue'
import { auth } from '../api/index'

const routes = [
  { 
    path: '/', 
    component: Login,
    meta: { requiresGuest: true }
  },
  { 
    path: '/login', 
    component: Login,
    meta: { requiresGuest: true }
  },
  { 
    path: '/signup', 
    component: Signup,
    meta: { requiresGuest: true }
  },
  { 
    path: '/dashboard', 
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  { 
    path: '/challenge', 
    component: Challenge,
    meta: { requiresAuth: true }
  },
  { 
    path: '/mascot', 
    component: Mascot,
    meta: { requiresAuth: true }
  },
  { 
    path: '/mascot/create', 
    component: MascotCreate,
    meta: { requiresAuth: true }
  },
  { 
    path: '/mascot/customize', 
    component: MascotCustomize,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 인증 가드
router.beforeEach((to, from, next) => {
  const isAuthenticated = auth.isAuthenticated()
  
  // 인증이 필요한 페이지
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }
  
  // 게스트만 접근 가능한 페이지 (로그인, 회원가입)
  if (to.meta.requiresGuest && isAuthenticated) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
