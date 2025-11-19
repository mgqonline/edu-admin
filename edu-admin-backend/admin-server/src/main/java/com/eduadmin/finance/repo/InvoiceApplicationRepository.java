package com.eduadmin.finance.repo;

import com.eduadmin.finance.entity.InvoiceApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface InvoiceApplicationRepository extends JpaRepository<InvoiceApplication, Long> {
    List<InvoiceApplication> findAllByOrderByIdDesc();
    List<InvoiceApplication> findByStatusOrderByIdDesc(String status);
    List<InvoiceApplication> findByCreatedAtBetween(Date start, Date end);
}