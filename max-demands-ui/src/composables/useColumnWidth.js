import { ref } from 'vue'

/**
 * 表格列宽记忆
 * @param {string} storageKey localStorage 键名
 * @param {Object} defaultWidths 默认列宽 { prop: width }
 * @param {Function} propMapper 列 prop 到列宽 key 的映射，默认原样返回
 */
export function useColumnWidth(storageKey, defaultWidths, propMapper = (prop) => prop) {
  const colWidths = ref({ ...defaultWidths })

  const loadColWidths = () => {
    try {
      const saved = localStorage.getItem(storageKey)
      if (saved) {
        colWidths.value = { ...defaultWidths, ...JSON.parse(saved) }
      }
    } catch (e) {
      console.error('加载列宽失败', e)
    }
  }

  const handleHeaderDragend = (newWidth, oldWidth, column) => {
    const prop = propMapper(column.property)
    if (prop && colWidths.value[prop] !== undefined) {
      colWidths.value[prop] = newWidth
      localStorage.setItem(storageKey, JSON.stringify(colWidths.value))
    }
  }

  return {
    colWidths,
    loadColWidths,
    handleHeaderDragend
  }
}
