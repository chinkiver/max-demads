<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">角色权限管理</h1>
      <p class="page-header-desc">Define roles and assign permissions</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: flex-end; align-items: center;">
        </div>
      </template>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleConfigPermission(row)" v-if="authStore.userInfo?.permissions?.includes('sys:role:edit')">配置权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 配置权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="配置权限" width="600px">
      <el-form label-width="120px">
        <el-form-item v-for="(perms, module) in permissionGroups" :key="module" :label="module">
          <el-checkbox-group v-model="selectedPermissions">
            <el-checkbox v-for="perm in perms" :key="perm.id" :label="perm.id">
              {{ perm.permName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions" :loading="permSubmitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'

const authStore = useAuthStore()

const list = ref([])
const loading = ref(false)
const permDialogVisible = ref(false)
const permSubmitting = ref(false)
const currentRoleId = ref(null)
const selectedPermissions = ref([])
const permissionGroups = ref({})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/role')
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const fetchPermissions = async () => {
  // 假设后端有 /permission 接口返回所有权限，按模块分组
  // 这里用模拟数据，实际项目中替换为真实接口
  try {
    const res = await request.get('/permission')
    const groups = {}
    ;(res.data || []).forEach(p => {
      const module = p.module || '其他'
      if (!groups[module]) groups[module] = []
      groups[module].push(p)
    })
    permissionGroups.value = groups
  } catch (e) {
    // 如果后端没有 /permission 接口，使用空分组
    permissionGroups.value = {}
  }
}

const handleConfigPermission = async (row) => {
  currentRoleId.value = row.id
  const res = await request.get(`/role/${row.id}/permissions`)
  selectedPermissions.value = res.data || []
  permDialogVisible.value = true
}

const handleSavePermissions = async () => {
  permSubmitting.value = true
  try {
    await request.put(`/role/${currentRoleId.value}/permissions`, { permissionIds: selectedPermissions.value })
    ElMessage.success('权限配置成功')
    permDialogVisible.value = false
  } finally {
    permSubmitting.value = false
  }
}

onMounted(() => {
  fetchList()
  fetchPermissions()
})
</script>
