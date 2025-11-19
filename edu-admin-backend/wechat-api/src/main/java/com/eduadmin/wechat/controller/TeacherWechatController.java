package com.eduadmin.wechat.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.wechat.domain.*;
import com.eduadmin.wechat.mapper.*;
import com.eduadmin.wechat.service.TeacherIdentityService;
import com.eduadmin.wechat.service.FileStorageService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/wechat/api/teacher")
public class TeacherWechatController {

    private final TeacherHomeworkMapper teacherHomeworkMapper;
    private final TeacherAnswerFileMapper teacherAnswerFileMapper;
    private final GradeMapper gradeMapper;
    private final TeacherFeeMapper teacherFeeMapper;
    private final AdjustRequestMapper adjustRequestMapper;
    private final SignSessionMapper signSessionMapper;
    private final TeacherIdentityService teacherIdentityService;
    private final FileStorageService fileStorageService;
    private final TeacherClassRecordMapper teacherClassRecordMapper;
    private final TeacherRoomRequestMapper teacherRoomRequestMapper;
    private final TeacherTeachPlanMapper teacherTeachPlanMapper;
    private final TeacherClassNoticeMapper teacherClassNoticeMapper;
  private final ParentMessageMapper parentMessageMapper;
  private final TeacherProfileMapper teacherProfileMapper;

    public TeacherWechatController(TeacherHomeworkMapper teacherHomeworkMapper,
                                   TeacherAnswerFileMapper teacherAnswerFileMapper,
                                   GradeMapper gradeMapper,
                                   TeacherFeeMapper teacherFeeMapper,
                                   AdjustRequestMapper adjustRequestMapper,
                                   SignSessionMapper signSessionMapper,
                                   TeacherIdentityService teacherIdentityService,
                                   FileStorageService fileStorageService,
                                   TeacherClassRecordMapper teacherClassRecordMapper,
                                   TeacherRoomRequestMapper teacherRoomRequestMapper,
                                   TeacherTeachPlanMapper teacherTeachPlanMapper,
                                   TeacherClassNoticeMapper teacherClassNoticeMapper,
                                  ParentMessageMapper parentMessageMapper,
                                  TeacherProfileMapper teacherProfileMapper) {
        this.teacherHomeworkMapper = teacherHomeworkMapper;
        this.teacherAnswerFileMapper = teacherAnswerFileMapper;
        this.gradeMapper = gradeMapper;
        this.teacherFeeMapper = teacherFeeMapper;
        this.adjustRequestMapper = adjustRequestMapper;
        this.signSessionMapper = signSessionMapper;
        this.teacherIdentityService = teacherIdentityService;
        this.fileStorageService = fileStorageService;
        this.teacherClassRecordMapper = teacherClassRecordMapper;
        this.teacherRoomRequestMapper = teacherRoomRequestMapper;
        this.teacherTeachPlanMapper = teacherTeachPlanMapper;
        this.teacherClassNoticeMapper = teacherClassNoticeMapper;
        this.parentMessageMapper = parentMessageMapper;
        this.teacherProfileMapper = teacherProfileMapper;
    }

    @GetMapping("/schedule")
    public ApiResponse<List<Map<String, Object>>> schedule() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(new HashMap<String, Object>() {{
            put("date", "2025-11-05");
            put("className", "英语听力班");
            put("startTime", "14:00");
            put("endTime", "16:00");
            put("classId", "class-002");
        }});
        return ApiResponse.success(list);
    }

    @PostMapping("/sign")
    public ApiResponse<Map<String, Object>> startSign(HttpServletRequest request,
                                                      @RequestParam(value = "classId", required = false) String classId) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        int expireMinutes = 10;
        SignSession session = new SignSession();
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        session.setTeacherId(teacherId);
        session.setClassId(classId);
        session.setCode(code);
        session.setExpireMinutes(expireMinutes);
        signSessionMapper.insert(session);
        Map<String, Object> resp = new HashMap<>();
        resp.put("signCode", code);
        resp.put("expireMinutes", expireMinutes);
        return ApiResponse.success(resp);
    }

    /**
     * 作业布置：发布作业
     */
    @PostMapping("/homework/publish")
    public ApiResponse<Map<String, Object>> homeworkPublish(HttpServletRequest request,
                                                            @RequestBody Map<String, Object> payload) {
        String title = String.valueOf(payload.getOrDefault("title", ""));
        String content = String.valueOf(payload.getOrDefault("content", ""));
        TeacherHomework hw = new TeacherHomework();
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        hw.setTeacherId(teacherId);
        hw.setTitle(title);
        hw.setContent(content);
        hw.setStatus("published");
        teacherHomeworkMapper.insert(hw);
        Map<String, Object> result = new HashMap<>();
        result.put("id", hw.getId());
        result.put("title", hw.getTitle());
        result.put("content", hw.getContent());
        result.put("status", hw.getStatus());
        result.put("publishAt", hw.getPublishAt());
        return ApiResponse.success(result);
    }

    /**
     * 作业布置：上传参考答案
     */
    @PostMapping(value = "/homework/answer/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> uploadAnswer(HttpServletRequest request,
                                                         @RequestParam(value = "homeworkId", required = false) Long homeworkId,
                                                         @RequestParam(value = "title", required = false) String title,
                                                         @RequestPart("file") MultipartFile file) {
        TeacherAnswerFile f = new TeacherAnswerFile();
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        f.setTeacherId(teacherId);
        f.setHomeworkId(homeworkId);
        f.setTitle(title != null ? title : (file.getOriginalFilename() == null ? "参考答案" : file.getOriginalFilename()));
        f.setFilename(file.getOriginalFilename());
        f.setSize(file.getSize());
        try {
            String url = fileStorageService.store("teacher/answers", file);
            f.setUrl(url);
        } catch (Exception e) {
            f.setUrl("/files/teacher/answers/" + (file.getOriginalFilename() == null ? UUID.randomUUID() : file.getOriginalFilename()));
        }
        teacherAnswerFileMapper.insert(f);
        Map<String, Object> result = new HashMap<>();
        result.put("id", f.getId());
        result.put("title", f.getTitle());
        result.put("filename", f.getFilename());
        result.put("size", f.getSize());
        result.put("url", f.getUrl());
        return ApiResponse.success(result);
    }

    /**
     * 成绩录入：登记学员测试成绩
     */
    @PostMapping("/grades/record")
    public ApiResponse<Map<String, Object>> gradesRecord(@RequestBody Map<String, Object> payload) {
        GradeRecord record = new GradeRecord();
        record.setStudentId(payload.get("studentId") instanceof Number ? ((Number) payload.get("studentId")).longValue() : 1L);
        record.setCourseId(null);
        record.setType(String.valueOf(payload.getOrDefault("type", "")));
        Object scoreObj = payload.get("score");
        Integer score = null;
        if (scoreObj instanceof Number) { score = ((Number) scoreObj).intValue(); }
        else { try { score = Integer.valueOf(String.valueOf(scoreObj)); } catch (Exception ignore) { score = null; } }
        record.setScore(score);
        gradeMapper.insert(record);
        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("studentId", record.getStudentId());
        result.put("type", record.getType());
        result.put("score", record.getScore());
        result.put("recordedAt", record.getExamDate());
        return ApiResponse.success(result);
    }

    /**
     * 课时费查询：本月汇总
     */
    @GetMapping("/fee/summary")
    public ApiResponse<Map<String, Object>> feeSummary(HttpServletRequest request) {
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        BigDecimal total = teacherFeeMapper.sumByMonth(teacherId, month);
        Map<String, Object> summary = new HashMap<>();
        summary.put("monthTotal", total);
        summary.put("month", month);
        return ApiResponse.success(summary);
    }

    /**
     * 课时费查询：历史明细
     */
    @GetMapping("/fee/history")
    public ApiResponse<List<TeacherFeeRecord>> feeHistory(HttpServletRequest request) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        List<TeacherFeeRecord> list = teacherFeeMapper.listByTeacher(teacherId);
        return ApiResponse.success(list);
    }

    /**
     * 课堂记录：创建
     */
    @PostMapping("/classrecord/create")
    public ApiResponse<Void> createClassRecord(HttpServletRequest request,
                                               @RequestParam(value = "class_id", required = false) String classId,
                                               @RequestParam("content") String content,
                                               @RequestParam(value = "performance", required = false) String performance) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        if (teacherId == null) {
            return ApiResponse.error(400, "teacherId 解析失败");
        }
        TeacherClassRecord record = new TeacherClassRecord();
        record.setTeacherId(teacherId);
        record.setClassId(classId);
        record.setContent(content);
        record.setPerformance(performance);
        teacherClassRecordMapper.insert(record);
        return ApiResponse.success(null);
    }

    /**
     * 调课申请：列表
     */
    @GetMapping("/adjust/list")
    public ApiResponse<List<AdjustRequest>> adjustList(HttpServletRequest request) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        List<AdjustRequest> list = adjustRequestMapper.listByTeacher(teacherId);
        return ApiResponse.success(list);
    }

    /**
     * 调课申请：提交
     */
    @PostMapping("/adjust/apply")
    public ApiResponse<AdjustRequest> adjustApply(HttpServletRequest request,
                                                  @RequestBody Map<String, Object> payload) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        AdjustRequest req = new AdjustRequest();
        req.setTeacherId(teacherId);
        req.setCourse(String.valueOf(payload.getOrDefault("course", "")));
        req.setOriginalTime(String.valueOf(payload.getOrDefault("originalTime", "")));
        req.setNewTime(String.valueOf(payload.getOrDefault("newTime", "")));
        req.setReason(String.valueOf(payload.getOrDefault("reason", "")));
        req.setStatus("审批中");
        adjustRequestMapper.insert(req);
        return ApiResponse.success(req);
    }

    /**
     * 教室申请：提交
     */
    @PostMapping("/room/apply")
    public ApiResponse<TeacherRoomRequest> roomApply(HttpServletRequest request,
                                                     @RequestBody Map<String, Object> payload) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        TeacherRoomRequest req = new TeacherRoomRequest();
        req.setTeacherId(teacherId);
        try {
            String dateOnlyStr = String.valueOf(payload.getOrDefault("dateOnly", ""));
            if (dateOnlyStr != null && !dateOnlyStr.isEmpty()) {
                req.setDateOnly(new SimpleDateFormat("yyyy-MM-dd").parse(dateOnlyStr));
            }
        } catch (Exception ignore) { /* no-op */ }
        req.setStartTimeText(String.valueOf(payload.getOrDefault("startTime", "")));
        req.setEndTimeText(String.valueOf(payload.getOrDefault("endTime", "")));
        Object capObj = payload.get("capacity");
        Integer cap = null;
        if (capObj instanceof Number) cap = ((Number) capObj).intValue();
        else { try { cap = Integer.valueOf(String.valueOf(capObj)); } catch (Exception ignore) { cap = null; } }
        req.setCapacity(cap);
        req.setReason(String.valueOf(payload.getOrDefault("reason", "")));
        req.setStatus("pending");
        teacherRoomRequestMapper.insert(req);
        return ApiResponse.success(req);
    }

    /**
     * 教学计划：新增
     */
    @PostMapping("/plan/create")
    public ApiResponse<TeacherTeachPlan> planCreate(HttpServletRequest request,
                                                    @RequestBody Map<String, Object> payload) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        TeacherTeachPlan plan = new TeacherTeachPlan();
        plan.setTeacherId(teacherId);
        plan.setClassId(String.valueOf(payload.getOrDefault("classId", "")));
        plan.setTitle(String.valueOf(payload.getOrDefault("title", "")));
        plan.setContent(String.valueOf(payload.getOrDefault("content", "")));
        plan.setResourceUrl(String.valueOf(payload.getOrDefault("resourceUrl", "")));
        teacherTeachPlanMapper.insert(plan);
        return ApiResponse.success(plan);
    }

    /**
     * 班级通知：新增与列表
     */
    @PostMapping("/notice/create")
    public ApiResponse<TeacherClassNotice> noticeCreate(HttpServletRequest request,
                                                        @RequestBody Map<String, Object> payload) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        TeacherClassNotice n = new TeacherClassNotice();
        n.setTeacherId(teacherId);
        n.setClassId(String.valueOf(payload.getOrDefault("classId", "")));
        n.setTitle(String.valueOf(payload.getOrDefault("title", "")));
        n.setContent(String.valueOf(payload.getOrDefault("content", "")));
        n.setStatus("published");
        teacherClassNoticeMapper.insert(n);
        return ApiResponse.success(n);
    }

    @GetMapping("/notice/list")
    public ApiResponse<List<TeacherClassNotice>> noticeList(HttpServletRequest request) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        return ApiResponse.success(teacherClassNoticeMapper.listByTeacher(teacherId));
    }

    /**
     * 家长留言：列表与回复
     */
    @GetMapping("/message/list")
    public ApiResponse<List<ParentMessage>> messageList(HttpServletRequest request) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        return ApiResponse.success(parentMessageMapper.listByTeacher(teacherId));
    }

    @PostMapping("/message/reply")
    public ApiResponse<Map<String, Object>> messageReply(@RequestBody Map<String, Object> payload) {
        Long id = payload.get("id") instanceof Number ? ((Number) payload.get("id")).longValue() : null;
        String reply = String.valueOf(payload.getOrDefault("reply", ""));
        if (id == null) return ApiResponse.error(400, "缺少 id");
        parentMessageMapper.reply(id, reply);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("reply", reply);
        return ApiResponse.success(result);
    }
    /**
     * 教师个人信息：基础资料
     */
    @GetMapping("/profile")
    public ApiResponse<TeacherProfile> teacherProfile(HttpServletRequest request) {
        Long teacherId = teacherIdentityService.resolveTeacherId(request, null);
        TeacherProfile p = teacherProfileMapper.getByTeacherId(teacherId);
        if (p == null) {
            p = new TeacherProfile();
            p.setTeacherId(teacherId);
            p.setName("教师#" + teacherId);
            p.setSubjects("未设定");
            p.setYears(3);
            p.setPhone("未绑定");
        }
        return ApiResponse.success(p);
    }
}