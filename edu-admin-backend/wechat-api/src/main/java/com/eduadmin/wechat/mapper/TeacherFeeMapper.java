package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.TeacherFeeRecord;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TeacherFeeMapper {
    int insert(TeacherFeeRecord rec);
    BigDecimal sumByMonth(@Param("teacherId") Long teacherId, @Param("month") String month);
    List<TeacherFeeRecord> listByTeacher(@Param("teacherId") Long teacherId);
}