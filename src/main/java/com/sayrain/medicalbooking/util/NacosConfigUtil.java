package com.sayrain.medicalbooking.util;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Nacos配置管理工具类
 * 提供配置获取、发布、删除等功能
 * 只有在Nacos配置启用时才会生效
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", havingValue = "true", matchIfMissing = false)
public class NacosConfigUtil {

    private final ConfigService configService;
    private final NacosConfigProperties nacosConfigProperties;

    /**
     * 获取配置内容
     * 
     * @param dataId 配置文件ID
     * @return 配置内容
     */
    public String getConfig(String dataId) {
        return getConfig(dataId, nacosConfigProperties.getGroup(), 5000);
    }

    /**
     * 获取配置内容
     * 
     * @param dataId 配置文件ID
     * @param group 分组
     * @return 配置内容
     */
    public String getConfig(String dataId, String group) {
        return getConfig(dataId, group, 5000);
    }

    /**
     * 获取配置内容
     * 
     * @param dataId 配置文件ID
     * @param group 分组
     * @param timeout 超时时间（毫秒）
     * @return 配置内容
     */
    public String getConfig(String dataId, String group, long timeout) {
        try {
            return configService.getConfig(dataId, group, timeout);
        } catch (NacosException e) {
            log.error("Failed to get config for dataId: {}, group: {}", dataId, group, e);
            return null;
        }
    }

    /**
     * 发布配置
     * 
     * @param dataId 配置文件ID
     * @param content 配置内容
     * @return 是否成功
     */
    public boolean publishConfig(String dataId, String content) {
        return publishConfig(dataId, nacosConfigProperties.getGroup(), content);
    }

    /**
     * 发布配置
     * 
     * @param dataId 配置文件ID
     * @param group 分组
     * @param content 配置内容
     * @return 是否成功
     */
    public boolean publishConfig(String dataId, String group, String content) {
        try {
            return configService.publishConfig(dataId, group, content);
        } catch (NacosException e) {
            log.error("Failed to publish config for dataId: {}, group: {}", dataId, group, e);
            return false;
        }
    }

    /**
     * 删除配置
     * 
     * @param dataId 配置文件ID
     * @return 是否成功
     */
    public boolean removeConfig(String dataId) {
        return removeConfig(dataId, nacosConfigProperties.getGroup());
    }

    /**
     * 删除配置
     * 
     * @param dataId 配置文件ID
     * @param group 分组
     * @return 是否成功
     */
    public boolean removeConfig(String dataId, String group) {
        try {
            return configService.removeConfig(dataId, group);
        } catch (NacosException e) {
            log.error("Failed to remove config for dataId: {}, group: {}", dataId, group, e);
            return false;
        }
    }

    /**
     * 检查配置是否存在
     * 
     * @param dataId 配置文件ID
     * @return 是否存在
     */
    public boolean configExists(String dataId) {
        return configExists(dataId, nacosConfigProperties.getGroup());
    }

    /**
     * 检查配置是否存在
     * 
     * @param dataId 配置文件ID
     * @param group 分组
     * @return 是否存在
     */
    public boolean configExists(String dataId, String group) {
        String config = getConfig(dataId, group);
        return config != null;
    }

    /**
     * 获取默认分组
     */
    public String getDefaultGroup() {
        return nacosConfigProperties.getGroup();
    }

    /**
     * 获取命名空间
     */
    public String getNamespace() {
        return nacosConfigProperties.getNamespace();
    }

    /**
     * 获取服务器地址
     */
    public String getServerAddr() {
        return nacosConfigProperties.getServerAddr();
    }

    /**
     * 获取当前应用名称
     */
    public String getApplicationName() {
        return nacosConfigProperties.getName();
    }

    /**
     * 批量发布配置
     * 
     * @param configs 配置映射，key为dataId，value为配置内容
     * @return 成功发布的数量
     */
    public int batchPublishConfig(java.util.Map<String, String> configs) {
        return batchPublishConfig(configs, nacosConfigProperties.getGroup());
    }

    /**
     * 批量发布配置
     * 
     * @param configs 配置映射，key为dataId，value为配置内容
     * @param group 分组
     * @return 成功发布的数量
     */
    public int batchPublishConfig(java.util.Map<String, String> configs, String group) {
        int successCount = 0;
        for (java.util.Map.Entry<String, String> entry : configs.entrySet()) {
            if (publishConfig(entry.getKey(), group, entry.getValue())) {
                successCount++;
            }
        }
        log.info("Batch publish config completed: {}/{} successful", successCount, configs.size());
        return successCount;
    }
}