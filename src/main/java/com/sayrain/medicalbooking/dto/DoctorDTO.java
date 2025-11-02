package com.sayrain.medicalbooking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class DoctorDTO {

    @Schema(description = "医生ID", example = "1")
    private Long id;

    @NotBlank(message = "医生姓名不能为空")
    @Schema(description = "医生姓名", example = "张医生")
    private String name;

    @Schema(description = "医生职称", example = "主任医师")
    private String title;

    @NotNull(message = "所属科室不能为空")
    @Schema(description = "所属科室ID", example = "2")
    private Long departmentId;

    @Schema(description = "医生状态：0-停用，1-启用", example = "1")
    private Integer status = 1; // 默认启用

    @Schema(description = "医生描述", example = "擅长内科治疗")
    private String description;

    @Schema(description = "医生擅长领域", example = "内科")
    private String specialty;

    @Schema(description = "医生照片URL", example = "http://example.com/image.jpg")
    private String imageUrl;
}
