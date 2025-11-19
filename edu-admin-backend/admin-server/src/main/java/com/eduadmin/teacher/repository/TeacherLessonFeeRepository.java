package com.eduadmin.teacher.repository;

import com.eduadmin.teacher.entity.TeacherLessonFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherLessonFeeRepository extends JpaRepository<TeacherLessonFee, Long> {
    Optional<TeacherLessonFee> findByScheduleId(Long scheduleId);
    List<TeacherLessonFee> findByTeacherIdAndLessonDateBetween(Long teacherId, Date start, Date end);
    List<TeacherLessonFee> findByTeacherIdAndLessonDateBetweenAndCourseType(Long teacherId, Date start, Date end, String courseType);
    List<TeacherLessonFee> findByLessonDateBetween(Date start, Date end);
}