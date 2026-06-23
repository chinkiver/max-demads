<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>数据字典管理</span>
          <el-button type="primary" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('sys:dict:add')">新增</el-button>
        </div>
      </template>

      <el-tabs v-model="activeType" @tab-change="onTabChange">
        <el-tab-pane
          v-for="group in dictGroups"
          :key="group.type"
          :label="typeNameMap[group.type] || group.type"
          :name="group.type"
        >
          <div style="margin-bottom: 10px; color: #909399; font-size: 13px;">
            字段名：{{ group.type }}
          </div>
          <el-table :data="group.items" border>
            <el-table-column prop="dictCode" label="字典编码" />
            <el-table-column prop="dictName" label="字典名称" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)" v-if="authStore.userInfo?.permissions?.includes('sys:dict:edit')">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)" v-if="authStore.userInfo?.permissions?.includes('sys:dict:delete')">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑字典' : '新增字典'" width="500px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="form.dictType" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="form.dictCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="form.dictName" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import request from '@/api/request'

const authStore = useAuthStore()
const dictStore = useDictStore()

// 字典类型中文映射
const typeNameMap = {
  prod_req_status: '产品需求状态',
  req_category: '业务需求分类',
  branch_status: '分支状态',
  batch_status: '批次状态',
  batch_type: '批次类型',
  priority: '优先级',
  biz_req_status: '业务需求状态'
}

// 字典类型展示顺序
const typeOrder = [
  'req_category',
  'biz_req_status',
  'priority',
  'prod_req_status',
  'branch_status',
  'batch_type',
  'batch_status'
]

const activeType = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = ref({
  dictType: '',
  dictCode: '',
  dictName: '',
  sort: 0
})

const rules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

const dictGroups = computed(() => {
  const dicts = dictStore.dicts || {}
  const groups = Object.keys(dicts).map(type => ({ type, items: dicts[type] }))
  groups.sort((a, b) => {
    const indexA = typeOrder.indexOf(a.type)
    const indexB = typeOrder.indexOf(b.type)
    if (indexA !== -1 && indexB !== -1) return indexA - indexB
    if (indexA !== -1) return -1
    if (indexB !== -1) return 1
    return a.type.localeCompare(b.type)
  })
  return groups
})

const onTabChange = (name) => {
  activeType.value = name
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    dictType: activeType.value || '',
    dictCode: '',
    dictName: '',
    sort: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = {
    dictType: row.dictType,
    dictCode: row.dictCode,
    dictName: row.dictName,
    sort: row.sort
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/dict/${form.value.dictType}/${form.value.dictCode}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/dict', form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    dictStore.loadDicts()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该字典项？', '提示', { type: 'warning' })
    await request.delete(`/dict/${row.dictType}/${row.dictCode}`)
    ElMessage.success('删除成功')
    dictStore.loadDicts()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(async () => {
  await dictStore.loadDicts()
  const groups = dictGroups.value
  if (groups.length > 0) {
    activeType.value = groups[0].type
  }
})
</script>
