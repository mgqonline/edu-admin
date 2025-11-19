package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.AdjustRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdjustRequestMapper {
    int insert(AdjustRequest req);
    List<AdjustRequest> listByTeacher(@Param("teacherId") Long teacherId);
}