package com.sayrain.medicalbooking.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos服务发现配置
 * 
 * @author AI Assistant
 */
@Configuration
@ConditionalOnClass(NacosDiscoveryProperties.class)
@ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enabled", havingValue = "true", matchIfMissing = false)
public class NacosDiscoveryConfig {

    private static final Logger logger = LoggerFactory.getLogger(NacosDiscoveryConfig.class);

    @Autowired(required = false)
    private NamingService namingService;

    @Autowired(required = false)
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    /**
     * 服务发现管理器
     */
    @Bean
    @ConditionalOnBean(NamingService.class)
    public ServiceDiscoveryManager serviceDiscoveryManager(DiscoveryClient discoveryClient, 
                                                          NamingService namingService,
                                                          NacosDiscoveryProperties nacosDiscoveryProperties) {
        logger.info("Creating ServiceDiscoveryManager bean");
        return new ServiceDiscoveryManager(discoveryClient, namingService, nacosDiscoveryProperties);
    }

    /**
     * Nacos Watch
     */
    @Bean
    @ConditionalOnBean(NamingService.class)
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties nacosDiscoveryProperties) {
        logger.info("Creating NacosWatch bean");
        return new NacosWatch(nacosServiceManager, nacosDiscoveryProperties);
    }

    @PostConstruct
    public void init() {
        logger.info("NacosDiscoveryConfig initialized");
        if (namingService != null) {
            logger.info("NamingService is available");
        } else {
            logger.warn("NamingService is not available");
        }
        if (nacosDiscoveryProperties != null) {
            logger.info("NacosDiscoveryProperties is available");
        } else {
            logger.warn("NacosDiscoveryProperties is not available");
        }
    }

    /**
     * 服务发现管理器实现
     */
    @Component
    @ConditionalOnProperty(name = "spring.cloud.nacos.discovery.enabled", havingValue = "true")
    @ConditionalOnBean(NamingService.class)
    public static class ServiceDiscoveryManager {
        
        private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryManager.class);
        
        private final DiscoveryClient discoveryClient;
        private final NamingService namingService;
        private final NacosDiscoveryProperties nacosDiscoveryProperties;

        public ServiceDiscoveryManager(DiscoveryClient discoveryClient, 
                                     NamingService namingService,
                                     NacosDiscoveryProperties nacosDiscoveryProperties) {
            this.discoveryClient = discoveryClient;
            this.namingService = namingService;
            this.nacosDiscoveryProperties = nacosDiscoveryProperties;
            
            log.info("Nacos Service Discovery initialized");
            log.info("Service name: {}", nacosDiscoveryProperties.getService());
            log.info("Server address: {}", nacosDiscoveryProperties.getServerAddr());
            log.info("Namespace: {}", nacosDiscoveryProperties.getNamespace());
            log.info("Group: {}", nacosDiscoveryProperties.getGroup());
            
            // 获取当前服务实例信息
            try {
                List<Instance> instances = namingService.getAllInstances(
                        nacosDiscoveryProperties.getService(),
                        nacosDiscoveryProperties.getGroup()
                );
                
                log.info("Current service instances count: {}", instances.size());
                instances.forEach(instance -> 
                    log.info("Instance: {}:{}, healthy: {}", 
                        instance.getIp(), instance.getPort(), instance.isHealthy())
                );
            } catch (Exception e) {
                log.warn("Failed to get service instances", e);
            }
        }

        /**
         * 获取所有可用的服务实例
         */
        public List<ServiceInstance> getInstances(String serviceId) {
            return discoveryClient.getInstances(serviceId);
        }

        /**
         * 获取所有服务名称
         */
        public List<String> getServices() {
            return discoveryClient.getServices();
        }

        /**
         * 获取健康的服务实例
         */
        public List<ServiceInstance> getHealthyInstances(String serviceId) {
            return getInstances(serviceId).stream()
                    .filter(instance -> {
                        try {
                            Instance nacosInstance = namingService.selectOneHealthyInstance(
                                    serviceId, nacosDiscoveryProperties.getGroup());
                            return nacosInstance != null && nacosInstance.isHealthy();
                        } catch (Exception e) {
                            log.warn("Failed to check health for instance of service: {}", serviceId, e);
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        /**
         * 检查服务是否可用
         */
        public boolean isServiceAvailable(String serviceId) {
            try {
                List<ServiceInstance> instances = getHealthyInstances(serviceId);
                return !instances.isEmpty();
            } catch (Exception e) {
                log.error("Failed to check service availability for: {}", serviceId, e);
                return false;
            }
        }

        /**
         * 获取当前服务的实例信息
         */
        public Instance getCurrentInstance() {
            try {
                return namingService.selectOneHealthyInstance(
                        nacosDiscoveryProperties.getService(),
                        nacosDiscoveryProperties.getGroup()
                );
            } catch (Exception e) {
                log.error("Failed to get current instance", e);
                return null;
            }
        }

        /**
         * 手动注册服务实例（通常不需要，Spring会自动注册）
         */
        public void registerInstance(String ip, int port) {
            try {
                Instance instance = new Instance();
                instance.setIp(ip);
                instance.setPort(port);
                instance.setWeight(nacosDiscoveryProperties.getWeight());
                instance.setHealthy(true);
                instance.setEnabled(true);
                instance.setEphemeral(true);
                instance.setClusterName(nacosDiscoveryProperties.getClusterName());
                instance.setMetadata(nacosDiscoveryProperties.getMetadata());
                
                namingService.registerInstance(
                        nacosDiscoveryProperties.getService(),
                        nacosDiscoveryProperties.getGroup(),
                        instance
                );
                
                log.info("Successfully registered instance: {}:{}", ip, port);
            } catch (Exception e) {
                log.error("Failed to register instance: {}:{}", ip, port, e);
            }
        }

        /**
         * 手动注销服务实例
         */
        public void deregisterInstance(String ip, int port) {
            try {
                namingService.deregisterInstance(
                        nacosDiscoveryProperties.getService(),
                        nacosDiscoveryProperties.getGroup(),
                        ip,
                        port
                );
                
                log.info("Successfully deregistered instance: {}:{}", ip, port);
            } catch (Exception e) {
                log.error("Failed to deregister instance: {}:{}", ip, port, e);
            }
        }
    }
}