package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.StudentFeedback;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentFeedbackMapper {
    void insert(StudentFeedback fb);
    List<StudentFeedback> listByStudent(@Param("studentId") Long studentId);
}