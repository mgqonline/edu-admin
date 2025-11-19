package com.eduadmin.finance.repo;

import com.eduadmin.finance.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findAllByOrderByIdDesc();
}