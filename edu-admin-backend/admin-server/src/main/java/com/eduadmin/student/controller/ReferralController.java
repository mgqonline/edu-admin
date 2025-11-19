package com.eduadmin.student.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.student.service.StudentReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student/referral")
public class ReferralController {

    @Autowired
    private StudentReferralService referralService;

    @PostMapping("/relate")
    public ApiResponse<Map<String, Object>> relate(@RequestBody Map<String, Object> payload) {
        return referralService.relate(payload);
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(required = false) Long referrerId,
            @RequestParam(required = false) Long newStudentId
    ) {
        return referralService.list(referrerId, newStudentId);
    }
}