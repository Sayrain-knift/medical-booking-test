// File: src/main/java/com/sayrain/medicalbooking/controller/FileUploadController.java
package com.sayrain.medicalbooking.controller;

import com.sayrain.medicalbooking.service.FileStorageService;
import com.sayrain.medicalbooking.service.DoctorService;
import com.sayrain.medicalbooking.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Tag(name = "文件管理", description = "文件上传和下载相关操作")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DoctorService doctorService;

    @Operation(summary = "上传医生照片", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/doctor-image/{doctorId}")
    public ResponseResult<String> uploadDoctorImage(
            @PathVariable Long doctorId,
            @RequestParam("file") MultipartFile file) {

        try {
            // 检查医生是否存在
            doctorService.getDoctorById(doctorId);

            // 生成文件名
            String fileName = "doctor_" + doctorId;

            // 上传文件
            String storedFileName = fileStorageService.storeFile(file, fileName);

            // 构建访问URL
            String fileUrl = "/api/files/doctor-image/" + storedFileName;

            return new ResponseResult<>(200, "上传成功", fileUrl);
        } catch (Exception e) {
            return new ResponseResult<>(500, "上传失败: " + e.getMessage(), null);
        }
    }

    @Operation(summary = "获取医生照片")
    @GetMapping("/doctor-image/{fileName:.+}")
    public ResponseEntity<Resource> getDoctorImage(
            @PathVariable String fileName,
            HttpServletRequest request) {

        try {
            // 加载文件资源
            Resource resource = fileStorageService.loadAsResource(fileName);

            // 确定文件内容类型
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                // 如果无法确定内容类型，则默认为二进制流
            }

            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
