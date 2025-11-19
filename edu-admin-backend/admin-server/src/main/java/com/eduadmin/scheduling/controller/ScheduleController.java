package com.eduadmin.scheduling.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.entity.ClassInfo;
import com.eduadmin.course.repo.ClassInfoRepository;
import com.eduadmin.scheduling.entity.ScheduleInfo;
import com.eduadmin.scheduling.repo.ScheduleRepository;
import com.eduadmin.scheduling.repo.SystemHolidayRepository;
import com.eduadmin.system.security.RequiresPerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SystemHolidayRepository holidayRepository;

    @Autowired
    private ClassInfoRepository classInfoRepository;

    private Date parseTime(Date dateOnly, String hhmm) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat dFmt = new SimpleDateFormat("yyyy-MM-dd");
        String base = dFmt.format(dateOnly) + " " + hhmm;
        return fmt.parse(base);
    }

    private static class ConflictItem {
        public String type; // teacher/room/student
        public String reason;
        public Date date;
        public String timeRange;
        public Long refId; // teacherId/room as hash/studentId
    }

    @RequiresPerm("course:schedule")
    @PostMapping("/batch/month")
    @Transactional
    public ApiResponse<Map<String, Object>> batchMonth(@RequestBody Map<String, Object> params) {
        Long classId = toLong(params.get("classId"));
        String targetMonth = String.valueOf(params.get("targetMonth")); // yyyy-MM
        @SuppressWarnings("unchecked") List<Integer> weekDays = (List<Integer>) params.getOrDefault("weekDays", Collections.emptyList());
        @SuppressWarnings("unchecked") List<String> timeRanges = (List<String>) params.get("timeRanges");
        String timeRange = String.valueOf(params.get("timeRange")); // HH:mm-HH:mm (fallback)
        boolean skipHolidays = Boolean.TRUE.equals(params.get("skipHolidays"));

        Optional<ClassInfo> opt = classInfoRepository.findById(classId == null ? -1L : classId);
        if (!opt.isPresent()) return ApiResponse.error(404, "Class not found");
        ClassInfo ce = opt.get();

        List<String> effectiveRanges = (timeRanges != null && !timeRanges.isEmpty())
                ? timeRanges
                : (timeRange != null && timeRange.contains("-") ? Arrays.asList(timeRange) : Collections.emptyList());
        if (effectiveRanges.isEmpty()) return ApiResponse.error(400, "timeRange 或 timeRanges 必须提供");

        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        try {
            Date monthDate = monthFmt.parse(targetMonth);
            cal.setTime(monthDate);
        } catch (ParseException e) {
            return ApiResponse.error(400, "Invalid targetMonth");
        }
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date monthStart = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date monthEnd = cal.getTime();

        // 过滤在班级起止日期范围内
        Date effectiveStart = ce.getStartDateAt() != null && ce.getStartDateAt().after(monthStart) ? ce.getStartDateAt() : monthStart;
        Date effectiveEnd = ce.getEndDateAt() != null && ce.getEndDateAt().before(monthEnd) ? ce.getEndDateAt() : monthEnd;

        List<ConflictItem> conflicts = new ArrayList<>();
        int generated = 0;

        Calendar it = Calendar.getInstance();
        it.setTime(effectiveStart);
        while (!it.getTime().after(effectiveEnd)) {
            int dow = it.get(Calendar.DAY_OF_WEEK); // 1=Sunday ... 7=Saturday
            int chinaDow = dow == Calendar.SUNDAY ? 7 : dow - 1; // 转换为 1=Mon ... 7=Sun
            if (weekDays.contains(chinaDow)) {
                Date dateOnly = it.getTime();
                if (skipHolidays && holidayRepository.existsByDateOnly(dateOnly)) {
                    it.add(Calendar.DAY_OF_MONTH, 1);
                    continue;
                }
                for (String range : effectiveRanges) {
                    String[] tr = range.split("-");
                    if (tr.length != 2) { conflicts.add(conf("system", "Invalid timeRange: " + range, dateOnly, range, null)); continue; }
                    try {
                        Date startAt = parseTime(dateOnly, tr[0]);
                        Date endAt = parseTime(dateOnly, tr[1]);
                        // 教师选取：优先 fixedTeacherId，否则 teacherMainId
                        Long selectedTeacherId = ce.getFixedTeacherId() != null ? ce.getFixedTeacherId() : ce.getTeacherMainId();
                        // 冲突校验
                        List<ScheduleInfo> tc = selectedTeacherId != null ? scheduleRepository.findTeacherConflicts(selectedTeacherId, startAt, endAt) : Collections.emptyList();
                        List<ScheduleInfo> rc = (ce.getCampusId() != null && ce.getRoom() != null) ? scheduleRepository.findRoomConflicts(ce.getCampusId(), ce.getRoom(), startAt, endAt) : Collections.emptyList();
                        List<ScheduleInfo> sc = ce.getExclusiveStudentId() != null ? scheduleRepository.findStudentConflicts(ce.getExclusiveStudentId(), startAt, endAt) : Collections.emptyList();
                        boolean hasConflict = false;
                        if (!tc.isEmpty()) { conflicts.add(conf("teacher", "教师时间冲突", dateOnly, tr[0] + "-" + tr[1], selectedTeacherId)); hasConflict = true; }
                        if (!rc.isEmpty()) { conflicts.add(conf("room", "教室占用冲突", dateOnly, tr[0] + "-" + tr[1], null)); hasConflict = true; }
                        if (!sc.isEmpty()) { conflicts.add(conf("student", "学员时间冲突", dateOnly, tr[0] + "-" + tr[1], ce.getExclusiveStudentId())); hasConflict = true; }
                        if (!hasConflict) {
                            ScheduleInfo si = new ScheduleInfo();
                            si.setClassId(ce.getId());
                            si.setCourseId(ce.getCourseId());
                            si.setTeacherId(selectedTeacherId);
                            si.setCampusId(ce.getCampusId());
                            si.setRoom(ce.getRoom());
                            si.setStudentId(ce.getExclusiveStudentId());
                            si.setStartAt(startAt);
                            si.setEndAt(endAt);
                            si.setDateOnly(dayFmt.parse(dayFmt.format(dateOnly)));
                            si.setStartTimeText(tr[0]);
                            si.setEndTimeText(tr[1]);
                            si.setStatus("scheduled");
                            si.setSource("batch_month");
                            scheduleRepository.save(si);
                            generated++;
                        }
                    } catch (Exception ex) {
                        conflicts.add(conf("system", "解析时间失败: " + ex.getMessage(), it.getTime(), range, null));
                    }
                }
            }
            it.add(Calendar.DAY_OF_MONTH, 1);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("generatedCount", generated);
        resp.put("conflicts", conflicts.stream().map(this::toMap).collect(Collectors.toList()));
        return ApiResponse.success(resp);
    }

    @RequiresPerm("course:schedule")
    @PostMapping("/batch/week")
    @Transactional
    public ApiResponse<Map<String, Object>> batchWeek(@RequestBody Map<String, Object> params) {
        Long classId = toLong(params.get("classId"));
        String startDateStr = String.valueOf(params.get("startDate")); // yyyy-MM-dd
        Integer weeks = toInt(params.get("weeks"));
        @SuppressWarnings("unchecked") List<Integer> weekDays = (List<Integer>) params.getOrDefault("weekDays", Collections.emptyList());
        @SuppressWarnings("unchecked") List<String> timeRanges = (List<String>) params.get("timeRanges");
        String timeRange = String.valueOf(params.get("timeRange")); // HH:mm-HH:mm (fallback)
        @SuppressWarnings("unchecked") List<Map<String, Object>> weekTimeRangesObj = (List<Map<String, Object>>) params.get("weekTimeRanges");
        boolean skipHolidays = Boolean.TRUE.equals(params.get("skipHolidays"));

        if (weeks == null || weeks <= 0) return ApiResponse.error(400, "weeks must be > 0");

        Optional<ClassInfo> opt = classInfoRepository.findById(classId == null ? -1L : classId);
        if (!opt.isPresent()) return ApiResponse.error(404, "Class not found");
        ClassInfo ce = opt.get();
        List<String> baseRanges = (timeRanges != null && !timeRanges.isEmpty())
                ? timeRanges
                : (timeRange != null && timeRange.contains("-") ? Arrays.asList(timeRange) : Collections.emptyList());
        if (baseRanges.isEmpty() && (weekTimeRangesObj == null || weekTimeRangesObj.isEmpty())) {
            return ApiResponse.error(400, "timeRange/timeRanges 或 weekTimeRanges 必须提供其一");
        }

        Map<Integer, List<String>> perWeekRanges = new HashMap<>();
        if (weekTimeRangesObj != null) {
            for (Map<String, Object> item : weekTimeRangesObj) {
                Integer w = toInt(item.get("weekOffset"));
                @SuppressWarnings("unchecked") List<String> rs = (List<String>) item.get("ranges");
                if (w != null && rs != null && !rs.isEmpty()) perWeekRanges.put(w, rs);
            }
        }

        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate;
        try { startDate = dayFmt.parse(startDateStr); } catch (Exception e) { return ApiResponse.error(400, "Invalid startDate"); }

        Calendar it = Calendar.getInstance();
        it.setTime(startDate);

        List<ConflictItem> conflicts = new ArrayList<>();
        int generated = 0;
        for (int w = 0; w < weeks; w++) {
            List<String> effectiveRanges = perWeekRanges.getOrDefault(w, baseRanges);
            if (effectiveRanges == null || effectiveRanges.isEmpty()) {
                // 如果该周未配置时间段且无基础时间段，跳过该周
                for (int d = 0; d < 7; d++) it.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }
            for (int d = 0; d < 7; d++) {
                int dow = it.get(Calendar.DAY_OF_WEEK);
                int chinaDow = dow == Calendar.SUNDAY ? 7 : dow - 1;
                if (weekDays.contains(chinaDow)) {
                    Date dateOnly = it.getTime();
                    if (skipHolidays && holidayRepository.existsByDateOnly(dateOnly)) {
                        it.add(Calendar.DAY_OF_MONTH, 1);
                        continue;
                    }
                    for (String range : effectiveRanges) {
                        String[] tr = range.split("-");
                        if (tr.length != 2) { conflicts.add(conf("system", "Invalid timeRange: " + range, dateOnly, range, null)); continue; }
                        try {
                            Date startAt = parseTime(dateOnly, tr[0]);
                            Date endAt = parseTime(dateOnly, tr[1]);
                            Long selectedTeacherId = ce.getFixedTeacherId() != null ? ce.getFixedTeacherId() : ce.getTeacherMainId();
                            List<ScheduleInfo> tc = selectedTeacherId != null ? scheduleRepository.findTeacherConflicts(selectedTeacherId, startAt, endAt) : Collections.emptyList();
                            List<ScheduleInfo> rc = (ce.getCampusId() != null && ce.getRoom() != null) ? scheduleRepository.findRoomConflicts(ce.getCampusId(), ce.getRoom(), startAt, endAt) : Collections.emptyList();
                            List<ScheduleInfo> sc = ce.getExclusiveStudentId() != null ? scheduleRepository.findStudentConflicts(ce.getExclusiveStudentId(), startAt, endAt) : Collections.emptyList();
                            boolean hasConflict = false;
                            if (!tc.isEmpty()) { conflicts.add(conf("teacher", "教师时间冲突", dateOnly, tr[0] + "-" + tr[1], selectedTeacherId)); hasConflict = true; }
                            if (!rc.isEmpty()) { conflicts.add(conf("room", "教室占用冲突", dateOnly, tr[0] + "-" + tr[1], null)); hasConflict = true; }
                            if (!sc.isEmpty()) { conflicts.add(conf("student", "学员时间冲突", dateOnly, tr[0] + "-" + tr[1], ce.getExclusiveStudentId())); hasConflict = true; }
                            if (!hasConflict) {
                                ScheduleInfo si = new ScheduleInfo();
                                si.setClassId(ce.getId());
                                si.setCourseId(ce.getCourseId());
                                si.setTeacherId(selectedTeacherId);
                                si.setCampusId(ce.getCampusId());
                                si.setRoom(ce.getRoom());
                                si.setStudentId(ce.getExclusiveStudentId());
                                si.setStartAt(startAt);
                                si.setEndAt(endAt);
                                si.setDateOnly(dayFmt.parse(dayFmt.format(dateOnly)));
                                si.setStartTimeText(tr[0]);
                                si.setEndTimeText(tr[1]);
                                si.setStatus("scheduled");
                                si.setSource("batch_week");
                                scheduleRepository.save(si);
                                generated++;
                            }
                        } catch (Exception ex) {
                            conflicts.add(conf("system", "解析时间失败: " + ex.getMessage(), it.getTime(), range, null));
                        }
                    }
                }
                it.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("generatedCount", generated);
        resp.put("conflicts", conflicts.stream().map(this::toMap).collect(Collectors.toList()));
        return ApiResponse.success(resp);
    }

    @RequiresPerm("course:reschedule")
    @PostMapping("/adjust")
    @Transactional
    public ApiResponse<Map<String, Object>> adjust(@RequestBody Map<String, Object> params) {
        Long id = toLong(params.get("scheduleId"));
        String newDate = String.valueOf(params.get("date")); // yyyy-MM-dd
        String newTimeRange = String.valueOf(params.get("timeRange")); // HH:mm-HH:mm
        Optional<ScheduleInfo> opt = scheduleRepository.findById(id == null ? -1L : id);
        if (!opt.isPresent()) return ApiResponse.error(404, "Schedule not found");
        ScheduleInfo si = opt.get();
        String[] tr = newTimeRange.split("-");
        if (tr.length != 2) return ApiResponse.error(400, "Invalid timeRange");
        try {
            SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
            Date dateOnly = dayFmt.parse(newDate);
            Date startAt = parseTime(dateOnly, tr[0]);
            Date endAt = parseTime(dateOnly, tr[1]);
            // 冲突校验
            List<ScheduleInfo> tc = si.getTeacherId() != null ? scheduleRepository.findTeacherConflicts(si.getTeacherId(), startAt, endAt) : Collections.emptyList();
            tc = tc.stream().filter(x -> !Objects.equals(x.getId(), si.getId())).collect(Collectors.toList());
            List<ScheduleInfo> rc = (si.getCampusId() != null && si.getRoom() != null) ? scheduleRepository.findRoomConflicts(si.getCampusId(), si.getRoom(), startAt, endAt) : Collections.emptyList();
            rc = rc.stream().filter(x -> !Objects.equals(x.getId(), si.getId())).collect(Collectors.toList());
            List<ScheduleInfo> sc = si.getStudentId() != null ? scheduleRepository.findStudentConflicts(si.getStudentId(), startAt, endAt) : Collections.emptyList();
            sc = sc.stream().filter(x -> !Objects.equals(x.getId(), si.getId())).collect(Collectors.toList());
            if (!tc.isEmpty() || !rc.isEmpty() || !sc.isEmpty()) {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("adjusted", false);
                resp.put("conflicts", Arrays.asList(
                        !tc.isEmpty() ? "教师时间冲突" : null,
                        !rc.isEmpty() ? "教室占用冲突" : null,
                        !sc.isEmpty() ? "学员时间冲突" : null
                ).stream().filter(Objects::nonNull).collect(Collectors.toList()));
                return ApiResponse.success(resp);
            }
            si.setDateOnly(dateOnly);
            si.setStartAt(startAt);
            si.setEndAt(endAt);
            si.setStartTimeText(tr[0]);
            si.setEndTimeText(tr[1]);
            si.setStatus("adjusted");
            scheduleRepository.save(si);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("adjusted", true);
            resp.put("scheduleId", si.getId());
            return ApiResponse.success(resp);
        } catch (Exception e) {
            return ApiResponse.error(400, "解析失败: " + e.getMessage());
        }
    }

    @RequiresPerm("course:schedule")
    @PostMapping("/manual/save")
    @Transactional
    public ApiResponse<Map<String, Object>> manualSave(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked") List<Map<String, Object>> items = (List<Map<String, Object>>) params.getOrDefault("items", Collections.emptyList());
        if (items == null || items.isEmpty()) return ApiResponse.error(400, "items 不能为空");
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        List<ConflictItem> conflicts = new ArrayList<>();
        int saved = 0;
        for (Map<String, Object> it : items) {
            try {
                Long classId = toLong(it.get("classId"));
                Long teacherId = toLong(it.get("teacherId"));
                Object ctObj = it.get("classType");
                String classType = ctObj == null ? null : String.valueOf(ctObj); // 前端可能传 1/2 或中文
                String dateStr = String.valueOf(it.get("date"));
                String startStr = String.valueOf(it.get("start")); // HH:mm
                String endStr = String.valueOf(it.get("end"));   // HH:mm
                if (dateStr == null || startStr == null || endStr == null) {
                    conflicts.add(conf("system", "缺少必填项", new Date(), startStr + "-" + endStr, null));
                    continue;
                }
                Date dateOnly = dayFmt.parse(dateStr);
                Date startAt = parseTime(dateOnly, startStr);
                Date endAt = parseTime(dateOnly, endStr);
                // 冲突校验
                List<ScheduleInfo> tc = teacherId != null ? scheduleRepository.findTeacherConflicts(teacherId, startAt, endAt) : Collections.emptyList();
                boolean hasConflict = !tc.isEmpty();
                if (hasConflict) {
                    conflicts.add(conf("teacher", "教师时间冲突", dateOnly, startStr + "-" + endStr, teacherId));
                    continue;
                }
                ScheduleInfo si = new ScheduleInfo();
                si.setClassId(classId);
                si.setTeacherId(teacherId);
                si.setStartAt(startAt);
                si.setEndAt(endAt);
                si.setDateOnly(dayFmt.parse(dayFmt.format(dateOnly)));
                si.setStartTimeText(startStr);
                si.setEndTimeText(endStr);
                si.setStatus("scheduled");
                si.setSource("manual");
                si.setClassType(classType);
                scheduleRepository.save(si);
                saved++;
            } catch (Exception e) {
                conflicts.add(conf("system", "解析失败: " + e.getMessage(), new Date(), String.valueOf(it.get("start")) + "-" + String.valueOf(it.get("end")), null));
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("savedCount", saved);
        resp.put("conflicts", conflicts.stream().map(this::toMap).collect(Collectors.toList()));
        return ApiResponse.success(resp);
    }

    @RequiresPerm("course:schedule")
    @GetMapping("/conflict/check")
    public ApiResponse<Map<String, Object>> checkConflicts(
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "campusId", required = false) Long campusId,
            @RequestParam(value = "room", required = false) String room,
            @RequestParam(value = "studentId", required = false) Long studentId,
            @RequestParam(value = "date", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @RequestParam(value = "start", required = true) String start,
            @RequestParam(value = "end", required = true) String end
    ) {
        try {
            Date startAt = parseTime(date, start);
            Date endAt = parseTime(date, end);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("teacherConflicts", teacherId != null ? scheduleRepository.findTeacherConflicts(teacherId, startAt, endAt).size() : 0);
            resp.put("roomConflicts", (campusId != null && room != null) ? scheduleRepository.findRoomConflicts(campusId, room, startAt, endAt).size() : 0);
            resp.put("studentConflicts", studentId != null ? scheduleRepository.findStudentConflicts(studentId, startAt, endAt).size() : 0);
            return ApiResponse.success(resp);
        } catch (Exception e) {
            return ApiResponse.error(400, "解析失败: " + e.getMessage());
        }
    }

    private Long toLong(Object v) { return v instanceof Number ? ((Number) v).longValue() : v != null ? Long.parseLong(String.valueOf(v)) : null; }
    private Integer toInt(Object v) { return v instanceof Number ? ((Number) v).intValue() : v != null ? Integer.parseInt(String.valueOf(v)) : null; }

    private ConflictItem conf(String type, String reason, Date date, String timeRange, Long refId) {
        ConflictItem c = new ConflictItem();
        c.type = type; c.reason = reason; c.date = date; c.timeRange = timeRange; c.refId = refId; return c;
    }

    private Map<String, Object> toMap(ConflictItem c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", c.type);
        m.put("reason", c.reason);
        m.put("date", new SimpleDateFormat("yyyy-MM-dd").format(c.date));
        m.put("timeRange", c.timeRange);
        m.put("refId", c.refId);
        return m;
    }

    // ===== 课表查询与分享 =====
    private Map<String, Object> scheduleToMap(ScheduleInfo s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("classId", s.getClassId());
        m.put("courseId", s.getCourseId());
        m.put("teacherId", s.getTeacherId());
        m.put("campusId", s.getCampusId());
        m.put("room", s.getRoom());
        m.put("studentId", s.getStudentId());
        m.put("classType", s.getClassType());
        m.put("date", s.getDateOnly() != null ? new SimpleDateFormat("yyyy-MM-dd").format(s.getDateOnly()) : null);
        m.put("start", s.getStartTimeText());
        m.put("end", s.getEndTimeText());
        m.put("status", s.getStatus());
        return m;
    }

    @RequiresPerm("course:schedule")
    @GetMapping("/query/class")
    public ApiResponse<List<Map<String, Object>>> queryByClass(
            @RequestParam("classId") Long classId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        List<ScheduleInfo> list = scheduleRepository.findByClassId(classId);
        if (startDate != null && endDate != null) {
            list = list.stream().filter(s -> {
                Date d = s.getDateOnly();
                return d != null && !d.before(startDate) && !d.after(endDate);
            }).collect(Collectors.toList());
        }
        return ApiResponse.success(list.stream().map(this::scheduleToMap).collect(Collectors.toList()));
    }

    @RequiresPerm("course:schedule")
    @GetMapping("/query/teacher")
    public ApiResponse<List<Map<String, Object>>> queryByTeacher(
            @RequestParam("teacherId") Long teacherId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        List<ScheduleInfo> list = scheduleRepository.findByTeacherId(teacherId);
        if (startDate != null && endDate != null) {
            list = list.stream().filter(s -> {
                Date d = s.getDateOnly();
                return d != null && !d.before(startDate) && !d.after(endDate);
            }).collect(Collectors.toList());
        }
        return ApiResponse.success(list.stream().map(this::scheduleToMap).collect(Collectors.toList()));
    }

    @RequiresPerm("course:schedule")
    @GetMapping("/query/student")
    public ApiResponse<List<Map<String, Object>>> queryByStudent(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        List<ScheduleInfo> list = scheduleRepository.findByStudentId(studentId);
        if (startDate != null && endDate != null) {
            list = list.stream().filter(s -> {
                Date d = s.getDateOnly();
                return d != null && !d.before(startDate) && !d.after(endDate);
            }).collect(Collectors.toList());
        }
        return ApiResponse.success(list.stream().map(this::scheduleToMap).collect(Collectors.toList()));
    }

    @RequiresPerm("course:schedule")
    @GetMapping("/query/room")
    public ApiResponse<List<Map<String, Object>>> queryByRoom(
            @RequestParam("campusId") Long campusId,
            @RequestParam("room") String room,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        List<ScheduleInfo> list = scheduleRepository.findByCampusIdAndRoom(campusId, room);
        if (startDate != null && endDate != null) {
            list = list.stream().filter(s -> {
                Date d = s.getDateOnly();
                return d != null && !d.before(startDate) && !d.after(endDate);
            }).collect(Collectors.toList());
        }
        return ApiResponse.success(list.stream().map(this::scheduleToMap).collect(Collectors.toList()));
    }

    @RequiresPerm("course:schedule")
    @GetMapping("/upcoming")
    public ApiResponse<List<Map<String, Object>>> upcoming(
            @RequestParam(value = "days", required = false, defaultValue = "3") Integer days,
            @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit
    ) {
        if (days == null || days <= 0) days = 3;
        if (limit == null || limit <= 0) limit = 50;
        // 计算日期范围（从今天开始的接下来 N 天）
        Calendar base = Calendar.getInstance();
        base.set(Calendar.HOUR_OF_DAY, 0);
        base.set(Calendar.MINUTE, 0);
        base.set(Calendar.SECOND, 0);
        base.set(Calendar.MILLISECOND, 0);
        Date startDate = base.getTime();
        Calendar endCal = (Calendar) base.clone();
        endCal.add(Calendar.DAY_OF_MONTH, days - 1);
        Date endDate = endCal.getTime();

        List<ScheduleInfo> items = scheduleRepository.findByDateOnlyBetween(startDate, endDate);
        // 按开始时间升序，时间越近越靠前
        items.sort(Comparator.comparing((ScheduleInfo si) -> {
            Date st = si.getStartAt();
            return st != null ? st : si.getDateOnly();
        }));

        Date now = new Date();
        List<Map<String, Object>> out = new ArrayList<>();
        for (ScheduleInfo s : items) {
            Map<String, Object> m = scheduleToMap(s);
            // 补充班级名称
            String className = null;
            if (s.getClassId() != null) {
                Optional<ClassInfo> ceOpt = classInfoRepository.findById(s.getClassId());
                if (ceOpt.isPresent()) className = ceOpt.get().getName();
            }
            m.put("className", className);
            // 近24小时作为醒目提示
            boolean urgent = false;
            Date st = s.getStartAt();
            if (st != null) {
                long diff = st.getTime() - now.getTime();
                urgent = diff >= 0 && diff <= 24L * 60 * 60 * 1000;
            } else if (s.getDateOnly() != null) {
                Calendar d = Calendar.getInstance(); d.setTime(s.getDateOnly());
                Calendar t = Calendar.getInstance();
                urgent = d.get(Calendar.YEAR) == t.get(Calendar.YEAR) && d.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR);
            }
            m.put("urgent", urgent);
            out.add(m);
            if (out.size() >= limit) break;
        }

        return ApiResponse.success(out);
    }

    @RequiresPerm("course:schedule")
    @PostMapping("/share")
    public ApiResponse<Map<String, Object>> shareSchedule(@RequestBody Map<String, Object> params) {
        String dimension = String.valueOf(params.get("dimension")); // class/teacher/student/room
        String targetId = String.valueOf(params.get("targetId"));
        String range = String.valueOf(params.get("range")); // yyyy-MM 或 yyyy-MM-dd~yyyy-MM-dd
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("accepted", true);
        resp.put("dimension", dimension);
        resp.put("targetId", targetId);
        resp.put("range", range);
        resp.put("message", "分享已入队，将通过微信端推送（示例 stub）");
        return ApiResponse.success(resp);
    }
}