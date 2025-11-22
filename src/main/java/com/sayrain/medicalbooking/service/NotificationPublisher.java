package com.sayrain.medicalbooking.service;

import org.springframework.stereotype.Service;

/**
 * 通知发布服务 - 已禁用
 * 由于RabbitMQ依赖已被移除，此类仅作为占位符保留
 */
@Service
public class NotificationPublisher {
    
    // RabbitMQ相关功能已被移除
    // 如需重新启用，请先在pom.xml中添加spring-boot-starter-amqp依赖
    
    public void publishNotification(Object message) {
        // 暂时不做任何操作，日志记录即可
        System.out.println("通知发布已禁用: " + message);
    }
}
