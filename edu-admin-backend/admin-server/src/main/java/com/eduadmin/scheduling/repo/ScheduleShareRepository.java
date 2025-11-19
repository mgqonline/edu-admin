package com.eduadmin.scheduling.repo;

import com.eduadmin.scheduling.entity.ScheduleShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleShareRepository extends JpaRepository<ScheduleShare, Long> {
    Optional<ScheduleShare> findByShareToken(String shareToken);
}