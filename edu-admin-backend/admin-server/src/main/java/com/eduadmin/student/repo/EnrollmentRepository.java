package com.eduadmin.student.repo;

import com.eduadmin.student.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    Enrollment findTopByStudentIdOrderByIdDesc(Long studentId);
}