package com.sayrain.medicalbooking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 * 提供精细化的缓存管理策略
 */
@Slf4j
@Configuration
// @EnableCaching
public class CacheConfig {

    /**
     * 自定义缓存键生成器
     */
    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName()).append(":");
            for (Object param : params) {
                sb.append(param.toString()).append(":");
            }
            return sb.toString();
        };
    }

    /**
     * Redis缓存管理器配置
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 针对不同缓存的个性化配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户相关缓存 - 15分钟
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 预约相关缓存 - 10分钟，因为预约数据变化频繁
        cacheConfigurations.put("appointments", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 医生相关缓存 - 20分钟
        cacheConfigurations.put("doctors", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        
        // 科室相关缓存 - 60分钟，科室数据相对稳定
        cacheConfigurations.put("departments", defaultConfig.entryTtl(Duration.ofMinutes(60)));
        
        // 排班相关缓存 - 5分钟，排班数据可能经常更新
        cacheConfigurations.put("schedules", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 患者相关缓存 - 25分钟
        cacheConfigurations.put("patients", defaultConfig.entryTtl(Duration.ofMinutes(25)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}