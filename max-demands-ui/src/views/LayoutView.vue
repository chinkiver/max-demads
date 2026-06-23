<template>
  <el-container style="height: 100vh;">
    <el-aside width="200px" style="background: #304156;">
      <div style="height: 60px; line-height: 60px; text-align: center; color: white; font-size: 18px; font-weight: bold;">
        需求管理系统
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        style="border-right: none;"
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

        <el-sub-menu index="/system" v-if="showSystemMenu">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/dict" v-if="hasPermi('sys:dict:menu')">数据字典</el-menu-item>
          <el-menu-item index="/user" v-if="hasPermi('sys:user:menu')">用户管理</el-menu-item>
          <el-menu-item index="/role" v-if="hasPermi('sys:role:menu')">角色权限</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; justify-content: space-between; align-items: center; background: white; box-shadow: 0 1px 4px rgba(0,0,0,0.1);">
        <span></span>
        <el-dropdown @command="handleCommand">
          <span style="cursor: pointer;">
            {{ authStore.userInfo?.realName || authStore.userInfo?.username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background: #f0f2f5; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const dictStore = useDictStore()

const permissions = computed(() => authStore.userInfo?.permissions || [])

const hasPermi = (perm) => {
  return permissions.value.includes(perm)
}

const showSystemMenu = computed(() => {
  return hasPermi('sys:dict:menu') || hasPermi('sys:user:menu') || hasPermi('sys:role:menu')
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
