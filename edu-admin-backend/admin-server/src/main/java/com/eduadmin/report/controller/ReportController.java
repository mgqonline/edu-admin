package com.eduadmin.report.controller;

import com.eduadmin.attendance.entity.AttendanceRecord;
import com.eduadmin.attendance.repo.AttendanceRecordRepository;
import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.entity.ClassEntity;
import com.eduadmin.course.entity.ClassEnrollment;
import com.eduadmin.course.repo.ClassEnrollmentRepository;
import com.eduadmin.course.repo.ClassRepository;
import com.eduadmin.course.store.InMemoryCourseStore;
import com.eduadmin.finance.entity.FinanceSettlement;
import com.eduadmin.finance.entity.RefundRequest;
import com.eduadmin.finance.repo.FinanceSettlementRepository;
import com.eduadmin.finance.repo.RefundRequestRepository;
import com.eduadmin.scheduling.entity.ScheduleInfo;
import com.eduadmin.scheduling.repo.ScheduleRepository;
import com.eduadmin.teacher.entity.TeacherLessonFee;
import com.eduadmin.teacher.repository.TeacherLessonFeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ClassEnrollmentRepository classEnrollmentRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private FinanceSettlementRepository financeSettlementRepository;
    @Autowired
    private RefundRequestRepository refundRequestRepository;
    @Autowired
    private TeacherLessonFeeRepository teacherLessonFeeRepository;

    // ============== 招生数据 ==============
    @GetMapping("/enroll/new")
    public ApiResponse<Map<String, Object>> enrollNew(@RequestParam(value = "range", required = false, defaultValue = "day") String range) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<ClassEnrollment> all = classEnrollmentRepository.findAll();
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM");
        Calendar c = Calendar.getInstance();
        List<Map<String, Object>> points = new ArrayList<>();
        if ("month".equalsIgnoreCase(range)) {
            // 最近 6 个月
            c.set(Calendar.DAY_OF_MONTH, 1);
            for (int i = 0; i < 6; i++) {
                Date start = c.getTime();
                Calendar e = (Calendar) c.clone();
                e.add(Calendar.MONTH, 1);
                e.add(Calendar.DAY_OF_MONTH, -1);
                Date end = e.getTime();
                String key = monthFmt.format(start);
                int cnt = 0;
                for (ClassEnrollment ce : all) {
                    Date d = ce.getEnrolledAt();
                    if (d != null && !d.before(start) && !d.after(end)) cnt++;
                }
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("month", key);
                p.put("count", cnt);
                points.add(p);
                c.add(Calendar.MONTH, -1);
            }
            Collections.reverse(points);
            out.put("range", "month");
        } else if ("week".equalsIgnoreCase(range)) {
            // 最近 8 周
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            SimpleDateFormat weekFmt = new SimpleDateFormat("yyyy-'W'ww");
            for (int i = 0; i < 8; i++) {
                Date weekStart = c.getTime();
                Calendar e = (Calendar) c.clone();
                e.add(Calendar.DAY_OF_MONTH, 6);
                Date weekEnd = e.getTime();
                int cnt = 0;
                for (ClassEnrollment ce : all) {
                    Date d = ce.getEnrolledAt();
                    if (d != null && !d.before(weekStart) && !d.after(weekEnd)) cnt++;
                }
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("week", weekFmt.format(weekStart));
                p.put("count", cnt);
                points.add(p);
                c.add(Calendar.DAY_OF_MONTH, -7);
            }
            Collections.reverse(points);
            out.put("range", "week");
        } else {
            // 最近 14 天
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            for (int i = 0; i < 14; i++) {
                Date start = c.getTime();
                Calendar e = (Calendar) c.clone();
                e.add(Calendar.DAY_OF_MONTH, 1);
                e.add(Calendar.MILLISECOND, -1);
                Date end = e.getTime();
                int cnt = 0;
                for (ClassEnrollment ce : all) {
                    Date d = ce.getEnrolledAt();
                    if (d != null && !d.before(start) && !d.after(end)) cnt++;
                }
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("date", dayFmt.format(start));
                p.put("count", cnt);
                points.add(p);
                c.add(Calendar.DAY_OF_MONTH, -1);
            }
            Collections.reverse(points);
            out.put("range", "day");
        }
        out.put("points", points);
        return ApiResponse.success(out);
    }

    @GetMapping("/enroll/channel-share")
    public ApiResponse<List<Map<String, Object>>> enrollChannelShare() {
        List<ClassEnrollment> list = classEnrollmentRepository.findAll();
        Map<String, Integer> cnt = new LinkedHashMap<>();
        int total = 0;
        for (ClassEnrollment ce : list) {
            String src = (ce.getSource() == null || ce.getSource().isEmpty()) ? "unknown" : ce.getSource();
            cnt.put(src, cnt.getOrDefault(src, 0) + 1);
            total++;
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : cnt.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("channel", e.getKey());
            m.put("count", e.getValue());
            m.put("ratio", total > 0 ? Math.round(e.getValue() * 1000.0 / total) / 10.0 : 0.0);
            out.add(m);
        }
        out.sort((a,b) -> Integer.compare(((Number)b.get("count")).intValue(), ((Number)a.get("count")).intValue()));
        return ApiResponse.success(out);
    }

    // ============== 教学数据 ==============
    @GetMapping("/teaching/class-attendance-rank")
    public ApiResponse<List<Map<String, Object>>> classAttendanceRank(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(value = "top", required = false, defaultValue = "5") Integer top
    ) {
        List<ClassEntity> classes = classRepository.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (ClassEntity clazz : classes) {
            List<ScheduleInfo> schedules = scheduleRepository.findByClassId(clazz.getId());
            double rateSum = 0.0;
            int rateCount = 0;
            for (ScheduleInfo s : schedules) {
                Date d = s.getDateOnly();
                if (d == null) continue;
                if (startDate != null && endDate != null) {
                    if (d.before(startDate) || d.after(endDate)) continue;
                }
                int should = classEnrollmentRepository.findByClassIdAndStatus(clazz.getId(), "active").size();
                if (should == 0) continue;
                List<AttendanceRecord> recs = attendanceRecordRepository.findByScheduleId(s.getId());
                int present = 0;
                for (AttendanceRecord r : recs) {
                    if (r.getSource() != null && "makeup".equalsIgnoreCase(r.getSource())) continue;
                    if (!Integer.valueOf(4).equals(r.getSignType())) present++;
                }
                double rate = present * 100.0 / should;
                rateSum += rate;
                rateCount++;
            }
            double avg = (rateCount > 0) ? Math.round(rateSum * 10.0 / rateCount) / 10.0 : 0.0;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("classId", clazz.getId());
            m.put("className", clazz.getName());
            m.put("avgAttendanceRate", avg);
            out.add(m);
        }
        out.sort((a,b) -> Double.compare(((Number)b.get("avgAttendanceRate")).doubleValue(), ((Number)a.get("avgAttendanceRate")).doubleValue()));
        if (top != null && top > 0 && out.size() > top) out = out.subList(0, top);
        return ApiResponse.success(out);
    }

    @GetMapping("/teaching/teacher-hours")
    public ApiResponse<List<Map<String, Object>>> teacherHours(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        List<ScheduleInfo> schedules = (startDate != null && endDate != null)
                ? scheduleRepository.findByDateOnlyBetween(startDate, endDate)
                : scheduleRepository.findAll();
        Map<Long, Double> hoursByTeacher = new LinkedHashMap<>();
        for (ScheduleInfo s : schedules) {
            Long tid = s.getTeacherId();
            if (tid == null) continue;
            Date st = s.getStartAt();
            Date ed = s.getEndAt();
            if (st == null || ed == null) continue;
            double hours = Math.max(0, (ed.getTime() - st.getTime()) / 3600000.0);
            hoursByTeacher.put(tid, hoursByTeacher.getOrDefault(tid, 0.0) + hours);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<Long, Double> e : hoursByTeacher.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("teacherId", e.getKey());
            m.put("totalHours", Math.round(e.getValue() * 10.0) / 10.0);
            out.add(m);
        }
        out.sort((a,b) -> Double.compare(((Number)b.get("totalHours")).doubleValue(), ((Number)a.get("totalHours")).doubleValue()));
        return ApiResponse.success(out);
    }

    @GetMapping("/student/renew-rate")
    public ApiResponse<List<Map<String, Object>>> renewRate(@RequestParam(value = "type", required = false, defaultValue = "course") String type) {
        // 近似定义：同一课程/班级中，缴费记录数量>=2 的学生视为续费
        List<FinanceSettlement> settlements = financeSettlementRepository.findAll();
        Map<String, Set<Long>> studentsByKey = new LinkedHashMap<>(); // key -> student set
        for (FinanceSettlement f : settlements) {
            String key;
            if ("class".equalsIgnoreCase(type)) {
                key = String.valueOf(f.getClassId());
            } else {
                // 通过班级反查课程
                Map<String, Object> cls = (f.getClassId() == null) ? null : InMemoryCourseStore.classes.get(String.valueOf(f.getClassId()));
                String courseId = (cls == null) ? null : String.valueOf(cls.get("courseId"));
                key = String.valueOf(courseId);
            }
            if (key == null || key.equals("null")) continue;
            studentsByKey.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(f.getStudentId());
        }
        // 统计每个 key 下学生的缴费次数
        Map<String, Map<Long, Integer>> payTimes = new LinkedHashMap<>();
        for (FinanceSettlement f : settlements) {
            String key;
            if ("class".equalsIgnoreCase(type)) {
                key = String.valueOf(f.getClassId());
            } else {
                Map<String, Object> cls = (f.getClassId() == null) ? null : InMemoryCourseStore.classes.get(String.valueOf(f.getClassId()));
                String courseId = (cls == null) ? null : String.valueOf(cls.get("courseId"));
                key = String.valueOf(courseId);
            }
            if (key == null || key.equals("null")) continue;
            Long sid = f.getStudentId();
            payTimes.computeIfAbsent(key, k -> new LinkedHashMap<>());
            Map<Long, Integer> m = payTimes.get(key);
            m.put(sid, m.getOrDefault(sid, 0) + 1);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Set<Long>> e : studentsByKey.entrySet()) {
            Map<Long, Integer> m = payTimes.getOrDefault(e.getKey(), Collections.emptyMap());
            int total = e.getValue().size();
            int renew = 0;
            for (Long sid : e.getValue()) { if (m.getOrDefault(sid, 0) >= 2) renew++; }
            double rate = total > 0 ? Math.round(renew * 1000.0 / total) / 10.0 : 0.0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", e.getKey());
            row.put("total", total);
            row.put("renew", renew);
            row.put("rate", rate);
            out.add(row);
        }
        out.sort((a,b) -> Double.compare(((Number)b.get("rate")).doubleValue(), ((Number)a.get("rate")).doubleValue()));
        return ApiResponse.success(out);
    }

    @GetMapping("/student/trial-conversion-trend")
    public ApiResponse<Map<String, Object>> trialConversionTrend() {
        // 最近 6 个月的试听完成 -> 转化（30 天内产生缴费）趋势
        List<com.eduadmin.student.store.AuditionStore.Record> auditions = com.eduadmin.student.store.AuditionStore.list();
        List<FinanceSettlement> settlements = financeSettlementRepository.findAll();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        List<Map<String, Object>> points = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Date start = c.getTime();
            Calendar e = (Calendar) c.clone();
            e.add(Calendar.MONTH, 1); e.add(Calendar.DAY_OF_MONTH, -1);
            Date end = e.getTime();

            int finished = 0;
            int converted = 0;
            for (com.eduadmin.student.store.AuditionStore.Record a : auditions) {
                if (!"finished".equalsIgnoreCase(a.status)) continue;
                Date at = a.time; if (at == null) continue;
                if (at.before(start) || at.after(end)) continue;
                finished++;
                // 判断 30 天内是否有同学员的缴费记录，若能反查到课程则尽量匹配同课程
                Calendar limit = Calendar.getInstance();
                limit.setTime(at); limit.add(Calendar.DAY_OF_MONTH, 30);
                Date deadline = limit.getTime();
                boolean ok = false;
                for (FinanceSettlement f : settlements) {
                    if (f.getStudentId() == null) continue;
                    if (!Objects.equals(f.getStudentId(), a.studentId)) continue;
                    Date ct = f.getCreatedAt(); if (ct == null) continue;
                    if (ct.before(at) || ct.after(deadline)) continue;
                    // 课程匹配：通过班级映射课程
                    boolean courseMatch = true;
                    if (f.getClassId() != null && a.courseId != null) {
                        Map<String, Object> cls = InMemoryCourseStore.classes.get(String.valueOf(f.getClassId()));
                        String courseId = cls == null ? null : String.valueOf(cls.get("courseId"));
                        courseMatch = Objects.equals(String.valueOf(a.courseId), courseId);
                    }
                    if (courseMatch) { ok = true; break; }
                }
                if (ok) converted++;
            }
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("month", fmt.format(start));
            p.put("finished", finished);
            p.put("converted", converted);
            p.put("rate", finished > 0 ? Math.round(converted * 1000.0 / finished) / 10.0 : 0.0);
            points.add(p);
            c.add(Calendar.MONTH, -1);
        }
        Collections.reverse(points);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("points", points);
        return ApiResponse.success(out);
    }

    // ============== 财务数据 ==============
    @GetMapping("/finance/revenue-trend")
    public ApiResponse<Map<String, Object>> revenueTrend(@RequestParam(value = "range", required = false, defaultValue = "month") String range) {
        List<FinanceSettlement> list = financeSettlementRepository.findAll();
        Map<String, Object> out = new LinkedHashMap<>();
        List<Map<String, Object>> points = new ArrayList<>();
        if ("quarter".equalsIgnoreCase(range)) {
            // 最近 4 个季度
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            for (int i = 0; i < 4; i++) {
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH) + 1; // 1..12
                int q = (month - 1) / 3 + 1;
                Calendar start = (Calendar) c.clone();
                start.set(Calendar.MONTH, (q - 1) * 3);
                Date s = start.getTime();
                Calendar end = (Calendar) start.clone();
                end.add(Calendar.MONTH, 3);
                end.add(Calendar.DAY_OF_MONTH, -1);
                Date e = end.getTime();
                BigDecimal sum = BigDecimal.ZERO;
                for (FinanceSettlement f : list) {
                    Date d = f.getCreatedAt();
                    if (d != null && !d.before(s) && !d.after(e)) sum = sum.add(nvl(f.getTotalFee()));
                }
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("quarter", year + "Q" + q);
                p.put("amount", sum.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                points.add(p);
                c.add(Calendar.MONTH, -3);
            }
            Collections.reverse(points);
            out.put("range", "quarter");
        } else {
            // 最近 6 个月
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            for (int i = 0; i < 6; i++) {
                Date s = c.getTime();
                Calendar e = (Calendar) c.clone();
                e.add(Calendar.MONTH, 1);
                e.add(Calendar.DAY_OF_MONTH, -1);
                Date end = e.getTime();
                BigDecimal sum = BigDecimal.ZERO;
                for (FinanceSettlement f : list) {
                    Date d = f.getCreatedAt();
                    if (d != null && !d.before(s) && !d.after(end)) sum = sum.add(nvl(f.getTotalFee()));
                }
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("month", fmt.format(s));
                p.put("amount", sum.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                points.add(p);
                c.add(Calendar.MONTH, -1);
            }
            Collections.reverse(points);
            out.put("range", "month");
        }
        out.put("points", points);
        return ApiResponse.success(out);
    }

    @GetMapping("/finance/course-income-share")
    public ApiResponse<List<Map<String, Object>>> courseIncomeShare() {
        List<FinanceSettlement> list = financeSettlementRepository.findAll();
        Map<String, BigDecimal> sumByCourse = new LinkedHashMap<>();
        for (FinanceSettlement f : list) {
            Map<String, Object> cls = (f.getClassId() == null) ? null : InMemoryCourseStore.classes.get(String.valueOf(f.getClassId()));
            String courseId = (cls == null) ? null : String.valueOf(cls.get("courseId"));
            if (courseId == null) continue;
            sumByCourse.put(courseId, sumByCourse.getOrDefault(courseId, BigDecimal.ZERO).add(nvl(f.getTotalFee())));
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : sumByCourse.values()) total = total.add(v);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : sumByCourse.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            String courseName = Optional.ofNullable(InMemoryCourseStore.courses.get(e.getKey()))
                    .map(c -> String.valueOf(c.get("name"))).orElse("课程#" + e.getKey());
            m.put("courseId", e.getKey());
            m.put("courseName", courseName);
            m.put("amount", e.getValue().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            double ratio = (total.compareTo(BigDecimal.ZERO) > 0)
                    ? e.getValue().multiply(new BigDecimal("100")).divide(total, 1, BigDecimal.ROUND_HALF_UP).doubleValue()
                    : 0.0;
            m.put("ratio", ratio);
            out.add(m);
        }
        out.sort((a,b) -> Double.compare(((Number)b.get("amount")).doubleValue(), ((Number)a.get("amount")).doubleValue()));
        return ApiResponse.success(out);
    }

    @GetMapping("/finance/refund-stats")
    public ApiResponse<Map<String, Object>> refundStats(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        List<RefundRequest> list = refundRequestRepository.findAll();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
        Map<String, BigDecimal> sumByMonth = new LinkedHashMap<>();
        for (RefundRequest r : list) {
            Date d = r.getCreatedAt();
            if (d == null) continue;
            if (startDate != null && endDate != null) {
                if (d.before(startDate) || d.after(endDate)) continue;
            }
            String key = fmt.format(d);
            sumByMonth.put(key, sumByMonth.getOrDefault(key, BigDecimal.ZERO).add(nvl(r.getFinalAmount())));
        }
        List<Map<String, Object>> points = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : sumByMonth.entrySet()) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("month", e.getKey());
            p.put("amount", e.getValue().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            points.add(p);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("points", points);
        return ApiResponse.success(out);
    }

    @GetMapping("/finance/net-profit")
    public ApiResponse<Map<String, Object>> netProfit(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        Date s = startDate, e = endDate;
        // 计算总营收
        BigDecimal revenue = BigDecimal.ZERO;
        for (FinanceSettlement f : financeSettlementRepository.findAll()) {
            Date d = f.getCreatedAt();
            if (inRange(d, s, e)) revenue = revenue.add(nvl(f.getTotalFee()));
        }
        // 计算退费
        BigDecimal refunds = BigDecimal.ZERO;
        for (RefundRequest r : refundRequestRepository.findAll()) {
            Date d = r.getCreatedAt();
            if (inRange(d, s, e)) refunds = refunds.add(nvl(r.getFinalAmount()));
        }
        // 计算教师课时费支出
        BigDecimal teacherFees = BigDecimal.ZERO;
        for (TeacherLessonFee tlf : teacherLessonFeeRepository.findAll()) {
            Date d = tlf.getCreatedAt();
            if (inRange(d, s, e)) teacherFees = teacherFees.add(nvl(tlf.getFinalAmount()));
        }
        BigDecimal profit = revenue.subtract(refunds).subtract(teacherFees);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("revenue", revenue.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        out.put("refunds", refunds.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        out.put("teacherFees", teacherFees.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        out.put("netProfit", profit.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        return ApiResponse.success(out);
    }

    // ============== 导出 CSV ==============
    private String csvEscape(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\n") || t.contains("\r")) return "\"" + t + "\"";
        return t;
    }
    private ResponseEntity<byte[]> csv(String filename, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8");
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"" );
        return ResponseEntity.ok().headers(headers).body(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @GetMapping("/export/enroll-new.csv")
    public ResponseEntity<byte[]> exportEnrollNew(@RequestParam(value = "range", required = false, defaultValue = "day") String range) {
        ApiResponse<Map<String, Object>> resp = enrollNew(range);
        Map<String, Object> data = resp.getData();
        List<Map<String, Object>> points = (List<Map<String, Object>>) data.getOrDefault("points", Collections.emptyList());
        StringBuilder sb = new StringBuilder();
        if ("week".equalsIgnoreCase(range)) { sb.append("week,count\n"); for (Map<String,Object> p: points){ sb.append(csvEscape(String.valueOf(p.get("week")))).append(',').append(String.valueOf(p.get("count"))).append('\n'); } }
        else if ("month".equalsIgnoreCase(range)) { sb.append("month,count\n"); for (Map<String,Object> p: points){ sb.append(csvEscape(String.valueOf(p.get("month")))).append(',').append(String.valueOf(p.get("count"))).append('\n'); } }
        else { sb.append("date,count\n"); for (Map<String,Object> p: points){ sb.append(csvEscape(String.valueOf(p.get("date")))).append(',').append(String.valueOf(p.get("count"))).append('\n'); } }
        return csv("enroll-new.csv", sb.toString());
    }

    @GetMapping("/export/channel-share.csv")
    public ResponseEntity<byte[]> exportChannelShare() {
        ApiResponse<List<Map<String, Object>>> resp = enrollChannelShare();
        List<Map<String, Object>> list = resp.getData();
        StringBuilder sb = new StringBuilder(); sb.append("channel,count,ratio\n");
        for (Map<String,Object> m: list){ sb.append(csvEscape(String.valueOf(m.get("channel")))).append(',').append(String.valueOf(m.get("count"))).append(',').append(String.valueOf(m.get("ratio"))).append('\n'); }
        return csv("channel-share.csv", sb.toString());
    }

    @GetMapping("/export/class-attendance-rank.csv")
    public ResponseEntity<byte[]> exportClassAttendanceRank(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(value = "top", required = false, defaultValue = "5") Integer top
    ) {
        ApiResponse<List<Map<String, Object>>> resp = classAttendanceRank(startDate, endDate, top);
        List<Map<String, Object>> list = resp.getData();
        StringBuilder sb = new StringBuilder(); sb.append("classId,className,avgAttendanceRate\n");
        for (Map<String,Object> m: list){ sb.append(String.valueOf(m.get("classId"))).append(',').append(csvEscape(String.valueOf(m.get("className")))).append(',').append(String.valueOf(m.get("avgAttendanceRate"))).append('\n'); }
        return csv("class-attendance-rank.csv", sb.toString());
    }

    @GetMapping("/export/teacher-hours.csv")
    public ResponseEntity<byte[]> exportTeacherHours(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        ApiResponse<List<Map<String, Object>>> resp = teacherHours(startDate, endDate);
        List<Map<String, Object>> list = resp.getData();
        StringBuilder sb = new StringBuilder(); sb.append("teacherId,totalHours\n");
        for (Map<String,Object> m: list){ sb.append(String.valueOf(m.get("teacherId"))).append(',').append(String.valueOf(m.get("totalHours"))).append('\n'); }
        return csv("teacher-hours.csv", sb.toString());
    }

    @GetMapping("/export/revenue-trend.csv")
    public ResponseEntity<byte[]> exportRevenueTrend(@RequestParam(value = "range", required = false, defaultValue = "month") String range) {
        ApiResponse<Map<String, Object>> resp = revenueTrend(range);
        Map<String, Object> data = resp.getData();
        List<Map<String, Object>> points = (List<Map<String, Object>>) data.getOrDefault("points", Collections.emptyList());
        StringBuilder sb = new StringBuilder();
        if ("quarter".equalsIgnoreCase(range)) { sb.append("quarter,amount\n"); for (Map<String,Object> p: points){ sb.append(csvEscape(String.valueOf(p.get("quarter")))).append(',').append(String.valueOf(p.get("amount"))).append('\n'); } }
        else { sb.append("month,amount\n"); for (Map<String,Object> p: points){ sb.append(csvEscape(String.valueOf(p.get("month")))).append(',').append(String.valueOf(p.get("amount"))).append('\n'); } }
        return csv("revenue-trend.csv", sb.toString());
    }

    @GetMapping("/export/course-income-share.csv")
    public ResponseEntity<byte[]> exportCourseIncomeShare() {
        ApiResponse<List<Map<String, Object>>> resp = courseIncomeShare();
        List<Map<String, Object>> list = resp.getData();
        StringBuilder sb = new StringBuilder(); sb.append("courseId,courseName,amount,ratio\n");
        for (Map<String,Object> m: list){ sb.append(String.valueOf(m.get("courseId"))).append(',').append(csvEscape(String.valueOf(m.get("courseName")))).append(',').append(String.valueOf(m.get("amount"))).append(',').append(String.valueOf(m.get("ratio"))).append('\n'); }
        return csv("course-income-share.csv", sb.toString());
    }

    @GetMapping("/export/refund-stats.csv")
    public ResponseEntity<byte[]> exportRefundStats(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        ApiResponse<Map<String, Object>> resp = refundStats(startDate, endDate);
        Map<String, Object> data = resp.getData();
        List<Map<String, Object>> points = (List<Map<String, Object>>) data.getOrDefault("points", Collections.emptyList());
        StringBuilder sb = new StringBuilder(); sb.append("month,amount\n");
        for (Map<String,Object> p: points){ sb.append(csvEscape(String.valueOf(p.get("month")))).append(',').append(String.valueOf(p.get("amount"))).append('\n'); }
        return csv("refund-stats.csv", sb.toString());
    }

    @GetMapping("/export/net-profit.csv")
    public ResponseEntity<byte[]> exportNetProfit(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        ApiResponse<Map<String, Object>> resp = netProfit(startDate, endDate);
        Map<String, Object> d = resp.getData();
        StringBuilder sb = new StringBuilder(); sb.append("revenue,refunds,teacherFees,netProfit\n");
        sb.append(String.valueOf(d.getOrDefault("revenue", 0))).append(',')
                .append(String.valueOf(d.getOrDefault("refunds", 0))).append(',')
                .append(String.valueOf(d.getOrDefault("teacherFees", 0))).append(',')
                .append(String.valueOf(d.getOrDefault("netProfit", 0))).append('\n');
        return csv("net-profit.csv", sb.toString());
    }

    @GetMapping("/export/trial-conversion-trend.csv")
    public ResponseEntity<byte[]> exportTrialConversionTrend() {
        ApiResponse<Map<String, Object>> resp = trialConversionTrend();
        Map<String, Object> data = resp.getData();
        List<Map<String, Object>> points = (List<Map<String, Object>>) data.getOrDefault("points", Collections.emptyList());
        StringBuilder sb = new StringBuilder(); sb.append("month,finished,converted,rate\n");
        for (Map<String,Object> p: points){ sb.append(csvEscape(String.valueOf(p.get("month")))).append(',').append(String.valueOf(p.get("finished"))).append(',').append(String.valueOf(p.get("converted"))).append(',').append(String.valueOf(p.get("rate"))).append('\n'); }
        return csv("trial-conversion-trend.csv", sb.toString());
    }

    private boolean inRange(Date d, Date s, Date e) {
        if (d == null) return false;
        if (s != null && d.before(s)) return false;
        if (e != null) {
            Calendar c = Calendar.getInstance(); c.setTime(e);
            c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999);
            Date end = c.getTime();
            if (d.after(end)) return false;
        }
        return true;
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}