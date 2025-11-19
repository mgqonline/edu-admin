package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.ParentMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ParentMessageMapper {
    void insert(ParentMessage msg);
    void reply(@Param("id") Long id, @Param("reply") String reply);
    List<ParentMessage> listByStudent(@Param("studentId") Long studentId);
    List<ParentMessage> listByTeacher(@Param("teacherId") Long teacherId);
}