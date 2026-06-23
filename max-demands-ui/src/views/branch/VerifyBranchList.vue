<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">验证分支管理</h1>
      <p class="page-header-desc">管理关联投产批次的验证分支</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: flex-end; align-items: center;">
          <el-button type="primary" class="page-action-btn" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('verify_branch:add')">+ 新增验证分支</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" border @header-dragend="handleHeaderDragend">
        <el-table-column prop="branchName" label="分支名" :width="colWidths.branchName" />
        <el-table-column prop="systemName" label="关联系统" :width="colWidths.systemName">
          <template #default="{ row }">
            {{ appSystemList.find(s => s.id === row.systemId)?.systemName || row.systemId }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" :width="colWidths.status">
          <template #default="{ row }">
            {{ dictStore.getDict('branch_status').find(d => d.dictCode === row.status)?.dictName || row.status }}
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="关联投产批次" :width="colWidths.batchNo">
          <template #default="{ row }">
            {{ batchList.find(b => b.id === row.batchId)?.batchNo || row.batchId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="colWidths.operation">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewRelation(row)">查看归属</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="authStore.userInfo?.permissions?.includes('verify_branch:edit')">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="authStore.userInfo?.permissions?.includes('verify_branch:delete')">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑验证分支' : '新增验证分支'" width="500px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="分支名" prop="branchName">
          <el-input v-model="form.branchName" />
        </el-form-item>
        <el-form-item label="关联系统" prop="systemId">
          <el-select v-model="form.systemId" placeholder="请选择">
            <el-option
              v-for="item in appSystemList"
              :key="item.id"
              :label="item.systemName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option
              v-for="item in dictStore.getDict('branch_status')"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联批次" prop="batchId">
          <QuickSelect
            v-model="form.batchId"
            api="/batch?current=1&size=100&availableOnly=true"
            create-api="/batch"
            label-key="batchNo"
            placeholder="请选择批次"
            :create-fields="['batchType', 'batchDate', 'status']"
          >
            <template #create-form="{ form }">
              <el-form-item label="批次种类" prop="batchType">
                <el-select v-model="form.batchType" placeholder="请选择">
                  <el-option
                    v-for="item in dictStore.getDict('batch_type')"
                    :key="item.dictCode"
                    :label="item.dictName"
                    :value="item.dictCode"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="批次日期" prop="batchDate">
                <el-date-picker v-model="form.batchDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="form.status" placeholder="请选择">
                  <el-option
                    v-for="item in dictStore.getDict('batch_status')"
                    :key="item.dictCode"
                    :label="item.dictName"
                    :value="item.dictCode"
                  />
                </el-select>
              </el-form-item>
            </template>
          </QuickSelect>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="relationVisible" title="验证分支归属关系" width="700px">
      <div v-if="relationLoading" style="text-align: center; padding: 40px;">加载中...</div>
      <el-tree
        v-else
        :data="relationData"
        :props="{ label: 'label', children: 'children' }"
        node-key="id"
        default-expand-all
        highlight-current
      >
        <template #default="{ node, data }">
          <span style="display: flex; align-items: center;">
            <span>{{ node.label }}</span>
            <el-tag :type="getRelationTypeTag(data.type)" size="small" style="margin-left: 8px;">{{ getRelationTypeName(data.type) }}</el-tag>
          </span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="relationVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { useColumnWidth } from '@/composables/useColumnWidth'
import request from '@/api/request'
import QuickSelect from '@/components/QuickSelect.vue'

const authStore = useAuthStore()
const dictStore = useDictStore()

const list = ref([])
const appSystemList = ref([])
const batchList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentId = ref(null)

const relationVisible = ref(false)
const relationLoading = ref(false)
const relationData = ref([])

const page = ref({ current: 1, size: 10, total: 0 })

const { colWidths, loadColWidths, handleHeaderDragend } = useColumnWidth(
  'verify-branch-col-widths',
  {
    branchName: 140,
    systemName: 120,
    status: 100,
    batchNo: 180,
    operation: 180
  }
)

const form = ref({
  branchName: '',
  systemId: null,
  status: '',
  batchId: null
})

const rules = {
  branchName: [{ required: true, message: '请输入分支名', trigger: 'blur' }],
  systemId: [{ required: true, message: '请选择关联系统', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/verify-branch', { params: page.value })
    list.value = res.data?.records || []
    page.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchAppSystems = async () => {
  const res = await request.get('/app-system')
  appSystemList.value = res.data || []
}

const fetchBatchList = async () => {
  const res = await request.get('/batch?current=1&size=100')
  batchList.value = res.data?.records || []
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  form.value = { branchName: '', systemId: null, status: '', batchId: null }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    branchName: row.branchName,
    systemId: row.systemId,
    status: row.status,
    batchId: row.batchId
  }
  dialogVisible.value = true
}

const handleViewRelation = async (row) => {
  relationVisible.value = true
  relationLoading.value = true
  try {
    const res = await request.get(`/verify-branch/${row.id}/relation-tree`)
    relationData.value = res.data ? [res.data] : []
  } finally {
    relationLoading.value = false
  }
}

const getRelationTypeTag = (type) => {
  const map = {
    verifyBranch: 'danger',
    devBranch: 'warning',
    prodRequirement: 'success',
    bizRequirement: 'primary'
  }
  return map[type] || ''
}

const getRelationTypeName = (type) => {
  const map = {
    verifyBranch: '验证分支',
    devBranch: '开发分支',
    prodRequirement: '产品需求',
    bizRequirement: '业务需求'
  }
  return map[type] || type
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/verify-branch/${currentId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/verify-branch', form.value)
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
    await ElMessageBox.confirm('确认删除该验证分支？', '提示', { type: 'warning' })
    await request.delete(`/verify-branch/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadColWidths()
  fetchList()
  fetchAppSystems()
  fetchBatchList()
})
</script>
