package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.dto.DepartmentDTO;
import com.sayrain.medicalbooking.model.Department;
import java.util.List;

public interface DepartmentService {
    Department createDepartment(DepartmentDTO departmentDTO);
    Department updateDepartment(Long id, DepartmentDTO departmentDTO);
    void deleteDepartment(Long id);
    Department getDepartmentById(Long id);
    List<Department> getAllDepartments();
    List<Department> getActiveDepartments();
    List<Department> getChildDepartments(Long parentId);
    List<Department> getActiveChildDepartments(Long parentId);
    void changeDepartmentStatus(Long id, Integer status);
}