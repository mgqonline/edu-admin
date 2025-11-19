package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.StudentWechatBinding;
import org.apache.ibatis.annotations.Param;

public interface StudentWechatMapper {
    Long findStudentIdByOpenid(@Param("openid") String openid);
    int insertBinding(StudentWechatBinding binding);
}