package com.eduadmin.student.repo;

import com.eduadmin.student.entity.StudentTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentTransferRepository extends JpaRepository<StudentTransfer, Long> {
    List<StudentTransfer> findByStudentIdOrderByIdDesc(Long studentId);
}