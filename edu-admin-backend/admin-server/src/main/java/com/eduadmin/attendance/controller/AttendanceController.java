package com.eduadmin.attendance.controller;

import com.eduadmin.attendance.entity.AttendanceRecord;
import com.eduadmin.attendance.entity.AttendanceSignCode;
import com.eduadmin.attendance.entity.MakeupApply;
import com.eduadmin.attendance.entity.OneToOneConfirm;
import com.eduadmin.attendance.repo.AttendanceRecordRepository;
import com.eduadmin.attendance.repo.AttendanceSignCodeRepository;
import com.eduadmin.attendance.repo.MakeupApplyRepository;
import com.eduadmin.attendance.repo.OneToOneConfirmRepository;
import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.entity.ClassEnrollment;
import com.eduadmin.course.repo.ClassEnrollmentRepository;
import com.eduadmin.scheduling.entity.ScheduleInfo;
import com.eduadmin.scheduling.repo.ScheduleRepository;
import com.eduadmin.teacher.service.TeacherFeeService;
import com.eduadmin.course.store.InMemoryCourseStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private AttendanceSignCodeRepository attendanceSignCodeRepository;
    @Autowired
    private OneToOneConfirmRepository oneToOneConfirmRepository;
    @Autowired
    private MakeupApplyRepository makeupApplyRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ClassEnrollmentRepository classEnrollmentRepository;

    @Autowired
    private TeacherFeeService teacherFeeService;

    // =============== 开发辅助：查询近期课时（无权限校验，仅用于联调/演示） ===============
    @GetMapping("/dev/schedules")
    public ApiResponse<List<Map<String, Object>>> devSchedules() {
        try {
            List<ScheduleInfo> all = scheduleRepository.findAll();
            // 取最近 10 条按 id 倒序
            all.sort((a, b) -> Long.compare(b.getId() == null ? 0 : b.getId(), a.getId() == null ? 0 : a.getId()));
            List<ScheduleInfo> list = all.size() > 10 ? all.subList(0, 10) : all;
            List<Map<String, Object>> out = new ArrayList<>();
            for (ScheduleInfo s : list) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("classId", s.getClassId());
                m.put("teacherId", s.getTeacherId());
                m.put("studentId", s.getStudentId());
                m.put("date", s.getDateOnly());
                m.put("startTime", s.getStartTimeText());
                m.put("endTime", s.getEndTimeText());
                out.add(m);
            }
            return ApiResponse.success(out);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询课时失败: " + e.getMessage());
        }
    }

    // =============== 基础查询（占位：待后续统计逻辑完善） ===============
    @GetMapping("/student/detail")
    public ApiResponse<List<Map<String, Object>>> studentDetail(@RequestParam("scheduleId") Long scheduleId) {
        try {
            List<AttendanceRecord> records = attendanceRecordRepository.findByScheduleId(scheduleId);
            List<Map<String, Object>> list = new ArrayList<>();
            for (AttendanceRecord r : records) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("studentId", r.getStudentId());
                m.put("name", ""); // 可后续补充姓名查询
                m.put("signType", r.getSignType());
                m.put("signTime", r.getSignTime());
                list.add(m);
            }
            return ApiResponse.success(list);
        } catch (Exception e) {
            return ApiResponse.success(Collections.emptyList());
        }
    }

    @GetMapping("/student/stats")
    public ApiResponse<Map<String, Object>> studentStats(@RequestParam("studentId") Long studentId,
                                                         @RequestParam("startDate") String startDate,
                                                         @RequestParam("endDate") String endDate) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("studentId", studentId);

        try {
            // 解析日期范围（包含端点日）
            Date start = null, end = null;
            if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
                fmt.setLenient(false);
                start = fmt.parse(startDate);
                end = fmt.parse(endDate);
                // 将 end 扩展到当天结束
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTime(end);
                c.set(java.util.Calendar.HOUR_OF_DAY, 23);
                c.set(java.util.Calendar.MINUTE, 59);
                c.set(java.util.Calendar.SECOND, 59);
                c.set(java.util.Calendar.MILLISECOND, 999);
                end = c.getTime();
            }

            List<AttendanceRecord> records;
            if (start != null && end != null) {
                records = attendanceRecordRepository.findByStudentIdAndSignTimeBetween(studentId, start, end);
            } else {
                records = attendanceRecordRepository.findByStudentId(studentId);
            }

            // 统计：总课时=记录数；完成课时=非缺勤、且排除补签来源；缺勤次数=signType==4；迟到次数=signType==2
            int totalHours = 0, completedHours = 0, absent = 0, late = 0;
            for (AttendanceRecord r : records) {
                totalHours++;
                if (Integer.valueOf(4).equals(r.getSignType())) {
                    absent++;
                } else {
                    // makeup 记录不计入完成课时
                    if (r.getSource() == null || !"makeup".equalsIgnoreCase(r.getSource())) {
                        completedHours++;
                    }
                    if (Integer.valueOf(2).equals(r.getSignType())) late++;
                }
            }
            stats.put("totalHours", totalHours);
            stats.put("completedHours", completedHours);
            stats.put("absent", absent);
            stats.put("late", late);

            // 计算报名总课时与剩余课时（演示/联调基于内存课程数据 + 报名表）
            int enrolledLessons = 0;
            try {
                List<ClassEnrollment> enrolls = classEnrollmentRepository.findByStudentId(studentId);
                for (ClassEnrollment ce : enrolls) {
                    if (!"active".equalsIgnoreCase(String.valueOf(ce.getStatus()))) continue;
                    Map<String, Object> c = InMemoryCourseStore.classes.get(ce.getClassId());
                    if (c == null) continue;
                    String courseId = String.valueOf(c.get("courseId"));
                    Map<String, Object> course = InMemoryCourseStore.courses.get(courseId);
                    if (course == null) continue;
                    Object lc = course.get("lessonCount");
                    int lessons = (lc instanceof Number) ? ((Number) lc).intValue() : 0;
                    enrolledLessons += Math.max(0, lessons);
                }
            } catch (Exception ignore) {}
            stats.put("enrolledHours", enrolledLessons);
            stats.put("remainingHours", Math.max(0, enrolledLessons - completedHours));
            return ApiResponse.success(stats);
        } catch (Exception e) {
            stats.put("totalHours", 0);
            stats.put("completedHours", 0);
            stats.put("absent", 0);
            stats.put("late", 0);
            stats.put("enrolledHours", 0);
            stats.put("remainingHours", 0);
            return ApiResponse.success(stats);
        }
    }

    @GetMapping("/class/stats")
    public ApiResponse<Map<String, Object>> classStats(@RequestParam("classId") Long classId,
                                                        @RequestParam("month") String month) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("classId", classId);
        try {
            // 解析月份（yyyy-MM），得到当月起止日期
            java.text.SimpleDateFormat ym = new java.text.SimpleDateFormat("yyyy-MM");
            ym.setLenient(false);
            Date monthDate = ym.parse(month);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(monthDate);
            c.set(java.util.Calendar.DAY_OF_MONTH, 1);
            Date start = c.getTime();
            c.add(java.util.Calendar.MONTH, 1);
            c.add(java.util.Calendar.DAY_OF_MONTH, -1);
            Date end = c.getTime();

            // 找到当月该班级的课时
            List<ScheduleInfo> schedules = scheduleRepository.findByClassId(classId);
            List<ScheduleInfo> monthSchedules = new ArrayList<>();
            for (ScheduleInfo s : schedules) {
                Date d = s.getDateOnly();
                if (d != null && !d.before(start) && !d.after(end)) monthSchedules.add(s);
            }

            // 班级有效学员数
            List<ClassEnrollment> enrollments = classEnrollmentRepository.findByClassIdAndStatus(classId, "active");
            int totalStudents = enrollments.size();

            double rateSum = 0.0;
            int rateCount = 0;

            // 缺勤统计
            Map<Long, Integer> absentCountByStudent = new LinkedHashMap<>();

            for (ScheduleInfo s : monthSchedules) {
                // 获取该课时的考勤记录
                List<AttendanceRecord> recs = attendanceRecordRepository.findByScheduleId(s.getId());
                int presentCount = 0;
                for (AttendanceRecord r : recs) {
                    // makeup 不计入出勤率
                    if (r.getSource() != null && "makeup".equalsIgnoreCase(r.getSource())) continue;
                    if (!Integer.valueOf(4).equals(r.getSignType())) {
                        presentCount++;
                    } else {
                        absentCountByStudent.put(r.getStudentId(), absentCountByStudent.getOrDefault(r.getStudentId(), 0) + 1);
                    }
                }
                if (totalStudents > 0) {
                    double rate = (presentCount * 100.0) / totalStudents;
                    rateSum += rate;
                    rateCount++;
                }
            }

            double avgRate = (rateCount > 0) ? Math.round((rateSum / rateCount) * 10.0) / 10.0 : 0.0;
            stats.put("avgAttendanceRate", avgRate);

            // 缺勤 TOP3（按缺勤次数降序，返回 studentId 列表）
            List<Map.Entry<Long, Integer>> entries = new ArrayList<>(absentCountByStudent.entrySet());
            entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
            List<Long> top3 = new ArrayList<>();
            for (int i = 0; i < Math.min(3, entries.size()); i++) top3.add(entries.get(i).getKey());
            stats.put("absentTop3", top3);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            stats.put("avgAttendanceRate", 0.0);
            stats.put("absentTop3", Collections.emptyList());
            return ApiResponse.success(stats);
        }
    }

    @GetMapping("/class/trend")
    public ApiResponse<Map<String, Object>> classTrend(@RequestParam("classId") Long classId,
                                                       @RequestParam(value = "range", required = false) String range,
                                                       @RequestParam(value = "month", required = false) String month) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("classId", classId);
        try {
            String r = (range == null || range.trim().isEmpty()) ? "month" : range.trim().toLowerCase();
            List<Map<String, Object>> points = new ArrayList<>();

            if ("month".equals(r)) {
                // 月度：按天统计当月出勤率
                java.text.SimpleDateFormat ym = new java.text.SimpleDateFormat("yyyy-MM");
                ym.setLenient(false);
                Date monthDate = (month != null && !month.trim().isEmpty()) ? ym.parse(month) : new Date();
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTime(monthDate);
                c.set(java.util.Calendar.DAY_OF_MONTH, 1);
                Date start = c.getTime();
                c.add(java.util.Calendar.MONTH, 1);
                c.add(java.util.Calendar.DAY_OF_MONTH, -1);
                Date end = c.getTime();

                List<ScheduleInfo> list = scheduleRepository.findByClassId(classId);
                java.text.SimpleDateFormat dFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Map<String, List<ScheduleInfo>> byDay = new LinkedHashMap<>();
                for (ScheduleInfo s : list) {
                    Date d = s.getDateOnly();
                    if (d == null || d.before(start) || d.after(end)) continue;
                    String key = dFmt.format(d);
                    byDay.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
                }
                for (Map.Entry<String, List<ScheduleInfo>> e : byDay.entrySet()) {
                    int totalShould = 0, totalPresent = 0;
                    for (ScheduleInfo s : e.getValue()) {
                        int should = 0;
                        List<ClassEnrollment> enrollments = (s.getClassId() == null)
                                ? java.util.Collections.emptyList()
                                : classEnrollmentRepository.findByClassIdAndStatus(s.getClassId(), "active");
                        should = enrollments.size();
                        if (should == 0) continue;
                        List<AttendanceRecord> recs = attendanceRecordRepository.findByScheduleId(s.getId());
                        int present = 0;
                        for (AttendanceRecord rcd : recs) {
                            if (rcd.getSource() != null && "makeup".equalsIgnoreCase(rcd.getSource())) continue;
                            if (!Integer.valueOf(4).equals(rcd.getSignType())) present++;
                        }
                        totalShould += should;
                        totalPresent += present;
                    }
                    double rate = (totalShould > 0) ? Math.round((totalPresent * 100.0 / totalShould) * 10.0) / 10.0 : 0.0;
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("date", e.getKey());
                    p.put("rate", rate);
                    points.add(p);
                }
                out.put("range", "month");
                out.put("points", points);
                return ApiResponse.success(out);
            } else {
                // 周度：最近 8 周，按周统计
                java.util.Calendar c = java.util.Calendar.getInstance();
                // 对齐到本周一
                c.set(java.util.Calendar.HOUR_OF_DAY, 0);
                c.set(java.util.Calendar.MINUTE, 0);
                c.set(java.util.Calendar.SECOND, 0);
                c.set(java.util.Calendar.MILLISECOND, 0);
                c.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);

                java.text.SimpleDateFormat weekFmt = new java.text.SimpleDateFormat("yyyy-'W'ww");
                List<ScheduleInfo> list = scheduleRepository.findByClassId(classId);
                for (int i = 0; i < 8; i++) {
                    Date weekStart = c.getTime();
                    c.add(java.util.Calendar.DAY_OF_MONTH, 6);
                    Date weekEnd = c.getTime();
                    // 回退到上一周开始（下个循环）
                    c.add(java.util.Calendar.DAY_OF_MONTH, -13);

                    int totalShould = 0, totalPresent = 0;
                    for (ScheduleInfo s : list) {
                        Date d = s.getDateOnly();
                        if (d == null || d.before(weekStart) || d.after(weekEnd)) continue;
                        int should = 0;
                        List<ClassEnrollment> enrollments = (s.getClassId() == null)
                                ? java.util.Collections.emptyList()
                                : classEnrollmentRepository.findByClassIdAndStatus(s.getClassId(), "active");
                        should = enrollments.size();
                        if (should == 0) continue;
                        List<AttendanceRecord> recs = attendanceRecordRepository.findByScheduleId(s.getId());
                        int present = 0;
                        for (AttendanceRecord rcd : recs) {
                            if (rcd.getSource() != null && "makeup".equalsIgnoreCase(rcd.getSource())) continue;
                            if (!Integer.valueOf(4).equals(rcd.getSignType())) present++;
                        }
                        totalShould += should;
                        totalPresent += present;
                    }
                    double rate = (totalShould > 0) ? Math.round((totalPresent * 100.0 / totalShould) * 10.0) / 10.0 : 0.0;
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("week", weekFmt.format(weekStart));
                    p.put("rate", rate);
                    points.add(p);
                }
                out.put("range", "week");
                out.put("points", points);
                return ApiResponse.success(out);
            }
        } catch (Exception e) {
            out.put("range", range);
            out.put("points", Collections.emptyList());
            return ApiResponse.success(out);
        }
    }

    @GetMapping("/teacher/stats")
    public ApiResponse<Map<String, Object>> teacherStats(@RequestParam("teacherId") Long teacherId,
                                                          @RequestParam("startDate") String startDate,
                                                          @RequestParam("endDate") String endDate) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("teacherId", teacherId);
        try {
            // 解析日期范围（包含端点日）
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            fmt.setLenient(false);
            Date start = null, end = null;
            if (startDate != null && !startDate.trim().isEmpty()) start = fmt.parse(startDate);
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = fmt.parse(endDate);
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTime(end);
                c.set(java.util.Calendar.HOUR_OF_DAY, 23);
                c.set(java.util.Calendar.MINUTE, 59);
                c.set(java.util.Calendar.SECOND, 59);
                c.set(java.util.Calendar.MILLISECOND, 999);
                end = c.getTime();
            }

            // 取该教师的课时并按日期范围过滤
            List<ScheduleInfo> list = scheduleRepository.findByTeacherId(teacherId);
            List<ScheduleInfo> range = new ArrayList<>();
            for (ScheduleInfo s : list) {
                Date d = s.getDateOnly();
                if (d == null) continue;
                if (start != null && end != null) {
                    if (!d.before(start) && !d.after(end)) range.add(s);
                } else {
                    range.add(s);
                }
            }

            // 计算整体出勤率（按所有课时汇总 present/应到）
            int totalShould = 0;
            int totalPresent = 0;
            for (ScheduleInfo s : range) {
                int should = 0;
                if ("2".equals(s.getClassType())) {
                    should = (s.getStudentId() != null) ? 1 : 0;
                } else {
                    List<ClassEnrollment> enrollments = (s.getClassId() == null)
                            ? java.util.Collections.emptyList()
                            : classEnrollmentRepository.findByClassIdAndStatus(s.getClassId(), "active");
                    should = enrollments.size();
                }
                if (should == 0) continue;

                List<AttendanceRecord> recs = attendanceRecordRepository.findByScheduleId(s.getId());
                int present = 0;
                for (AttendanceRecord r : recs) {
                    if (r.getSource() != null && "makeup".equalsIgnoreCase(r.getSource())) continue;
                    if (!Integer.valueOf(4).equals(r.getSignType())) present++;
                }
                totalShould += should;
                totalPresent += present;
            }

            double rate = (totalShould > 0) ? Math.round((totalPresent * 100.0 / totalShould) * 10.0) / 10.0 : 0.0;
            stats.put("overallAttendanceRate", rate);

            // ===== 教师签到发起及时率 =====
            // 口径：课时开始后 10 分钟内发起签到码记为及时；未发起记为不及时
            int scheduleCount = range.size();
            int startedCount = 0;
            int timelyCount = 0;
            long delaySumMs = 0L; // 仅统计已发起的延迟（若提前则按0）
            for (ScheduleInfo s : range) {
                java.util.Optional<AttendanceSignCode> codeOpt = attendanceSignCodeRepository.findTopByScheduleIdOrderByCreatedAtDesc(s.getId());
                AttendanceSignCode code = codeOpt.orElse(null);
                if (code != null) {
                    startedCount++;
                    Date startAt = s.getStartAt();
                    Date createdAt = code.getCreatedAt();
                    if (startAt != null && createdAt != null) {
                        long delta = createdAt.getTime() - startAt.getTime();
                        if (delta <= 10 * 60 * 1000) timelyCount++;
                        delaySumMs += Math.max(0L, delta);
                    }
                }
            }
            double timelyRate = (scheduleCount > 0) ? Math.round((timelyCount * 100.0 / scheduleCount) * 10.0) / 10.0 : 0.0;
            double avgDelayMinutes = (startedCount > 0) ? Math.round(((delaySumMs / 60000.0) / startedCount) * 10.0) / 10.0 : 0.0;
            stats.put("signStartTimelyRate", timelyRate);
            stats.put("signStartStartedCount", startedCount);
            stats.put("signStartAvgDelayMinutes", avgDelayMinutes);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            stats.put("overallAttendanceRate", 0.0);
            stats.put("signStartTimelyRate", 0.0);
            stats.put("signStartStartedCount", 0);
            stats.put("signStartAvgDelayMinutes", 0.0);
            return ApiResponse.success(stats);
        }
    }

    // =============== 班课签到：签到码生成与扫码签到/教师点名 ===============
    @PostMapping("/code/start")
    public ApiResponse<Map<String, Object>> startSignCode(@RequestBody Map<String, Object> payload) {
        Long scheduleId = toLong(payload.get("scheduleId"));
        Long teacherId = toLong(payload.get("teacherId"));
        if (scheduleId == null) return ApiResponse.error(400, "缺少参数 scheduleId");
        Optional<ScheduleInfo> sOpt = scheduleRepository.findById(scheduleId);
        if (!sOpt.isPresent()) return ApiResponse.error(404, "课时不存在");
        String code = genShortCode();
        AttendanceSignCode sc = new AttendanceSignCode();
        sc.setScheduleId(scheduleId);
        sc.setTeacherId(teacherId);
        sc.setCode(code);
        sc.setExpireAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
        attendanceSignCodeRepository.save(sc);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scheduleId", scheduleId);
        out.put("code", code);
        out.put("expireAt", sc.getExpireAt());
        return ApiResponse.success(out);
    }

    @PostMapping("/sign/scan")
    public ApiResponse<Map<String, Object>> scanSign(@RequestBody Map<String, Object> payload) {
        String code = String.valueOf(payload.getOrDefault("code", ""));
        Long scheduleId = toLong(payload.get("scheduleId"));
        Long studentId = toLong(payload.get("studentId"));
        if ((code == null || code.trim().isEmpty()) && scheduleId == null) return ApiResponse.error(400, "缺少 code 或 scheduleId");
        if (studentId == null) return ApiResponse.error(400, "缺少参数 studentId");
        if (code != null && !code.trim().isEmpty()) {
            Optional<AttendanceSignCode> cOpt = attendanceSignCodeRepository.findTopByCodeAndExpireAtAfter(code, new Date());
            if (!cOpt.isPresent()) return ApiResponse.error(400, "签到码无效或已过期");
            scheduleId = cOpt.get().getScheduleId();
        }
        Optional<ScheduleInfo> sOpt = scheduleRepository.findById(scheduleId);
        if (!sOpt.isPresent()) return ApiResponse.error(404, "课时不存在");
        ScheduleInfo s = sOpt.get();
        Date now = new Date();
        int signType = now.after(s.getStartAt()) ? 2 : 1; // 晚于上课时间记为迟到
        AttendanceRecord r = new AttendanceRecord();
        r.setScheduleId(scheduleId);
        r.setStudentId(studentId);
        r.setSignType(signType);
        r.setSignTime(now);
        r.setSource("qr");
        r.setRemark("扫码签到");
        attendanceRecordRepository.save(r);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("scheduleId", r.getScheduleId());
        out.put("studentId", r.getStudentId());
        out.put("signType", r.getSignType());
        out.put("signTime", r.getSignTime());
        // 自动核算：课时签到完成后触发
        try { teacherFeeService.autoCalcForSchedule(scheduleId); } catch (Exception ignore) {}
        return ApiResponse.success(out);
    }

    @PostMapping("/teacher/mark")
    public ApiResponse<Map<String, Object>> teacherBulkMark(@RequestBody Map<String, Object> payload) {
        Long scheduleId = toLong(payload.get("scheduleId"));
        String op = String.valueOf(payload.getOrDefault("op", "")); // all_present / all_absent
        if (scheduleId == null) return ApiResponse.error(400, "缺少参数 scheduleId");
        if (!"all_present".equals(op) && !"all_absent".equals(op)) return ApiResponse.error(400, "不支持的操作");
        Optional<ScheduleInfo> sOpt = scheduleRepository.findById(scheduleId);
        if (!sOpt.isPresent()) return ApiResponse.error(404, "课时不存在");
        ScheduleInfo s = sOpt.get();
        Date now = new Date();
        int presentType = now.after(s.getStartAt()) ? 2 : 1;
        int absentType = 4;

        int affected = 0;
        if ("2".equals(s.getClassType())) { // 一对一：仅针对该学员
            if (s.getStudentId() != null) {
                AttendanceRecord r = new AttendanceRecord();
                r.setScheduleId(scheduleId);
                r.setStudentId(s.getStudentId());
                r.setSignType("all_present".equals(op) ? presentType : absentType);
                r.setSignTime(now);
                r.setSource("manual");
                r.setRemark("教师点名:" + op);
                attendanceRecordRepository.save(r);
                affected++;
            }
        } else { // 班课：遍历班级报名学员
            Long classId = s.getClassId();
            List<ClassEnrollment> list = (classId == null) ? Collections.emptyList() : classEnrollmentRepository.findByClassIdAndStatus(classId, "active");
            for (ClassEnrollment ce : list) {
                AttendanceRecord r = new AttendanceRecord();
                r.setScheduleId(scheduleId);
                r.setStudentId(ce.getStudentId());
                r.setSignType("all_present".equals(op) ? presentType : absentType);
                r.setSignTime(now);
                r.setSource("manual");
                r.setRemark("教师点名:" + op);
                attendanceRecordRepository.save(r);
                affected++;
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("affected", affected);
        // 自动核算：教师批量点名完成后触发
        try { teacherFeeService.autoCalcForSchedule(scheduleId); } catch (Exception ignore) {}
        return ApiResponse.success(out);
    }

    // =============== 一对一签到：开始、学员确认、状态查询 ===============
    @PostMapping("/o2o/start")
    public ApiResponse<Map<String, Object>> o2oStart(@RequestBody Map<String, Object> payload) {
        Long scheduleId = toLong(payload.get("scheduleId"));
        if (scheduleId == null) return ApiResponse.error(400, "缺少参数 scheduleId");
        Optional<ScheduleInfo> sOpt = scheduleRepository.findById(scheduleId);
        if (!sOpt.isPresent()) return ApiResponse.error(404, "课时不存在");
        OneToOneConfirm c = new OneToOneConfirm();
        c.setScheduleId(scheduleId);
        c.setTeacherConfirmedAt(new Date());
        c.setExpireAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
        c.setStatus("pending");
        oneToOneConfirmRepository.save(c);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scheduleId", scheduleId);
        out.put("status", c.getStatus());
        out.put("expireAt", c.getExpireAt());
        return ApiResponse.success(out);
    }

    @PostMapping("/o2o/student-confirm")
    public ApiResponse<Map<String, Object>> o2oStudentConfirm(@RequestBody Map<String, Object> payload) {
        Long scheduleId = toLong(payload.get("scheduleId"));
        Long studentId = toLong(payload.get("studentId"));
        if (scheduleId == null || studentId == null) return ApiResponse.error(400, "缺少参数 scheduleId 或 studentId");
        Optional<OneToOneConfirm> opt = oneToOneConfirmRepository.findTopByScheduleIdOrderByCreatedAtDesc(scheduleId);
        if (!opt.isPresent()) return ApiResponse.error(404, "未找到待确认记录");
        OneToOneConfirm c = opt.get();
        if (c.getExpireAt() != null && c.getExpireAt().before(new Date())) {
            c.setStatus("expired");
            oneToOneConfirmRepository.save(c);
            return ApiResponse.error(400, "确认已过期");
        }
        Optional<ScheduleInfo> sOpt = scheduleRepository.findById(scheduleId);
        if (!sOpt.isPresent()) return ApiResponse.error(404, "课时不存在");
        ScheduleInfo s = sOpt.get();
        c.setStudentConfirmedAt(new Date());
        c.setStatus("finished");
        oneToOneConfirmRepository.save(c);

        // 完成双向确认后记录已签到
        Date now = new Date();
        int signType = now.after(s.getStartAt()) ? 2 : 1;
        AttendanceRecord r = new AttendanceRecord();
        r.setScheduleId(scheduleId);
        r.setStudentId(studentId);
        r.setSignType(signType);
        r.setSignTime(now);
        r.setSource("o2o");
        r.setRemark("一对一双向确认");
        attendanceRecordRepository.save(r);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", c.getStatus());
        out.put("attendanceId", r.getId());
        // 自动核算：一对一双方确认完成后触发
        try { teacherFeeService.autoCalcForSchedule(scheduleId); } catch (Exception ignore) {}
        return ApiResponse.success(out);
    }

    @GetMapping("/o2o/status")
    public ApiResponse<Map<String, Object>> o2oStatus(@RequestParam("scheduleId") Long scheduleId) {
        Optional<OneToOneConfirm> opt = oneToOneConfirmRepository.findTopByScheduleIdOrderByCreatedAtDesc(scheduleId);
        Map<String, Object> out = new LinkedHashMap<>();
        if (!opt.isPresent()) {
            out.put("status", "none");
            return ApiResponse.success(out);
        }
        OneToOneConfirm c = opt.get();
        out.put("status", c.getStatus());
        out.put("expireAt", c.getExpireAt());
        long remainMs = (c.getExpireAt() == null) ? 0 : (c.getExpireAt().getTime() - System.currentTimeMillis());
        out.put("remainMs", remainMs);
        out.put("expired", c.getExpireAt() != null && c.getExpireAt().before(new Date()));
        return ApiResponse.success(out);
    }

    // =============== 补签管理：申请与审批 ===============
    @PostMapping("/makeup/apply")
    public ApiResponse<Map<String, Object>> makeupApply(@RequestBody Map<String, Object> payload) {
        Long scheduleId = toLong(payload.get("scheduleId"));
        Long studentId = toLong(payload.get("studentId"));
        String reason = String.valueOf(payload.getOrDefault("reason", ""));
        if (scheduleId == null || studentId == null) return ApiResponse.error(400, "缺少参数 scheduleId 或 studentId");
        Optional<ScheduleInfo> sOpt = scheduleRepository.findById(scheduleId);
        if (!sOpt.isPresent()) return ApiResponse.error(404, "课时不存在");
        ScheduleInfo s = sOpt.get();
        Date now = new Date();
        if (now.before(s.getEndAt())) return ApiResponse.error(400, "课时尚未结束，暂不可申请补签");
        if (now.after(new Date(s.getEndAt().getTime() + 24 * 60 * 60 * 1000))) return ApiResponse.error(400, "超过补签申请时间窗口");
        MakeupApply m = new MakeupApply();
        m.setScheduleId(scheduleId);
        m.setStudentId(studentId);
        m.setReason(reason);
        m.setStatus("pending");
        makeupApplyRepository.save(m);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("applyId", m.getId());
        out.put("status", m.getStatus());
        return ApiResponse.success(out);
    }

    @PostMapping("/makeup/approve")
    public ApiResponse<Map<String, Object>> makeupApprove(@RequestBody Map<String, Object> payload) {
        Long applyId = toLong(payload.get("applyId"));
        Boolean approved = (payload.get("approved") instanceof Boolean) ? (Boolean) payload.get("approved") : null;
        if (applyId == null || approved == null) return ApiResponse.error(400, "缺少参数 applyId 或 approved");
        Optional<MakeupApply> mOpt = makeupApplyRepository.findById(applyId);
        if (!mOpt.isPresent()) return ApiResponse.error(404, "补签申请不存在");
        MakeupApply m = mOpt.get();
        m.setStatus(approved ? "approved" : "rejected");
        m.setAuditedAt(new Date());
        makeupApplyRepository.save(m);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("applyId", m.getId());
        out.put("status", m.getStatus());

        // 审批通过：记录一条“补签”考勤，不影响出勤率的统计由统计接口侧过滤 source=makeup
        if (approved) {
            AttendanceRecord r = new AttendanceRecord();
            r.setScheduleId(m.getScheduleId());
            r.setStudentId(m.getStudentId());
            r.setSignType(1);
            r.setSignTime(new Date());
            r.setSource("makeup");
            r.setRemark("补签记录");
            attendanceRecordRepository.save(r);
            out.put("attendanceId", r.getId());
        }
        return ApiResponse.success(out);
    }

    // =============== 工具方法 ===============
    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private String genShortCode() {
        String s = Long.toString(Math.abs(new java.util.Random().nextLong()), 36).toUpperCase();
        return s.substring(0, Math.min(6, s.length()));
    }
}