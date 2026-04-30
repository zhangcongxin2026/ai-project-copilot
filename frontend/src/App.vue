<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>AI Project Copilot</h1>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
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
  justify-content: center;
}

.el-header h1 {
  margin: 0;
  font-size: 24px;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
</style>
