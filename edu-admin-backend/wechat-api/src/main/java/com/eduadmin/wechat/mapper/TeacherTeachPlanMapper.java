package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.TeacherTeachPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeacherTeachPlanMapper {
    void insert(TeacherTeachPlan plan);
    List<TeacherTeachPlan> listByTeacher(@Param("teacherId") Long teacherId);
}