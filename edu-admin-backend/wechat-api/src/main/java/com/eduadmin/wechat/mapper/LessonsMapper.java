package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.RemainingLessonsItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LessonsMapper {
    List<RemainingLessonsItem> remainingByStudent(@Param("studentId") Long studentId);
}