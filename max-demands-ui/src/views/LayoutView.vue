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
          :default-openeds="[]"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>

          <el-sub-menu index="biz-requirement-group" v-if="hasPermi('biz:requirement:menu') || hasPermi('biz:requirement:overview:menu') || hasPermi('biz:requirement:completed:menu')">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>业务需求</span>
            </template>
            <el-menu-item index="/biz-requirement" v-if="hasPermi('biz:requirement:menu')">
              <span>需求管理</span>
            </el-menu-item>
            <el-menu-item index="/biz-requirement-overview" v-if="hasPermi('biz:requirement:overview:menu')">
              <span>需求全览</span>
            </el-menu-item>
            <el-menu-item index="/biz-requirement-completed" v-if="hasPermi('biz:requirement:completed:menu')">
              <span>已投产需求</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="prod-requirement-group" v-if="hasPermi('prod:requirement:menu')">
            <template #title>
              <el-icon><DocumentCopy /></el-icon>
              <span>产品需求</span>
            </template>
            <el-menu-item index="/prod-requirement" v-if="hasPermi('prod:requirement:menu')">
              <span>产品需求</span>
            </el-menu-item>
          </el-sub-menu>

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
          :default-openeds="[]"
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
    '/biz-requirement': '需求管理',
    '/biz-requirement-overview': '需求全览',
    '/biz-requirement-completed': '已投产需求',
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
  background: var(--main-bg);
}

.sidebar {
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  border-right: 2px solid #000;
  box-shadow: 4px 0 0 #000;
  z-index: 10;
}

.sidebar-header {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 2px solid #000;
  background: var(--sidebar-bg);
}

.logo-icon {
  width: 38px;
  height: 38px;
  background: #1a1a1a;
  border: 2px solid #000;
  box-shadow: 3px 3px 0 #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: #1a1a1a;
}

.sidebar-menu-wrapper {
  flex: 1;
  padding: 16px 14px;
  overflow-y: auto;
}

.menu-section-title {
  font-size: 11px;
  font-weight: 800;
  color: rgba(0,0,0,0.6);
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 0 12px;
  margin-bottom: 10px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-sub-menu__title) {
  color: var(--text-dark);
  font-weight: 700;
  border-radius: 0;
  height: 44px;
  line-height: 44px;
  margin-bottom: 4px;
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(0,0,0,0.08);
}

:deep(.el-sub-menu.is-active .el-sub-menu__title) {
  background: rgba(0,0,0,0.1);
  font-weight: 800;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 42px;
  line-height: 42px;
  border-radius: 0;
  margin-bottom: 4px;
  color: var(--text-dark);
  font-weight: 600;
  border: 2px solid transparent;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(0,0,0,0.08);
  border-color: var(--card-border);
  box-shadow: 3px 3px 0 var(--card-border);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--accent-yellow);
  color: var(--text-dark);
  border: 2px solid var(--card-border);
  box-shadow: 3px 3px 0 var(--card-border);
  font-weight: 800;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: var(--text-dark);
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--text-dark);
}

.main-container {
  background: var(--main-bg);
}

.main-header {
  height: 72px;
  background: var(--main-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  border-bottom: 2px solid #000;
  box-shadow: 0 4px 0 #000;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menu-toggle {
  color: #e5e5e5;
  cursor: pointer;
}

.page-title {
  font-size: 18px;
  font-weight: 800;
  color: white;
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
  border-radius: 0;
  background: white;
  box-shadow: 0 0 0 2px #000 inset !important;
}

.header-icon {
  color: #e5e5e5;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: white;
}

.user-avatar {
  background: var(--accent-pink);
  color: white;
  font-weight: 700;
  font-size: 13px;
  border: 2px solid #000;
  box-shadow: 3px 3px 0 #000;
}

.user-name {
  font-size: 14px;
  font-weight: 700;
  color: #e5e5e5;
}

.main-content {
  padding: 28px;
  overflow-y: auto;
}
</style>
