package com.eduadmin.notify.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.notify.entity.UserMessage;
import com.eduadmin.notify.repo.UserMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notify/message")
public class MessageController {

    @Autowired
    private UserMessageRepository repository;

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(name = "username", required = false) String username) {
        String u = (username == null || username.trim().isEmpty()) ? "admin" : username.trim();
        List<UserMessage> list = repository.findByUsernameOrderByCreatedAtDesc(u);
        // 初始化默认消息以便演示
        if (list.isEmpty()) {
            UserMessage m1 = new UserMessage(); m1.setUsername(u); m1.setText("15 人待续费，建议今日跟进"); m1.setRead(false);
            UserMessage m2 = new UserMessage(); m2.setUsername(u); m2.setText("本周排课已完成审核"); m2.setRead(true);
            UserMessage m3 = new UserMessage(); m3.setUsername(u); m3.setText("有 3 条新的报名记录"); m3.setRead(false);
            repository.saveAll(Arrays.asList(m1, m2, m3));
            list = repository.findByUsernameOrderByCreatedAtDesc(u);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (UserMessage m : list) out.add(toMap(m));
        return ApiResponse.success(out);
    }

    @PostMapping("/read")
    public ApiResponse<Map<String, Object>> markRead(@RequestBody Map<String, Object> payload) {
        Long id = toLong(payload.get("id"));
        if (id == null) return ApiResponse.error(400, "缺少参数 id");
        Optional<UserMessage> opt = repository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "消息不存在");
        UserMessage m = opt.get();
        m.setRead(true);
        repository.save(m);
        return ApiResponse.success(toMap(m));
    }

    @PostMapping("/read-all")
    public ApiResponse<Map<String, Object>> markAllRead(@RequestBody Map<String, Object> payload) {
        String username = String.valueOf(payload.getOrDefault("username", "")).trim();
        if (username.isEmpty()) username = "admin";
        List<UserMessage> list = repository.findByUsernameOrderByCreatedAtDesc(username);
        for (UserMessage m : list) { m.setRead(true); }
        repository.saveAll(list);
        Map<String, Object> out = new HashMap<>();
        out.put("username", username);
        out.put("updated", list.size());
        return ApiResponse.success(out);
    }

    private Map<String, Object> toMap(UserMessage m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("username", m.getUsername());
        map.put("text", m.getText());
        map.put("read", Boolean.TRUE.equals(m.getRead()));
        map.put("createdAt", m.getCreatedAt());
        return map;
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch(Exception e) { return null; }
    }
}