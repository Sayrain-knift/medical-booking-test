package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.dto.DoctorDTO;
import com.sayrain.medicalbooking.model.Doctor;

import java.util.List;

public interface DoctorService {

    // 创建医生
    Doctor createDoctor(DoctorDTO doctorDTO);

    // 更新医生信息
    Doctor updateDoctor(Long id, DoctorDTO doctorDTO);

    // 删除医生
    void deleteDoctor(Long id);

    // 根据ID获取医生
    Doctor getDoctorById(Long id);

    // 获取所有医生
    List<Doctor> getAllDoctors();

    // 根据科室ID获取医生
    List<Doctor> getDoctorsByDepartment(Long departmentId);

    // 获取所有启用的医生
    List<Doctor> getActiveDoctors();

    // 根据科室ID获取启用的医生
    List<Doctor> getActiveDoctorsByDepartment(Long departmentId);

    // 修改医生状态
    void changeDoctorStatus(Long id, Integer status);

    boolean isDoctorActive(Long doctorId);
    void updateDoctorImageUrl(Long id, String imageUrl);

}
