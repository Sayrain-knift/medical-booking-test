package com.sayrain.medicalbooking.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存通用工具类
 * 提供基础缓存操作 + 科室缓存 + 医生缓存功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==========================
    // 缓存键常量定义
    // ==========================
    public static final String DEPARTMENT_CACHE_KEY = "medical:departments";
    public static final String DOCTOR_CACHE_KEY = "medical:doctors:dept:%s";
    private static final long DEFAULT_TIMEOUT = 30; // 单位：分钟

    // ==========================
    // 基础缓存操作
    // ==========================

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, DEFAULT_TIMEOUT, TimeUnit.MINUTES);
            log.info("设置缓存成功：key={}, 默认过期={}分钟", key, DEFAULT_TIMEOUT);
            return true;
        } catch (Exception e) {
            log.error("设置缓存失败：key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean set(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
            log.info("设置缓存成功：key={}, timeout={}分钟", key, timeout);
            return true;
        } catch (Exception e) {
            log.error("设置缓存失败：key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("获取缓存：key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("获取缓存失败：key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.info("删除缓存：key={}, result={}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("删除缓存失败：key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            log.debug("缓存存在性检查：key={}, exists={}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("检查缓存存在失败：key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    // ==========================
    // 科室缓存操作
    // ==========================

    public boolean cacheDepartment(Object departmentData) {
        try {
            redisTemplate.opsForValue().set(DEPARTMENT_CACHE_KEY, departmentData, DEFAULT_TIMEOUT, TimeUnit.MINUTES);
            log.info("缓存科室数据成功，key={}，有效期={}分钟", DEPARTMENT_CACHE_KEY, DEFAULT_TIMEOUT);
            return true;
        } catch (Exception e) {
            log.error("缓存科室数据失败：error={}", e.getMessage(), e);
            return false;
        }
    }

    public Object getCachedDepartments() {
        try {
            Object data = redisTemplate.opsForValue().get(DEPARTMENT_CACHE_KEY);
            log.debug("获取缓存科室数据：{}", data);
            return data;
        } catch (Exception e) {
            log.error("获取缓存科室数据失败：error={}", e.getMessage(), e);
            return null;
        }
    }

    public boolean clearDepartmentCache() {
        try {
            Boolean result = redisTemplate.delete(DEPARTMENT_CACHE_KEY);
            log.info("清除科室缓存：result={}", result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("清除科室缓存失败：error={}", e.getMessage(), e);
            return false;
        }
    }

    // ==========================
    // 医生缓存操作
    // ==========================

    public boolean cacheDoctorsByDepartment(Long departmentId, Object doctors) {
        String key = String.format(DOCTOR_CACHE_KEY, departmentId);
        try {
            redisTemplate.opsForValue().set(key, doctors, DEFAULT_TIMEOUT, TimeUnit.MINUTES);
            log.info("缓存医生数据成功：departmentId={}，key={}，有效期={}分钟", departmentId, key, DEFAULT_TIMEOUT);
            return true;
        } catch (Exception e) {
            log.error("缓存医生数据失败：departmentId={}，error={}", departmentId, e.getMessage(), e);
            return false;
        }
    }

    public Object getCachedDoctorsByDepartment(Long departmentId) {
        String key = String.format(DOCTOR_CACHE_KEY, departmentId);
        try {
            Object data = redisTemplate.opsForValue().get(key);
            log.debug("获取医生缓存：departmentId={}，data={}", departmentId, data);
            return data;
        } catch (Exception e) {
            log.error("获取医生缓存失败：departmentId={}，error={}", departmentId, e.getMessage(), e);
            return null;
        }
    }

    public boolean clearDoctorCache(Long departmentId) {
        String key = String.format(DOCTOR_CACHE_KEY, departmentId);
        try {
            Boolean result = redisTemplate.delete(key);
            log.info("清除医生缓存：departmentId={}，result={}", departmentId, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("清除医生缓存失败：departmentId={}，error={}", departmentId, e.getMessage(), e);
            return false;
        }
    }
}
