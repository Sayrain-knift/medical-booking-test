package com.sayrain.medicalbooking.controller;

import com.sayrain.medicalbooking.dto.ScheduleDTO;
import com.sayrain.medicalbooking.model.Schedule;
import com.sayrain.medicalbooking.service.ScheduleService;
import com.sayrain.medicalbooking.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "排班管理", description = "排班相关操作")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "创建排班", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseResult<Schedule> createSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO) {
        Schedule schedule = scheduleService.createSchedule(scheduleDTO);
        return new ResponseResult<>(200, "创建排班成功", schedule);
    }

    @Operation(summary = "更新排班", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseResult<Schedule> updateSchedule(@PathVariable Long id, @Valid @RequestBody ScheduleDTO scheduleDTO) {
        Schedule schedule = scheduleService.updateSchedule(id, scheduleDTO);
        return new ResponseResult<>(200, "更新排班成功", schedule);
    }

    @Operation(summary = "删除排班", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return new ResponseResult<>(200, "删除排班成功", null);
    }

    @Operation(summary = "获取排班详情", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseResult<Schedule> getScheduleById(@PathVariable Long id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return new ResponseResult<>(200, "获取排班详情成功", schedule);
    }

    @Operation(summary = "获取所有排班", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseResult<List<Schedule>> getAllSchedules() {
        List<Schedule> schedules = scheduleService.getAllSchedules();
        return new ResponseResult<>(200, "获取所有排班成功", schedules);
    }

    @Operation(summary = "根据医生ID获取排班", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/doctor/{doctorId}")
    public ResponseResult<List<Schedule>> getSchedulesByDoctor(@PathVariable Long doctorId) {
        List<Schedule> schedules = scheduleService.getSchedulesByDoctor(doctorId);
        return new ResponseResult<>(200, "获取医生排班成功", schedules);
    }

    @Operation(summary = "获取可预约排班", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/available")
    public ResponseResult<List<Schedule>> getAvailableSchedules(@RequestParam Long departmentId, @RequestParam LocalDate date) {
        List<Schedule> schedules = scheduleService.getAvailableSchedules(departmentId, date);
        return new ResponseResult<>(200, "获取可预约排班成功", schedules);
    }

    @Operation(summary = "按日期范围查询排班", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/date-range")
    public ResponseResult<List<Schedule>> getSchedulesByDateRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Schedule> schedules = scheduleService.getSchedulesByDateRange(startDate, endDate);
        return new ResponseResult<>(200, "获取排班信息成功", schedules);
    }

    @Operation(summary = "修改排班状态", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseResult<Void> changeScheduleStatus(@PathVariable Long id, @RequestParam Integer status) {
        scheduleService.changeScheduleStatus(id, status);
        return new ResponseResult<>(200, "修改排班状态成功", null);
    }
}
