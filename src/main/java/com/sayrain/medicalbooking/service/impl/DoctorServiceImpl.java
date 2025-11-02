package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.DoctorDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Doctor;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.repository.DoctorRepository;
import com.sayrain.medicalbooking.repository.DepartmentRepository;
import com.sayrain.medicalbooking.service.DoctorService;
import com.sayrain.medicalbooking.util.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final RedisService redisService;

    // 创建医生
    @Override
    @Transactional
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

        // 清除该科室缓存
        try {
            redisService.clearDoctorCache(doctorDTO.getDepartmentId());
        } catch (Exception e) {
            log.warn("创建医生后清除缓存失败，departmentId={}，原因：{}", doctorDTO.getDepartmentId(), e.getMessage());
        }

        return savedDoctor;
    }

    // 更新医生
    @Override
    @Transactional
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

        // 缓存清理逻辑
        try {
            if (!oldDeptId.equals(newDeptId)) {
                redisService.clearDoctorCache(oldDeptId);
                redisService.clearDoctorCache(newDeptId);
                log.info("医生科室发生变化，已清除旧科室({})与新科室({})缓存", oldDeptId, newDeptId);
            } else {
                redisService.clearDoctorCache(newDeptId);
                log.info("医生科室未变化，已清除科室({})缓存", newDeptId);
            }
        } catch (Exception e) {
            log.warn("更新医生后清除缓存失败，departmentId={}，原因：{}", newDeptId, e.getMessage());
        }

        return updatedDoctor;
    }

    // 删除医生
    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));

        Long deptId = doctor.getDepartment().getId();

        doctorRepository.delete(doctor);
        log.info("删除医生成功：{}", doctor.getName());

        try {
            redisService.clearDoctorCache(deptId);
        } catch (Exception e) {
            log.warn("删除医生后清除缓存失败，departmentId={}，原因：{}", deptId, e.getMessage());
        }
    }

    // 根据ID获取医生
    @Override
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
    }

    // 获取所有医生
    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // 根据科室ID获取医生（含缓存逻辑）
    @Override
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        try {
            Object cached = redisService.getCachedDoctorsByDepartment(departmentId);
            if (cached != null && cached instanceof List) {
                log.info("从缓存中获取医生列表：departmentId={}", departmentId);
                return (List<Doctor>) cached;
            }
        } catch (Exception e) {
            log.warn("读取医生缓存失败，departmentId={}，原因：{}", departmentId, e.getMessage());
        }

        List<Doctor> doctors = doctorRepository.findByDepartmentId(departmentId);
        try {
            redisService.cacheDoctorsByDepartment(departmentId, doctors);
            log.info("已缓存医生列表：departmentId={}，数量={}", departmentId, doctors.size());
        } catch (Exception e) {
            log.warn("缓存医生列表失败，departmentId={}，原因：{}", departmentId, e.getMessage());
        }

        return doctors;
    }

    // 获取所有启用医生
    @Override
    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findByStatus(1);
    }

    // 根据科室ID获取启用医生
    @Override
    public List<Doctor> getActiveDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndStatus(departmentId, 1);
    }

    // 修改医生状态
    @Override
    @Transactional
    public void changeDoctorStatus(Long id, Integer status) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
        doctor.setStatus(status);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);
        log.info("修改医生状态成功：{}，状态：{}", doctor.getName(), status);

        try {
            redisService.clearDoctorCache(doctor.getDepartment().getId());
        } catch (Exception e) {
            log.warn("修改医生状态后清除缓存失败，departmentId={}，原因：{}", doctor.getDepartment().getId(), e.getMessage());
        }
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
    public void updateDoctorImageUrl(Long id, String imageUrl) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
        doctor.setImageUrl(imageUrl);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        // 清除该科室缓存
        try {
            redisService.clearDoctorCache(doctor.getDepartment().getId());
        } catch (Exception e) {
            log.warn("更新医生图片后清除缓存失败，departmentId={}，原因：{}", doctor.getDepartment().getId(), e.getMessage());
        }
    }

}
