package com.sayrain.medicalbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleDTO {
    private Long id;
    
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private Long departmentId;
    private String departmentName;
    
    @NotNull(message = "排班日期不能为空")
    @FutureOrPresent(message = "排班日期不能是过去日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    
    @Positive(message = "最大患者数必须大于0")
    private Integer maxPatients = 10;
    
    private Integer maxAppointments;
    private Integer currentAppointments;
    private Integer status = 1;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 验证时间有效性
    public boolean isTimeValid() {
        return endTime != null && startTime != null && endTime.isAfter(startTime);
    }
}