package com.eduadmin.finance.repo;

import com.eduadmin.finance.entity.InvoiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface InvoiceRecordRepository extends JpaRepository<InvoiceRecord, Long> {
    List<InvoiceRecord> findAllByOrderByIdDesc();
    List<InvoiceRecord> findByInvoiceDateBetween(Date start, Date end);
    List<InvoiceRecord> findByCampusIdAndInvoiceDateBetween(Long campusId, Date start, Date end);
}