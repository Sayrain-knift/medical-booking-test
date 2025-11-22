package com.sayrain.medicalbooking.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Nacos配置类
 * 提供Nacos配置中心的配置监听和动态刷新功能
 * 只有在Nacos配置启用时才会生效
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", havingValue = "true", matchIfMissing = false)
public class NacosConfig {

    private final NacosConfigProperties nacosConfigProperties;
    
    private ConfigService configService;

    /**
     * 创建Nacos ConfigService
     */
    @Bean
    public ConfigService nacosConfigService() throws NacosException {
        if (configService == null) {
            Properties properties = new Properties();
            properties.put("serverAddr", nacosConfigProperties.getServerAddr());
            properties.put("namespace", nacosConfigProperties.getNamespace());
            
            if (StringUtils.hasText(nacosConfigProperties.getUsername())) {
                properties.put("username", nacosConfigProperties.getUsername());
            }
            if (StringUtils.hasText(nacosConfigProperties.getPassword())) {
                properties.put("password", nacosConfigProperties.getPassword());
            }
            
            configService = NacosFactory.createConfigService(properties);
            log.info("Nacos ConfigService created successfully with serverAddr: {}", 
                    nacosConfigProperties.getServerAddr());
        }
        return configService;
    }

    /**
     * 配置监听器
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> nacosConfigListener() {
        return event -> {
            try {
                ConfigService configService = nacosConfigService();
                
                // 监听主配置文件
                String dataId = nacosConfigProperties.getName() + "." + nacosConfigProperties.getFileExtension();
                configService.addListener(dataId, nacosConfigProperties.getGroup(), new ConfigListener(dataId));
                
                // 监听共享配置文件
                if (nacosConfigProperties.getSharedConfigs() != null) {
                    nacosConfigProperties.getSharedConfigs().forEach(sharedConfig -> {
                        try {
                            configService.addListener(sharedConfig.getDataId(), 
                                    sharedConfig.getGroup(), 
                                    new ConfigListener(sharedConfig.getDataId()));
                        } catch (NacosException e) {
                            log.error("Failed to add listener for shared config: {}", sharedConfig.getDataId(), e);
                        }
                    });
                }
                
                log.info("Nacos config listeners initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Nacos config listeners", e);
            }
        };
    }

    /**
     * 配置监听器实现类
     */
    private class ConfigListener implements Listener {
        private final String dataId;

        public ConfigListener(String dataId) {
            this.dataId = dataId;
        }

        @Override
        public Executor getExecutor() {
            return null; // 使用默认线程池
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            log.info("Received config update for dataId: {}, content: {}", dataId, configInfo);
            // 配置更新时会自动刷新Spring上下文，这里可以添加额外的处理逻辑
            // 例如：发送通知、更新缓存等
        }
    }

    /**
     * 获取配置内容
     */
    public String getConfig(String dataId, String group) throws NacosException {
        return nacosConfigService().getConfig(dataId, group, 5000);
    }

    /**
     * 发布配置
     */
    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        return nacosConfigService().publishConfig(dataId, group, content);
    }

    /**
     * 销毁时清理资源
     */
    @PreDestroy
    public void destroy() {
        if (configService != null) {
            // Nacos ConfigService没有提供close方法，这里只是标记清理
            configService = null;
            log.info("Nacos ConfigService cleaned up");
        }
    }
}