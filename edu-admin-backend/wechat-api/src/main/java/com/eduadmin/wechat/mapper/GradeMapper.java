package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.GradeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GradeMapper {
    int insert(GradeRecord record);
    List<GradeRecord> listByStudent(@Param("studentId") Long studentId);
}