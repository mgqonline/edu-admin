package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.PaymentItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaymentMapper {
    List<PaymentItem> listByStudent(@Param("studentId") Long studentId);
}