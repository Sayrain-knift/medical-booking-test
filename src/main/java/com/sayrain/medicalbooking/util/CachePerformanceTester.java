package com.sayrain.medicalbooking.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存性能测试工具
 * 用于测试缓存性能和并发访问
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CachePerformanceTester implements CommandLineRunner {

    private final CacheManager cacheManager;
    private final CacheMonitor cacheMonitor;

    /**
     * 应用启动后自动执行缓存性能测试（可选）
     */
    @Override
    public void run(String... args) throws Exception {
        // 可以通过配置控制是否启用自动测试
        // performCachePerformanceTest();
    }

    /**
     * 执行缓存性能测试
     */
    public Map<String, Object> performCachePerformanceTest() {
        Map<String, Object> results = new HashMap<>();
        
        try {
            log.info("开始缓存性能测试...");
            
            // 测试不同缓存类型的性能
            testBasicCacheOperations(results);
            testConcurrentAccess(results);
            testCacheEviction(results);
            
            log.info("缓存性能测试完成: {}", results);
            
        } catch (Exception e) {
            log.error("缓存性能测试失败", e);
            results.put("error", e.getMessage());
        }
        
        return results;
    }

    /**
     * 测试基本缓存操作性能
     */
    private void testBasicCacheOperations(Map<String, Object> results) {
        Map<String, Object> basicResults = new HashMap<>();
        
        try {
            var cache = cacheManager.getCache("testCache");
            if (cache == null) {
                basicResults.put("error", "测试缓存不存在");
                results.put("basicOperations", basicResults);
                return;
            }
            
            // 测试写入性能
            AtomicLong writeTime = new AtomicLong(0);
            int writeCount = 1000;
            
            long writeStart = System.nanoTime();
            for (int i = 0; i < writeCount; i++) {
                cache.put("key" + i, "value" + i);
            }
            long writeEnd = System.nanoTime();
            
            writeTime.set(writeEnd - writeStart);
            basicResults.put("writeCount", writeCount);
            basicResults.put("writeTimeMs", writeTime.get() / 1_000_000);
            basicResults.put("writeOpsPerSecond", writeCount * 1_000_000_000L / writeTime.get());
            
            // 测试读取性能
            AtomicLong readTime = new AtomicLong(0);
            int readCount = 1000;
            
            long readStart = System.nanoTime();
            for (int i = 0; i < readCount; i++) {
                cache.get("key" + i);
            }
            long readEnd = System.nanoTime();
            
            readTime.set(readEnd - readStart);
            basicResults.put("readCount", readCount);
            basicResults.put("readTimeMs", readTime.get() / 1_000_000);
            basicResults.put("readOpsPerSecond", readCount * 1_000_000_000L / readTime.get());
            
            // 测试命中率
            int hitTests = 1000;
            AtomicInteger hits = new AtomicInteger(0);
            
            for (int i = 0; i < hitTests; i++) {
                Object value = cache.get("key" + (i % writeCount));
                if (value != null) {
                    hits.incrementAndGet();
                }
            }
            
            basicResults.put("hitTests", hitTests);
            basicResults.put("hits", hits.get());
            basicResults.put("hitRate", (double) hits.get() / hitTests * 100);
            
        } catch (Exception e) {
            basicResults.put("error", e.getMessage());
        }
        
        results.put("basicOperations", basicResults);
    }

    /**
     * 测试并发访问性能
     */
    private void testConcurrentAccess(Map<String, Object> results) {
        Map<String, Object> concurrentResults = new HashMap<>();
        
        try {
            var cache = cacheManager.getCache("testCache");
            if (cache == null) {
                concurrentResults.put("error", "测试缓存不存在");
                results.put("concurrentAccess", concurrentResults);
                return;
            }
            
            int threadCount = 10;
            int operationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            
            AtomicInteger totalOperations = new AtomicInteger(0);
            AtomicLong totalTime = new AtomicLong(0);
            
            long testStart = System.nanoTime();
            
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    long threadStart = System.nanoTime();
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        String key = "concurrent" + threadId + "_" + i;
                        
                        // 写入操作
                        cache.put(key, "value" + threadId + "_" + i);
                        
                        // 读取操作
                        cache.get(key);
                        
                        totalOperations.incrementAndGet();
                    }
                    
                    long threadEnd = System.nanoTime();
                    totalTime.addAndGet(threadEnd - threadStart);
                });
            }
            
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
            
            long testEnd = System.nanoTime();
            
            concurrentResults.put("threadCount", threadCount);
            concurrentResults.put("operationsPerThread", operationsPerThread);
            concurrentResults.put("totalOperations", totalOperations.get());
            concurrentResults.put("totalTimeMs", (testEnd - testStart) / 1_000_000);
            concurrentResults.put("avgThreadTimeMs", totalTime.get() / threadCount / 1_000_000);
            concurrentResults.put("throughputOpsPerSecond", totalOperations.get() * 1_000_000_000L / (testEnd - testStart));
            
        } catch (Exception e) {
            concurrentResults.put("error", e.getMessage());
        }
        
        results.put("concurrentAccess", concurrentResults);
    }

    /**
     * 测试缓存淘汰策略
     */
    private void testCacheEviction(Map<String, Object> results) {
        Map<String, Object> evictionResults = new HashMap<>();
        
        try {
            var cache = cacheManager.getCache("testEvictionCache");
            if (cache == null) {
                evictionResults.put("error", "测试缓存不存在");
                results.put("cacheEviction", evictionResults);
                return;
            }
            
            // 清空缓存
            cache.clear();
            
            // 写入大量数据触发淘汰
            int writeCount = 2000;
            int survivingCount = 0;
            
            for (int i = 0; i < writeCount; i++) {
                cache.put("evictionKey" + i, "value" + i);
            }
            
            // 检查有多少数据存活
            for (int i = 0; i < writeCount; i++) {
                if (cache.get("evictionKey" + i) != null) {
                    survivingCount++;
                }
            }
            
            evictionResults.put("writeCount", writeCount);
            evictionResults.put("survivingCount", survivingCount);
            evictionResults.put("evictionRate", (double) (writeCount - survivingCount) / writeCount * 100);
            
        } catch (Exception e) {
            evictionResults.put("error", e.getMessage());
        }
        
        results.put("cacheEviction", evictionResults);
    }

    /**
     * 生成性能测试报告
     */
    public String generatePerformanceReport(Map<String, Object> results) {
        StringBuilder report = new StringBuilder();
        report.append("=== 缓存性能测试报告 ===\n\n");
        
        // 基本操作测试结果
        if (results.containsKey("basicOperations")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> basic = (Map<String, Object>) results.get("basicOperations");
            
            report.append("1. 基本操作性能测试:\n");
            report.append(String.format("   - 写入操作: %d 次, 耗时 %d ms, 吞吐量 %d ops/s\n",
                basic.get("writeCount"), basic.get("writeTimeMs"), basic.get("writeOpsPerSecond")));
            report.append(String.format("   - 读取操作: %d 次, 耗时 %d ms, 吞吐量 %d ops/s\n",
                basic.get("readCount"), basic.get("readTimeMs"), basic.get("readOpsPerSecond")));
            report.append(String.format("   - 缓存命中率: %.2f%%\n\n", basic.get("hitRate")));
        }
        
        // 并发访问测试结果
        if (results.containsKey("concurrentAccess")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> concurrent = (Map<String, Object>) results.get("concurrentAccess");
            
            report.append("2. 并发访问性能测试:\n");
            report.append(String.format("   - 线程数: %d, 每线程操作数: %d\n",
                concurrent.get("threadCount"), concurrent.get("operationsPerThread")));
            report.append(String.format("   - 总操作数: %d, 总耗时: %d ms\n",
                concurrent.get("totalOperations"), concurrent.get("totalTimeMs")));
            report.append(String.format("   - 并发吞吐量: %d ops/s\n\n", concurrent.get("throughputOpsPerSecond")));
        }
        
        // 缓存淘汰测试结果
        if (results.containsKey("cacheEviction")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> eviction = (Map<String, Object>) results.get("cacheEviction");
            
            report.append("3. 缓存淘汰策略测试:\n");
            report.append(String.format("   - 写入数据量: %d, 存活数据量: %d\n",
                eviction.get("writeCount"), eviction.get("survivingCount")));
            report.append(String.format("   - 淘汰率: %.2f%%\n\n", eviction.get("evictionRate")));
        }
        
        // 缓存健康状态
        Map<String, Object> health = cacheMonitor.checkCacheHealth();
        report.append("4. 缓存健康状态:\n");
        report.append(String.format("   - Redis连接: %s\n", health.get("redisConnection")));
        report.append(String.format("   - 缓存管理器: %s\n", health.get("cacheManagerStatus")));
        report.append(String.format("   - 缓存数量: %d\n", health.get("cacheCount")));
        
        return report.toString();
    }
}