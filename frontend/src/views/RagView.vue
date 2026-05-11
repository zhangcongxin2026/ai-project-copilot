<template>
  <div class="rag-view">
    <!-- 添加文档 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span>添加文档</span>
      </template>
      <el-form :model="newDoc" label-width="80px">
        <el-form-item label="文档 ID">
          <el-input v-model="newDoc.id" placeholder="留空则自动生成" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="newDoc.content" type="textarea" :rows="4" placeholder="输入文档内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addDocument" :loading="adding">添加到知识库</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 搜索知识库 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span>搜索知识库</span>
      </template>
      <el-input
        v-model="searchQuery"
        placeholder="输入搜索内容"
        @keyup.enter="searchDocuments"
      >
        <template #append>
          <el-button @click="searchDocuments" :loading="searching">搜索</el-button>
        </template>
      </el-input>

      <div v-if="searchResults.length > 0" style="margin-top: 20px;">
        <h4>搜索结果（{{ searchResults.length }} 条）</h4>
        <el-table :data="searchResults" style="width: 100%">
          <el-table-column prop="id" label="文档 ID" width="200" />
          <el-table-column prop="content" label="内容" min-width="400" show-overflow-tooltip />
          <el-table-column prop="similarity" label="相似度" width="120">
            <template #default="{ row }">
              <el-tag :type="row.similarity > 0.8 ? 'success' : row.similarity > 0.5 ? 'warning' : 'info'">
                {{ (row.similarity * 100).toFixed(1) }}%
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else-if="searched" description="未找到相关文档" />
    </el-card>

    <!-- 管理操作 -->
    <el-card>
      <template #header>
        <span>知识库管理</span>
      </template>
      <el-space>
        <el-button type="danger" @click="clearAll" :loading="clearing">清空知识库</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { copilotApi, type SearchResult } from '@/api/copilot'

const newDoc = ref({ id: '', content: '' })
const adding = ref(false)

const searchQuery = ref('')
const searchResults = ref<SearchResult[]>([])
const searching = ref(false)
const searched = ref(false)

const clearing = ref(false)

const addDocument = async () => {
  if (!newDoc.value.content.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }
  adding.value = true
  try {
    await copilotApi.addDocument({
      id: newDoc.value.id || undefined,
      content: newDoc.value.content,
    })
    ElMessage.success('文档添加成功')
    newDoc.value = { id: '', content: '' }
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    adding.value = false
  }
}

const searchDocuments = async () => {
  if (!searchQuery.value.trim()) return
  searching.value = true
  searched.value = false
  try {
    searchResults.value = await copilotApi.searchDocuments(searchQuery.value)
    searched.value = true
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空整个知识库吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    clearing.value = true
    await copilotApi.clearDocuments()
    searchResults.value = []
    ElMessage.success('知识库已清空')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败')
    }
  } finally {
    clearing.value = false
  }
}
</script>

<style scoped>
.rag-view {
  max-width: 1000px;
  margin: 0 auto;
}
</style>
