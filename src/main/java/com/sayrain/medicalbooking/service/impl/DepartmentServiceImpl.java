package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.DepartmentDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.repository.DepartmentRepository;
import com.sayrain.medicalbooking.service.DepartmentService;
import com.sayrain.medicalbooking.util.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final RedisService redisService;

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
        log.info("创建科室成功：{}", departmentDTO.getName());

        // 清除缓存
        clearDepartmentCacheSafely("createDepartment");

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
        log.info("更新科室成功：{}", departmentDTO.getName());

        // 清除缓存
        clearDepartmentCacheSafely("updateDepartment");

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
        log.info("删除科室成功：{}", department.getName());

        // 清除缓存
        clearDepartmentCacheSafely("deleteDepartment");
    }

    // 根据ID获取科室
    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科室不存在"));
    }

    // 获取所有科室（带缓存）
    @Override
    public List<Department> getAllDepartments() {
        try {
            // 尝试从Redis获取
            Object cachedData = redisService.getCachedDepartments();
            if (cachedData != null) {
                log.info("[Redis] 从缓存中获取科室列表");
                return (List<Department>) cachedData;
            }
        } catch (Exception e) {
            log.warn("[Redis] 获取缓存失败，降级到数据库查询: {}", e.getMessage());
        }

        log.info("[Redis] 缓存未命中，查询数据库");
        List<Department> departments = departmentRepository.findAllByOrderBySortOrderAsc();

        // 尝试缓存到Redis
        cacheDepartmentSafely(departments);

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
        log.info("修改科室状态：{} -> {}", department.getName(), status);

        // 清除缓存
        clearDepartmentCacheSafely("changeDepartmentStatus");
    }

    /**
     * 安全地清除科室缓存，避免Redis异常影响主要业务
     */
    private void clearDepartmentCacheSafely(String operation) {
        try {
            redisService.clearDepartmentCache();
            log.info("[Redis] 已清除科室缓存（{}）", operation);
        } catch (Exception e) {
            log.warn("[Redis] 清除缓存失败（{}）：{}", operation, e.getMessage());
        }
    }

    /**
     * 安全地缓存科室数据，避免Redis异常影响主要业务
     */
    private void cacheDepartmentSafely(List<Department> departments) {
        try {
            redisService.cacheDepartment(departments);
            log.info("[Redis] 已缓存科室列表，共 {} 条", departments.size());
        } catch (Exception e) {
            log.warn("[Redis] 缓存失败，但不影响业务: {}", e.getMessage());
        }
    }
}