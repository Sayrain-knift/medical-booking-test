package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.DoctorDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Doctor;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.repository.DoctorRepository;
import com.sayrain.medicalbooking.repository.DepartmentRepository;
import com.sayrain.medicalbooking.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Profile("!minimal")
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    // 创建医生
    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public Doctor createDoctor(DoctorDTO doctorDTO) {
        if (doctorRepository.findByNameAndDepartmentId(doctorDTO.getName(), doctorDTO.getDepartmentId()).isPresent()) {
            throw new BusinessException("该科室下已存在同名医生");
        }

        Department department = departmentRepository.findById(doctorDTO.getDepartmentId())
                .orElseThrow(() -> new BusinessException("科室不存在"));
        if (department.getStatus() != 1) {
            throw new BusinessException("所属科室状态为停用，无法创建医生");
        }

        Doctor doctor = new Doctor();
        doctor.setName(doctorDTO.getName());
        doctor.setTitle(doctorDTO.getTitle());
        doctor.setDepartment(department);
        doctor.setStatus(doctorDTO.getStatus() != null ? doctorDTO.getStatus() : 1);
        doctor.setDescription(doctorDTO.getDescription());
        doctor.setSpecialty(doctorDTO.getSpecialty());
        doctor.setImageUrl(doctorDTO.getImageUrl());

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("创建医生成功：{}", doctorDTO.getName());

        return savedDoctor;
    }

    // 更新医生
    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public Doctor updateDoctor(Long id, DoctorDTO doctorDTO) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));

        doctorRepository.findByNameAndDepartmentId(doctorDTO.getName(), doctorDTO.getDepartmentId())
                .ifPresent(dupDoctor -> {
                    if (!dupDoctor.getId().equals(id)) {
                        throw new BusinessException("该科室下已存在同名医生");
                    }
                });

        Department newDepartment = departmentRepository.findById(doctorDTO.getDepartmentId())
                .orElseThrow(() -> new BusinessException("科室不存在"));
        if (newDepartment.getStatus() != 1) {
            throw new BusinessException("所属科室状态为停用，无法更新医生信息");
        }

        Long oldDeptId = existingDoctor.getDepartment().getId();
        Long newDeptId = doctorDTO.getDepartmentId();

        existingDoctor.setName(doctorDTO.getName());
        existingDoctor.setTitle(doctorDTO.getTitle());
        existingDoctor.setDepartment(newDepartment);
        if (doctorDTO.getStatus() != null) {
            existingDoctor.setStatus(doctorDTO.getStatus());
        }
        existingDoctor.setDescription(doctorDTO.getDescription());
        existingDoctor.setSpecialty(doctorDTO.getSpecialty());
        existingDoctor.setImageUrl(doctorDTO.getImageUrl());

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        log.info("更新医生成功：{}", doctorDTO.getName());

        return updatedDoctor;
    }

    // 删除医生
    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));

        doctorRepository.delete(doctor);
        log.info("删除医生成功：{}", doctor.getName());
    }

    // 根据ID获取医生
    @Override
    @Cacheable(value = "doctors", key = "#id", unless = "#result == null")
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
    }

    // 获取所有医生
    @Override
    @Cacheable(value = "doctors", key = "'all'", unless = "#result == null or #result.size() == 0")
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // 根据科室ID获取医生
    @Override
    @Cacheable(value = "doctors", key = "'department:' + #departmentId", unless = "#result == null or #result.size() == 0")
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentId(departmentId);
    }

    // 获取所有启用医生
    @Override
    @Cacheable(value = "doctors", key = "'active'", unless = "#result == null or #result.size() == 0")
    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findByStatus(1);
    }

    // 根据科室ID获取启用医生
    @Override
    @Cacheable(value = "doctors", key = "'active-department:' + #departmentId", unless = "#result == null or #result.size() == 0")
    public List<Doctor> getActiveDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndStatus(departmentId, 1);
    }

    // 修改医生状态
    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void changeDoctorStatus(Long id, Integer status) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
        doctor.setStatus(status);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);
        log.info("修改医生状态成功：{}，状态：{}", doctor.getName(), status);
    }

    // 检查医生是否启用
    @Override
    public boolean isDoctorActive(Long doctorId) {
        try {
            Doctor doctor = getDoctorById(doctorId);
            return doctor.getStatus() == 1;
        } catch (BusinessException e) {
            return false;
        }
    }

    // 在File: src/main/java/com/sayrain/medicalbooking/service/impl/DoctorServiceImpl.java中添加实现
    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void updateDoctorImageUrl(Long id, String imageUrl) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
        doctor.setImageUrl(imageUrl);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);
    }

}
