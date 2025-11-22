package com.sayrain.medicalbooking.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 缓存监控工具类
 * 提供缓存性能监控、统计和清理功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheMonitor {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 定时监控缓存状态
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void monitorCacheStatus() {
        try {
            Map<String, Object> cacheStats = new HashMap<>();
            
            // 获取所有缓存名称
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    // 获取缓存统计信息
                    Map<String, Object> stats = getCacheStats(cacheName);
                    cacheStats.put(cacheName, stats);
                }
            });
            
            log.info("缓存监控统计: {}", cacheStats);
            
        } catch (Exception e) {
            log.error("缓存监控异常", e);
        }
    }

    /**
     * 获取特定缓存的统计信息
     */
    public Map<String, Object> getCacheStats(String cacheName) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取Redis键数量
            String pattern = "medical:cache:" + cacheName + ":*";
            Long keyCount = redisTemplate.countExistingKeys(redisTemplate.keys(pattern));
            stats.put("keyCount", keyCount != null ? keyCount : 0);
            
            // 获取缓存内存使用情况
            Properties info = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Properties>) connection -> {
                return connection.info("memory");
            });
            
            if (info != null) {
                // 解析Redis内存信息
                String usedMemory = info.getProperty("used_memory_human");
                if (usedMemory != null) {
                    stats.put("usedMemory", usedMemory);
                }
            }
            
            // 获取缓存命中率（需要集成Micrometer等监控工具）
            stats.put("hitRate", "N/A (需要集成监控工具)");
            
        } catch (Exception e) {
            log.warn("获取缓存 {} 统计信息失败: {}", cacheName, e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 清理特定缓存
     */
    public void clearCache(String cacheName) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("已清理缓存: {}", cacheName);
            }
        } catch (Exception e) {
            log.error("清理缓存 {} 失败", cacheName, e);
        }
    }

    /**
     * 清理所有缓存
     */
    public void clearAllCaches() {
        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
            log.info("已清理所有缓存");
        } catch (Exception e) {
            log.error("清理所有缓存失败", e);
        }
    }

    /**
     * 预热缓存
     */
    public void warmupCache(String cacheName, Map<String, Object> data) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                data.forEach((key, value) -> {
                    cache.put(key, value);
                });
                log.info("已预热缓存: {}, 数据量: {}", cacheName, data.size());
            }
        } catch (Exception e) {
            log.error("预热缓存 {} 失败", cacheName, e);
        }
    }

    /**
     * 检查缓存健康状态
     */
    public Map<String, Object> checkCacheHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查Redis连接
            String pong = redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<String>) connection -> {
                return connection.ping();
            });
            health.put("redisConnection", "pong".equals(pong) ? "UP" : "DOWN");
            
            // 检查缓存管理器状态
            health.put("cacheManagerStatus", cacheManager != null ? "UP" : "DOWN");
            health.put("cacheCount", cacheManager.getCacheNames().size());
            
            // 检查各缓存状态
            Map<String, String> cacheStatus = new HashMap<>();
            cacheManager.getCacheNames().forEach(name -> {
                try {
                    var cache = cacheManager.getCache(name);
                    cacheStatus.put(name, cache != null ? "UP" : "DOWN");
                } catch (Exception e) {
                    cacheStatus.put(name, "ERROR: " + e.getMessage());
                }
            });
            health.put("cacheStatus", cacheStatus);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    /**
     * 获取缓存键的TTL（生存时间）
     */
    public Long getKeyTTL(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("获取键 {} TTL失败: {}", key, e.getMessage());
            return -1L;
        }
    }

    /**
     * 设置缓存键的TTL
     */
    public boolean setKeyTTL(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.warn("设置键 {} TTL失败: {}", key, e.getMessage());
            return false;
        }
    }
}