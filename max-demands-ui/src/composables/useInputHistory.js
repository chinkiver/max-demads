import { ref } from 'vue'

/**
 * 输入历史记忆（标签点击回填）
 * @param {string} prefix localStorage 键前缀
 * @param {Object} fields 字段配置 { key: { limit, getLabel } }
 *   - limit: 最多保存条数，默认 5
 *   - getLabel: (value, form) => string，用于将值转换为标签显示文本（如 select 的 code-name）
 */
export function useInputHistory(prefix, fields) {
  const historyMap = ref({})

  Object.keys(fields).forEach(key => {
    historyMap.value[key] = []
  })

  const storageKey = (key) => `${prefix}-history-${key}`

  const loadHistory = () => {
    Object.keys(fields).forEach(key => {
      try {
        const saved = localStorage.getItem(storageKey(key))
        if (saved) {
          historyMap.value[key] = JSON.parse(saved)
        }
      } catch (e) {
        console.error('加载历史记录失败', e)
      }
    })
  }

  const saveHistory = (form) => {
    Object.keys(fields).forEach(key => {
      const config = fields[key] || {}
      const limit = config.limit || 5
      const rawValue = form[key]
      if (rawValue === null || rawValue === undefined || rawValue === '') return

      const value = rawValue
      const label = config.getLabel ? config.getLabel(rawValue, form) : String(rawValue)

      const list = historyMap.value[key] || []
      const newList = [{ value, label }, ...list.filter(item => item.value !== value)].slice(0, limit)
      historyMap.value[key] = newList
      localStorage.setItem(storageKey(key), JSON.stringify(newList))
    })
  }

  return {
    historyMap,
    loadHistory,
    saveHistory
  }
}
