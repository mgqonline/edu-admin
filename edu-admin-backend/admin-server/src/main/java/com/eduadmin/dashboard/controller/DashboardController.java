package com.eduadmin.dashboard.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.dashboard.store.DashboardStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.putAll(DashboardStore.aggregateToday());
        out.put("trend7Days", DashboardStore.trend7Days());
        out.put("enrollPie", DashboardStore.enrollPieByCourseTypeLast30Days());
        return ApiResponse.success(out);
    }
}