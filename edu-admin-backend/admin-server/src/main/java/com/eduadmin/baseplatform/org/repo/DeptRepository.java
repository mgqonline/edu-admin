package com.eduadmin.baseplatform.org.repo;

import com.eduadmin.baseplatform.org.entity.Dept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeptRepository extends JpaRepository<Dept, Long> {
    List<Dept> findByCampusIdOrderBySortOrderAsc(Long campusId);
    List<Dept> findByParentIdOrderBySortOrderAsc(Long parentId);
}