package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.DepartmentDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.repository.DepartmentRepository;
import com.sayrain.medicalbooking.util.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private DepartmentDTO departmentDTO;

    @BeforeEach
    void setUp() {
        // 设置测试数据 - 匹配您实际的Department实体结构
        department = new Department();
        department.setId(1L);
        department.setName("内科");
        department.setDescription("内科部门");
        department.setStatus(1);

        departmentDTO = new DepartmentDTO();
        departmentDTO.setName("内科");
        departmentDTO.setDescription("内科部门");
        departmentDTO.setStatus(1);
    }

    @Test
    void testGetAllDepartments_CacheHit() {
        // 准备：模拟缓存命中
        List<Department> cachedDepartments = Arrays.asList(department);
        when(redisService.getCachedDepartments()).thenReturn(cachedDepartments);

        // 执行
        List<Department> result = departmentService.getAllDepartments();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("内科", result.get(0).getName());
        verify(redisService).getCachedDepartments();
        verify(departmentRepository, never()).findAllByOrderBySortOrderAsc();
    }

    @Test
    void testGetAllDepartments_CacheMiss() {
        // 准备：模拟缓存未命中
        List<Department> dbDepartments = Arrays.asList(department);
        when(redisService.getCachedDepartments()).thenReturn(null);
        when(departmentRepository.findAllByOrderBySortOrderAsc()).thenReturn(dbDepartments);

        // 执行
        List<Department> result = departmentService.getAllDepartments();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisService).getCachedDepartments();
        verify(departmentRepository).findAllByOrderBySortOrderAsc();
        verify(redisService).cacheDepartment(dbDepartments);
    }

    @Test
    void testCreateDepartment_Success() {
        // 准备
        when(departmentRepository.findByName("内科")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // 执行
        Department result = departmentService.createDepartment(departmentDTO);

        // 验证
        assertNotNull(result);
        verify(departmentRepository).save(any(Department.class));
        verify(redisService).clearDepartmentCache();
    }

    @Test
    void testCreateDepartment_DuplicateName() {
        // 准备
        when(departmentRepository.findByName("内科")).thenReturn(Optional.of(department));

        // 执行和验证
        assertThrows(BusinessException.class, () -> {
            departmentService.createDepartment(departmentDTO);
        });

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void testGetDepartmentById_Success() {
        // 准备
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        // 执行
        Department result = departmentService.getDepartmentById(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("内科", result.getName());
    }

    @Test
    void testGetDepartmentById_NotFound() {
        // 准备
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行和验证
        assertThrows(BusinessException.class, () -> {
            departmentService.getDepartmentById(999L);
        });
    }
}