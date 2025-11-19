package com.eduadmin.course.repo;

import com.eduadmin.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(String status);
    List<Course> findByNameContaining(String keyword);
    List<Course> findByStatusAndNameContaining(String status, String keyword);
}