package com.eduadmin.attendance.repo;

import com.eduadmin.attendance.entity.AttendanceSignCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.Optional;

public interface AttendanceSignCodeRepository extends JpaRepository<AttendanceSignCode, Long> {
    Optional<AttendanceSignCode> findTopByCodeAndExpireAtAfter(String code, Date now);
    Optional<AttendanceSignCode> findTopByScheduleIdOrderByCreatedAtDesc(Long scheduleId);
}