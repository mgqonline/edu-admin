package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.TeacherHomework;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeacherHomeworkMapper {
    int insert(TeacherHomework hw);
    List<TeacherHomework> listByTeacher(@Param("teacherId") Long teacherId);
}