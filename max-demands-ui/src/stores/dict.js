import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api/request'

export const useDictStore = defineStore('dict', () => {
  const dicts = ref({})

  const loadDicts = async () => {
    const res = await request.get('/dict/types')
    dicts.value = res.data
  }

  const getDict = (type) => {
    const list = dicts.value[type] || []
    return [...list].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
  }

  return { dicts, loadDicts, getDict }
})