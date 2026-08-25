<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">开发分支管理</h1>
      <p class="page-header-desc">管理开发分支及其关联关系</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <el-form :inline="true" style="margin: 0;">
            <el-form-item label="关联需求" style="margin-bottom: 0;">
              <el-select
                v-model="query.reqRef"
                placeholder="按业务需求 / 产品需求筛选"
                clearable
                filterable
                style="width: 360px;"
                :loading="filterOptionsLoading"
                @change="handleSearch"
              >
                <el-option-group label="业务需求">
                  <el-option
                    v-for="b in bizOptions"
                    :key="'biz-' + b.id"
                    :label="`${b.reqCode} - ${b.reqName}`"
                    :value="'biz:' + b.id"
                  />
                </el-option-group>
                <el-option-group label="产品需求">
                  <el-option
                    v-for="p in prodOptions"
                    :key="'prod-' + p.id"
                    :label="`${p.prodReqCode} - ${p.prodReqName}${p.bizReqCode ? ' (归属 ' + p.bizReqCode + ')' : ''}`"
                    :value="'prod:' + p.id"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
            <el-form-item style="margin-bottom: 0;">
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
          <el-button type="primary" class="page-action-btn" @click="handleAdd" v-if="authStore.userInfo?.permissions?.includes('dev_branch:add')">+ 新增分支</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" border @header-dragend="handleHeaderDragend">
        <el-table-column prop="prodRequirements" label="关联业务及产品需求" :width="colWidths.prodRequirements">
          <template #default="{ row }">
            <div v-if="row.prodRequirements?.length">
              <div v-for="group in groupByBizReq(row.prodRequirements)" :key="group.bizReqId" style="margin-bottom: 8px;">
                <div style="font-weight: bold; color: #409EFF; margin-bottom: 2px;">
                  {{ group.bizReqCode }}-{{ group.bizReqName }}
                </div>
                <div style="padding-left: 12px;">
                  <div v-for="prod in group.prodList" :key="prod.id" style="margin-bottom: 2px; color: #67C23A;">
                    {{ prod.prodReqCode }}-{{ prod.prodReqName }}
                  </div>
                </div>
              </div>
            </div>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="branchName" label="分支名" :width="colWidths.branchName" />
        <el-table-column prop="systemName" label="关联系统" :width="colWidths.systemName">
          <template #default="{ row }">
            {{ appSystemList.find(s => s.id === row.systemId)?.systemName || row.systemId }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" :width="colWidths.status">
          <template #default="{ row }">
            <DictTag type="branch_status" :code="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="verifyBranchName" label="关联验证分支" :width="colWidths.verifyBranchName" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatVerifyBranch(row.verifyBranchId) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="colWidths.operation">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="authStore.userInfo?.permissions?.includes('dev_branch:edit')">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" title="编辑开发分支" width="500px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="分支名" prop="branchName">
          <el-input v-model="form.branchName" />
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
        <el-form-item label="关联验证分支" prop="verifyBranchId">
          <el-select v-model="form.verifyBranchId" placeholder="请选择" clearable>
            <el-option
              v-for="item in verifyBranchList"
              :key="item.id"
              :label="item.branchName + '-' + (appSystemList.find(s => s.id === item.systemId)?.systemName || item.systemId)"
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useDictStore } from '@/stores/dict'
import { useColumnWidth } from '@/composables/useColumnWidth'
import request from '@/api/request'
import DictTag from '@/components/DictTag.vue'

const authStore = useAuthStore()
const dictStore = useDictStore()

const list = ref([])
const appSystemList = ref([])
const verifyBranchList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const currentId = ref(null)

// 筛选条件
const query = ref({ reqRef: '' })
const bizOptions = ref([])
const prodOptions = ref([])
const filterOptionsLoading = ref(false)

const page = ref({ current: 1, size: 10, total: 0 })

const { colWidths, loadColWidths, handleHeaderDragend } = useColumnWidth(
  'dev-branch-col-widths',
  {
    prodRequirements: 320,
    branchName: 180,
    systemName: 140,
    status: 110,
    verifyBranchName: 220,
    operation: 120
  }
)

const form = ref({
  branchName: '',
  status: '',
  verifyBranchId: null
})

const rules = {
  branchName: [{ required: true, message: '请输入分支名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/dev-branch', {
      params: {
        current: page.value.current,
        size: page.value.size,
        ...(query.value.reqRef ? { reqRef: query.value.reqRef } : {})
      }
    })
    list.value = res.data?.records || []
    page.value.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value.current = 1
  fetchList()
}

const handleReset = () => {
  query.value.reqRef = ''
  page.value.current = 1
  fetchList()
}

const fetchAppSystems = async () => {
  const res = await request.get('/app-system')
  appSystemList.value = res.data || []
}

const fetchVerifyBranches = async () => {
  const res = await request.get('/verify-branch?current=1&size=100')
  verifyBranchList.value = res.data?.records || []
}

/**
 * 加载筛选下拉的可选项：业务需求 + 产品需求（含父业务编码）
 * 两个查询并发；产品需求用 size=500 截断（够用即可，下拉有 filterable 兜底）
 */
const loadFilterOptions = async () => {
  filterOptionsLoading.value = true
  try {
    const [bizRes, prodRes] = await Promise.all([
      request.get('/biz-requirement?current=1&size=1000'),
      request.get('/prod-requirement?current=1&size=500')
    ])
    bizOptions.value = bizRes.data?.records || []
    // 为 prodOption 补 bizReqCode 用于显示
    const bizMap = new Map(bizOptions.value.map(b => [b.id, b]))
    const prodList = prodRes.data?.records || []
    prodOptions.value = prodList.map(p => ({
      ...p,
      bizReqCode: bizMap.get(p.bizReqId)?.reqCode || ''
    }))
  } catch (e) {
    console.error('加载筛选下拉选项失败', e)
  } finally {
    filterOptionsLoading.value = false
  }
}

const formatVerifyBranch = (verifyBranchId) => {
  if (!verifyBranchId) return '-'
  const branch = verifyBranchList.value.find(v => v.id === verifyBranchId)
  if (!branch) return verifyBranchId
  const systemName = appSystemList.value.find(s => s.id === branch.systemId)?.systemName || branch.systemId
  return `${branch.branchName}-${systemName}`
}

const groupByBizReq = (prodList) => {
  const map = new Map()
  prodList.forEach(prod => {
    const key = prod.bizReqId || 0
    if (!map.has(key)) {
      map.set(key, {
        bizReqId: prod.bizReqId,
        bizReqCode: prod.bizReqCode || '-',
        bizReqName: prod.bizReqName || '-',
        prodList: []
      })
    }
    map.get(key).prodList.push(prod)
  })
  return Array.from(map.values())
}

const handleAdd = () => {
  currentId.value = null
  form.value = { branchName: '', status: '', verifyBranchId: null }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentId.value = row.id
  form.value = {
    branchName: row.branchName,
    status: row.status,
    verifyBranchId: row.verifyBranchId
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await request.put(`/dev-branch/${currentId.value}`, form.value)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadColWidths()
  fetchList()
  fetchAppSystems()
  fetchVerifyBranches()
  loadFilterOptions()
})
</script>
