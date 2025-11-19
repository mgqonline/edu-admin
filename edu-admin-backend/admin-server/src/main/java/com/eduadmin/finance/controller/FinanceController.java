package com.eduadmin.finance.controller;

import com.eduadmin.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("monthIncome", 100000);
        data.put("monthRefund", 2000);
        return ApiResponse.success(data);
    }
}