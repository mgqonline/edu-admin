package com.eduadmin.finance.repo;

import com.eduadmin.finance.entity.FinanceSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceSettlementRepository extends JpaRepository<FinanceSettlement, Long> {
    List<FinanceSettlement> findAllByOrderByIdDesc();
    List<FinanceSettlement> findByClassroomIdAndClassId(Long classroomId, Long classId);
    boolean existsByClassroomIdAndClassIdAndSeatNo(Long classroomId, Long classId, Integer seatNo);
}