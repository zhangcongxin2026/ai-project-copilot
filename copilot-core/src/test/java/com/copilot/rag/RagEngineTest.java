package com.copilot.rag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * RAG 引擎测试
 */
class RagEngineTest {

    private final EmbeddingService embeddingService = new LocalEmbeddingService();
    private final RagEngine ragEngine = new RagEngine(embeddingService);

    @Test
    void testAddDocument() {
        ragEngine.addDocument("doc-1", "这是一段测试文本", Map.of("type", "test"));
        // 如果没抛异常就是成功
        assertTrue(true);
    }

    @Test
    void testSearch() {
        // 添加测试文档
        ragEngine.addDocument("doc-1", "Java 是一种编程语言", Map.of("category", "programming"));
        ragEngine.addDocument("doc-2", "Python 是一种脚本语言", Map.of("category", "programming"));
        ragEngine.addDocument("doc-3", "今天天气真好", Map.of("category", "daily"));

        List<RagEngine.SearchResult> results = ragEngine.search("编程语言", 2);

        assertNotNull(results);
        assertEquals(2, results.size());
        // 最相关的应该是 Java 和 Python
        assertTrue(results.get(0).content().contains("Java") || results.get(0).content().contains("Python"));
    }

    @Test
    void testDeleteDocument() {
        ragEngine.addDocument("doc-to-delete", "要删除的文档", Map.of());
        ragEngine.deleteDocument("doc-to-delete");

        List<RagEngine.SearchResult> results = ragEngine.search("要删除的文档", 10);
        // 搜索不到已删除的文档
        assertTrue(results.stream().noneMatch(r -> r.id().equals("doc-to-delete")));
    }

    @Test
    void testClear() {
        ragEngine.addDocument("doc-1", "文档 1", Map.of());
        ragEngine.addDocument("doc-2", "文档 2", Map.of());
        ragEngine.clear();

        List<RagEngine.SearchResult> results = ragEngine.search("测试", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testCosineSimilarity() {
        // 测试相同向量相似度为 1
        double[] vec1 = {1.0, 0.0, 0.0};
        double[] vec2 = {1.0, 0.0, 0.0};

        // 这里无法直接测试私有方法，但可以通过搜索结果间接验证
        ragEngine.addDocument("test", "test", Map.of());
        List<RagEngine.SearchResult> results = ragEngine.search("test", 1);

        assertEquals(1, results.size());
        assertEquals(1.0, results.get(0).similarity(), 0.01); // 完全匹配
    }
}
