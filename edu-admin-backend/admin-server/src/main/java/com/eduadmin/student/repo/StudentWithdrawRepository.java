package com.eduadmin.student.repo;

import com.eduadmin.student.entity.StudentWithdraw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentWithdrawRepository extends JpaRepository<StudentWithdraw, Long> {
    List<StudentWithdraw> findByStudentIdOrderByIdDesc(Long studentId);
}