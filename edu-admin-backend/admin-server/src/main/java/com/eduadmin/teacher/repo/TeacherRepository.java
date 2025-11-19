package com.eduadmin.teacher.repo;

import com.eduadmin.teacher.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findByStatus(String status);
    List<Teacher> findByNameContaining(String keyword);
    List<Teacher> findByCampusId(Long campusId);
    List<Teacher> findByCampusIdAndStatus(Long campusId, String status);
}