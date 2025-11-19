package com.eduadmin.wechat.mapper;

import com.eduadmin.wechat.domain.TeacherWechatBinding;
import org.apache.ibatis.annotations.Param;

public interface TeacherWechatMapper {
    Long findTeacherIdByOpenid(@Param("openid") String openid);
    int insertBinding(TeacherWechatBinding binding);
}