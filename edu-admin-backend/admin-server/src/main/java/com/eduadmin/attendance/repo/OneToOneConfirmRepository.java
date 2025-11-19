package com.eduadmin.attendance.repo;

import com.eduadmin.attendance.entity.OneToOneConfirm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OneToOneConfirmRepository extends JpaRepository<OneToOneConfirm, Long> {
    Optional<OneToOneConfirm> findTopByScheduleIdOrderByCreatedAtDesc(Long scheduleId);
}