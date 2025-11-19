package com.eduadmin.course.repo;

import com.eduadmin.course.entity.TextbookInventoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextbookInventoryRecordRepository extends JpaRepository<TextbookInventoryRecord, Long> {
    List<TextbookInventoryRecord> findByTextbookIdOrderByTimeAsc(String textbookId);
}