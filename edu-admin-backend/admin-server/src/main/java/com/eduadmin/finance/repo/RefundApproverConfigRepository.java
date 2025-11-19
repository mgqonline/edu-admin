package com.eduadmin.finance.repo;

import com.eduadmin.finance.entity.RefundApproverConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundApproverConfigRepository extends JpaRepository<RefundApproverConfig, Long> {
    Optional<RefundApproverConfig> findTopByOrderByIdAsc();
}