// File: src/main/java/com/sayrain/medicalbooking/service/FileStorageService.java
package com.sayrain.medicalbooking.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorageService {
    void init();
    String storeFile(MultipartFile file, String fileName);
    Path load(String filename);
    Resource loadAsResource(String filename);
    void deleteAll();
    Stream<Path> loadAll();
}
