package com.eduadmin.baseplatform.role.repo;

import com.eduadmin.baseplatform.role.entity.RolePerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermRepository extends JpaRepository<RolePerm, Long> {
    List<RolePerm> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
    List<RolePerm> findByValue(String value);
    void deleteByValue(String value);
}