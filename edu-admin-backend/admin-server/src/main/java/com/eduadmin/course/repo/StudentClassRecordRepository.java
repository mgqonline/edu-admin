package com.eduadmin.course.repo;

import com.eduadmin.course.entity.StudentClassRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentClassRecordRepository extends JpaRepository<StudentClassRecord, Long> {
    List<StudentClassRecord> findByStudentIdOrderByTimeAsc(Long studentId);
}