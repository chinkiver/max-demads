<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">
          <el-icon size="22" color="white"><Lightning /></el-icon>
        </div>
        <span class="logo-text">需求管理系统</span>
      </div>

      <div class="sidebar-menu-wrapper">
        <div class="menu-section-title">业务菜单</div>
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>

          <el-menu-item index="/biz-requirement" v-if="hasPermi('biz:requirement:menu')">
            <el-icon><Document /></el-icon>
            <span>业务需求</span>
          </el-menu-item>

          <el-menu-item index="/prod-requirement" v-if="hasPermi('prod:requirement:menu')">
            <el-icon><DocumentCopy /></el-icon>
            <span>产品需求</span>
          </el-menu-item>

          <el-menu-item index="/dev-branch" v-if="hasPermi('dev_branch:menu')">
            <el-icon><Share /></el-icon>
            <span>开发分支</span>
          </el-menu-item>

          <el-menu-item index="/verify-branch" v-if="hasPermi('verify_branch:menu')">
            <el-icon><Check /></el-icon>
            <span>验证分支</span>
          </el-menu-item>

          <el-menu-item index="/batch" v-if="hasPermi('batch:menu')">
            <el-icon><Calendar /></el-icon>
            <span>投产批次</span>
          </el-menu-item>

          <el-menu-item index="/app-system" v-if="hasPermi('app:system:menu')">
            <el-icon><Monitor /></el-icon>
            <span>应用系统</span>
          </el-menu-item>
        </el-menu>

        <div class="menu-section-title" style="margin-top: 16px;">系统管理</div>
        <el-menu
          :default-active="$route.path"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dict" v-if="hasPermi('sys:dict:menu')">
            <el-icon><Collection /></el-icon>
            <span>数据字典</span>
          </el-menu-item>
          <el-menu-item index="/user" v-if="hasPermi('sys:user:menu')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/role" v-if="hasPermi('sys:role:menu')">
            <el-icon><UserFilled /></el-icon>
            <span>角色权限</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="sidebar-footer">
        <div class="plan-card">
          <div class="plan-header">
            <span class="plan-name">Pro Plan</span>
            <span class="plan-percent">74%</span>
          </div>
          <el-progress :percentage="74" :show-text="false" color="#6366f1" />
          <div class="plan-desc">Storage used</div>
        </div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="main-header">
        <div class="header-left">
          <el-icon class="menu-toggle" size="20"><Fold /></el-icon>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-input
            placeholder="Search anything..."
            :prefix-icon="Search"
            class="header-search"
          />
          <el-icon size="20" class="header-icon"><Moon /></el-icon>
          <el-icon size="20" class="header-icon"><Bell /></el-icon>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ userInitials }}
              </el-avatar>
              <span class="user-name">{{ authStore.userInfo?.realName || authStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import request from '@/api/request'
import {
  DataLine, Document, DocumentCopy, Share, Check, Calendar,
  Monitor, Collection, User, UserFilled, Fold, Search, Moon,
  Bell, ArrowDown, Lightning
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const dictStore = useDictStore()

const sidebarWidth = ref('260px')
const permissions = computed(() => authStore.userInfo?.permissions || [])

const hasPermi = (perm) => {
  return permissions.value.includes(perm)
}

const pageTitle = computed(() => {
  const titles = {
    '/dashboard': 'Analytics Dashboard',
    '/biz-requirement': '业务需求管理',
    '/prod-requirement': '产品需求管理',
    '/dev-branch': '开发分支管理',
    '/verify-branch': '验证分支管理',
    '/batch': '投产批次管理',
    '/app-system': '应用系统管理',
    '/dict': '数据字典管理',
    '/user': '用户管理',
    '/role': '角色权限管理'
  }
  return titles[route.path] || ''
})

const userInitials = computed(() => {
  const name = authStore.userInfo?.realName || authStore.userInfo?.username || ''
  return name.slice(0, 2).toUpperCase()
})

const fetchUserInfo = async () => {
  try {
    const res = await request.get('/auth/info')
    if (res.data) {
      authStore.setUserInfo({ ...authStore.userInfo, ...res.data })
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

onMounted(() => {
  fetchUserInfo()
  dictStore.loadDicts()
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    authStore.clearToken()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  background: white;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.sidebar-header {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid #f3f4f6;
}

.logo-icon {
  width: 38px;
  height: 38px;
  background: #6366f1;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.sidebar-menu-wrapper {
  flex: 1;
  padding: 16px 14px;
  overflow-y: auto;
}

.menu-section-title {
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  padding: 0 12px;
  margin-bottom: 8px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  border-radius: 10px;
  margin-bottom: 4px;
  color: #4b5563;
  font-weight: 500;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #f3f4f6;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #eef2ff;
  color: #6366f1;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: #9ca3af;
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #6366f1;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f3f4f6;
}

.plan-card {
  background: #eef2ff;
  border-radius: 12px;
  padding: 14px;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.plan-name {
  font-size: 14px;
  font-weight: 600;
  color: #4f46e5;
}

.plan-percent {
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
}

.plan-desc {
  font-size: 12px;
  color: #6b7280;
  margin-top: 8px;
}

.main-container {
  background: #f5f7fa;
}

.main-header {
  height: 72px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menu-toggle {
  color: #6b7280;
  cursor: pointer;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-search {
  width: 240px;
}

.header-search :deep(.el-input__wrapper) {
  border-radius: 20px;
  background: #f9fafb;
}

.header-icon {
  color: #6b7280;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-avatar {
  background: #a855f7;
  color: white;
  font-weight: 600;
  font-size: 13px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.main-content {
  padding: 28px;
  overflow-y: auto;
}
</style>
