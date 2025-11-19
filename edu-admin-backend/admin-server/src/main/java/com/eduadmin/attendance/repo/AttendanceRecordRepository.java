package com.eduadmin.attendance.repo;

import com.eduadmin.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByScheduleId(Long scheduleId);
    List<AttendanceRecord> findByStudentId(Long studentId);
    List<AttendanceRecord> findByStudentIdAndSignTimeBetween(Long studentId, Date start, Date end);
}