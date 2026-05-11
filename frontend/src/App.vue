<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>AI Project Copilot</h1>
        <el-menu
          :default-active="activeRoute"
          mode="horizontal"
          :router="true"
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#ffd04b"
          class="nav-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/workflow">工作流</el-menu-item>
          <el-menu-item index="/history">历史记录</el-menu-item>
          <el-menu-item index="/rag">知识库</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeRoute = computed(() => route.path)
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
}

.el-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.el-header h1 {
  margin: 0;
  font-size: 20px;
  white-space: nowrap;
  margin-right: 40px;
}

.nav-menu {
  border-bottom: none !important;
  flex: 1;
}

.nav-menu .el-menu-item {
  color: #fff !important;
  border-bottom: none !important;
}

.nav-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
}

.nav-menu .el-menu-item.is-active {
  color: #ffd04b !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100px - 60px);
}
</style>
