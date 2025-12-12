package com.sayrain.medicalbooking.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.sayrain.medicalbooking.dto.AppointmentDTO;
import com.sayrain.medicalbooking.model.Appointment;
import com.sayrain.medicalbooking.model.Appointment.AppointmentStatus;
import com.sayrain.medicalbooking.model.Patient;
import com.sayrain.medicalbooking.model.User;
import com.sayrain.medicalbooking.repository.PatientRepository;
import com.sayrain.medicalbooking.repository.UserRepository;
import com.sayrain.medicalbooking.security.CustomUserDetails;
import com.sayrain.medicalbooking.service.AppointmentService;
import com.sayrain.medicalbooking.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "预约管理", description = "预约相关操作接口")
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    private final AppointmentService appointmentService;

    @Operation(summary = "获取预约列表（分页）", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @SentinelResource(value = "getAllAppointments", blockHandler = "getAllAppointmentsBlockHandler", fallback = "getAllAppointmentsFallback")
    public ResponseResult<Page<Appointment>> getAllAppointments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) AppointmentStatus status) {

        // 创建分页请求，按ID倒序排列
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());

        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        Page<Appointment> appointments;

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            // 根据用户角色返回不同的数据
            if ("ROLE_ADMIN".equals(role)) {
                // 管理员可以查看所有预约，支持筛选
                appointments = appointmentService.getAllAppointments(pageable, patientId, status);
            } else if ("ROLE_DOCTOR".equals(role)) {
                // 医生可以查看与自己相关的预约
                Long doctorId = userDetails.getUserId(); // 假设用户ID就是医生ID
                appointments = appointmentService.getDoctorAppointments(doctorId, pageable);
            } else if ("ROLE_PATIENT".equals(role)) {
                // 患者只能查看自己的预约
                Long currentPatientId = getCurrentPatientId();
                appointments = appointmentService.getPatientAppointments(currentPatientId, pageable);
            } else {
                throw new RuntimeException("用户角色不支持此操作");
            }
        } else {
            throw new RuntimeException("用户认证信息异常");
        }

        return new ResponseResult<>(200, "获取预约列表成功", appointments);
    }

    @Operation(summary = "创建预约", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    @PostMapping
    @SentinelResource(value = "createAppointment", blockHandler = "createAppointmentBlockHandler", fallback = "createAppointmentFallback")
    public ResponseResult<Appointment> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        // 从安全上下文获取当前用户ID并设置到DTO中
        Long currentPatientId = getCurrentPatientId();
        appointmentDTO.setPatientId(currentPatientId);

        Appointment appointment = appointmentService.createAppointment(appointmentDTO);
        return new ResponseResult<>(200, "创建预约成功", appointment);
    }


    @Operation(summary = "取消预约", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    @DeleteMapping("/{id}")
    @SentinelResource(value = "cancelAppointment", blockHandler = "cancelAppointmentBlockHandler", fallback = "cancelAppointmentFallback")
    public ResponseResult<Void> cancelAppointment(@PathVariable Long id) {
        // 从安全上下文获取当前用户ID
        Long currentPatientId = getCurrentPatientId();
        appointmentService.cancelAppointment(id, currentPatientId);
        return new ResponseResult<>(200, "取消预约成功", null);
    }

    @Operation(summary = "更新预约状态", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DOCTOR')")
    @PatchMapping("/{id}/status")
    @SentinelResource(value = "updateAppointmentStatus", blockHandler = "updateAppointmentStatusBlockHandler", fallback = "updateAppointmentStatusFallback")
    public ResponseResult<Void> updateAppointmentStatus(@PathVariable Long id, @RequestParam AppointmentStatus status) {
        appointmentService.updateAppointmentStatus(id, status);
        return new ResponseResult<>(200, "更新预约状态成功", null);
    }

    @Operation(summary = "获取患者的所有预约",description = "返回患者未取消的预约列表", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    @GetMapping("/my-appointments")
    @SentinelResource(value = "getMyAppointments", blockHandler = "getMyAppointmentsBlockHandler", fallback = "getMyAppointmentsFallback")
    public ResponseResult<List<Appointment>> getMyAppointments() {
        // 从安全上下文获取当前用户ID
        Long currentPatientId = getCurrentPatientId();
        List<Appointment> list = appointmentService.getPatientAppointments(currentPatientId);
        return new ResponseResult<>(200, "获取患者预约成功", list);
    }

    @Operation(summary = "获取某排班的所有预约", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DOCTOR')")
    @GetMapping("/schedule/{scheduleId}")
    @SentinelResource(value = "getScheduleAppointments", blockHandler = "getScheduleAppointmentsBlockHandler", fallback = "getScheduleAppointmentsFallback")
    public ResponseResult<List<Appointment>> getScheduleAppointments(@PathVariable Long scheduleId) {
        List<Appointment> list = appointmentService.getScheduleAppointments(scheduleId);
        return new ResponseResult<>(200, "获取排班预约成功", list);
    }

    @Operation(summary = "根据状态获取预约", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/status/{status}")
    @SentinelResource(value = "getAppointmentsByStatus", blockHandler = "getAppointmentsByStatusBlockHandler", fallback = "getAppointmentsByStatusFallback")
    public ResponseResult<List<Appointment>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        List<Appointment> list = appointmentService.getAppointmentsByStatus(status);
        return new ResponseResult<>(200, "获取指定状态预约成功", list);
    }

    @Operation(summary = "获取预约详情", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    @SentinelResource(value = "getAppointmentById", blockHandler = "getAppointmentByIdBlockHandler", fallback = "getAppointmentByIdFallback")
    public ResponseResult<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return new ResponseResult<>(200, "获取预约详情成功", appointment);
    }

    @Operation(summary = "检查排班是否可预约", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/schedule/{scheduleId}/availability")
    @SentinelResource(value = "checkScheduleAvailability", blockHandler = "checkScheduleAvailabilityBlockHandler", fallback = "checkScheduleAvailabilityFallback")
    public ResponseResult<Boolean> checkScheduleAvailability(@PathVariable Long scheduleId) {
        boolean available = appointmentService.isScheduleAvailable(scheduleId);
        return new ResponseResult<>(200, "检查排班可用性成功", available);
    }

    // Sentinel流控处理方法
    public ResponseResult<Page<Appointment>> getAllAppointmentsBlockHandler(
            int page, int pageSize, Long patientId, AppointmentStatus status,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<Appointment> createAppointmentBlockHandler(AppointmentDTO appointmentDTO,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<Void> cancelAppointmentBlockHandler(Long id,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<Void> updateAppointmentStatusBlockHandler(Long id, AppointmentStatus status,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<List<Appointment>> getMyAppointmentsBlockHandler(
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<List<Appointment>> getScheduleAppointmentsBlockHandler(Long scheduleId,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<List<Appointment>> getAppointmentsByStatusBlockHandler(AppointmentStatus status,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<Appointment> getAppointmentByIdBlockHandler(Long id,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    public ResponseResult<Boolean> checkScheduleAvailabilityBlockHandler(Long scheduleId,
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return new ResponseResult<>(429, "请求过于频繁，请稍后再试", null);
    }

    // Sentinel降级处理方法
    public ResponseResult<Page<Appointment>> getAllAppointmentsFallback(
            int page, int pageSize, Long patientId, AppointmentStatus status, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<Appointment> createAppointmentFallback(AppointmentDTO appointmentDTO, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<Void> cancelAppointmentFallback(Long id, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<Void> updateAppointmentStatusFallback(Long id, AppointmentStatus status, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<List<Appointment>> getMyAppointmentsFallback(Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<List<Appointment>> getScheduleAppointmentsFallback(Long scheduleId, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<List<Appointment>> getAppointmentsByStatusFallback(AppointmentStatus status, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<Appointment> getAppointmentByIdFallback(Long id, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    public ResponseResult<Boolean> checkScheduleAvailabilityFallback(Long scheduleId, Throwable ex) {
        return new ResponseResult<>(500, "服务暂时不可用，请稍后再试", null);
    }

    // 从安全上下文获取当前患者ID
    private Long getCurrentPatientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                Long userId = userDetails.getUserId();

                // 通过用户ID查询患者
                Patient patient = patientRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("患者信息不存在，用户ID: " + userId));

                return patient.getId();
            }

            // 回退方案：通过用户名查询
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在: " + username));

            Patient patient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("患者信息不存在，用户名: " + username));

            return patient.getId();
        } catch (Exception e) {
            throw new RuntimeException("获取当前患者ID失败: " + e.getMessage(), e);
        }
    }

}