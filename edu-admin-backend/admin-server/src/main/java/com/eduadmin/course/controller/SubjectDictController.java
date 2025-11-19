package com.eduadmin.course.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.entity.SubjectDict;
import com.eduadmin.course.repo.SubjectDictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dict/subject")
public class SubjectDictController {

    @Autowired
    private SubjectDictRepository subjectRepo;

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(value = "status", required = false) String status) {
        List<SubjectDict> list = (status == null || status.isEmpty())
                ? subjectRepo.findAll()
                : subjectRepo.findByStatusOrderBySortOrderAsc(status);
        List<Map<String, Object>> data = new ArrayList<>();
        for (SubjectDict s : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("status", s.getStatus());
            m.put("sortOrder", s.getSortOrder());
            data.add(m);
        }
        return ApiResponse.success(data);
    }

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> payload) {
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        if (name.isEmpty()) return ApiResponse.error(400, "科目名称不能为空");
        SubjectDict entity = subjectRepo.findByName(name).orElseGet(SubjectDict::new);
        entity.setName(name);
        entity.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        Object sortOrder = payload.get("sortOrder");
        entity.setSortOrder(sortOrder == null ? 0 : Integer.parseInt(String.valueOf(sortOrder)));
        SubjectDict saved = subjectRepo.save(entity);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("name", saved.getName());
        out.put("status", saved.getStatus());
        out.put("sortOrder", saved.getSortOrder());
        return ApiResponse.success(out);
    }
}