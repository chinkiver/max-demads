<template>
  <div>
    <div class="page-header">
      <h1 class="page-header-title">Analytics Dashboard</h1>
      <p class="page-header-desc">Business performance & growth metrics</p>
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
          <div class="stat-trend up">+12.5% <span>vs last month</span></div>
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
          <div class="stat-trend up">+8.2% <span>vs last month</span></div>
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
          <div class="stat-trend up">+2.4% <span>vs last month</span></div>
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
          <div class="stat-trend down">-1.2% <span>vs last month</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px;">
      <el-col :span="16">
        <el-card :body-style="{ padding: '24px' }">
          <div class="section-title">Recent Activity</div>
          <div class="section-desc">Latest actions across the system</div>
          <div class="activity-list">
            <div class="activity-item" v-for="(item, index) in activities" :key="index">
              <div class="activity-dot"></div>
              <div class="activity-content">
                <div class="activity-title">{{ item.title }}</div>
                <div class="activity-desc">{{ item.desc }}</div>
              </div>
              <div class="activity-time">{{ item.time }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card :body-style="{ padding: '24px' }">
          <div class="section-title">Quick Stats</div>
          <div class="section-desc">System overview</div>
          <div class="quick-stats">
            <div class="quick-stat-item">
              <div class="quick-stat-label">Active Users</div>
              <div class="quick-stat-value">83%</div>
            </div>
            <el-progress :percentage="83" :show-text="false" color="#6366f1" />
            <div class="quick-stat-item">
              <div class="quick-stat-label">Role Coverage</div>
              <div class="quick-stat-value">3/4</div>
            </div>
            <el-progress :percentage="75" :show-text="false" color="#10b981" />
            <div class="quick-stat-item">
              <div class="quick-stat-label">Permission Utilization</div>
              <div class="quick-stat-value">14</div>
            </div>
            <el-progress :percentage="70" :show-text="false" color="#f59e0b" />
          </div>
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

const activities = [
  { title: 'New user created', desc: 'Admin was added to the system', time: '2 min ago' },
  { title: 'Role updated', desc: 'Editor role permissions modified', time: '15 min ago' },
  { title: 'Permission added', desc: 'New "Export Data" permission created', time: '1 hour ago' },
  { title: 'User deactivated', desc: 'Viewer account marked inactive', time: '3 hours ago' }
]

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

.stat-trend {
  font-size: 13px;
  font-weight: 600;
}

.stat-trend.up {
  color: #10b981;
}

.stat-trend.down {
  color: #ef4444;
}

.stat-trend span {
  color: #9ca3af;
  font-weight: 400;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.section-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 24px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #6366f1;
  margin-top: 6px;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.activity-desc {
  font-size: 13px;
  color: #6b7280;
}

.activity-time {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

.quick-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.quick-stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.quick-stat-label {
  font-size: 14px;
  color: #4b5563;
}

.quick-stat-value {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}
</style>
