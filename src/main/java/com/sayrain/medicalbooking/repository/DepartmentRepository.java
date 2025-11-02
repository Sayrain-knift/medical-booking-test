package com.sayrain.medicalbooking.repository;

import com.sayrain.medicalbooking.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 根据科室名称查询
    Optional<Department> findByName(String name);

    // 根据科室状态查询
    List<Department> findByStatus(Integer status);

    // 根据父科室ID查询子科室
    List<Department> findByParentId(Long parentId);

    // 根据父科室ID并按sortOrder升序排列查询子科室
    List<Department> findByParentIdOrderBySortOrderAsc(Long parentId);

    // 根据父科室ID和状态查询子科室并按sortOrder升序排列
    List<Department> findByParentIdAndStatusOrderBySortOrderAsc(Long parentId, Integer status);

    // 根据科室名称和ID查重（更新时使用）
    Optional<Department> findByNameAndIdNot(String name, Long id);

    // 查询所有科室并按sortOrder升序排列
    List<Department> findAllByOrderBySortOrderAsc();
}
