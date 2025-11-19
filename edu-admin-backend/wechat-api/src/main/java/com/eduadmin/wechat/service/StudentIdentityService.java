package com.eduadmin.wechat.service;

import com.eduadmin.wechat.mapper.StudentWechatMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class StudentIdentityService {
    private final StudentWechatMapper studentWechatMapper;

    public StudentIdentityService(StudentWechatMapper studentWechatMapper) {
        this.studentWechatMapper = studentWechatMapper;
    }

    /**
     * Resolve current studentId from request headers or fallback param.
     * Priority: X-Student-Id -> X-Openid -> Authorization: Bearer wechat <openid>
     */
    public Long resolveStudentId(HttpServletRequest request, Long fallbackStudentId) {
        // 1) Explicit student id header (useful for local dev)
        String sid = request.getHeader("X-Student-Id");
        if (sid != null && sid.trim().length() > 0) {
            try { return Long.valueOf(sid.trim()); } catch (Exception ignore) { /* no-op */ }
        }
        // 2) Wechat openid header
        String openid = request.getHeader("X-Openid");
        if (openid == null || openid.trim().isEmpty()) {
            // 3) Authorization bearer token: "Bearer wechat <openid>"
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.toLowerCase().startsWith("bearer wechat ")) {
                openid = auth.substring("bearer wechat ".length()).trim();
            }
        }
        if (openid != null && !openid.trim().isEmpty()) {
            Long studentId = studentWechatMapper.findStudentIdByOpenid(openid.trim());
            if (studentId != null) return studentId;
        }
        // 4) fallback to param
        return fallbackStudentId != null ? fallbackStudentId : 1L;
    }
}