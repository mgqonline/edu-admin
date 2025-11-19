package com.eduadmin.wechat.service;

import com.eduadmin.wechat.mapper.TeacherWechatMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class TeacherIdentityService {
    private final TeacherWechatMapper teacherWechatMapper;

    public TeacherIdentityService(TeacherWechatMapper teacherWechatMapper) {
        this.teacherWechatMapper = teacherWechatMapper;
    }

    /**
     * Resolve current teacherId from request.
     * Priority: X-Teacher-Id -> X-Openid -> Authorization: Bearer wechat <openid> -> fallback
     */
    public Long resolveTeacherId(HttpServletRequest request, Long fallbackTeacherId) {
        String tid = request.getHeader("X-Teacher-Id");
        if (tid != null && tid.trim().length() > 0) {
            try { return Long.valueOf(tid.trim()); } catch (Exception ignore) { /* no-op */ }
        }

        String openid = request.getHeader("X-Openid");
        if (openid == null || openid.trim().isEmpty()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.toLowerCase().startsWith("bearer wechat ")) {
                openid = auth.substring("bearer wechat ".length()).trim();
            }
        }
        if (openid != null && !openid.trim().isEmpty()) {
            Long teacherId = teacherWechatMapper.findTeacherIdByOpenid(openid.trim());
            if (teacherId != null) return teacherId;
        }
        return fallbackTeacherId != null ? fallbackTeacherId : 1L;
    }
}