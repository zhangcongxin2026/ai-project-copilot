package com.copilot.cache;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存管理器测试
 */
class CacheManagerTest {

    private final CacheManager cacheManager = new CacheManager();

    @Test
    void testCachePutAndGet() {
        String key = cacheManager.generateCacheKey("test content");
        cacheManager.put(key, "test content");

        String cached = cacheManager.get(key);
        assertEquals("test content", cached);
    }

    @Test
    void testCacheMiss() {
        String result = cacheManager.get("non-existent-key");
        assertNull(result);
    }

    @Test
    void testCacheContains() {
        String key = cacheManager.generateCacheKey("test");
        cacheManager.put(key, "test");

        assertTrue(cacheManager.contains(key));
        assertFalse(cacheManager.contains("non-existent"));
    }

    @Test
    void testCacheClear() {
        String key = cacheManager.generateCacheKey("test");
        cacheManager.put(key, "test");
        cacheManager.clear();

        assertNull(cacheManager.get(key));
    }

    @Test
    void testCacheStats() {
        CacheManager.CacheStats stats = cacheManager.getStats();
        assertNotNull(stats);
    }
}
