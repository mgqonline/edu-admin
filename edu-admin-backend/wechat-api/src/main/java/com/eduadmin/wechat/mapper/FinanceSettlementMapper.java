package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.FinanceSettlement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FinanceSettlementMapper {
    List<FinanceSettlement> listByStudent(@Param("studentId") Long studentId);
}