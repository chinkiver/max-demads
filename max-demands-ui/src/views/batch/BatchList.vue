<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">投产批次管理</h1>
      <p class="page-header-desc">管理投产批次与部署计划</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
          <el-button @click="handleAutoUpdate" v-if="authStore.userInfo?.permissions?.includes('batch:edit')">自动更新</el-button>
          <el-button @click="openAutoDialog" v-if="authStore.userInfo?.permissions?.includes('batch:edit')">自动生成批次</el-button>
          <el-button type="primary" class="page-action-btn" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('batch:add')">+ 新增批次</el-button>
        </div>
      </template>

      <el-alert
        title="批次说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px;"
      >
        <div>常规投产：可投产业务需求或自主优化需求</div>
        <div>标准投产：只能投产自主优化需求</div>
      </el-alert>

      <el-table :data="list" v-loading="loading" border :row-class-name="rowClassName" @header-dragend="handleHeaderDragend">
        <el-table-column prop="batchNo" label="批次号" :width="colWidths.batchNo" />
        <el-table-column prop="batchType" label="批次种类" :width="colWidths.batchType">
          <template #default="{ row }">
            <el-tag :type="getBatchTypeTag(row.batchType)" size="small">
              {{ dictStore.getDict('batch_type').find(d => d.dictCode === row.batchType)?.dictName || row.batchType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batchDate" label="批次日期" :width="colWidths.batchDate" />
        <el-table-column prop="status" label="状态" :width="colWidths.status">
          <template #default="{ row }">
            {{ dictStore.getDict('batch_status').find(d => d.dictCode === row.status)?.dictName || row.status }}
          </template>
        </el-table-column>
        <el-table-column prop="requirements" label="关联投产需求列表" :width="colWidths.requirements">
          <template #default="{ row }">
            <div v-if="row.requirements?.length">
              <div v-for="(req, index) in row.requirements" :key="req.id" class="req-item">
                <span class="req-index">{{ index + 1 }}</span>
                <span>{{ req.reqCode }}-{{ req.reqName }}</span>
              </div>
            </div>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewRequirements(row)">查看需求</el-button>
            <el-button type="primary" link @click="handleEdit(row)" v-if="authStore.userInfo?.permissions?.includes('batch:edit')">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="authStore.userInfo?.permissions?.includes('batch:delete')">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑批次' : '新增批次'" width="500px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看需求弹窗 -->
    <el-dialog v-model="reqDialogVisible" title="批次需求" width="1300px">
      <div v-if="reqLoading" style="text-align: center; padding: 40px;">加载中...</div>
      <el-table v-else :data="reqList" border size="small" empty-text="该批次下暂无需求">
        <el-table-column type="expand" width="50">
          <template #default="{ row }">
            <div style="padding: 12px 24px;">
              <div style="font-weight: bold; margin-bottom: 8px;">归属产品需求</div>
              <el-table :data="row.prodRequirements" border size="small" empty-text="暂无产品需求">
                <el-table-column label="产品需求" min-width="260" show-overflow-tooltip>
                  <template #default="{ row: prod }">
                    {{ prod.prodReqCode }}-{{ prod.prodReqName }}
                  </template>
                </el-table-column>
                <el-table-column prop="developer" label="开发人员" min-width="100" />
                <el-table-column prop="status" label="状态" min-width="100">
                  <template #default="{ row: prod }">
                    {{ dictStore.getDict('prod_req_status').find(d => d.dictCode === prod.status)?.dictName || prod.status }}
                  </template>
                </el-table-column>
                <el-table-column prop="devBranchName" label="开发分支" min-width="140" show-overflow-tooltip />
                <el-table-column prop="devBranchStatus" label="开发分支状态" min-width="110">
                  <template #default="{ row: prod }">
                    {{ dictStore.getDict('branch_status').find(d => d.dictCode === prod.devBranchStatus)?.dictName || prod.devBranchStatus || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="verifyBranchName" label="验证分支" min-width="140" show-overflow-tooltip />
                <el-table-column prop="verifyBranchStatus" label="验证分支状态" min-width="110">
                  <template #default="{ row: prod }">
                    {{ dictStore.getDict('branch_status').find(d => d.dictCode === prod.verifyBranchStatus)?.dictName || prod.verifyBranchStatus || '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="需求" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.reqCode }}-{{ row.reqName }}
          </template>
        </el-table-column>
        <el-table-column prop="reqCategory" label="需求种类" min-width="100">
          <template #default="{ row }">
            {{ dictStore.getDict('req_category').find(d => d.dictCode === row.reqCategory)?.dictName || row.reqCategory }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" min-width="100">
          <template #default="{ row }">
            {{ dictStore.getDict('priority').find(d => d.dictCode === row.priority)?.dictName || row.priority }}
          </template>
        </el-table-column>
        <el-table-column prop="proposer" label="提出人" min-width="100" />
        <el-table-column prop="proposerDept" label="提出部门" min-width="120" />
        <el-table-column prop="owner" label="负责人" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            {{ dictStore.getDict('biz_req_status').find(d => d.dictCode === row.status)?.dictName || row.status }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="reqDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 自动生成批次弹窗 -->
    <el-dialog v-model="autoDialogVisible" title="自动生成批次" width="700px">
      <el-form :model="autoForm" ref="autoFormRef" :rules="autoRules" label-width="100px">
        <el-form-item label="月份" prop="month">
          <el-date-picker v-model="autoForm.month" type="month" value-format="YYYY-MM" placeholder="选择月份" />
        </el-form-item>
      </el-form>
      <el-table v-if="autoResult.length" :data="autoResult" border size="small" style="margin-top: 16px;">
        <el-table-column prop="batchDate" label="日期" />
        <el-table-column prop="batchType" label="种类" />
        <el-table-column prop="batchNo" label="批次号" />
        <el-table-column prop="result" label="结果">
          <template #default="{ row }">
            <el-tag :type="row.result === 'success' ? 'success' : (row.result === 'existed' ? 'warning' : 'danger')">
              {{ row.result === 'success' ? '新增成功' : (row.result === 'existed' ? '已存在' : '失败') }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="autoDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleAutoGenerate" :loading="autoLoading">生成</el-button>
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

const authStore = useAuthStore()
const dictStore = useDictStore()

const { colWidths, loadColWidths, handleHeaderDragend } = useColumnWidth(
  'batch-col-widths',
  {
    batchNo: 150,
    batchType: 120,
    batchDate: 120,
    status: 100,
    requirements: 220
  }
)
loadColWidths()

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const submitting = ref(false)
const formRef = ref()
const page = ref({ current: 1, size: 10, total: 0 })

const form = ref({
  batchType: '',
  batchDate: '',
  status: ''
})

const rules = {
  batchType: [{ required: true, message: '请选择批次种类', trigger: 'change' }],
  batchDate: [{ required: true, message: '请选择批次日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const autoDialogVisible = ref(false)
const autoFormRef = ref()
const autoForm = ref({ month: '' })
const autoRules = {
  month: [{ required: true, message: '请选择月份', trigger: 'change' }]
}
const autoResult = ref([])
const autoLoading = ref(false)

const reqDialogVisible = ref(false)
const reqLoading = ref(false)
const reqList = ref([])

const getBatchTypeTag = (batchType) => {
  const map = {
    routine_production: 'primary',
    standard_production: 'success',
    emergency_production: 'danger',
    special_approval_production: 'warning'
  }
  return map[batchType] || ''
}

const isBatchExpired = (batchDate) => {
  if (!batchDate) return false
  const date = new Date(batchDate)
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return date < now
}

const rowClassName = ({ row }) => {
  return (row.status === 'completed' || isBatchExpired(row.batchDate)) ? 'batch-completed-row' : ''
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/batch', { params: page.value })
    list.value = res.data?.records || []
    page.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  form.value = { batchType: '', batchDate: '', status: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    batchType: row.batchType,
    batchDate: row.batchDate,
    status: row.status
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/batch/${currentId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/batch', form.value)
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
    await ElMessageBox.confirm('确认删除该批次？', '提示', { type: 'warning' })
    await request.delete(`/batch/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleAutoUpdate = async () => {
  try {
    await ElMessageBox.confirm('确认将批次日期小于今天的未完成批次自动更新为已完成？', '提示', { type: 'warning' })
    const res = await request.post('/batch/auto-update')
    const count = res.data || 0
    ElMessage.success(`已自动更新 ${count} 个批次`)
    fetchList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleViewRequirements = async (row) => {
  reqDialogVisible.value = true
  reqLoading.value = true
  try {
    const res = await request.get(`/batch/${row.id}/requirements`)
    reqList.value = res.data || []
  } finally {
    reqLoading.value = false
  }
}

const openAutoDialog = () => {
  autoForm.value = { month: '' }
  autoResult.value = []
  autoDialogVisible.value = true
}

const handleAutoGenerate = async () => {
  await autoFormRef.value.validate()
  autoLoading.value = true
  try {
    const res = await request.post('/batch/auto-generate', { month: autoForm.value.month })
    autoResult.value = res.data || []
    const failedCount = autoResult.value.filter(r => r.result === 'error').length
    if (failedCount === 0) {
      ElMessage.success('全部生成成功')
    } else {
      ElMessage.warning(`部分生成失败，请查看结果`)
    }
    fetchList()
  } finally {
    autoLoading.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
:deep(.batch-completed-row) {
  color: #999;
  text-decoration: line-through;
}
:deep(.batch-completed-row .cell) {
  color: #999;
}

.req-item {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.req-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  margin-right: 8px;
  flex-shrink: 0;
}
</style>
