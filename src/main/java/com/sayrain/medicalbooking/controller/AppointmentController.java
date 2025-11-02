package com.sayrain.medicalbooking.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "创建预约", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    @PostMapping
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
    public ResponseResult<Void> cancelAppointment(@PathVariable Long id) {
        // 从安全上下文获取当前用户ID
        Long currentPatientId = getCurrentPatientId();
        appointmentService.cancelAppointment(id, currentPatientId);
        return new ResponseResult<>(200, "取消预约成功", null);
    }

    @Operation(summary = "更新预约状态", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DOCTOR')")
    @PatchMapping("/{id}/status")
    public ResponseResult<Void> updateAppointmentStatus(@PathVariable Long id, @RequestParam AppointmentStatus status) {
        appointmentService.updateAppointmentStatus(id, status);
        return new ResponseResult<>(200, "更新预约状态成功", null);
    }

    @Operation(summary = "获取患者的所有预约",description = "返回患者未取消的预约列表", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_PATIENT')")
    @GetMapping("/my-appointments")
    public ResponseResult<List<Appointment>> getMyAppointments() {
        // 从安全上下文获取当前用户ID
        Long currentPatientId = getCurrentPatientId();
        List<Appointment> list = appointmentService.getPatientAppointments(currentPatientId);
        return new ResponseResult<>(200, "获取患者预约成功", list);
    }

    @Operation(summary = "获取某排班的所有预约", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DOCTOR')")
    @GetMapping("/schedule/{scheduleId}")
    public ResponseResult<List<Appointment>> getScheduleAppointments(@PathVariable Long scheduleId) {
        List<Appointment> list = appointmentService.getScheduleAppointments(scheduleId);
        return new ResponseResult<>(200, "获取排班预约成功", list);
    }

    @Operation(summary = "根据状态获取预约", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseResult<List<Appointment>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        List<Appointment> list = appointmentService.getAppointmentsByStatus(status);
        return new ResponseResult<>(200, "获取指定状态预约成功", list);
    }

    @Operation(summary = "获取预约详情", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseResult<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return new ResponseResult<>(200, "获取预约详情成功", appointment);
    }

    @Operation(summary = "检查排班是否可预约", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/schedule/{scheduleId}/availability")
    public ResponseResult<Boolean> checkScheduleAvailability(@PathVariable Long scheduleId) {
        boolean available = appointmentService.isScheduleAvailable(scheduleId);
        return new ResponseResult<>(200, "检查排班可用性成功", available);
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
                Patient patient = (Patient) patientRepository.findByUserId(userId)
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