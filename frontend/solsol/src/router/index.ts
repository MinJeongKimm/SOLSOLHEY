import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Signup from '../views/Signup.vue'
import Challenge from '../views/Challenge.vue'
import Mascot from '../views/Mascot.vue'
import MascotCreate from '../views/MascotCreate.vue'
import MascotCustomize from '../views/MascotCustomize.vue'
import { auth, mascot } from '../api/index'

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
  const hasMascot = mascot.hasMascot()
  
  // 인증이 필요한 페이지
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }
  
  // 게스트만 접근 가능한 페이지 (로그인, 회원가입)
  if (to.meta.requiresGuest && isAuthenticated) {
    // 로그인한 사용자의 경우 마스코트 생성 여부에 따라 리디렉트
    if (!hasMascot) {
      next('/mascot/create')
    } else {
      next('/dashboard')
    }
    return
  }
  
  // 인증된 사용자가 마스코트 관련 페이지에 접근할 때
  if (isAuthenticated && to.meta.requiresAuth) {
    // 마스코트가 없는데 마스코트 메인 페이지나 꾸미기 페이지에 접근하려는 경우
    if (!hasMascot && (to.path === '/mascot' || to.path === '/mascot/customize')) {
      next('/mascot/create')
      return
    }
    
    // 마스코트가 있는데 생성 페이지에 접근하려는 경우
    if (hasMascot && to.path === '/mascot/create') {
      next('/mascot')
      return
    }
  }
  
  next()
})

export default router
