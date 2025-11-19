package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.HomeworkSubmission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HomeworkSubmissionMapper {
    int insert(HomeworkSubmission sub);
    List<HomeworkSubmission> listByStudentId(@Param("studentId") Long studentId);
}