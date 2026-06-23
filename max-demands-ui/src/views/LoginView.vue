<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-logo">
        <div class="logo-icon">
          <el-icon size="28" color="white"><Lightning /></el-icon>
        </div>
        <div class="logo-text">
          <div class="logo-title">需求管理系统</div>
        </div>
      </div>

      <h1 class="login-title">Welcome back</h1>
      <p class="login-subtitle">Sign in to your account</p>

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

      <div class="login-demo">
        Demo: admin / admin123
      </div>
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
  background: #f5f7fa;
}

.login-card {
  width: 420px;
  background: white;
  border-radius: 20px;
  padding: 48px 40px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 10px 10px -5px rgba(0, 0, 0, 0.02);
}

.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: #6366f1;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
}

.logo-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px 0;
}

.login-subtitle {
  text-align: center;
  font-size: 15px;
  color: #6b7280;
  margin: 0 0 32px 0;
}

.login-form :deep(.el-input__inner) {
  height: 44px;
}

.input-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.login-demo {
  margin-top: 24px;
  padding: 12px 16px;
  background: #eef2ff;
  border-radius: 10px;
  text-align: center;
  font-size: 13px;
  color: #4f46e5;
}
</style>
