import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Signup from '../views/Signup.vue'
import Challenge from '../views/Challenge.vue'
import Mascot from '../views/Mascot.vue'
import MascotCreate from '../views/MascotCreate.vue'
import MascotCustomize from '../views/MascotCustomize.vue'
import FriendList from '../views/FriendList.vue'
import FriendAdd from '../views/FriendAdd.vue'
import Ranking from '../views/Ranking.vue'
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
  },
  { 
    path: '/friend', 
    component: FriendList,
    meta: { requiresAuth: true }
  },
  { 
    path: '/friend/add', 
    component: FriendAdd,
    meta: { requiresAuth: true }
  },
  { 
    path: '/ranking', 
    component: Ranking,
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
    // 로그인한 사용자는 마스코트 메인 페이지로 리디렉트
    // 마스코트가 없으면 컴포넌트에서 자동으로 생성 페이지로 이동
    next('/mascot')
    return
  }
  
  // 인증된 사용자의 마스코트 관련 페이지 접근 제어는 컴포넌트 레벨에서 처리
  // (백엔드 API 호출로 실시간 확인)
  
  next()
})

export default router
