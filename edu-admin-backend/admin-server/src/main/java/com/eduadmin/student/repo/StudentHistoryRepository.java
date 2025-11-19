package com.eduadmin.student.repo;

import com.eduadmin.student.entity.StudentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentHistoryRepository extends JpaRepository<StudentHistory, Long> {
    List<StudentHistory> findByStudentIdOrderByTimeDesc(Long studentId);
}