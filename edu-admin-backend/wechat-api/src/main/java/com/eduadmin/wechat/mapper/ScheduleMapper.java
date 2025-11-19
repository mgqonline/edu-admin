package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.ScheduleItem;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ScheduleMapper {
    List<ScheduleItem> listTodayByStudent(@Param("studentId") Long studentId);
    List<ScheduleItem> listByStudentBetween(@Param("studentId") Long studentId,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);
}