package com.eduadmin.course.repo;

import com.eduadmin.course.entity.ClassSelectApply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSelectApplyRepository extends JpaRepository<ClassSelectApply, Long> {
    List<ClassSelectApply> findByClassId(Long classId);
    List<ClassSelectApply> findByClassIdAndStatus(Long classId, String status);
}