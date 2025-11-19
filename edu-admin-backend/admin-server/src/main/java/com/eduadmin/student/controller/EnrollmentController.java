package com.eduadmin.student.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.store.InMemoryCourseStore;
import com.eduadmin.student.entity.Enrollment;
import com.eduadmin.student.repo.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/enroll")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @PostMapping("/apply")
    public ApiResponse<Map<String, Object>> apply(@RequestBody Map<String, Object> payload) {
        Enrollment e = new Enrollment();
        e.setStudentId(((Number) payload.get("studentId")).longValue());
        e.setClassId(((Number) payload.get("classId")).longValue());
        e.setApplyInfo((String) payload.getOrDefault("applyInfo", ""));
        e.setFee(payload.get("fee") == null ? 0.0 : ((Number) payload.get("fee")).doubleValue());
        Boolean approvalRequired = (Boolean) payload.get("approvalRequired");
        e.setApprovalRequired(approvalRequired != null ? approvalRequired : false);

        // 自动叠加教材费（沿用内存课程数据的演示逻辑）
        double materialsFee = 0.0;
        Map<String, Object> cls = e.getClassId() == null ? null : InMemoryCourseStore.classes.get(e.getClassId());
        if (cls != null) {
            String courseId = String.valueOf(cls.get("courseId"));
            Map<String, Object> course = InMemoryCourseStore.courses.get(courseId);
            if (course != null) {
                List<String> materialIds = (List<String>) course.getOrDefault("materials", Collections.emptyList());
                for (String mid : materialIds) {
                    Map<String, Object> tb = InMemoryCourseStore.textbooks.get(mid);
                    if (tb != null) materialsFee += ((Number) tb.getOrDefault("unitPrice", 0.0)).doubleValue();
                }
            }
        }
        e.setMaterialsFee(materialsFee);
        e.setFee((e.getFee() == null ? 0.0 : e.getFee()) + materialsFee);
        if (materialsFee > 0) {
            String ai = e.getApplyInfo() == null ? "" : e.getApplyInfo();
            e.setApplyInfo(ai + (ai.isEmpty() ? "" : "；") + "自动关联教材费：" + materialsFee);
        }

        // 状态：默认根据是否需要审批设置
        e.setStatus(Boolean.TRUE.equals(e.getApprovalRequired()) ? "pending" : "approved");

        e = enrollmentRepository.save(e);

        try { com.eduadmin.dashboard.store.DashboardStore.addOperation("enroll", null, e.getClassId(), new Date()); } catch (Exception ignore) {}

        return ApiResponse.success(toMap(e));
    }

    @PostMapping("/contract/generate")
    public ApiResponse<Map<String, Object>> generateContract(@RequestBody Map<String, Object> payload) {
        Long id = ((Number) payload.get("enrollId")).longValue();
        Optional<Enrollment> opt = enrollmentRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "未找到报名记录");
        Enrollment e = opt.get();
        e.setContractUrl("https://example.com/contracts/" + id + ".pdf");
        e.setSigned(false);
        enrollmentRepository.save(e);
        return ApiResponse.success(toMap(e));
    }

    @PostMapping("/approve")
    public ApiResponse<Map<String, Object>> approve(@RequestBody Map<String, Object> payload) {
        Long id = ((Number) payload.get("enrollId")).longValue();
        String action = (String) payload.getOrDefault("action", "approve");
        Optional<Enrollment> opt = enrollmentRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "未找到报名记录");
        Enrollment e = opt.get();
        if (!Boolean.TRUE.equals(e.getApprovalRequired())) return ApiResponse.error(400, "该报名无需审批");
        e.setStatus("approve".equals(action) ? "approved" : "rejected");
        enrollmentRepository.save(e);
        return ApiResponse.success(toMap(e));
    }

    @PostMapping("/contract/sign")
    public ApiResponse<Map<String, Object>> sign(@RequestBody Map<String, Object> payload) {
        Long id = ((Number) payload.get("enrollId")).longValue();
        Optional<Enrollment> opt = enrollmentRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "未找到报名记录");
        Enrollment e = opt.get();
        e.setSigned(true);
        enrollmentRepository.save(e);
        return ApiResponse.success(toMap(e));
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(required = false) Long studentId) {
        List<Enrollment> arr = (studentId == null) ? enrollmentRepository.findAll() : enrollmentRepository.findByStudentId(studentId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Enrollment e : arr) list.add(toMap(e));
        return ApiResponse.success(list);
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> delete(@RequestBody Map<String, Object> payload) {
        Object idObj = payload.get("id");
        if (idObj == null) return ApiResponse.error(400, "缺少ID");
        Long id = ((Number) idObj).longValue();
        if (!enrollmentRepository.existsById(id)) return ApiResponse.error(404, "未找到报名记录");
        enrollmentRepository.deleteById(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/last")
    public ApiResponse<Map<String, Object>> last(@RequestParam Long studentId) {
        Enrollment latest = enrollmentRepository.findTopByStudentIdOrderByIdDesc(studentId);
        if (latest == null) return ApiResponse.error(404, "未找到该学员的报名记录");
        return ApiResponse.success(toMap(latest));
    }

    private Map<String, Object> toMap(Enrollment e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("studentId", e.getStudentId());
        m.put("classId", e.getClassId());
        m.put("applyInfo", e.getApplyInfo());
        m.put("fee", e.getFee());
        m.put("materialsFee", e.getMaterialsFee());
        m.put("status", e.getStatus());
        m.put("approvalRequired", e.getApprovalRequired());
        m.put("contractUrl", e.getContractUrl());
        m.put("signed", e.getSigned());
        return m;
    }
}