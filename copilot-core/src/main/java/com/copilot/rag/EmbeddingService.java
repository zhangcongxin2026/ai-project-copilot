package com.copilot.rag;

/**
 * 嵌入服务接口
 */
public interface EmbeddingService {
    /**
     * 将文本转换为向量
     */
    double[] embed(String text);

    /**
     * 获取向量维度
     */
    int getDimension();
}
