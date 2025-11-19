package com.eduadmin.course.repo;

import com.eduadmin.course.entity.ClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {
    long countByClassIdAndStatus(Long classId, String status);
    List<ClassEnrollment> findByClassIdAndStatus(Long classId, String status);
    List<ClassEnrollment> findByStudentId(Long studentId);
    boolean existsByClassIdAndStudentIdAndStatus(Long classId, Long studentId, String status);
}