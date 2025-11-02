package com.sayrain.medicalbooking.dto;

import com.sayrain.medicalbooking.model.Appointment.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "预约 DTO")
public class AppointmentDTO {

    @Schema(description = "预约ID", example = "1")
    private Long id;

    @NotNull(message = "患者不能为空")
    @Schema(description = "患者ID", example = "1001", required = true)
    private Long patientId;

    @NotNull(message = "排班不能为空")
    @Schema(description = "排班ID", example = "2001", required = true)
    private Long scheduleId;

    @Schema(description = "排队号（自动生成）", example = "5")
    private Integer queueNumber;

    @Schema(description = "预约状态，枚举值：waiting（等待）, confirmed（已确认）, completed（已完成）, cancelled（已取消）", example = "waiting")
    private AppointmentStatus status;
}
