package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.TeacherClassNotice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeacherClassNoticeMapper {
    void insert(TeacherClassNotice notice);
    List<TeacherClassNotice> listByTeacher(@Param("teacherId") Long teacherId);
}