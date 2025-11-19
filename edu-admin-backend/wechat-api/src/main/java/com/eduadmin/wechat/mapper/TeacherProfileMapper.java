package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.TeacherProfile;
import org.apache.ibatis.annotations.Param;

public interface TeacherProfileMapper {
    TeacherProfile getByTeacherId(@Param("teacherId") Long teacherId);
    int upsert(TeacherProfile profile);
}