package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.LeaveRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LeaveRequestMapper {
    int insert(LeaveRequest req);
    List<LeaveRequest> listByStudentId(@Param("studentId") Long studentId);
}