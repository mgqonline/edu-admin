package com.eduadmin.student.repo;

import com.eduadmin.student.entity.StudentReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentReferralRepository extends JpaRepository<StudentReferral, Long> {
    List<StudentReferral> findByReferrerIdOrderByIdAsc(Long referrerId);
    List<StudentReferral> findByNewStudentIdOrderByIdAsc(Long newStudentId);
    List<StudentReferral> findByReferrerIdAndNewStudentIdOrderByIdAsc(Long referrerId, Long newStudentId);
    boolean existsByReferrerIdAndNewStudentId(Long referrerId, Long newStudentId);
}