package com.copilot.rag;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于 ONNX 的本地嵌入服务实现
 */
@Slf4j
@Component
public class LocalEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final int dimension;

    public LocalEmbeddingService() {
        // 使用轻量级的 all-MiniLM-L6-v2 模型
        this.embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        this.dimension = 384; // 该模型的向量维度
        log.info("Initialized local embedding service, dimension={}", dimension);
    }

    @Override
    public double[] embed(String text) {
        try {
            return embeddingModel.embed(text).content().vector();
        } catch (Exception e) {
            log.error("Embedding failed for text: {}", text.substring(0, Math.min(50, text.length())), e);
            // 返回零向量作为降级处理
            return new double[dimension];
        }
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}
