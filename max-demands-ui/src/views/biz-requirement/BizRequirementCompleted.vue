<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">已投产需求</h1>
      <p class="page-header-desc">查看已完成的业务需求及其归属产品需求、开发分支、验证分支的整体情况</p>
    </div>
    <el-card class="page-card">
      <el-table
        :data="treeData"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
        border
        :row-class-name="getRowClassName"
        @header-dragend="handleHeaderDragend"
      >
        <el-table-column prop="name" label="层级/名称" :width="colWidths.name">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small" style="margin-right: 8px;">
              {{ typeLabel[row.type] }}
            </el-tag>
            <span>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" :width="colWidths.status">
          <template #default="{ row }">
            <DictTag v-if="row.status" :type="statusDictType(row.type)" :code="row.status" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="负责人/开发人" :width="colWidths.owner">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'biz' && row.owner" type="info" size="small">{{ row.owner }}</el-tag>
            <el-tag v-else-if="row.type === 'prod' && row.developer" type="info" size="small">{{ row.developer }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="batchNo" label="关联批次" :width="colWidths.batchNo">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'biz' && row.batchNo" type="warning" size="small">{{ row.batchNo }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="productionDate" label="投产日期" :width="colWidths.productionDate">
          <template #default="{ row }">
            <el-date-picker
              v-if="row.type === 'biz'"
              v-model="row.productionDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              size="small"
              style="width: 120px;"
              @change="handleProductionDateChange(row)"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="systemName" label="所属系统" :width="colWidths.systemName">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'prod' && row.systemName" type="warning" size="small">{{ row.systemName }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useDictStore } from '@/stores/dict'
import { useColumnWidth } from '@/composables/useColumnWidth'
import DictTag from '@/components/DictTag.vue'

const dictStore = useDictStore()
const loading = ref(false)
const treeData = ref([])

const { colWidths, loadColWidths, handleHeaderDragend } = useColumnWidth(
  'biz-requirement-completed-col-widths',
  {
    name: 280,
    status: 100,
    owner: 120,
    batchNo: 150,
    productionDate: 140,
    systemName: 130
  }
)

loadColWidths()

const typeLabel = {
  biz: '业务需求',
  prod: '产品需求',
  dev: '开发分支',
  verify: '验证分支'
}

const getTypeTag = (type) => {
  const map = {
    biz: 'primary',
    prod: 'success',
    dev: 'warning',
    verify: 'info'
  }
  return map[type] || ''
}

const getBizStatusType = (status) => {
  const map = {
    draft: 'info',
    pending: 'warning',
    assigned: 'primary',
    in_progress: 'success',
    system_testing: 'primary',
    acceptance_testing: 'warning',
    pending_production: 'danger',
    completed: 'success',
    cancelled: 'info'
  }
  return map[status] || ''
}

const getProdStatusType = (status) => {
  const map = {
    draft: 'info',
    pending: 'warning',
    developing: 'primary',
    testing: 'warning',
    completed: 'success',
    cancelled: 'info'
  }
  return map[status] || ''
}

const getBranchStatusType = (status) => {
  const map = {
    active: 'success',
    merged: 'primary',
    closed: 'info'
  }
  return map[status] || ''
}

const getStatusTag = (type, status) => {
  if (type === 'biz') return getBizStatusType(status)
  if (type === 'prod') return getProdStatusType(status)
  return getBranchStatusType(status)
}

const statusDictType = (type) => {
  if (type === 'biz') return 'biz_req_status'
  if (type === 'prod') return 'prod_req_status'
  return 'branch_status'
}

const getRowClassName = ({ row }) => {
  return row.type === 'biz' ? 'biz-requirement-row' : ''
}

const buildTree = (list) => {
  return list.map(biz => {
    const bizNode = {
      id: `biz-${biz.id}`,
      type: 'biz',
      name: `${biz.reqCode}-${biz.reqName}`,
      owner: biz.owner,
      status: biz.status,
      statusName: biz.statusName,
      batchNo: biz.batchNo,
      batchDate: biz.batchDate,
      productionDate: biz.productionDate,
      children: (biz.prodRequirements || []).map(prod => {
        const prodNode = {
          id: `prod-${prod.id}`,
          type: 'prod',
          name: `${prod.prodReqCode}-${prod.prodReqName}`,
          developer: prod.developer,
          status: prod.status,
          statusName: prod.statusName,
          systemName: prod.systemName,
          children: []
        }
        if (prod.devBranch) {
          const devNode = {
            id: `dev-${prod.devBranch.id}`,
            type: 'dev',
            name: prod.devBranch.branchName,
            status: prod.devBranch.status,
            statusName: prod.devBranch.statusName,
            children: []
          }
          if (prod.devBranch.verifyBranch) {
            devNode.children.push({
              id: `verify-${prod.devBranch.verifyBranch.id}`,
              type: 'verify',
              name: prod.devBranch.verifyBranch.branchName,
              status: prod.devBranch.verifyBranch.status,
              statusName: prod.devBranch.verifyBranch.statusName
            })
          }
          prodNode.children.push(devNode)
        }
        return prodNode
      })
    }
    return bizNode
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/biz-requirement/overview/completed')
    treeData.value = buildTree(res.data || [])
  } catch (e) {
    ElMessage.error('加载需求全览失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleProductionDateChange = async (row) => {
  try {
    await request.put(`/biz-requirement/${row.id.replace('biz-', '')}/production-date`, {
      productionDate: row.productionDate
    })
    ElMessage.success('投产日期更新成功')
  } catch (e) {
    ElMessage.error('更新失败')
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
:deep(.biz-requirement-row td) {
  background-color: #eef2ff !important;
}
:deep(.el-table__body tr.biz-requirement-row:hover > td.el-table__cell) {
  background-color: #e0e7ff !important;
}
</style>
