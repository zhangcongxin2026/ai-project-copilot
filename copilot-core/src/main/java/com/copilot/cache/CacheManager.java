package com.copilot.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语义缓存管理器
 * 使用哈希缓存相同的请求，减少 Token 消耗
 */
@Slf4j
@Component
public class CacheManager {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int maxCacheSize;
    private final long ttlMillis;

    public CacheManager() {
        this.maxCacheSize = 1000;
        this.ttlMillis = 5 * 60 * 1000; // 5 分钟
    }

    /**
     * 生成缓存键
     */
    public String generateCacheKey(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate cache key", e);
            return String.valueOf(content.hashCode());
        }
    }

    /**
     * 获取缓存
     */
    public String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (System.currentTimeMillis() - entry.getCreatedAt() > ttlMillis) {
            cache.remove(key);
            return null;
        }

        entry.setHitCount(entry.getHitCount() + 1);
        return entry.getContent();
    }

    /**
     * 设置缓存
     */
    public void put(String key, String content) {
        if (cache.size() >= maxCacheSize) {
            evictOldest();
        }

        cache.put(key, new CacheEntry(content, System.currentTimeMillis()));
        log.debug("Cache put: key={}, size={}", key, cache.size());
    }

    /**
     * 检查是否命中缓存
     */
    public boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * 清除缓存
     */
    public void clear() {
        cache.clear();
        log.info("Cache cleared");
    }

    /**
     * 获取缓存统计
     */
    public CacheStats getStats() {
        long totalHits = cache.values().stream().mapToLong(CacheEntry::getHitCount).sum();
        return new CacheStats(cache.size(), totalHits);
    }

    /**
     * 淘汰最旧的条目
     */
    private void evictOldest() {
        // 简单策略：随机淘汰 10%
        int toEvict = maxCacheSize / 10;
        cache.keySet().stream().limit(toEvict).forEach(cache::remove);
        log.debug("Evicted {} cache entries", toEvict);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 缓存条目
     */
    static class CacheEntry {
        private final String content;
        private final long createdAt;
        private long hitCount;

        public CacheEntry(String content, long createdAt) {
            this.content = content;
            this.createdAt = createdAt;
            this.hitCount = 0;
        }

        public String getContent() {
            return content;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public long getHitCount() {
            return hitCount;
        }

        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }
    }

    /**
     * 缓存统计
     */
    public record CacheStats(long size, long totalHits) {}
}
