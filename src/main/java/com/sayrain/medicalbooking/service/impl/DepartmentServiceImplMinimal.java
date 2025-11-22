package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.DepartmentDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.repository.DepartmentRepository;
import com.sayrain.medicalbooking.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Department Service 的 minimal profile 实现
 * 不使用Redis缓存，直接查询数据库
 */
@Slf4j
@Service
@Profile("minimal")
@RequiredArgsConstructor
public class DepartmentServiceImplMinimal implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    // 创建科室
    @Override
    @Transactional
    public Department createDepartment(DepartmentDTO departmentDTO) {
        if (departmentRepository.findByName(departmentDTO.getName()).isPresent()) {
            throw new BusinessException("科室名称已存在");
        }

        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setParentId(departmentDTO.getParentId());
        department.setStatus(departmentDTO.getStatus());
        department.setIcon(departmentDTO.getIcon());
        department.setSortOrder(departmentDTO.getSortOrder() != null ? departmentDTO.getSortOrder() : 1);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());

        Department savedDepartment = departmentRepository.save(department);
        log.info("[Minimal] 创建科室成功：{}", departmentDTO.getName());

        return savedDepartment;
    }

    // 更新科室
    @Override
    @Transactional
    public Department updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科室不存在"));

        if (departmentRepository.findByNameAndIdNot(departmentDTO.getName(), id).isPresent()) {
            throw new BusinessException("科室名称已存在");
        }

        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department.setParentId(departmentDTO.getParentId());
        department.setStatus(departmentDTO.getStatus());
        department.setIcon(departmentDTO.getIcon());
        department.setSortOrder(departmentDTO.getSortOrder() != null ? departmentDTO.getSortOrder() : department.getSortOrder());
        department.setUpdatedAt(LocalDateTime.now());

        Department updatedDepartment = departmentRepository.save(department);
        log.info("[Minimal] 更新科室成功：{}", departmentDTO.getName());

        return updatedDepartment;
    }

    // 删除科室
    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科室不存在"));

        List<Department> childDepartments = departmentRepository.findByParentId(id);
        if (!childDepartments.isEmpty()) {
            throw new BusinessException("请先删除子科室");
        }

        departmentRepository.delete(department);
        log.info("[Minimal] 删除科室成功：{}", department.getName());
    }

    // 根据ID获取科室
    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科室不存在"));
    }

    // 获取所有科室（不使用缓存）
    @Override
    public List<Department> getAllDepartments() {
        log.info("[Minimal] 直接查询数据库获取科室列表");
        List<Department> departments = departmentRepository.findAllByOrderBySortOrderAsc();
        log.info("[Minimal] 查询到 {} 个科室", departments.size());
        return departments;
    }

    // 获取所有启用的科室
    @Override
    public List<Department> getActiveDepartments() {
        return departmentRepository.findByStatus(1);
    }

    // 获取指定父级ID的子科室
    @Override
    public List<Department> getChildDepartments(Long parentId) {
        return departmentRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }

    // 获取指定父级ID的启用子科室
    @Override
    public List<Department> getActiveChildDepartments(Long parentId) {
        return departmentRepository.findByParentIdAndStatusOrderBySortOrderAsc(parentId, 1);
    }

    // 修改科室状态
    @Override
    @Transactional
    public void changeDepartmentStatus(Long id, Integer status) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科室不存在"));
        department.setStatus(status);
        department.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(department);
        log.info("[Minimal] 修改科室状态：{} -> {}", department.getName(), status);
    }
}