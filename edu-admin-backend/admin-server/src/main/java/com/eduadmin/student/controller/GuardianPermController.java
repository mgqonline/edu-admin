package com.eduadmin.student.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.student.entity.GuardianPerm;
import com.eduadmin.student.service.GuardianService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student/guardian/perm")
public class GuardianPermController {

    @Autowired
    private GuardianService guardianService;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam Long studentId,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<GuardianPerm> all = guardianService.listByStudent(studentId);
        if (page == null || page < 1) page = 1; if (size == null || size < 1) size = 10;
        int total = all.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<GuardianPerm> pageList = all.subList(from, to);
        List<Map<String, Object>> items = new ArrayList<>();
        for (GuardianPerm g : pageList) items.add(toMap(g));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("total", total);
        out.put("page", page);
        out.put("size", size);
        out.put("pages", (int) Math.ceil(total * 1.0 / size));
        return ApiResponse.success(out);
    }

    @PostMapping("/set")
    public ApiResponse<Map<String, Object>> set(@RequestBody Map<String, Object> payload) {
        GuardianPerm updated = guardianService.setPerms(payload);
        return ApiResponse.success(toMap(updated));
    }

    @PostMapping("/gen")
    public ApiResponse<Map<String, Object>> gen(@RequestBody Map<String, Object> payload) {
        Long studentId = toLong(payload.get("studentId"));
        Integer count = Integer.valueOf(String.valueOf(payload.getOrDefault("count", 15)));
        List<GuardianPerm> list = guardianService.generate(studentId, count);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("generated", count);
        out.put("studentId", studentId);
        out.put("total", list.size());
        return ApiResponse.success(out);
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private Map<String, Object> toMap(GuardianPerm g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("studentId", g.getStudentId());
        m.put("guardianName", g.getGuardianName());
        m.put("guardianPhone", g.getGuardianPhone());
        m.put("relation", g.getRelation());
        try {
            Map<String, Boolean> vp = g.getViewPermsJson() == null ? new HashMap<>() : mapper.readValue(g.getViewPermsJson(), new TypeReference<Map<String, Boolean>>() {});
            m.put("viewPerms", vp);
        } catch (Exception e) {
            m.put("viewPerms", new HashMap<>());
        }
        try {
            Map<String, Boolean> ap = g.getActionPermsJson() == null ? new HashMap<>() : mapper.readValue(g.getActionPermsJson(), new TypeReference<Map<String, Boolean>>() {});
            m.put("actionPerms", ap);
        } catch (Exception e) {
            m.put("actionPerms", new HashMap<>());
        }
        return m;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}