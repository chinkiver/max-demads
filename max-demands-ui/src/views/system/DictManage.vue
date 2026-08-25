<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">数据字典管理</h1>
      <p class="page-header-desc">管理系统字典项与分类</p>
    </div>
    <el-card class="page-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 10px;">
          <span style="color: #909399; font-size: 13px;">共 {{ totalItemCount }} 个字典子项</span>
          <div style="display: flex; gap: 8px;">
            <el-button
              type="warning"
              @click="handleRandomAll"
              :loading="randomAllLoading"
              v-if="authStore.userInfo?.permissions?.includes('sys:dict:edit')"
            >一键随机全部颜色</el-button>
            <el-button
              type="primary"
              class="page-action-btn"
              @click="handleAdd"
              v-if="authStore.userInfo?.permissions?.includes('sys:dict:add')"
            >+ 新增字典</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeType" @tab-change="onTabChange">
        <el-tab-pane
          v-for="group in dictGroups"
          :key="group.type"
          :label="typeNameMap[group.type] || group.type"
          :name="group.type"
        >
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <div style="color: #909399; font-size: 13px;">
              字段名：{{ group.type }} · 共 {{ group.items.length }} 项
            </div>
            <el-button
              size="small"
              type="warning"
              @click="handleRandomGroup(group.type)"
              :loading="randomGroupLoading === group.type"
              v-if="authStore.userInfo?.permissions?.includes('sys:dict:edit')"
            >随机分配颜色</el-button>
          </div>
          <el-table :data="group.items" border @header-dragend="handleHeaderDragend">
            <el-table-column prop="dictCode" label="字典编码" :width="colWidths.dictCode" />
            <el-table-column prop="dictName" label="字典名称" :width="colWidths.dictName" />
            <el-table-column prop="sortOrder" label="排序" :width="colWidths.sortOrder" />
            <el-table-column label="颜色" :width="colWidths.color">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <div
                    :style="{
                      width: '22px',
                      height: '22px',
                      borderRadius: '4px',
                      background: row.color || '#f4f4f5',
                      border: '1px solid #dcdfe6',
                      flexShrink: 0
                    }"
                  ></div>
                  <span style="color: #606266; font-size: 12px; font-family: monospace;">
                    {{ row.color || '-' }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" :resizable="false">
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
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker
            v-model="form.color"
            color-format="hex"
            :predefine="predefineColors"
          />
          <el-input
            v-model="form.color"
            placeholder="#RRGGBB"
            maxlength="7"
            style="width: 140px; margin-left: 8px;"
          />
          <span style="margin-left: 12px; color: #909399; font-size: 12px;">
            {{ form.color ? '当前：' + form.color : '未设置（默认灰色）' }}
          </span>
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
import request from '@/api/request'

const authStore = useAuthStore()
const dictStore = useDictStore()

const { colWidths, loadColWidths, handleHeaderDragend } = useColumnWidth(
  'dict-col-widths',
  {
    dictCode: 180,
    dictName: 240,
    sortOrder: 80,
    color: 180
  }
)
loadColWidths()

// 调色板预置（与后端 24 色保持一致，方便用户选择时心里有数）
const predefineColors = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
  '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#41b1e9',
  '#5b8ff9', '#5ad8a6', '#5d7092', '#f6bd16', '#e86452',
  '#6dc8ec', '#945fb9', '#ff9845', '#1e9493', '#ff99c3',
  '#3f4f5f', '#a1a0fc', '#2ec7c9', '#96dee8'
]

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
const randomGroupLoading = ref('')
const randomAllLoading = ref(false)

const form = ref({
  dictType: '',
  dictCode: '',
  dictName: '',
  sortOrder: 0,
  color: ''
})

const rules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
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

const totalItemCount = computed(() => {
  return dictGroups.value.reduce((sum, g) => sum + (g.items?.length || 0), 0)
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
    sortOrder: 0,
    color: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    dictType: row.dictType,
    dictCode: row.dictCode,
    dictName: row.dictName,
    sortOrder: row.sortOrder,
    color: row.color || ''
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
    await dictStore.loadDicts()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该字典项？', '提示', { type: 'warning' })
    await request.delete(`/dict/${row.dictType}/${row.dictCode}`)
    ElMessage.success('删除成功')
    await dictStore.loadDicts()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleRandomGroup = async (dictType) => {
  try {
    await ElMessageBox.confirm(
      `将为「${typeNameMap[dictType] || dictType}」分组下所有子项随机重新分配颜色（组内不重复），是否继续？`,
      '随机分配颜色',
      { type: 'warning' }
    )
    randomGroupLoading.value = dictType
    const res = await request.put(`/dict/${dictType}/random-colors`)
    const count = res.data || 0
    ElMessage.success(`已为 ${count} 个子项随机分配颜色`)
    await dictStore.loadDicts()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  } finally {
    randomGroupLoading.value = ''
  }
}

const handleRandomAll = async () => {
  try {
    await ElMessageBox.confirm(
      `将一次性重置所有字典分组下每个子项的颜色（每个分组内不重复），共影响 ${totalItemCount.value} 项。该操作不可撤销，是否继续？`,
      '一键随机全部颜色',
      { type: 'warning', confirmButtonText: '我已知晓风险，继续' }
    )
    randomAllLoading.value = true
    const res = await request.put('/dict/random-colors')
    const map = res.data || {}
    const total = Object.values(map).reduce((s, n) => s + n, 0)
    ElMessage.success(`已为 ${total} 个子项随机分配颜色（涉及 ${Object.keys(map).length} 个分组）`)
    await dictStore.loadDicts()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  } finally {
    randomAllLoading.value = false
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
