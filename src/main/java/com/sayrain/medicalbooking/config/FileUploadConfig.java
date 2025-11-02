// File: src/main/java/com/sayrain/medicalbooking/config/FileUploadConfig.java
package com.sayrain.medicalbooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    private String basePath = "/uploads";
    private long maxSize = 5242880; // 5MB
    private String[] allowedExtensions = {"jpg", "jpeg", "png", "gif"};
}
