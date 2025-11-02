package com.sayrain.medicalbooking.controller;

import com.sayrain.medicalbooking.dto.DoctorDTO;
import com.sayrain.medicalbooking.model.Doctor;
import com.sayrain.medicalbooking.service.DoctorService;
import com.sayrain.medicalbooking.util.ResponseResult;  // 确保使用正确包路径
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "医生管理", description = "医生管理相关操作")
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "创建医生", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseResult<Doctor> createDoctor(@Valid @RequestBody DoctorDTO doctorDTO) {
        Doctor doctor = doctorService.createDoctor(doctorDTO);
        return new ResponseResult<>(200, "创建医生成功", doctor);
    }

    @Operation(summary = "更新医生", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseResult<Doctor> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorDTO doctorDTO) {
        Doctor doctor = doctorService.updateDoctor(id, doctorDTO);
        return new ResponseResult<>(200, "更新医生成功", doctor);
    }

    @Operation(summary = "删除医生", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return new ResponseResult<>(200, "删除医生成功", null);
    }

    @Operation(summary = "获取医生详情", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseResult<Doctor> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return new ResponseResult<>(200, "获取医生详情成功", doctor);
    }

    @Operation(summary = "获取所有医生", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseResult<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return new ResponseResult<>(200, "获取所有医生成功", doctors);
    }

    @Operation(summary = "获取启用医生", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/active")
    public ResponseResult<List<Doctor>> getActiveDoctors() {
        List<Doctor> doctors = doctorService.getActiveDoctors();
        return new ResponseResult<>(200, "获取启用医生成功", doctors);
    }

    @Operation(summary = "获取科室医生", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/department/{departmentId}")
    public ResponseResult<List<Doctor>> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<Doctor> doctors = doctorService.getDoctorsByDepartment(departmentId);
        return new ResponseResult<>(200, "获取科室医生成功", doctors);
    }

    @Operation(summary = "获取科室启用医生", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/active/department/{departmentId}")
    public ResponseResult<List<Doctor>> getActiveDoctorsByDepartment(@PathVariable Long departmentId) {
        List<Doctor> doctors = doctorService.getActiveDoctorsByDepartment(departmentId);
        return new ResponseResult<>(200, "获取科室启用医生成功", doctors);
    }

    @Operation(summary = "修改医生状态", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseResult<Void> changeDoctorStatus(@PathVariable Long id, @RequestParam Integer status) {
        doctorService.changeDoctorStatus(id, status);
        return new ResponseResult<>(200, "修改医生状态成功", null);
    }


}
