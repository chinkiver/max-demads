<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">
        <div class="logo-icon">
          <el-icon size="36" color="white"><Lightning /></el-icon>
        </div>
      </div>

      <h1 class="login-title">Max 需求管理系统</h1>

      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <div class="input-label">用户名</div>
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-label">密码</div>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%; margin-top: 8px;"
            :loading="loading"
            @click="handleLogin"
          >
            登录 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>

    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'
import { User, Lock, ArrowRight, Lightning } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await request.post('/auth/login', form)
    authStore.setToken(res.data.token)
    authStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--layout-bg);
}

.login-card {
  width: 420px;
  background: var(--card-bg);
  border-radius: 0;
  padding: 48px 40px;
  border: 3px solid #000;
  box-shadow: 8px 8px 0 #000;
}

.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: var(--accent-yellow);
  border: 3px solid #000;
  box-shadow: 5px 5px 0 #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-title {
  text-align: center;
  font-size: 26px;
  font-weight: 800;
  color: var(--text-dark);
  margin: 0 0 32px 0;
  text-shadow: 2px 2px 0 rgba(0,0,0,0.15);
}

.login-form :deep(.el-input__inner) {
  height: 44px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 2px #000 inset !important;
  border-radius: 0 !important;
  background: white;
}

:deep(.el-button--primary) {
  background: var(--accent-pink);
  border-color: #000;
  box-shadow: 4px 4px 0 #000;
  color: #1a1a1a;
  font-weight: 700;
}

:deep(.el-button--primary:hover) {
  background: #ff4a86;
  border-color: #000;
}

:deep(.el-button--primary:active) {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 #000;
}

.input-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 6px;
}
</style>
