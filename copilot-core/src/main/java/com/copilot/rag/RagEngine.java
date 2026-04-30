package com.copilot.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RAG 引擎 - 检索增强生成
 * 使用 ChromaDB 进行向量存储和检索
 */
@Slf4j
@Component
public class RagEngine {

    // 简化的内存实现，实际应该使用 ChromaDB
    private final Map<String, VectorDocument> documents = new ConcurrentHashMap<>();
    private final EmbeddingService embeddingService;

    public RagEngine(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * 添加文档
     */
    public void addDocument(String id, String content, Map<String, String> metadata) {
        try {
            double[] embedding = embeddingService.embed(content);
            documents.put(id, new VectorDocument(id, content, embedding, metadata));
            log.info("Added document: id={}, contentLength={}", id, content.length());
        } catch (Exception e) {
            log.error("Failed to add document: {}", id, e);
        }
    }

    /**
     * 检索相关文档
     */
    public List<SearchResult> search(String query, int maxResults) {
        try {
            double[] queryEmbedding = embeddingService.embed(query);

            List<SearchResult> results = new ArrayList<>();
            for (VectorDocument doc : documents.values()) {
                double similarity = cosineSimilarity(queryEmbedding, doc.getEmbedding());
                results.add(new SearchResult(doc.getId(), doc.getContent(), doc.getMetadata(), similarity));
            }

            // 按相似度排序
            results.sort(Comparator.comparingDouble(SearchResult::getSimilarity).reversed());

            return results.subList(0, Math.min(maxResults, results.size()));
        } catch (Exception e) {
            log.error("Search failed for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String id) {
        documents.remove(id);
        log.info("Deleted document: {}", id);
    }

    /**
     * 清除所有文档
     */
    public void clear() {
        documents.clear();
        log.info("Cleared all documents");
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(double[] a, double[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 向量文档
     */
    public record VectorDocument(String id, String content, double[] embedding, Map<String, String> metadata) {}

    /**
     * 搜索结果
     */
    public record SearchResult(String id, String content, Map<String, String> metadata, double similarity) {}
}
