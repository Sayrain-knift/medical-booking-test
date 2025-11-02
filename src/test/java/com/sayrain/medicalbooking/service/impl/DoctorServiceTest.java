package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.DoctorDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.model.Doctor;
import com.sayrain.medicalbooking.repository.DepartmentRepository;
import com.sayrain.medicalbooking.repository.DoctorRepository;
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
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Doctor doctor;
    private DoctorDTO doctorDTO;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("内科");
        department.setStatus(1);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("张医生");
        doctor.setTitle("主任医师");
        doctor.setDepartment(department);
        doctor.setStatus(1);
        doctor.setDescription("心血管专家");
        doctor.setSpecialty("心血管疾病");

        doctorDTO = new DoctorDTO();
        doctorDTO.setName("张医生");
        doctorDTO.setTitle("主任医师");
        doctorDTO.setDepartmentId(1L);
        doctorDTO.setStatus(1);
        doctorDTO.setDescription("心血管专家");
        doctorDTO.setSpecialty("心血管疾病");
    }

    @Test
    void testGetDoctorsByDepartment_CacheHit() {
        // 准备缓存数据
        List<Doctor> cachedDoctors = Arrays.asList(doctor);
        when(redisService.getCachedDoctorsByDepartment(1L)).thenReturn(cachedDoctors);

        // 执行
        List<Doctor> result = doctorService.getDoctorsByDepartment(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("张医生", result.get(0).getName());
        verify(redisService, times(1)).getCachedDoctorsByDepartment(1L);
        verify(doctorRepository, never()).findByDepartmentId(1L);
    }

    @Test
    void testGetDoctorsByDepartment_CacheMiss() {
        // 准备：模拟缓存未命中
        List<Doctor> expectedDoctors = Arrays.asList(doctor);

        // 只保留实际被调用的桩方法
        when(redisService.getCachedDoctorsByDepartment(1L)).thenReturn(null);
        when(doctorRepository.findByDepartmentId(1L)).thenReturn(expectedDoctors);

        // 执行
        List<Doctor> result = doctorService.getDoctorsByDepartment(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisService, times(1)).getCachedDoctorsByDepartment(1L);
        verify(doctorRepository, times(1)).findByDepartmentId(1L);
        verify(redisService, times(1)).cacheDoctorsByDepartment(1L, expectedDoctors);
    }

    @Test
    void testCreateDoctor_Success() {
        // 准备
        when(doctorRepository.findByNameAndDepartmentId("张医生", 1L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // 执行
        Doctor result = doctorService.createDoctor(doctorDTO);

        // 验证
        assertNotNull(result);
        assertEquals("张医生", result.getName());
        verify(doctorRepository, times(1)).save(any(Doctor.class));
        verify(redisService, times(1)).clearDoctorCache(1L);
    }

    @Test
    void testCreateDoctor_DuplicateNameInDepartment() {
        // 准备
        when(doctorRepository.findByNameAndDepartmentId("张医生", 1L)).thenReturn(Optional.of(doctor));

        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class,
                () -> doctorService.createDoctor(doctorDTO));

        assertEquals("该科室下已存在同名医生", exception.getMessage());
        verify(doctorRepository, never()).save(any(Doctor.class));
        verify(redisService, never()).clearDoctorCache(1L);
    }

    @Test
    void testUpdateDoctor_DepartmentChange() {
        // 准备
        Doctor existingDoctor = new Doctor();
        existingDoctor.setId(1L);
        existingDoctor.setName("张医生");
        existingDoctor.setDepartment(department); // 原科室ID=1

        Department newDepartment = new Department();
        newDepartment.setId(2L);
        newDepartment.setName("外科");
        newDepartment.setStatus(1);

        DoctorDTO updateDTO = new DoctorDTO();
        updateDTO.setName("张医生");
        updateDTO.setDepartmentId(2L);
        updateDTO.setTitle("主任医师");

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existingDoctor));
        when(doctorRepository.findByNameAndDepartmentId("张医生", 2L)).thenReturn(Optional.empty());
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(newDepartment));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(existingDoctor);

        // 执行
        Doctor result = doctorService.updateDoctor(1L, updateDTO);

        // 验证：科室变更时应该清除新旧两个科室的缓存
        assertNotNull(result);
        verify(redisService, times(1)).clearDoctorCache(1L); // 原科室
        verify(redisService, times(1)).clearDoctorCache(2L); // 新科室
    }

    @Test
    void testUpdateDoctor_SameDepartment() {
        // 准备
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.findByNameAndDepartmentId("张医生", 1L)).thenReturn(Optional.of(doctor));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // 执行
        Doctor result = doctorService.updateDoctor(1L, doctorDTO);

        // 验证：科室未变更时只清除当前科室缓存
        assertNotNull(result);
        verify(redisService, times(1)).clearDoctorCache(1L);
        verify(redisService, never()).clearDoctorCache(2L);
    }

    @Test
    void testDeleteDoctor_Success() {
        // 准备
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // 执行
        doctorService.deleteDoctor(1L);

        // 验证
        verify(doctorRepository, times(1)).delete(doctor);
        verify(redisService, times(1)).clearDoctorCache(1L);
    }

    @Test
    void testChangeDoctorStatus_Success() {
        // 准备
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // 执行
        doctorService.changeDoctorStatus(1L, 0);

        // 验证
        verify(doctorRepository, times(1)).save(any(Doctor.class));
        verify(redisService, times(1)).clearDoctorCache(1L);
    }
}