package com.eduadmin.student.repo;

import com.eduadmin.student.entity.Audition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditionRepository extends JpaRepository<Audition, Long> {
    List<Audition> findByStudentId(Long studentId);
}