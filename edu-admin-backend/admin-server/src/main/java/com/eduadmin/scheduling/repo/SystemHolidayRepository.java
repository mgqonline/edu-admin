package com.eduadmin.scheduling.repo;

import com.eduadmin.scheduling.entity.SystemHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface SystemHolidayRepository extends JpaRepository<SystemHoliday, Long> {
    boolean existsByDateOnly(Date dateOnly);
    List<SystemHoliday> findByDateOnlyBetween(Date startDate, Date endDate);
}