<template>
  <div class="quick-select">
    <el-select
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      :placeholder="placeholder"
      clearable
      filterable
      class="quick-select-input"
    >
      <el-option
        v-for="item in list"
        :key="item.id"
        :label="item[labelKey]"
        :value="item.id"
      />
    </el-select>
    <el-button type="primary" @click="openCreateDialog" style="margin-left: 8px;">新建</el-button>

    <el-dialog v-model="dialogVisible" title="新建" :width="dialogWidth" append-to-body>
      <el-form :model="createForm" ref="createFormRef" :rules="effectiveCreateRules" label-width="120px" class="quick-create-form">
        <el-form-item label="名称" prop="name" v-if="createFields.includes('name')">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="编码" prop="code" v-if="createFields.includes('code')">
          <el-input v-model="createForm.code" />
        </el-form-item>
        <slot name="create-form" :form="createForm" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const props = defineProps({
  modelValue: [String, Number],
  api: { type: String, required: true },
  createApi: { type: String, required: true },
  labelKey: { type: String, default: 'name' },
  placeholder: { type: String, default: '请选择' },
  createFields: { type: Array, default: () => ['name'] },
  createExtra: { type: Object, default: () => ({}) },
  createRules: { type: Object, default: () => null },
  dialogWidth: { type: String, default: '500px' }
})

const emit = defineEmits(['update:modelValue'])

const list = ref([])
const dialogVisible = ref(false)
const creating = ref(false)
const createFormRef = ref()
const createForm = ref({ name: '', code: '' })

const defaultCreateRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }]
}
const effectiveCreateRules = computed(() => props.createRules || defaultCreateRules)

const fetchList = async () => {
  const res = await request.get(props.api)
  const data = res.data || []
  list.value = data.records || data || []
}

const openCreateDialog = () => {
  createForm.value = { name: '', code: '' }
  dialogVisible.value = true
}

const handleCreate = async () => {
  await createFormRef.value.validate()
  creating.value = true
  try {
    const payload = { ...createForm.value, ...props.createExtra }
    const res = await request.post(props.createApi, payload)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    await fetchList()
    emit('update:modelValue', res.data?.id || list.value[list.value.length - 1]?.id)
  } finally {
    creating.value = false
  }
}

onMounted(fetchList)
watch(() => props.api, fetchList)
</script>

<style scoped>
.quick-select {
  display: flex;
  align-items: center;
  width: 100%;
}
.quick-select .quick-select-input {
  flex: 1;
  min-width: 200px;
}
.quick-select .el-button {
  flex-shrink: 0;
  margin-left: 8px;
}
.quick-create-form :deep(.el-form-item__label) {
  white-space: nowrap;
}
</style>
