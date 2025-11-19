package com.eduadmin.teacher.repository;

import com.eduadmin.teacher.entity.TeacherFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherFeeRuleRepository extends JpaRepository<TeacherFeeRule, Long> {
    List<TeacherFeeRule> findByCampusId(Long campusId);
    Optional<TeacherFeeRule> findFirstByCampusIdAndTeacherLevel(Long campusId, String teacherLevel);
    Optional<TeacherFeeRule> findFirstByTeacherLevel(String teacherLevel);
}