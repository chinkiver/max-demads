<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">仪表盘</h1>
      <p class="page-header-desc">系统数据概览与统计指标</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '24px' }">
          <div class="stat-header">
            <span class="stat-label">业务需求总数</span>
            <div class="stat-icon purple">
              <el-icon size="22"><Document /></el-icon>
            </div>
          </div>
          <div class="stat-value">{{ stats.bizReqCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '24px' }">
          <div class="stat-header">
            <span class="stat-label">产品需求总数</span>
            <div class="stat-icon green">
              <el-icon size="22"><DocumentCopy /></el-icon>
            </div>
          </div>
          <div class="stat-value">{{ stats.prodReqCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '24px' }">
          <div class="stat-header">
            <span class="stat-label">开发分支总数</span>
            <div class="stat-icon yellow">
              <el-icon size="22"><Share /></el-icon>
            </div>
          </div>
          <div class="stat-value">{{ stats.devBranchCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '24px' }">
          <div class="stat-header">
            <span class="stat-label">投产批次总数</span>
            <div class="stat-icon red">
              <el-icon size="22"><Calendar /></el-icon>
            </div>
          </div>
          <div class="stat-value">{{ stats.batchCount }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import request from '@/api/request'
import { Document, DocumentCopy, Share, Calendar } from '@element-plus/icons-vue'

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

<style scoped>
.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #6366f1;
}

.stat-card:nth-child(2)::after {
  background: #10b981;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.purple {
  background: #eef2ff;
  color: #6366f1;
}

.stat-icon.green {
  background: #ecfdf5;
  color: #10b981;
}

.stat-icon.yellow {
  background: #fffbeb;
  color: #f59e0b;
}

.stat-icon.red {
  background: #fef2f2;
  color: #ef4444;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
}
</style>
