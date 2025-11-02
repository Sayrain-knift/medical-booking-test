package com.sayrain.medicalbooking.repository;

import com.sayrain.medicalbooking.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByDepartmentId(Long departmentId);
    List<Doctor> findByDepartmentIdAndStatus(Long departmentId, Integer status);
    Optional<Doctor> findByName(String name);
    List<Doctor> findByStatus(Integer status);
    List<Doctor> findByTitleContaining(String title);

    //  新增关键方法：检查同一科室下同名医生
    Optional<Doctor> findByNameAndDepartmentId(String name, Long departmentId);
}