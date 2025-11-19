package com.eduadmin.wechat.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.wechat.domain.ScheduleItem;
import com.eduadmin.wechat.domain.PaymentItem;
import com.eduadmin.wechat.domain.RemainingLessonsItem;
import com.eduadmin.wechat.domain.GradeRecord;
import com.eduadmin.wechat.mapper.ScheduleMapper;
import com.eduadmin.wechat.mapper.PaymentMapper;
import com.eduadmin.wechat.mapper.LessonsMapper;
import com.eduadmin.wechat.mapper.GradeMapper;
import com.eduadmin.wechat.service.StudentIdentityService;
import com.eduadmin.wechat.domain.ParentMessage;
import com.eduadmin.wechat.domain.StudentFeedback;
import com.eduadmin.wechat.mapper.ParentMessageMapper;
import com.eduadmin.wechat.mapper.StudentFeedbackMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.eduadmin.wechat.domain.LeaveRequest;
import com.eduadmin.wechat.domain.HomeworkSubmission;
import com.eduadmin.wechat.domain.SignRecord;
import com.eduadmin.wechat.mapper.LeaveRequestMapper;
import com.eduadmin.wechat.mapper.HomeworkSubmissionMapper;
import com.eduadmin.wechat.mapper.SignRecordMapper;

import java.util.*;

@RestController
@RequestMapping("/wechat/api/student")
public class StudentWechatController {

    private final LeaveRequestMapper leaveRequestMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final SignRecordMapper signRecordMapper;
    private final ScheduleMapper scheduleMapper;
    private final PaymentMapper paymentMapper;
    private final LessonsMapper lessonsMapper;
    private final GradeMapper gradeMapper;
    private final StudentIdentityService identityService;
    private final ParentMessageMapper parentMessageMapper;
    private final StudentFeedbackMapper studentFeedbackMapper;

    public StudentWechatController(LeaveRequestMapper leaveRequestMapper,
                                   HomeworkSubmissionMapper homeworkSubmissionMapper,
                                   SignRecordMapper signRecordMapper,
                                   ScheduleMapper scheduleMapper,
                                   PaymentMapper paymentMapper,
                                   LessonsMapper lessonsMapper,
                                   GradeMapper gradeMapper,
                                   StudentIdentityService identityService,
                                   ParentMessageMapper parentMessageMapper,
                                   StudentFeedbackMapper studentFeedbackMapper) {
        this.leaveRequestMapper = leaveRequestMapper;
        this.homeworkSubmissionMapper = homeworkSubmissionMapper;
        this.signRecordMapper = signRecordMapper;
        this.scheduleMapper = scheduleMapper;
        this.paymentMapper = paymentMapper;
        this.lessonsMapper = lessonsMapper;
        this.gradeMapper = gradeMapper;
        this.identityService = identityService;
        this.parentMessageMapper = parentMessageMapper;
        this.studentFeedbackMapper = studentFeedbackMapper;
    }

    @GetMapping("/schedule/today")
    public ApiResponse<List<ScheduleItem>> scheduleToday(@RequestParam(value = "studentId", required = false) Long studentId,
                                                         javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, studentId);
        List<ScheduleItem> list = scheduleMapper.listTodayByStudent(sid);
        return ApiResponse.success(list);
    }

    @GetMapping("/schedule/week")
    public ApiResponse<List<ScheduleItem>> scheduleWeek(@RequestParam(value = "studentId", required = false) Long studentId,
                                                        javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, studentId);
        Calendar cal = Calendar.getInstance();
        // start: today
        Date start = cal.getTime();
        // end: +7 days
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date end = cal.getTime();
        List<ScheduleItem> list = scheduleMapper.listByStudentBetween(sid, start, end);
        return ApiResponse.success(list);
    }

    @PostMapping("/sign/scan")
    public ApiResponse<Map<String, Object>> signScan(@RequestBody Map<String, Object> payload) {
        Long studentId = payload.get("studentId") instanceof Number ? ((Number) payload.get("studentId")).longValue() : 1L;
        String classId = String.valueOf(payload.getOrDefault("classId", ""));
        String code = String.valueOf(payload.getOrDefault("code", ""));
        SignRecord rec = new SignRecord();
        rec.setStudentId(studentId);
        rec.setClassId(classId);
        rec.setCode(code);
        rec.setMethod("scan");
        signRecordMapper.insert(rec);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("signedAt", new Date());
        return ApiResponse.success(result);
    }

    @PostMapping("/sign/confirm")
    public ApiResponse<Map<String, Object>> signConfirm(@RequestBody Map<String, Object> payload) {
        Long studentId = payload.get("studentId") instanceof Number ? ((Number) payload.get("studentId")).longValue() : 1L;
        String classId = String.valueOf(payload.getOrDefault("classId", ""));
        SignRecord rec = new SignRecord();
        rec.setStudentId(studentId);
        rec.setClassId(classId);
        rec.setMethod("confirm");
        signRecordMapper.insert(rec);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("confirmedAt", new Date());
        return ApiResponse.success(result);
    }

    @GetMapping("/homework/list")
    public ApiResponse<List<Map<String, Object>>> homeworkList(@RequestParam(value = "studentId", required = false) Long studentId) {
        List<Map<String, Object>> list = Arrays.asList(
                new HashMap<String, Object>() {{ put("id", 1); put("title", "数学作业：练习册第3章"); put("dueDate", "2025-11-15"); }},
                new HashMap<String, Object>() {{ put("id", 2); put("title", "英语作文：我的周末"); put("dueDate", "2025-11-20"); }}
        );
        return ApiResponse.success(list);
    }

    @PostMapping(value = "/homework/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> homeworkUpload(@RequestParam("homeworkId") Long homeworkId,
                                                           @RequestParam(value = "studentId", required = false) Long studentId,
                                                           @RequestPart("file") MultipartFile file) {
        HomeworkSubmission sub = new HomeworkSubmission();
        sub.setStudentId(studentId != null ? studentId : 1L);
        sub.setHomeworkId(homeworkId);
        sub.setFilename(file.getOriginalFilename());
        sub.setSize(file.getSize());
        sub.setUrl("/uploads/" + (file.getOriginalFilename() == null ? ("hw-" + homeworkId) : file.getOriginalFilename()));
        homeworkSubmissionMapper.insert(sub);
        Map<String, Object> result = new HashMap<>();
        result.put("id", sub.getId());
        result.put("homeworkId", homeworkId);
        result.put("filename", sub.getFilename());
        result.put("size", sub.getSize());
        result.put("url", sub.getUrl());
        return ApiResponse.success(result);
    }

    @GetMapping("/grades/list")
    public ApiResponse<List<GradeRecord>> gradesList(@RequestParam(value = "studentId", required = false) Long studentId,
                                                     javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, studentId);
        List<GradeRecord> list = gradeMapper.listByStudent(sid);
        return ApiResponse.success(list);
    }

    @PostMapping("/leave/submit")
    public ApiResponse<LeaveRequest> leaveSubmit(@RequestBody Map<String, Object> payload) {
        Long studentId = payload.get("studentId") instanceof Number ? ((Number) payload.get("studentId")).longValue() : 1L;
        LeaveRequest req = new LeaveRequest();
        req.setStudentId(studentId);
        req.setReason(String.valueOf(payload.getOrDefault("reason", "")));
        req.setStart(String.valueOf(payload.getOrDefault("start", "")));
        req.setEnd(String.valueOf(payload.getOrDefault("end", "")));
        req.setStatus("审批中");
        leaveRequestMapper.insert(req);
        return ApiResponse.success(req);
    }

    @GetMapping("/leave/list")
    public ApiResponse<List<LeaveRequest>> leaveList(@RequestParam(value = "studentId", required = false) Long studentId) {
        List<LeaveRequest> list = leaveRequestMapper.listByStudentId(studentId != null ? studentId : 1L);
        return ApiResponse.success(list);
    }

    @GetMapping("/lessons/remaining")
    public ApiResponse<List<RemainingLessonsItem>> lessonsRemaining(@RequestParam(value = "studentId", required = false) Long studentId,
                                                                   javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, studentId);
        List<RemainingLessonsItem> list = lessonsMapper.remainingByStudent(sid);
        return ApiResponse.success(list);
    }

    @GetMapping("/payments/list")
    public ApiResponse<List<PaymentItem>> paymentsList(@RequestParam(value = "studentId", required = false) Long studentId,
                                                       javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, studentId);
        List<PaymentItem> list = paymentMapper.listByStudent(sid);
        return ApiResponse.success(list);
    }

    /**
     * 家校互动：消息中心（家长留言列表）
     */
    @GetMapping("/messages/list")
    public ApiResponse<List<ParentMessage>> messagesList(@RequestParam(value = "studentId", required = false) Long studentId,
                                                         javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, studentId);
        return ApiResponse.success(parentMessageMapper.listByStudent(sid));
    }

    /**
     * 家长联系老师：发送留言
     */
    @PostMapping("/contact/send")
    public ApiResponse<ParentMessage> contactSend(@RequestBody Map<String, Object> payload,
                                                  javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, null);
        ParentMessage msg = new ParentMessage();
        msg.setStudentId(sid);
        Object tidObj = payload.get("teacherId");
        if (tidObj instanceof Number) { msg.setTeacherId(((Number) tidObj).longValue()); }
        else { try { msg.setTeacherId(Long.valueOf(String.valueOf(tidObj))); } catch (Exception ignore) { /* no-op */ } }
        msg.setParentName(String.valueOf(payload.getOrDefault("parentName", "家长")));
        msg.setContent(String.valueOf(payload.getOrDefault("content", "")));
        msg.setStatus("open");
        parentMessageMapper.insert(msg);
        return ApiResponse.success(msg);
    }

    /**
     * 学员意见反馈：提交
     */
    @PostMapping("/feedback/create")
    public ApiResponse<StudentFeedback> feedbackCreate(@RequestBody Map<String, Object> payload,
                                                       javax.servlet.http.HttpServletRequest request) {
        Long sid = identityService.resolveStudentId(request, null);
        StudentFeedback fb = new StudentFeedback();
        fb.setStudentId(sid);
        fb.setContent(String.valueOf(payload.getOrDefault("content", "")));
        fb.setStatus("new");
        studentFeedbackMapper.insert(fb);
        return ApiResponse.success(fb);
    }
}