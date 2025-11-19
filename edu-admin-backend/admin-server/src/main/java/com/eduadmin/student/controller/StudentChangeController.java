package com.eduadmin.student.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.student.entity.StudentSuspend;
import com.eduadmin.student.entity.StudentTransfer;
import com.eduadmin.student.entity.StudentWithdraw;
import com.eduadmin.student.repo.StudentSuspendRepository;
import com.eduadmin.student.repo.StudentTransferRepository;
import com.eduadmin.student.repo.StudentWithdrawRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student/change")
public class StudentChangeController {

    @Autowired
    private StudentTransferRepository transferRepo;
    @Autowired
    private StudentSuspendRepository suspendRepo;
    @Autowired
    private StudentWithdrawRepository withdrawRepo;

    @PostMapping("/transfer/apply")
    public ApiResponse<Map<String, Object>> transferApply(@RequestBody Map<String, Object> payload) {
        StudentTransfer r = new StudentTransfer();
        r.setStudentId(((Number) payload.get("studentId")).longValue());
        r.setFromClassId(((Number) payload.get("fromClassId")).longValue());
        r.setToClassId(((Number) payload.get("toClassId")).longValue());
        r.setReason(String.valueOf(payload.getOrDefault("reason", "")));
        r.setStatus("pending");
        r.setCreatedAt(new Date());
        r = transferRepo.save(r);
        return ApiResponse.success(toMap(r));
    }

    @PostMapping("/transfer/approve")
    public ApiResponse<Map<String, Object>> transferApprove(@RequestBody Map<String, Object> payload) {
        Long id = ((Number) payload.get("id")).longValue();
        Optional<StudentTransfer> opt = transferRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "未找到调班申请");
        StudentTransfer r = opt.get();
        r.setApproverFrom(toLong(payload.get("approverFrom"))); // 可选
        r.setApproverTo(toLong(payload.get("approverTo"))); // 可选
        // 前端未传 pass，默认为审批通过
        boolean pass = payload.get("pass") == null || Boolean.TRUE.equals(payload.get("pass"));
        r.setStatus(pass ? "approved" : "rejected");
        r = transferRepo.save(r);
        return ApiResponse.success(toMap(r));
    }

    @PostMapping({"/suspend/apply", "/suspend"})
    public ApiResponse<Map<String, Object>> suspend(@RequestBody Map<String, Object> payload) {
        StudentSuspend s = new StudentSuspend();
        s.setStudentId(((Number) payload.get("studentId")).longValue());
        s.setReason(String.valueOf(payload.getOrDefault("reason", "")));
        s.setExpectedReturnDate(String.valueOf(payload.getOrDefault("resumeDate", payload.getOrDefault("expectedReturnDate", ""))));
        s.setStatus("suspended");
        s.setCreatedAt(new Date());
        s = suspendRepo.save(s);
        return ApiResponse.success(toMap(s));
    }

    @PostMapping({"/resume/apply", "/resume"})
    public ApiResponse<Map<String, Object>> resume(@RequestBody Map<String, Object> payload) {
        Long studentId = ((Number) payload.get("studentId")).longValue();
        List<StudentSuspend> list = suspendRepo.findByStudentIdOrderByIdDesc(studentId);
        for (StudentSuspend s : list) {
            if ("suspended".equals(s.getStatus())) {
                s.setStatus("resumed");
                s = suspendRepo.save(s);
                return ApiResponse.success(toMap(s));
            }
        }
        return ApiResponse.error(404, "未找到该学员的休学记录");
    }

    @PostMapping({"/withdraw/apply", "/withdraw"})
    public ApiResponse<Map<String, Object>> withdraw(@RequestBody Map<String, Object> payload) {
        StudentWithdraw w = new StudentWithdraw();
        w.setStudentId(((Number) payload.get("studentId")).longValue());
        w.setReason(String.valueOf(payload.getOrDefault("reason", "")));
        Double lastFee = payload.get("lastFee") instanceof Number ? ((Number) payload.get("lastFee")).doubleValue() : null;
        double base = lastFee != null ? lastFee : 2000.0;
        w.setRefundAmount(Math.round(base * 0.7 * 100.0) / 100.0);
        w.setStatus("initiated");
        w.setCreatedAt(new Date());
        w = withdrawRepo.save(w);
        return ApiResponse.success(toMap(w));
    }

    @GetMapping("/transfer/list")
    public ApiResponse<List<Map<String, Object>>> transferList(@RequestParam(required = false) Long studentId) {
        List<StudentTransfer> source = (studentId != null) ? transferRepo.findByStudentIdOrderByIdDesc(studentId) : transferRepo.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (StudentTransfer r : source) out.add(toMap(r));
        return ApiResponse.success(out);
    }

    @GetMapping("/suspend/list")
    public ApiResponse<List<Map<String, Object>>> suspendList(@RequestParam(required = false) Long studentId) {
        List<StudentSuspend> source = (studentId != null) ? suspendRepo.findByStudentIdOrderByIdDesc(studentId) : suspendRepo.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (StudentSuspend s : source) out.add(toMap(s));
        return ApiResponse.success(out);
    }

    @GetMapping("/withdraw/list")
    public ApiResponse<List<Map<String, Object>>> withdrawList(@RequestParam(required = false) Long studentId) {
        List<StudentWithdraw> source = (studentId != null) ? withdrawRepo.findByStudentIdOrderByIdDesc(studentId) : withdrawRepo.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (StudentWithdraw w : source) out.add(toMap(w));
        return ApiResponse.success(out);
    }

    private Map<String, Object> toMap(StudentTransfer r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("studentId", r.getStudentId());
        m.put("fromClassId", r.getFromClassId());
        m.put("toClassId", r.getToClassId());
        m.put("reason", r.getReason());
        m.put("status", r.getStatus());
        m.put("remark", "");
        m.put("time", r.getCreatedAt());
        return m;
    }

    private Map<String, Object> toMap(StudentSuspend s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("studentId", s.getStudentId());
        m.put("reason", s.getReason());
        m.put("expectedReturnDate", s.getExpectedReturnDate());
        m.put("resumeDate", s.getExpectedReturnDate());
        m.put("status", s.getStatus());
        m.put("time", s.getCreatedAt());
        return m;
    }

    private Map<String, Object> toMap(StudentWithdraw w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", w.getId());
        m.put("studentId", w.getStudentId());
        m.put("reason", w.getReason());
        m.put("refundAmount", w.getRefundAmount());
        m.put("status", w.getStatus());
        m.put("time", w.getCreatedAt());
        return m;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}