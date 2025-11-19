package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.TeacherRoomRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeacherRoomRequestMapper {
    void insert(TeacherRoomRequest req);
    List<TeacherRoomRequest> listByTeacher(@Param("teacherId") Long teacherId);
}