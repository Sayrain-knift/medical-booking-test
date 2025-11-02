// File: src/main/java/com/sayrain/medicalbooking/service/impl/FileStorageServiceImpl.java
package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.config.FileUploadConfig;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private final FileUploadConfig fileUploadConfig;

    @Autowired
    public FileStorageServiceImpl(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
        this.fileStorageLocation = Paths.get(fileUploadConfig.getBasePath()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new BusinessException("无法创建文件存储目录");
        }
    }

    @Override
    public String storeFile(MultipartFile file, String fileName) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > fileUploadConfig.getMaxSize()) {
            throw new BusinessException("文件大小超出限制");
        }

        // 检查文件扩展名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        if (!Arrays.asList(fileUploadConfig.getAllowedExtensions()).contains(fileExtension.toLowerCase())) {
            throw new BusinessException("不支持的文件格式");
        }

        try {
            // 生成唯一文件名
            String uniqueFileName = fileName + "_" + UUID.randomUUID().toString() + "." + fileExtension;

            // 复制文件到目标位置
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new BusinessException("存储文件失败");
        }
    }

    @Override
    public Path load(String filename) {
        return fileStorageLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path filePath = load(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new BusinessException("文件不存在");
            }
        } catch (MalformedURLException ex) {
            throw new BusinessException("文件读取错误");
        }
    }

    @Override
    public void deleteAll() {
        try {
            Files.walk(this.fileStorageLocation)
                    .filter(path -> !path.equals(this.fileStorageLocation))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // 忽略删除错误
                        }
                    });
        } catch (IOException e) {
            throw new BusinessException("删除文件失败");
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.fileStorageLocation, 1)
                    .filter(path -> !path.equals(this.fileStorageLocation))
                    .map(this.fileStorageLocation::relativize);
        } catch (IOException e) {
            throw new BusinessException("读取文件失败");
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new BusinessException("初始化文件存储失败");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
