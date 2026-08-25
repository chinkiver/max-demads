<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">需求管理</h1>
      <p class="page-header-desc">管理业务需求，跟踪交付进度</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <el-button type="primary" class="page-action-btn" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('biz:requirement:add')">+ 新增需求</el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom: 16px; display: flex; flex-wrap: wrap; justify-content: flex-start; gap: 8px;">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择" clearable style="width: 160px;">
            <el-option
              v-for="item in dictStore.getDict('biz_req_status')"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="批次">
          <el-select v-model="query.batchId" placeholder="请选择" clearable style="width: 220px;">
            <el-option
              v-for="item in batchList"
              :key="item.id"
              :label="item.batchNo"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="mineOwner" @change="handleMineToggle('owner')" style="margin-right: 8px;">我的</el-checkbox>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="query = { status: '', batchId: '', owner: '' }; mineOwner = false; handleSearch()">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left: auto;">
          <el-select
            v-model="visibleColumns"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="选择展示字段"
            style="width: 180px;"
            @change="saveVisibleColumns"
          >
            <el-option
              v-for="col in columnOptions"
              :key="col.prop"
              :label="col.label"
              :value="col.prop"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border @header-dragend="handleHeaderDragend">
        <el-table-column v-if="visibleColumns.includes('reqCode')" prop="reqCode" label="业务需求编码-名称" :width="colWidths.req" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.reqCode }}-{{ row.reqName }}
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('summary')" prop="summary" label="需求概要" :width="colWidths.summary" show-overflow-tooltip />
        <el-table-column v-if="visibleColumns.includes('reqCategory')" prop="reqCategory" label="需求种类" :width="colWidths.reqCategory">
          <template #default="{ row }">
            <DictTag type="req_category" :code="row.reqCategory" />
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('priority')" prop="priority" label="优先级" :width="colWidths.priority">
          <template #default="{ row }">
            <DictTag type="priority" :code="row.priority" />
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('proposer')" prop="proposer" label="提出人/部门" :width="colWidths.proposer">
          <template #default="{ row }">
            {{ row.proposer }}/{{ row.proposerDept }}
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('owner')" prop="owner" label="负责人" :width="colWidths.owner">
          <template #default="{ row }">
            <span style="display: inline-flex; align-items: center; vertical-align: middle;">
              {{ row.owner }}
              <img
                v-if="row.owner && row.owner === authStore.userInfo?.realName"
                src="@/assets/me-badge.png"
                alt="我"
                style="width: 18px; height: 18px; margin-left: 1px;"
              />
            </span>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('batchNo')" prop="batchNo" label="关联批次" :width="colWidths.batchNo">
          <template #default="{ row }">
            <span style="display: inline-flex; align-items: center; vertical-align: middle;">
              <img
                v-if="isCurrentMonth(row.batchDate)"
                src="@/assets/attention.png"
                alt="注意"
                style="width: 18px; height: 18px; margin-right: 1px;"
              />
              <span>{{ batchList.find(item => item.id === row.batchId)?.batchNo || '-' }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.includes('status')" prop="status" label="状态" :width="colWidths.status">
          <template #default="{ row }">
            <DictTag type="biz_req_status" :code="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="colWidths.operation" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="authStore.userInfo?.permissions?.includes('biz:requirement:edit')">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="authStore.userInfo?.permissions?.includes('biz:requirement:delete')">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑业务需求' : '新增业务需求'" width="900px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="需求编码" prop="reqCode">
          <el-input v-model="form.reqCode" />
        </el-form-item>
        <el-form-item label="需求名称" prop="reqName">
          <el-input v-model="form.reqName" />
        </el-form-item>
        <el-form-item label="需求种类" prop="reqCategory">
          <el-select v-model="form.reqCategory" placeholder="请选择">
            <el-option
              v-for="item in dictStore.getDict('req_category')"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择">
            <el-option
              v-for="item in dictStore.getDict('priority')"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提出人" prop="proposer">
          <el-input v-model="form.proposer" placeholder="请输入提出人" />
          <div v-if="historyMap.proposer.length" style="margin-top: 6px;">
            <el-tag
              v-for="item in historyMap.proposer"
              :key="item.value"
              size="small"
              style="margin-right: 6px; margin-bottom: 4px; cursor: pointer;"
              @click="form.proposer = item.value"
            >{{ item.label }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="提出部门" prop="proposerDept">
          <el-input v-model="form.proposerDept" placeholder="请输入提出部门" />
          <div v-if="historyMap.proposerDept.length" style="margin-top: 6px;">
            <el-tag
              v-for="item in historyMap.proposerDept"
              :key="item.value"
              size="small"
              style="margin-right: 6px; margin-bottom: 4px; cursor: pointer;"
              @click="form.proposerDept = item.value"
            >{{ item.label }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="负责人" prop="owner">
          <el-input v-model="form.owner" placeholder="请输入负责人" />
          <div v-if="historyMap.owner.length" style="margin-top: 6px;">
            <el-tag
              v-for="item in historyMap.owner"
              :key="item.value"
              size="small"
              style="margin-right: 6px; margin-bottom: 4px; cursor: pointer;"
              @click="form.owner = item.value"
            >{{ item.label }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="关联批次" prop="batchId">
          <QuickSelect
            v-model="form.batchId"
            :api="isEdit ? '/batch?current=1&size=100' : '/batch?current=1&size=100&availableOnly=true'"
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
        <el-form-item label="需求概要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option
              v-for="item in dictStore.getDict('biz_req_status')"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="查看业务需求" width="1400px">
      <div style="margin-bottom: 16px;">
        <div style="font-weight: bold; margin-bottom: 12px;">业务需求信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需求编码">
            {{ viewRow.reqCode }}
            <el-button type="primary" link size="small" @click="copyReqInfo" style="margin-left: 8px;">
              <el-icon><Document-Copy /></el-icon>复制
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="需求名称">{{ viewRow.reqName }}</el-descriptions-item>
          <el-descriptions-item label="需求种类"><DictTag type="req_category" :code="viewRow.reqCategory" /></el-descriptions-item>
          <el-descriptions-item label="优先级"><DictTag type="priority" :code="viewRow.priority" /></el-descriptions-item>
          <el-descriptions-item label="提出人">{{ viewRow.proposer }}</el-descriptions-item>
          <el-descriptions-item label="提出部门">{{ viewRow.proposerDept }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ viewRow.owner }}</el-descriptions-item>
          <el-descriptions-item label="关联批次">{{ batchList.find(item => item.id === viewRow.batchId)?.batchNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态"><DictTag type="biz_req_status" :code="viewRow.status" /></el-descriptions-item>
          <el-descriptions-item label="需求概要">{{ viewRow.summary || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div>
        <div style="font-weight: bold; margin-bottom: 12px;">归属产品需求</div>
        <el-table :data="viewProdReqList" border size="small" empty-text="暂无归属产品需求">
          <el-table-column label="产品需求编码-名称" min-width="260" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.prodReqCode }}-{{ row.prodReqName }}
            </template>
          </el-table-column>
          <el-table-column prop="systemName" label="关联系统" min-width="140" show-overflow-tooltip />
          <el-table-column prop="developer" label="开发人员" min-width="100" />
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="{ row }">
              <DictTag type="prod_req_status" :code="row.status" />
            </template>
          </el-table-column>
          <el-table-column prop="devBranchName" label="开发分支" min-width="160" show-overflow-tooltip />
          <el-table-column prop="devBranchStatus" label="开发分支状态" min-width="110">
            <template #default="{ row }">
              <DictTag type="branch_status" :code="row.devBranchStatus" />
            </template>
          </el-table-column>
          <el-table-column prop="verifyBranchName" label="验证分支" min-width="160" show-overflow-tooltip />
          <el-table-column prop="verifyBranchStatus" label="验证分支状态" min-width="110">
            <template #default="{ row }">
              <DictTag type="branch_status" :code="row.verifyBranchStatus" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
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
import { useInputHistory } from '@/composables/useInputHistory'
import request from '@/api/request'
import QuickSelect from '@/components/QuickSelect.vue'
import DictTag from '@/components/DictTag.vue'

const authStore = useAuthStore()
const dictStore = useDictStore()

const list = ref([])
const batchList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentId = ref(null)

const viewDialogVisible = ref(false)
const viewRow = ref({})
const viewProdReqList = ref([])

const query = ref({ status: '', batchId: '', owner: '' })
const mineOwner = ref(false)
const page = ref({ current: 1, size: 10, total: 0 })

const { colWidths, loadColWidths, handleHeaderDragend } = useColumnWidth(
  'biz-requirement-col-widths',
  {
    req: 320,
    summary: 240,
    reqCategory: 100,
    priority: 100,
    proposer: 180,
    owner: 100,
    batchNo: 180,
    status: 100,
    operation: 180
  },
  (prop) => prop === 'reqCode' ? 'req' : prop
)

const COLUMN_VISIBLE_KEY = 'biz-requirement-visible-columns'
const columnOptions = [
  { prop: 'reqCode', label: '业务需求编码-名称' },
  { prop: 'summary', label: '需求概要' },
  { prop: 'reqCategory', label: '需求种类' },
  { prop: 'priority', label: '优先级' },
  { prop: 'proposer', label: '提出人/部门' },
  { prop: 'owner', label: '负责人' },
  { prop: 'batchNo', label: '关联批次' },
  { prop: 'status', label: '状态' }
]
const defaultVisibleColumns = columnOptions.map(c => c.prop)
const visibleColumns = ref([...defaultVisibleColumns])

const loadVisibleColumns = () => {
  try {
    const saved = localStorage.getItem(COLUMN_VISIBLE_KEY)
    if (saved) {
      visibleColumns.value = JSON.parse(saved)
    }
  } catch (e) {
    console.error('加载显示列失败', e)
  }
}

const saveVisibleColumns = () => {
  localStorage.setItem(COLUMN_VISIBLE_KEY, JSON.stringify(visibleColumns.value))
}

const { historyMap, loadHistory, saveHistory } = useInputHistory('biz-requirement', {
  proposer: { limit: 5 },
  proposerDept: { limit: 5 },
  owner: { limit: 5 }
})

const form = ref({
  reqCode: '',
  reqName: '',
  reqCategory: '',
  priority: '',
  proposer: '',
  owner: '',
  summary: '',
  status: '',
  batchId: null
})

const rules = {
  reqCode: [{ required: true, message: '请输入需求编码', trigger: 'blur' }],
  reqName: [{ required: true, message: '请输入需求名称', trigger: 'blur' }],
  reqCategory: [{ required: true, message: '请选择需求种类', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  proposer: [{ required: true, message: '请输入提出人', trigger: 'blur' }],
  proposerDept: [{ required: true, message: '请输入提出部门', trigger: 'blur' }],
  owner: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  batchId: [{ required: true, message: '请选择关联批次', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const isCurrentMonth = (batchDate) => {
  if (!batchDate) return false
  const date = new Date(batchDate)
  const now = new Date()
  return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth()
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { ...page.value, ...query.value }
    const res = await request.get('/biz-requirement', { params })
    list.value = res.data?.records || []
    page.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const fetchBatchList = async () => {
  const res = await request.get('/batch?current=1&size=100')
  batchList.value = res.data?.records || []
}

const handleSearch = () => {
  page.value.current = 1
  fetchList()
}

const handleMineToggle = (field) => {
  query.value[field] = mineOwner.value ? (authStore.userInfo?.realName || '') : ''
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  form.value = { reqCode: '', reqName: '', reqCategory: '', priority: '', proposer: '', proposerDept: '', owner: '', summary: '', status: '', batchId: null }
  dialogVisible.value = true
}

const handleView = async (row) => {
  viewRow.value = { ...row }
  viewDialogVisible.value = true
  const res = await request.get('/prod-requirement', { params: { bizReqId: row.id, current: 1, size: 100 } })
  viewProdReqList.value = res.data?.records || []
}

const copyReqInfo = async () => {
  const text = `${viewRow.value.reqCode || ''}-${viewRow.value.reqName || ''}`
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    console.error('复制失败', e)
    ElMessage.error('复制失败')
  }
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    reqCode: row.reqCode,
    reqName: row.reqName,
    reqCategory: row.reqCategory,
    priority: row.priority,
    proposer: row.proposer,
    proposerDept: row.proposerDept,
    owner: row.owner,
    summary: row.summary,
    status: row.status,
    batchId: row.batchId
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/biz-requirement/${currentId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/biz-requirement', form.value)
      ElMessage.success('新增成功')
    }
    saveHistory(form.value)
    dialogVisible.value = false
    query.value = { status: '', batchId: '' }
    page.value.current = 1
    fetchList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该业务需求？', '提示', { type: 'warning' })
    await request.delete(`/biz-requirement/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadColWidths()
  loadVisibleColumns()
  loadHistory()
  fetchList()
  fetchBatchList()
})
</script>

<style scoped>
.view-form :deep(.el-form-item__content) {
  padding-top: 6px;
  color: #606266;
}
</style>
