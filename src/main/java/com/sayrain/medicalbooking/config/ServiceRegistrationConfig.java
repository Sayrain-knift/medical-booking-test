package com.sayrain.medicalbooking.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 禁用服务自动注册的配置类
 * 在minimal profile下禁用Nacos等服务注册
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.nacos.discovery.enabled", havingValue = "false")
public class ServiceRegistrationConfig {
    
    public ServiceRegistrationConfig() {
        // 空配置类，仅用于条件化配置
    }
}