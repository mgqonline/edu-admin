package com.eduadmin.teacher.repository;

import com.eduadmin.teacher.entity.TeacherLessonAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherLessonAdjustmentRepository extends JpaRepository<TeacherLessonAdjustment, Long> {
    Optional<TeacherLessonAdjustment> findFirstByScheduleId(Long scheduleId);
    List<TeacherLessonAdjustment> findByTeacherIdAndScheduleIdIn(Long teacherId, List<Long> scheduleIds);
}