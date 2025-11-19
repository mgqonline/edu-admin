package com.eduadmin.baseplatform.staff.repo;

import com.eduadmin.baseplatform.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByCampusId(Long campusId);
    List<Staff> findByDeptId(Long deptId);
    List<Staff> findByCampusIdAndDeptId(Long campusId, Long deptId);
    Optional<Staff> findByUsername(String username);
}