package com.eduadmin.student.repo;

import com.eduadmin.student.entity.GuardianPerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardianPermRepository extends JpaRepository<GuardianPerm, Long> {
    List<GuardianPerm> findByStudentIdOrderByIdDesc(Long studentId);
    Optional<GuardianPerm> findByStudentIdAndGuardianPhone(Long studentId, String guardianPhone);
}