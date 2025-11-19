package com.eduadmin.course.repo;

import com.eduadmin.course.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long>, JpaSpecificationExecutor<ClassEntity> {
    List<ClassEntity> findByCourseId(Long courseId);
    List<ClassEntity> findByTeacherMainId(Long teacherMainId);
    List<ClassEntity> findByFixedTeacherId(Long fixedTeacherId);
    List<ClassEntity> findByCampusId(Long campusId);
    List<ClassEntity> findByStatus(String status);
}