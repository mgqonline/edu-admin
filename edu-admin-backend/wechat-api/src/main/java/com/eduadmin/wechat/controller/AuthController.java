package com.eduadmin.wechat.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.wechat.domain.StudentWechatBinding;
import com.eduadmin.wechat.domain.TeacherWechatBinding;
import com.eduadmin.wechat.mapper.StudentWechatMapper;
import com.eduadmin.wechat.mapper.TeacherWechatMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wechat/api/auth")
public class AuthController {

    private final StudentWechatMapper studentWechatMapper;
    private final TeacherWechatMapper teacherWechatMapper;

    public AuthController(StudentWechatMapper studentWechatMapper,
                          TeacherWechatMapper teacherWechatMapper) {
        this.studentWechatMapper = studentWechatMapper;
        this.teacherWechatMapper = teacherWechatMapper;
    }

    @PostMapping("/dev-login")
    public ApiResponse<Map<String, Object>> devLogin(@RequestBody Map<String, Object> payload) {
        String role = String.valueOf(payload.getOrDefault("role", "student")).toLowerCase();
        Long id = null;
        Object sid = payload.get("studentId");
        Object tid = payload.get("teacherId");
        if ("teacher".equals(role)) {
            if (tid instanceof Number) { id = ((Number) tid).longValue(); }
            else if (tid != null) { try { id = Long.valueOf(String.valueOf(tid)); } catch (Exception ignore) { } }
        } else {
            if (sid instanceof Number) { id = ((Number) sid).longValue(); }
            else if (sid != null) { try { id = Long.valueOf(String.valueOf(sid)); } catch (Exception ignore) { } }
        }
        if (id == null) {
            return ApiResponse.error(400, "缺少用户ID");
        }

        String openid = "dev-openid-" + role + "-" + id;
        if ("teacher".equals(role)) {
            Long existing = teacherWechatMapper.findTeacherIdByOpenid(openid);
            if (existing == null) {
                TeacherWechatBinding binding = new TeacherWechatBinding();
                binding.setTeacherId(id);
                binding.setOpenid(openid);
                teacherWechatMapper.insertBinding(binding);
            }
        } else {
            Long existing = studentWechatMapper.findStudentIdByOpenid(openid);
            if (existing == null) {
                StudentWechatBinding binding = new StudentWechatBinding();
                binding.setStudentId(id);
                binding.setOpenid(openid);
                studentWechatMapper.insertBinding(binding);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", "Bearer wechat " + openid);
        result.put("role", role);
        if ("teacher".equals(role)) result.put("teacherId", id); else result.put("studentId", id);
        return ApiResponse.success(result);
    }
}