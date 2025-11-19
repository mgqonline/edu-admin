package com.eduadmin.attendance.repo;

import com.eduadmin.attendance.entity.MakeupApply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.List;

public interface MakeupApplyRepository extends JpaRepository<MakeupApply, Long> {
    List<MakeupApply> findByStudentIdAndCreatedAtBetween(Long studentId, Date start, Date end);
}