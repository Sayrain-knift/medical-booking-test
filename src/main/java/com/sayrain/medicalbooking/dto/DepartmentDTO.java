package com.sayrain.medicalbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class DepartmentDTO {

    @Schema(description = "科室ID", example = "1")
    private Long id;

    @Schema(description = "科室名称", example = "内科", required = true)
    @NotBlank(message = "科室名称不能为空")
    private String name;

    @Schema(description = "科室描述", example = "负责内科相关疾病的诊断和治疗")
    private String description;

    @Schema(description = "父级科室ID，0表示顶级科室", example = "0")
    private Long parentId = 0L;

    @Schema(description = "科室状态，0表示停用，1表示启用", example = "1", required = true)
    @NotNull(message = "科室状态不能为空")
    private Integer status;

    @Schema(description = "科室图标", example = "icon.png")
    private String icon;

    @Schema(description = "科室排序字段", example = "1")
    private Integer sortOrder = 1;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}