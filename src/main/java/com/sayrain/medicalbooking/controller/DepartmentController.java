package com.sayrain.medicalbooking.controller;

import com.sayrain.medicalbooking.dto.DepartmentDTO;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.service.DepartmentService;
import com.sayrain.medicalbooking.util.ResponseResult;  // 修正包路径
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "科室管理", description = "科室管理相关操作")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "创建科室", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseResult<Department> createDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        Department department = departmentService.createDepartment(departmentDTO);
        return new ResponseResult<>(200, "创建科室成功", department);
    }

    @Operation(summary = "更新科室", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseResult<Department> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDTO departmentDTO) {
        Department department = departmentService.updateDepartment(id, departmentDTO);
        return new ResponseResult<>(200, "更新科室成功", department);
    }

    @Operation(summary = "删除科室", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return new ResponseResult<>(200, "删除科室成功", null);
    }

    @Operation(summary = "获取科室详情", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseResult<Department> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return new ResponseResult<>(200, "获取科室详情成功", department);
    }

    @Operation(summary = "获取所有科室", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseResult<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return new ResponseResult<>(200, "获取所有科室成功", departments);
    }

    @Operation(summary = "获取所有启用科室", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/active")
    public ResponseResult<List<Department>> getActiveDepartments() {
        List<Department> departments = departmentService.getActiveDepartments();
        return new ResponseResult<>(200, "获取启用科室成功", departments);
    }

    @Operation(summary = "获取子科室列表", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/parent/{parentId}")
    public ResponseResult<List<Department>> getChildDepartments(@PathVariable Long parentId) {
        List<Department> departments = departmentService.getChildDepartments(parentId);
        return new ResponseResult<>(200, "获取子科室列表成功", departments);
    }

    @Operation(summary = "获取启用的子科室列表", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/active/parent/{parentId}")
    public ResponseResult<List<Department>> getActiveChildDepartments(@PathVariable Long parentId) {
        List<Department> departments = departmentService.getActiveChildDepartments(parentId);
        return new ResponseResult<>(200, "获取启用子科室列表成功", departments);
    }

    @Operation(summary = "修改科室状态", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseResult<Void> changeDepartmentStatus(@PathVariable Long id, @RequestParam Integer status) {  // 修正为@RequestParam
        departmentService.changeDepartmentStatus(id, status);
        return new ResponseResult<>(200, "修改科室状态成功", null);
    }
}