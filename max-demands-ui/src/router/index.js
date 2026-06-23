import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('@/views/LayoutView.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/DashboardView.vue') },
      { path: 'biz-requirement', component: () => import('@/views/biz-requirement/BizRequirementList.vue') },
      { path: 'biz-requirement-overview', component: () => import('@/views/biz-requirement/BizRequirementOverview.vue') },
      { path: 'prod-requirement', component: () => import('@/views/prod-requirement/ProdRequirementList.vue') },
      { path: 'dev-branch', component: () => import('@/views/branch/DevBranchList.vue') },
      { path: 'verify-branch', component: () => import('@/views/branch/VerifyBranchList.vue') },
      { path: 'batch', component: () => import('@/views/batch/BatchList.vue') },
      { path: 'app-system', component: () => import('@/views/app-system/AppSystemList.vue') },
      { path: 'dict', component: () => import('@/views/system/DictManage.vue') },
      { path: 'user', component: () => import('@/views/system/UserManage.vue') },
      { path: 'role', component: () => import('@/views/system/RoleManage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.path !== '/login' && !authStore.isLoggedIn()) {
    next('/login')
  } else {
    next()
  }
})

export default router
