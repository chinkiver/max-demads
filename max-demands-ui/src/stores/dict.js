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
    return dicts.value[type] || []
  }

  return { dicts, loadDicts, getDict }
})