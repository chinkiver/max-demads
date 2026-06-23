<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #666;">业务需求总数</div>
          <div style="font-size: 28px; font-weight: bold; margin-top: 10px;">{{ stats.bizReqCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #666;">产品需求总数</div>
          <div style="font-size: 28px; font-weight: bold; margin-top: 10px;">{{ stats.prodReqCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #666;">开发分支总数</div>
          <div style="font-size: 28px; font-weight: bold; margin-top: 10px;">{{ stats.devBranchCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="font-size: 14px; color: #666;">投产批次总数</div>
          <div style="font-size: 28px; font-weight: bold; margin-top: 10px;">{{ stats.batchCount }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import request from '@/api/request'

const stats = reactive({
  bizReqCount: 0,
  prodReqCount: 0,
  devBranchCount: 0,
  batchCount: 0
})

onMounted(async () => {
  try {
    const [bizRes, prodRes, devRes, batchRes] = await Promise.all([
      request.get('/biz-requirement?current=1&size=1'),
      request.get('/prod-requirement?current=1&size=1'),
      request.get('/dev-branch?current=1&size=1'),
      request.get('/batch?current=1&size=1')
    ])
    stats.bizReqCount = bizRes.data.total || 0
    stats.prodReqCount = prodRes.data.total || 0
    stats.devBranchCount = devRes.data.total || 0
    stats.batchCount = batchRes.data.total || 0
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
})
</script>
