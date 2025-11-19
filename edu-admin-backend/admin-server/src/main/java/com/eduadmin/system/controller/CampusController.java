package com.eduadmin.system.controller;

import com.eduadmin.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// 显式指定 Bean 名称以避免与 com.eduadmin.baseplatform.campus.CampusController 冲突
@RestController("systemCampusController")
@RequestMapping("/api/campus")
public class CampusController {

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> campus) {
        campus.putIfAbsent("campusId", UUID.randomUUID().toString());
        return ApiResponse.success(campus);
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(new HashMap<String, Object>() {{
            put("campusId", "demo-001");
            put("name", "总部校区");
            put("address", "XX市XX路1号");
            put("status", "启用");
        }});
        return ApiResponse.success(items);
    }
}