<template>
  <div class="home-view">
    <el-card class="welcome-card">
      <h2>欢迎使用 AI Project Copilot</h2>
      <p class="description">
        基于多 Agent 协同的项目管理助手，可以帮您从需求解析到代码生成一站式完成
      </p>

      <el-divider />

      <div class="features">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover" class="feature-card">
              <el-icon :size="40" color="#409EFF"><Document /></el-icon>
              <h4>需求解析</h4>
              <p>自动分析用户需求，提取关键信息</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="feature-card">
              <el-icon :size="40" color="#67C23A"><List /></el-icon>
              <h4>任务分解</h4>
              <p>将需求拆解为可执行的具体任务</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="feature-card">
              <el-icon :size="40" color="#E6A23C"><Monitor /></el-icon>
              <h4>代码生成</h4>
              <p>根据任务描述生成高质量代码</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="feature-card">
              <el-icon :size="40" color="#F56C6C"><Finished /></el-icon>
              <h4>测试生成</h4>
              <p>自动生成全面的测试用例</p>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-divider />

      <div class="actions">
        <el-button type="primary" size="large" @click="goToWorkflow">
          开始使用
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Document, List, Monitor, Finished } from '@element-plus/icons-vue'

const router = useRouter()

const goToWorkflow = () => {
  router.push('/workflow')
}
</script>

<style scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  text-align: center;
}

.welcome-card h2 {
  color: #303133;
  margin-bottom: 10px;
}

.description {
  color: #606266;
  font-size: 16px;
}

.features {
  margin: 30px 0;
}

.feature-card {
  text-align: center;
  padding: 20px;
}

.feature-card h4 {
  margin: 15px 0 10px;
  color: #303133;
}

.feature-card p {
  color: #909399;
  font-size: 14px;
}

.actions {
  margin-top: 30px;
}
</style>
