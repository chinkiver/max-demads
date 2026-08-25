<template>
  <el-tag
    v-if="dict"
    :color="dict.color || undefined"
    disable-transitions
    effect="dark"
    :class="{ 'dict-tag--no-color': !dict.color }"
  >
    {{ dict.dictName }}
  </el-tag>
  <span v-else class="dict-tag-fallback">{{ code || '-' }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { useDictStore } from '@/stores/dict'

const props = defineProps({
  /** 字典类型，如 biz_req_status */
  type: { type: String, required: true },
  /** 字典编码 */
  code: { type: String, default: '' }
})

const dictStore = useDictStore()
const dict = computed(() => {
  if (!props.code) return null
  return dictStore.getDict(props.type).find(d => d.dictCode === props.code) || null
})
</script>

<style scoped>
.dict-tag-fallback {
  color: #909399;
  font-size: 12px;
}
.dict-tag--no-color {
  background-color: #f4f4f5 !important;
  color: #909399 !important;
  border-color: #e9e9eb !important;
}
</style>
