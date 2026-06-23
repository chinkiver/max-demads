<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">应用系统管理</h1>
      <p class="page-header-desc">管理应用系统与归属部门</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: flex-end; align-items: center;">
          <el-button type="primary" class="page-action-btn" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('app:system:add')">+ 新增系统</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="systemName" label="系统名称" />
        <el-table-column prop="businessDept" label="归属业务部门" />
        <el-table-column prop="owner" label="负责人" />
        <el-table-column prop="description" label="系统描述" show-overflow-tooltip />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="authStore.userInfo?.permissions?.includes('app:system:edit')">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="authStore.userInfo?.permissions?.includes('app:system:delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑系统' : '新增系统'" width="500px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="120px">
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="form.systemName" />
        </el-form-item>
        <el-form-item label="归属业务部门" prop="businessDept">
          <el-input v-model="form.businessDept" />
        </el-form-item>
        <el-form-item label="负责人" prop="owner">
          <el-input v-model="form.owner" />
        </el-form-item>
        <el-form-item label="系统描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
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
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentId = ref(null)

const form = ref({
  systemName: '',
  businessDept: '',
  owner: '',
  description: ''
})

const rules = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  businessDept: [{ required: true, message: '请输入归属业务部门', trigger: 'blur' }],
  owner: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/app-system')
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  form.value = { systemName: '', businessDept: '', owner: '', description: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = { systemName: row.systemName, businessDept: row.businessDept, owner: row.owner, description: row.description }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/app-system/${currentId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/app-system', form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该系统？', '提示', { type: 'warning' })
    await request.delete(`/app-system/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(fetchList)
</script>
