package com.eduadmin.course.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.service.TextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/course/textbook")
public class TextbookController {

    @Autowired
    private TextbookService textbookService;

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> tb) {
        return ApiResponse.success(textbookService.save(tb));
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(textbookService.list(keyword));
    }

    @PostMapping("/inventory/in")
    public ApiResponse<Map<String, Object>> inventoryIn(@RequestParam("textbookId") String textbookId,
                                                        @RequestParam("qty") int qty) {
        return ApiResponse.success(textbookService.inventoryIn(textbookId, qty));
    }

    @PostMapping("/inventory/out")
    public ApiResponse<Map<String, Object>> inventoryOut(@RequestParam("textbookId") String textbookId,
                                                         @RequestParam("qty") int qty) {
        return ApiResponse.success(textbookService.inventoryOut(textbookId, qty));
    }

    @GetMapping("/inventory/records/{textbookId}")
    public ApiResponse<List<Map<String, Object>>> inventoryRecords(@PathVariable("textbookId") String textbookId) {
        return ApiResponse.success(textbookService.inventoryRecords(textbookId));
    }

    @GetMapping("/low-stock")
    public ApiResponse<List<Map<String, Object>>> lowStock(@RequestParam(value = "threshold", defaultValue = "5") int threshold) {
        return ApiResponse.success(textbookService.lowStock(threshold));
    }
}