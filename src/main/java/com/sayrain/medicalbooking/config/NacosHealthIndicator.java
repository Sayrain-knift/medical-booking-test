package com.sayrain.medicalbooking.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Nacos健康检查配置
 * 提供Nacos连接状态的健康检查
 * 只有在Nacos服务发现启用时才会生效
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.nacos.discovery.enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnBean(NamingService.class)
public class NacosHealthIndicator implements HealthIndicator {

    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final NamingService namingService;

    @Override
    public Health health() {
        try {
            // 检查Nacos连接状态
            String serverStatus = namingService.getServerStatus();
            boolean isHealthy = checkNacosConnection();
            
            Health.Builder builder = isHealthy ? Health.up() : Health.down();
            
            builder
                .withDetail("serverAddr", nacosDiscoveryProperties.getServerAddr())
                .withDetail("namespace", nacosDiscoveryProperties.getNamespace())
                .withDetail("group", nacosDiscoveryProperties.getGroup())
                .withDetail("serviceName", nacosDiscoveryProperties.getService())
                .withDetail("serverStatus", serverStatus)
                .withDetail("registrationEnabled", nacosDiscoveryProperties.isRegisterEnabled());
            
            // 检查服务注册状态
            if (nacosDiscoveryProperties.isRegisterEnabled()) {
                try {
                    List<Instance> instances = namingService.getAllInstances(
                            nacosDiscoveryProperties.getService(),
                            nacosDiscoveryProperties.getGroup()
                    );
                    
                    builder
                        .withDetail("instanceCount", instances.size())
                        .withDetail("healthyInstanceCount", 
                                instances.stream().mapToInt(i -> i.isHealthy() ? 1 : 0).sum());
                    
                    // 检查当前实例是否已注册
                    boolean currentInstanceRegistered = instances.stream()
                            .anyMatch(instance -> 
                                instance.getIp().equals(nacosDiscoveryProperties.getIp()) &&
                                instance.getPort() == nacosDiscoveryProperties.getPort()
                            );
                    
                    builder.withDetail("currentInstanceRegistered", currentInstanceRegistered);
                    
                } catch (Exception e) {
                    builder
                        .withDetail("registrationError", e.getMessage())
                        .status(Status.DOWN);
                }
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Nacos health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("serverAddr", nacosDiscoveryProperties.getServerAddr())
                    .build();
        }
    }

    /**
     * 检查Nacos连接状态
     */
    private boolean checkNacosConnection() {
        try {
            // 尝试获取服务列表来验证连接
            namingService.getServicesOfServer(1, 1);
            return true;
        } catch (Exception e) {
            log.debug("Nacos connection check failed", e);
            return false;
        }
    }
}