<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">产品需求管理</h1>
      <p class="page-header-desc">管理业务需求下的产品子需求</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: flex-end; align-items: center;">
          <el-button type="primary" class="page-action-btn" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('prod:requirement:add')">+ 新增产品需求</el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="关联业务需求">
          <el-select v-model="query.bizReqId" placeholder="请选择" clearable filterable style="width: 260px;">
            <el-option
              v-for="item in bizReqList"
              :key="item.id"
              :label="`${item.reqCode}-${item.reqName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="query = { bizReqId: '' }; handleSearch()">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border @header-dragend="handleHeaderDragend">
        <el-table-column prop="prodReqCode" label="产品需求编码-名称" :width="colWidths.prodReqCode">
          <template #default="{ row }">
            {{ row.prodReqCode }}-{{ row.prodReqName }}
          </template>
        </el-table-column>
        <el-table-column prop="systemName" label="开发系统" :width="colWidths.systemName">
          <template #default="{ row }">
            {{ appSystemList.find(s => s.id === row.systemId)?.systemName || row.systemId }}
          </template>
        </el-table-column>
        <el-table-column prop="developer" label="开发人员" :width="colWidths.developer">
          <template #default="{ row }">
            {{ row.developer }}
            <img
              v-if="row.developer && row.developer === authStore.userInfo?.realName"
              src="@/assets/me-badge.png"
              alt="我"
              style="width: 18px; height: 18px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" :width="colWidths.status">
          <template #default="{ row }">
            {{ dictStore.getDict('prod_req_status').find(d => d.dictCode === row.status)?.dictName || row.status }}
          </template>
        </el-table-column>
        <el-table-column prop="devBranchName" label="关联开发分支" show-overflow-tooltip :width="colWidths.devBranchName" />
        <el-table-column prop="devBranchStatus" label="开发分支状态" :width="colWidths.devBranchStatus">
          <template #default="{ row }">
            {{ dictStore.getDict('branch_status').find(d => d.dictCode === row.devBranchStatus)?.dictName || row.devBranchStatus || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="verifyBranchName" label="验证分支" show-overflow-tooltip :width="colWidths.verifyBranchName" />
        <el-table-column prop="verifyBranchStatus" label="验证分支状态" :width="colWidths.verifyBranchStatus">
          <template #default="{ row }">
            {{ dictStore.getDict('branch_status').find(d => d.dictCode === row.verifyBranchStatus)?.dictName || row.verifyBranchStatus || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="colWidths.operation" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="authStore.userInfo?.permissions?.includes('prod:requirement:edit')">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="authStore.userInfo?.permissions?.includes('prod:requirement:delete')">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑产品需求' : '新增产品需求'" width="760px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="120px">
        <el-form-item label="产品需求编码" prop="prodReqCode">
          <el-input v-model="form.prodReqCode" />
        </el-form-item>
        <el-form-item label="产品需求名称" prop="prodReqName">
          <el-input v-model="form.prodReqName" />
        </el-form-item>
        <el-form-item label="关联业务需求" prop="bizReqId">
          <el-select v-model="form.bizReqId" placeholder="请选择" clearable filterable style="width: 100%;">
            <el-option
              v-for="item in dialogBizReqList"
              :key="item.id"
              :label="`${item.reqCode}-${item.reqName}`"
              :value="item.id"
            />
          </el-select>
          <div v-if="historyMap.bizReqId.length" style="margin-top: 6px;">
            <el-tag
              v-for="item in historyMap.bizReqId"
              :key="item.value"
              size="small"
              style="margin-right: 6px; margin-bottom: 4px; cursor: pointer;"
              @click="form.bizReqId = item.value"
            >{{ item.label }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="关联系统" prop="systemId">
          <QuickSelect
            v-model="form.systemId"
            api="/app-system"
            create-api="/app-system"
            label-key="systemName"
            placeholder="请选择系统"
            :create-fields="[]"
            :create-rules="appSystemCreateRules"
            dialog-width="600px"
            create-history-prefix="app-system-create"
            :create-history-fields="{ businessDept: { label: '归属业务部门', limit: 5 }, owner: { label: '负责人', limit: 5 } }"
          >
            <template #create-form="{ form }">
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
            </template>
          </QuickSelect>
          <div v-if="historyMap.systemId.length" style="margin-top: 6px;">
            <el-tag
              v-for="item in historyMap.systemId"
              :key="item.value"
              size="small"
              style="margin-right: 6px; margin-bottom: 4px; cursor: pointer;"
              @click="form.systemId = item.value"
            >{{ item.label }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="开发人员" prop="developer">
          <el-input v-model="form.developer" />
          <div v-if="historyMap.developer.length" style="margin-top: 6px;">
            <el-tag
              v-for="item in historyMap.developer"
              :key="item.value"
              size="small"
              style="margin-right: 6px; margin-bottom: 4px; cursor: pointer;"
              @click="form.developer = item.value"
            >{{ item.label }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option
              v-for="item in dictStore.getDict('prod_req_status')"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="需求概要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="开发分支选项" prop="branchAction">
          <el-radio-group v-model="form.branchAction">
            <el-radio label="none">不创建</el-radio>
            <el-radio label="associate">关联已有</el-radio>
            <el-radio label="create">新建分支</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联分支" prop="devBranchId" v-if="form.branchAction === 'associate'">
          <el-select v-model="form.devBranchId" placeholder="请选择分支">
            <el-option
              v-for="item in devBranchList"
              :key="item.id"
              :label="item.branchName"
              :value="item.id"
            />
          </el-select>
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
import { useColumnWidth } from '@/composables/useColumnWidth'
import { useInputHistory } from '@/composables/useInputHistory'
import request from '@/api/request'
import QuickSelect from '@/components/QuickSelect.vue'

const authStore = useAuthStore()
const dictStore = useDictStore()

const list = ref([])
const bizReqList = ref([])
const appSystemList = ref([])
const devBranchList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentId = ref(null)

const query = ref({ bizReqId: '' })
const page = ref({ current: 1, size: 10, total: 0 })

const dialogBizReqList = computed(() => {
  if (isEdit.value) return bizReqList.value
  return bizReqList.value.filter(item => item.status !== 'completed')
})

const { historyMap, loadHistory, saveHistory } = useInputHistory('prod-requirement', {
  bizReqId: {
    limit: 3,
    getLabel: (value, form) => {
      const item = bizReqList.value.find(b => b.id === value)
      return item ? `${item.reqCode}-${item.reqName}` : String(value)
    }
  },
  systemId: {
    limit: 5,
    getLabel: (value, form) => {
      const item = appSystemList.value.find(s => s.id === value)
      return item ? item.systemName : String(value)
    }
  },
  developer: { limit: 5 }
})

const { colWidths, loadColWidths, handleHeaderDragend } = useColumnWidth(
  'prod-requirement-col-widths',
  {
    prodReqCode: 280,
    systemName: 120,
    developer: 100,
    status: 100,
    devBranchName: 150,
    devBranchStatus: 120,
    verifyBranchName: 150,
    verifyBranchStatus: 120,
    operation: 180
  }
)

const form = ref({
  prodReqCode: '',
  prodReqName: '',
  bizReqId: null,
  systemId: null,
  developer: '',
  status: '',
  summary: '',
  branchAction: 'none',
  devBranchId: null
})

const rules = {
  prodReqCode: [{ required: true, message: '请输入产品需求编码', trigger: 'blur' }],
  prodReqName: [{ required: true, message: '请输入产品需求名称', trigger: 'blur' }],
  bizReqId: [{ required: true, message: '请选择关联业务需求', trigger: 'change' }],
  systemId: [{ required: true, message: '请选择关联系统', trigger: 'change' }],
  developer: [{ required: true, message: '请输入开发人员', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  branchAction: [{ required: true, message: '请选择开发分支选项', trigger: 'change' }]
}

const appSystemCreateRules = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  businessDept: [{ required: true, message: '请输入归属业务部门', trigger: 'blur' }],
  owner: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { ...page.value, ...query.value }
    const res = await request.get('/prod-requirement', { params })
    list.value = res.data?.records || []
    page.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchBizReqList = async () => {
  const res = await request.get('/biz-requirement?current=1&size=1000')
  bizReqList.value = res.data?.records || []
}

const fetchAppSystems = async () => {
  const res = await request.get('/app-system')
  appSystemList.value = res.data || []
}

const fetchDevBranches = async () => {
  const res = await request.get('/dev-branch?current=1&size=100')
  devBranchList.value = res.data?.records || []
}

const handleSearch = () => {
  page.value.current = 1
  fetchList()
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  form.value = { prodReqCode: '', prodReqName: '', bizReqId: null, systemId: null, developer: '', status: '', summary: '', branchAction: 'none', devBranchId: null }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    prodReqCode: row.prodReqCode,
    prodReqName: row.prodReqName,
    bizReqId: row.bizReqId,
    systemId: row.systemId,
    developer: row.developer,
    status: row.status,
    summary: row.summary,
    branchAction: row.branchAction || 'none',
    devBranchId: row.devBranchId
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/prod-requirement/${currentId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/prod-requirement', form.value)
      ElMessage.success('新增成功')
    }
    saveHistory(form.value)
    dialogVisible.value = false
    fetchList()
    fetchAppSystems()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该产品需求？', '提示', { type: 'warning' })
    await request.delete(`/prod-requirement/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadColWidths()
  loadHistory()
  fetchList()
  fetchBizReqList()
  fetchAppSystems()
  fetchDevBranches()
})
</script>
