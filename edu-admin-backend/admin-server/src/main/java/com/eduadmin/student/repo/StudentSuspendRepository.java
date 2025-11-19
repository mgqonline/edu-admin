package com.eduadmin.student.repo;

import com.eduadmin.student.entity.StudentSuspend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentSuspendRepository extends JpaRepository<StudentSuspend, Long> {
    List<StudentSuspend> findByStudentIdOrderByIdDesc(Long studentId);
}