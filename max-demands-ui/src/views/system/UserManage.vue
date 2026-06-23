<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('sys:user:add')">新增</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" size="small" style="margin-right: 5px; margin-bottom: 3px;">
              {{ role.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="warning" link @click="handleResetPwd(row)" v-if="authStore.userInfo?.permissions?.includes('sys:user:edit')">重置密码</el-button>
            <el-button type="primary" link @click="handleToggleStatus(row)" v-if="authStore.userInfo?.permissions?.includes('sys:user:edit') && row.username !== 'admin'">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button type="primary" link @click="handleEditRoles(row)" v-if="authStore.userInfo?.permissions?.includes('sys:role:edit') && row.username !== 'admin'">分配角色</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增用户" width="500px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox v-for="role in roleList" :key="role.id" :label="role.id">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in roleList" :key="role.id" :label="role.id">
          {{ role.roleName }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoles" :loading="roleSubmitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'

const authStore = useAuthStore()

const list = ref([])
const roleList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = ref({
  username: '',
  password: '',
  realName: '',
  status: 1,
  roleIds: []
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const page = ref({ current: 1, size: 10, total: 0 })

const roleDialogVisible = ref(false)
const roleSubmitting = ref(false)
const selectedRoleIds = ref([])
const currentUserId = ref(null)

const fetchList = async () => {
  loading.value = true
  try {
    const [userRes, userRoleRes] = await Promise.all([
      request.get('/user', { params: page.value }),
      request.get('/user/roles')
    ])
    const users = userRes.data?.records || []
    const userRoles = userRoleRes.data || []

    list.value = users.map(user => {
      const roleIds = userRoles
        .filter(ur => ur.userId === user.id)
        .map(ur => ur.roleId)
      const roles = roleList.value.filter(role => roleIds.includes(role.id))
      return { ...user, roles }
    })
    page.value.total = userRes.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  const res = await request.get('/role')
  roleList.value = res.data || []
}

const handleAdd = () => {
  form.value = { username: '', password: '', realName: '', status: 1, roleIds: [] }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await request.post('/user', form.value)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确认重置用户 [${row.username}] 的密码？`, '提示', { type: 'warning' })
    await request.put(`/user/${row.id}/reset-password`)
    ElMessage.success('密码已重置')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${actionText}用户 [${row.username}]？`, '提示', { type: 'warning' })
    await request.put(`/user/${row.id}/status?status=${newStatus}`)
    ElMessage.success(`${actionText}成功`)
    fetchList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleEditRoles = async (row) => {
  currentUserId.value = row.id
  const res = await request.get(`/user/${row.id}/roles`)
  selectedRoleIds.value = res.data || []
  roleDialogVisible.value = true
}

const handleSaveRoles = async () => {
  roleSubmitting.value = true
  try {
    await request.put(`/user/${currentUserId.value}/roles`, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchList()
  } finally {
    roleSubmitting.value = false
  }
}

onMounted(() => {
  fetchList()
  fetchRoles()
})
</script>
