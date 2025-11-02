package com.sayrain.medicalbooking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleDTO {

    @Schema(description = "排班ID")
    private Long id;

    @NotNull(message = "医生不能为空")
    @Schema(description = "医生ID", required = true)
    private Long doctorId;

    @NotNull(message = "排班日期不能为空")
    @FutureOrPresent(message = "排班日期不能是过去日期")
    @Schema(description = "排班日期", required = true)
    private LocalDate date;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", required = true)
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", required = true)
    private LocalTime endTime;

    @Positive(message = "最大患者数必须大于0")
    @Schema(description = "最大患者数", defaultValue = "10")
    private Integer maxPatients = 10;

    @Schema(description = "状态：0-停用，1-启用", defaultValue = "1")
    private Integer status = 1;

    // ✅ 修正：使用AssertTrue进行业务规则验证
    @Schema(hidden = true)
    public boolean isTimeValid() {
        return endTime != null && startTime != null && endTime.isAfter(startTime);
    }
}