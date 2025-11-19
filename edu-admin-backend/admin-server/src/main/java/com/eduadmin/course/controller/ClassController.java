package com.eduadmin.course.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.entity.ClassEntity;
import com.eduadmin.course.entity.ClassInfo;
import com.eduadmin.course.entity.ClassSelectApply;
import com.eduadmin.course.repo.ClassRepository;
import com.eduadmin.course.repo.ClassInfoRepository;
import com.eduadmin.course.repo.ClassEnrollmentRepository;
import com.eduadmin.course.repo.StudentClassRecordRepository;
import com.eduadmin.course.repo.ClassSelectApplyRepository;
import com.eduadmin.baseplatform.campus.entity.Campus;
import com.eduadmin.baseplatform.campus.repo.CampusRepository;
import com.eduadmin.course.entity.GradeDict;
import com.eduadmin.course.repo.GradeDictRepository;
import com.eduadmin.course.store.InMemoryCourseStore;
import com.eduadmin.course.repo.SubjectDictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.eduadmin.system.security.RequiresPerm;

import java.util.*;

@RestController
@RequestMapping("/api/class")
public class ClassController {
    private long idSeq = 3000L;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ClassSelectApplyRepository classSelectApplyRepository;

    @Autowired
    private ClassEnrollmentRepository classEnrollmentRepository;

    @Autowired
    private StudentClassRecordRepository studentClassRecordRepository;

    // 班级信息（字典）管理依赖
    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private GradeDictRepository gradeDictRepository;
    @Autowired
    private SubjectDictRepository subjectDictRepository;

    @RequiresPerm("class:view")
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "campusId", required = false) Long campusId,
                                                 @RequestParam(value = "gradeId", required = false) Long gradeId,
                                                 @RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "q", required = false) String q,
                                                 @RequestParam(value = "courseId", required = false) Long courseId,
                                                 @RequestParam(value = "teacherId", required = false) Long teacherId,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<ClassInfo> items = classInfoRepository.findAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (ClassInfo ci : items) {
            boolean ok = true;
            if (campusId != null && !Objects.equals(ci.getCampusId(), campusId)) ok = false;
            if (ok && gradeId != null && !Objects.equals(ci.getGradeId(), gradeId)) ok = false;
            if (ok && status != null && !status.isEmpty() && !status.equals(ci.getStatus())) ok = false;
            if (ok && q != null && !q.isEmpty()) {
                String name = ci.getName() != null ? ci.getName() : "";
                ok = name.contains(q);
            }
            if (ok && courseId != null && !Objects.equals(ci.getCourseId(), courseId)) ok = false;
            if (ok && teacherId != null) {
                boolean match = Objects.equals(ci.getTeacherMainId(), teacherId) || Objects.equals(ci.getTeacherAssistantId(), teacherId) || Objects.equals(ci.getFixedTeacherId(), teacherId);
                if (!match) ok = false;
            }
            if (!ok) continue;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", ci.getId());
            m.put("campusId", ci.getCampusId());
            m.put("gradeId", ci.getGradeId());
            m.put("name", ci.getName());
            m.put("mode", ci.getMode());
            m.put("startDate", ci.getStartDate());
            m.put("endDate", ci.getEndDate());
            m.put("term", ci.getTerm());
            m.put("state", ci.getState());
            m.put("classroom", ci.getClassroom());
            m.put("capacityLimit", ci.getCapacityLimit());
            // 兼容选班页面所需字段（班号/课程ID/教室/最大人数/剩余名额）
            m.put("classNo", String.valueOf(ci.getId()));
            m.put("courseId", ci.getCourseId() == null ? ci.getId() : ci.getCourseId());
            String subjectName2 = null;
            try { subjectName2 = ci.getCourseId() == null ? null : subjectDictRepository.findById(ci.getCourseId()).map(s -> s.getName()).orElse(null); } catch (Exception ignore) {}
            m.put("subject", ci.getSubject() != null ? ci.getSubject() : subjectName2);
            m.put("room", (ci.getRoom() == null || ci.getRoom().trim().isEmpty()) ? ci.getClassroom() : ci.getRoom());
            Integer maxSize = (ci.getMaxSize() != null) ? ci.getMaxSize() : ci.getCapacityLimit();
            m.put("maxSize", maxSize == null ? 0 : maxSize);
            m.put("feeStandard", ci.getFeeStandard());
            m.put("feeStatus", ci.getFeeStatus());
            m.put("status", ci.getStatus());
            m.put("sortOrder", ci.getSortOrder());

            // 解析班主任ID列表（字符串逗号分隔 -> 数组）
            List<Long> headIds = new ArrayList<>();
            String headsText = ci.getHeadTeacherIds();
            if (headsText != null && !headsText.trim().isEmpty()) {
                for (String s : headsText.split(",")) {
                    String t = s.trim();
                    if (!t.isEmpty()) {
                        try { headIds.add(Long.valueOf(t)); } catch (Exception ignore) {}
                    }
                }
            }
            m.put("headTeacherIds", headIds);

            // 解析标签（字符串逗号分隔 -> 数组）
            List<String> tags = new ArrayList<>();
            String tagsText = ci.getTags();
            if (tagsText != null && !tagsText.trim().isEmpty()) {
                for (String s : tagsText.split(",")) {
                    String t = s.trim();
                    if (!t.isEmpty()) tags.add(t);
                }
            }
            m.put("tags", tags);

            // 当前报名人数（active）
            int currentCount = 0;
            try {
                currentCount = (int) classEnrollmentRepository.countByClassIdAndStatus(ci.getId(), "active");
            } catch (Exception ignore) {}
            m.put("currentCount", currentCount);
            // 剩余名额
            int remain = Math.max(0, (maxSize == null ? 0 : maxSize) - currentCount);
            m.put("remaining", remain);

            out.add(m);
        }
        // 按排序字段升序
        out.sort(Comparator.comparingInt(o -> ((Number) o.getOrDefault("sortOrder", 0)).intValue()));
        if (page == null || page < 1) page = 1;
        if (size == null || size <= 0) size = 10;
        int total = out.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Map<String, Object>> pageItems = out.subList(from, to);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("items", pageItems);
        resp.put("total", total);
        resp.put("page", page);
        resp.put("size", size);
        resp.put("pages", (int) Math.ceil(total / (double) size));
        return ApiResponse.success(resp);
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable("id") Long id) {
        Map<String, Object> c = InMemoryCourseStore.classes.get(id);
        if (c == null) return ApiResponse.error(404, "未找到班级");
        return ApiResponse.success(withCapacity(c));
    }

    @RequiresPerm("class:manage")
    @PostMapping("/create-group")
    @Transactional
    public ApiResponse<Map<String, Object>> createGroup(@RequestBody Map<String, Object> payload) {
        Long id = ++idSeq;
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", id);
        c.put("classNo", payload.getOrDefault("classNo", String.valueOf(id)));
        c.put("name", payload.getOrDefault("name", "新建班级"));
        c.put("courseId", payload.get("courseId"));
        c.put("teacherMainId", payload.get("teacherMainId"));
        c.put("teacherAssistantId", payload.get("teacherAssistantId"));
        c.put("campusId", payload.get("campusId"));
        c.put("room", payload.get("room"));
        c.put("startDate", payload.get("startDate"));
        c.put("endDate", payload.get("endDate"));
        c.put("weekDays", payload.getOrDefault("weekDays", Collections.emptyList()));
        c.put("durationMinutes", payload.getOrDefault("durationMinutes", 90));
        c.put("maxSize", payload.getOrDefault("maxSize", 30));
        c.put("fee", payload.getOrDefault("fee", 0.0));
        c.put("status", "not_started");
        c.put("suspendReason", null);
        InMemoryCourseStore.classes.put(id, c);
        InMemoryCourseStore.classStudents.put(id, new LinkedHashSet<>());

        // 同步持久化到数据库
        try {
            ClassEntity ce = new ClassEntity();
            ce.setClassNo(String.valueOf(c.get("classNo")));
            ce.setName(String.valueOf(c.get("name")));
            ce.setCourseId(c.get("courseId") instanceof Number ? ((Number) c.get("courseId")).longValue() : null);
            ce.setTeacherMainId(c.get("teacherMainId") instanceof Number ? ((Number) c.get("teacherMainId")).longValue() : null);
            ce.setTeacherAssistantId(c.get("teacherAssistantId") instanceof Number ? ((Number) c.get("teacherAssistantId")).longValue() : null);
            ce.setCampusId(c.get("campusId") instanceof Number ? ((Number) c.get("campusId")).longValue() : null);
            ce.setRoom((String) c.get("room"));
            ce.setStartDate((Date) c.get("startDate"));
            ce.setEndDate((Date) c.get("endDate"));
            Object wd = c.get("weekDays");
            if (wd instanceof Collection) {
                ce.setWeekDaysText(String.join(",", ((Collection<?>) wd).stream().map(String::valueOf).toArray(String[]::new)));
            }
            ce.setDurationMinutes(((Number) c.getOrDefault("durationMinutes", 90)).intValue());
            ce.setMaxSize(((Number) c.getOrDefault("maxSize", 30)).intValue());
            ce.setFee(((Number) c.getOrDefault("fee", 0.0)).doubleValue());
            ce.setStatus(String.valueOf(c.get("status")));
            ce.setSuspendReason((String) c.get("suspendReason"));
            classRepository.save(ce);
        } catch (Exception ignore) {}
        return ApiResponse.success(withCapacity(c));
    }

    @RequiresPerm("class:manage")
    @PostMapping("/create-one2one")
    @Transactional
    public ApiResponse<Map<String, Object>> createOne2One(@RequestBody Map<String, Object> payload) {
        Long id = ++idSeq;
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", id);
        c.put("classNo", payload.getOrDefault("classNo", String.valueOf(id)));
        c.put("name", payload.getOrDefault("name", "一对一班级"));
        c.put("courseId", payload.get("courseId"));
        c.put("fixedTeacherId", payload.get("fixedTeacherId"));
        c.put("flexibleTeacher", payload.getOrDefault("flexibleTeacher", false));
        c.put("exclusiveStudentId", payload.get("exclusiveStudentId"));
        c.put("noFixedSchedule", payload.getOrDefault("noFixedSchedule", true));
        c.put("campusId", payload.get("campusId"));
        c.put("room", payload.get("room"));
        c.put("durationMinutes", payload.getOrDefault("durationMinutes", 60));
        c.put("maxSize", 1);
        c.put("fee", payload.getOrDefault("fee", 0.0));
        c.put("status", "not_started");
        c.put("suspendReason", null);
        InMemoryCourseStore.classes.put(id, c);
        InMemoryCourseStore.classStudents.put(id, new LinkedHashSet<>());
        // 若指定了专属学员，自动加入
        Object sidObj = payload.get("exclusiveStudentId");
        if (sidObj instanceof Number) {
            Long sid = ((Number) sidObj).longValue();
            InMemoryCourseStore.classStudents.get(id).add(sid);
            addRecord(sid, joinRecord(null, id, "创建一对一专属加入"));
        }

        // 同步持久化到数据库
        try {
            ClassEntity ce = new ClassEntity();
            ce.setClassNo(String.valueOf(c.get("classNo")));
            ce.setName(String.valueOf(c.get("name")));
            ce.setCourseId(c.get("courseId") instanceof Number ? ((Number) c.get("courseId")).longValue() : null);
            ce.setFixedTeacherId(c.get("fixedTeacherId") instanceof Number ? ((Number) c.get("fixedTeacherId")).longValue() : null);
            ce.setFlexibleTeacher(Boolean.TRUE.equals(c.get("flexibleTeacher")));
            ce.setExclusiveStudentId(c.get("exclusiveStudentId") instanceof Number ? ((Number) c.get("exclusiveStudentId")).longValue() : null);
            ce.setCampusId(c.get("campusId") instanceof Number ? ((Number) c.get("campusId")).longValue() : null);
            ce.setRoom((String) c.get("room"));
            ce.setDurationMinutes(((Number) c.getOrDefault("durationMinutes", 60)).intValue());
            ce.setMaxSize(1);
            ce.setFee(((Number) c.getOrDefault("fee", 0.0)).doubleValue());
            ce.setStatus(String.valueOf(c.get("status")));
            ce.setSuspendReason((String) c.get("suspendReason"));
            classRepository.save(ce);
        } catch (Exception ignore) {}
        return ApiResponse.success(withCapacity(c));
    }

    @RequiresPerm("class:manage")
    @PostMapping("/update")
    public ApiResponse<Map<String, Object>> update(@RequestBody Map<String, Object> payload) {
        Long id = ((Number) payload.get("id")).longValue();
        Map<String, Object> c = InMemoryCourseStore.classes.get(id);
        if (c == null) return ApiResponse.error(404, "未找到班级");
        c.putAll(payload);
        return ApiResponse.success(withCapacity(c));
    }

    @RequiresPerm("class:manage")
    @PostMapping("/status/{id}")
    public ApiResponse<Map<String, Object>> changeStatus(@PathVariable("id") Long id,
                                                         @RequestParam("status") String status,
                                                         @RequestParam(value = "reason", required = false) String reason) {
        Map<String, Object> c = InMemoryCourseStore.classes.get(id);
        if (c == null) return ApiResponse.error(404, "未找到班级");
        c.put("status", status);
        if ("suspended".equals(status)) {
            c.put("suspendReason", reason);
        } else {
            c.put("suspendReason", null);
        }
        return ApiResponse.success(withCapacity(c));
    }

    @RequiresPerm("class:manage")
    @PostMapping("/enroll-manual")
    public ApiResponse<Map<String, Object>> enrollManual(@RequestBody Map<String, Object> payload) {
        Long classId = ((Number) payload.get("classId")).longValue();
        Long studentId = ((Number) payload.get("studentId")).longValue();
        Set<Long> set = InMemoryCourseStore.classStudents.computeIfAbsent(classId, k -> new LinkedHashSet<>());
        set.add(studentId);
        addRecord(studentId, joinRecord(null, classId, "教务手动分班"));
        // 持久化到数据库（若已存在有效报名则跳过）
        try {
            if (!classEnrollmentRepository.existsByClassIdAndStudentIdAndStatus(classId, studentId, "active")) {
                com.eduadmin.course.entity.ClassEnrollment e = new com.eduadmin.course.entity.ClassEnrollment();
                e.setClassId(classId);
                e.setStudentId(studentId);
                e.setStatus("active");
                e.setSource("manual");
                classEnrollmentRepository.save(e);
            }
            com.eduadmin.course.entity.StudentClassRecord r = new com.eduadmin.course.entity.StudentClassRecord();
            r.setStudentId(studentId);
            r.setType("join");
            r.setFromClassId(null);
            r.setToClassId(classId);
            r.setReason("教务手动分班");
            studentClassRecordRepository.save(r);
        } catch (Exception ignore) {}
        Map<String, Object> c = InMemoryCourseStore.classes.get(classId);
        return ApiResponse.success(withCapacity(c));
    }

    @RequiresPerm("class:select")
    @PostMapping("/select/apply")
    @Transactional
    public ApiResponse<Map<String, Object>> selectApply(@RequestBody Map<String, Object> payload) {
        Long classId = ((Number) payload.get("classId")).longValue();
        Long studentId = ((Number) payload.get("studentId")).longValue();
        String note = String.valueOf(payload.getOrDefault("note", ""));
        long appId = InMemoryCourseStore.selectAppSeq++;
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("id", appId);
        app.put("classId", classId);
        app.put("studentId", studentId);
        app.put("status", "pending");
        app.put("note", note);
        app.put("reason", null);
        app.put("time", new Date());
        InMemoryCourseStore.selectApplications.put(appId, app);

        // 持久化申请到数据库
        try {
            ClassSelectApply a = new ClassSelectApply();
            a.setClassId(classId);
            a.setStudentId(studentId);
            a.setStatus("pending");
            a.setNote(note);
            classSelectApplyRepository.save(a);
        } catch (Exception ignore) {}
        return ApiResponse.success(app);
    }

    @RequiresPerm("class:manage")
    @PostMapping("/select/approve")
    @Transactional
    public ApiResponse<Map<String, Object>> selectApprove(@RequestBody Map<String, Object> payload) {
        Long appId = ((Number) payload.get("applicationId")).longValue();
        String action = String.valueOf(payload.getOrDefault("action", "approve"));
        String reason = String.valueOf(payload.getOrDefault("reason", ""));
        Map<String, Object> app = InMemoryCourseStore.selectApplications.get(appId);
        if (app == null) return ApiResponse.error(404, "未找到选班申请");
        Long classId = ((Number) app.get("classId")).longValue();
        Long studentId = ((Number) app.get("studentId")).longValue();
        if ("approve".equals(action)) {
            app.put("status", "approved");
            Set<Long> set = InMemoryCourseStore.classStudents.computeIfAbsent(classId, k -> new LinkedHashSet<>());
            set.add(studentId);
            addRecord(studentId, joinRecord(null, classId, "学员选班审核通过"));
            // DB：创建报名关系与记录
            try {
                if (!classEnrollmentRepository.existsByClassIdAndStudentIdAndStatus(classId, studentId, "active")) {
                    com.eduadmin.course.entity.ClassEnrollment e = new com.eduadmin.course.entity.ClassEnrollment();
                    e.setClassId(classId);
                    e.setStudentId(studentId);
                    e.setStatus("active");
                    e.setSource("select");
                    classEnrollmentRepository.save(e);
                }
                com.eduadmin.course.entity.StudentClassRecord r = new com.eduadmin.course.entity.StudentClassRecord();
                r.setStudentId(studentId);
                r.setType("join");
                r.setFromClassId(null);
                r.setToClassId(classId);
                r.setReason("学员选班审核通过");
                studentClassRecordRepository.save(r);
            } catch (Exception ignore) {}
        } else {
            app.put("status", "rejected");
            app.put("reason", reason);
        }

        // 同步更新数据库申请状态
        try {
            Optional<ClassSelectApply> opt = classSelectApplyRepository.findById(appId);
            if (opt.isPresent()) {
                ClassSelectApply a = opt.get();
                a.setStatus(String.valueOf(app.get("status")));
                a.setReason(String.valueOf(app.get("reason")));
                classSelectApplyRepository.save(a);
            }
        } catch (Exception ignore) {}
        return ApiResponse.success(app);
    }

    @RequiresPerm("class:manage")
    @GetMapping("/select/list")
    public ApiResponse<List<Map<String, Object>>> selectList(@RequestParam(value = "classId", required = false) Long classId,
                                                             @RequestParam(value = "status", required = false) String status) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> app : InMemoryCourseStore.selectApplications.values()) {
            boolean ok = true;
            if (classId != null) ok = Objects.equals(classId, ((Number) app.get("classId")).longValue());
            if (ok && status != null && !status.isEmpty()) ok = status.equals(app.get("status"));
            if (ok) out.add(app);
        }
        return ApiResponse.success(out);
    }

    @PostMapping("/transfer")
    public ApiResponse<Map<String, Object>> transfer(@RequestBody Map<String, Object> payload) {
        Long fromClassId = ((Number) payload.get("fromClassId")).longValue();
        Long toClassId = ((Number) payload.get("toClassId")).longValue();
        Long studentId = ((Number) payload.get("studentId")).longValue();
        String reason = String.valueOf(payload.getOrDefault("reason", ""));
        Set<Long> fromSet = InMemoryCourseStore.classStudents.computeIfAbsent(fromClassId, k -> new LinkedHashSet<>());
        Set<Long> toSet = InMemoryCourseStore.classStudents.computeIfAbsent(toClassId, k -> new LinkedHashSet<>());
        fromSet.remove(studentId);
        toSet.add(studentId);
        addRecord(studentId, transferRecord(fromClassId, toClassId, reason));
        Map<String, Object> c = InMemoryCourseStore.classes.get(toClassId);
        return ApiResponse.success(withCapacity(c));
    }

    @RequiresPerm("class:view")
    @GetMapping("/records")
    public ApiResponse<List<Map<String, Object>>> records(@RequestParam(value = "studentId", required = false) Long studentId) {
        if (studentId == null) return ApiResponse.success(Collections.emptyList());
        try {
            List<Map<String, Object>> out = new ArrayList<>();
            for (com.eduadmin.course.entity.StudentClassRecord r : studentClassRecordRepository.findByStudentIdOrderByTimeAsc(studentId)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("type", r.getType());
                m.put("fromClassId", r.getFromClassId());
                m.put("toClassId", r.getToClassId());
                m.put("time", r.getTime());
                m.put("reason", r.getReason());
                out.add(m);
            }
            // 兼容：追加内存记录
            out.addAll(InMemoryCourseStore.studentClassRecords.getOrDefault(studentId, Collections.emptyList()));
            return ApiResponse.success(out);
        } catch (Exception e) {
            return ApiResponse.success(InMemoryCourseStore.studentClassRecords.getOrDefault(studentId, Collections.emptyList()));
        }
    }

    // ===== ClassInfo（原 ClassDict）管理接口 =====

    @GetMapping("/info/list")
    public ApiResponse<Map<String, Object>> infoList(@RequestParam(value = "status", required = false) String status,
                                                     @RequestParam(value = "campusId", required = false) Long campusId,
                                                     @RequestParam(value = "gradeId", required = false) Long gradeId,
                                                     @RequestParam(value = "q", required = false) String keyword,
                                                     @RequestParam(value = "subject", required = false) Long subject,
                                                     @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                     @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<ClassInfo> list;
        if (campusId != null && gradeId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = classInfoRepository.findByCampusIdAndGradeIdAndNameContainingOrderBySortOrderAsc(campusId, gradeId, keyword.trim());
            } else if (status == null || status.isEmpty()) list = classInfoRepository.findByCampusIdAndGradeIdOrderBySortOrderAsc(campusId, gradeId);
            else list = classInfoRepository.findByCampusIdAndGradeIdAndStatusOrderBySortOrderAsc(campusId, gradeId, status);
        } else if (campusId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = classInfoRepository.findByCampusIdAndNameContainingOrderBySortOrderAsc(campusId, keyword.trim());
            } else if (status == null || status.isEmpty()) list = classInfoRepository.findByCampusIdOrderBySortOrderAsc(campusId);
            else list = classInfoRepository.findByCampusIdAndStatusOrderBySortOrderAsc(campusId, status);
        } else {
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = classInfoRepository.findByNameContainingOrderBySortOrderAsc(keyword.trim());
            } else {
                list = (status == null || status.isEmpty()) ? classInfoRepository.findAll() : classInfoRepository.findByStatusOrderBySortOrderAsc(status);
            }
        }
        // 构建校区与年级名称映射
        Set<Long> campusIds = new HashSet<>();
        Set<Long> gradeIds = new HashSet<>();
        for (ClassInfo c : list) {
            if (c.getCampusId() != null) campusIds.add(c.getCampusId());
            if (c.getGradeId() != null) gradeIds.add(c.getGradeId());
        }
        Map<Long, String> campusNameMap = new HashMap<>();
        if (!campusIds.isEmpty()) {
            List<Campus> campuses = campusRepository.findAllById(campusIds);
            for (Campus cp : campuses) campusNameMap.put(cp.getId(), cp.getName());
        }
        Map<Long, String> gradeNameMap = new HashMap<>();
        if (!gradeIds.isEmpty()) {
            List<GradeDict> grades = gradeDictRepository.findAllById(gradeIds);
            for (GradeDict gd : grades) gradeNameMap.put(gd.getId(), gd.getName());
        }
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClassInfo c : list) {
            if (subject != null) {
                String subText = c.getSubject();
                if (subText == null || !Objects.equals(String.valueOf(subject), subText)) {
                    continue;
                }
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("campusId", c.getCampusId());
            m.put("campusName", campusNameMap.get(c.getCampusId()));
            m.put("gradeId", c.getGradeId());
            m.put("gradeName", gradeNameMap.get(c.getGradeId()));
            m.put("name", c.getName());
            m.put("status", c.getStatus());
            m.put("sortOrder", c.getSortOrder());
            // 扩展字段（保持前端列表一致）
            m.put("classId", c.getId());
            m.put("mode", (c.getMode() == null || c.getMode().trim().isEmpty()) ? "线下" : c.getMode());
            m.put("startDate", c.getStartDate());
            m.put("endDate", c.getEndDate());
            m.put("term", c.getTerm() == null ? "" : c.getTerm());
            m.put("state", (c.getState() == null || c.getState().trim().isEmpty()) ? "normal" : c.getState());
            m.put("classroom", c.getClassroom() == null ? "" : c.getClassroom());
            m.put("capacityLimit", c.getCapacityLimit() == null ? 0 : c.getCapacityLimit());
            m.put("feeStandard", c.getFeeStandard() == null ? "" : c.getFeeStandard());
            m.put("feeStatus", (c.getFeeStatus() == null || c.getFeeStatus().trim().isEmpty()) ? "未设置" : c.getFeeStatus());
            m.put("note", c.getNote() == null ? "" : c.getNote());
            m.put("parentGroup", c.getParentGroup() == null ? "" : c.getParentGroup());
            m.put("contacts", c.getContacts() == null ? "" : c.getContacts());
            m.put("subject", c.getSubject());
            String subjectName = null;
            try {
                subjectName = (c.getSubject() == null || c.getSubject().trim().isEmpty()) ? null
                        : subjectDictRepository.findById(Long.valueOf(c.getSubject())).map(s -> s.getName()).orElse(null);
            } catch (Exception ignore) {}
            m.put("subjectName", subjectName);
            m.put("headTeacherIds", parseCsvToLongList(c.getHeadTeacherIds()));
            m.put("tags", parseCsv(c.getTags()));
            data.add(m);
        }
        // 新增时间降序（使用自增ID近似替代）
        data.sort((a, b) -> Long.compare(((Number) b.getOrDefault("id", 0L)).longValue(), ((Number) a.getOrDefault("id", 0L)).longValue()));
        if (page == null || page < 1) page = 1;
        if (size == null || size <= 0) size = 10;
        int total = data.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Map<String, Object>> items = data.subList(from, to);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("total", total);
        out.put("page", page);
        out.put("size", size);
        out.put("pages", (int) Math.ceil(total / (double) size));
        return ApiResponse.success(out);
    }

    // 导出：按筛选条件导出班级字典为 XLSX
    @RequiresPerm("class:view")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(value = "status", required = false) String status,
                                         @RequestParam(value = "campusId", required = false) Long campusId,
                                         @RequestParam(value = "gradeId", required = false) Long gradeId,
                                         @RequestParam(value = "q", required = false) String keyword) {
        // 复用 infoList 的查询逻辑
        List<ClassInfo> list;
        if (campusId != null && gradeId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = classInfoRepository.findByCampusIdAndGradeIdAndNameContainingOrderBySortOrderAsc(campusId, gradeId, keyword.trim());
            } else if (status == null || status.isEmpty()) list = classInfoRepository.findByCampusIdAndGradeIdOrderBySortOrderAsc(campusId, gradeId);
            else list = classInfoRepository.findByCampusIdAndGradeIdAndStatusOrderBySortOrderAsc(campusId, gradeId, status);
        } else if (campusId != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = classInfoRepository.findByCampusIdAndNameContainingOrderBySortOrderAsc(campusId, keyword.trim());
            } else if (status == null || status.isEmpty()) list = classInfoRepository.findByCampusIdOrderBySortOrderAsc(campusId);
            else list = classInfoRepository.findByCampusIdAndStatusOrderBySortOrderAsc(campusId, status);
        } else {
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = classInfoRepository.findByNameContainingOrderBySortOrderAsc(keyword.trim());
            } else {
                list = (status == null || status.isEmpty()) ? classInfoRepository.findAll() : classInfoRepository.findByStatusOrderBySortOrderAsc(status);
            }
        }

        // 构建名称映射
        Set<Long> campusIds = new HashSet<>();
        Set<Long> gradeIds = new HashSet<>();
        for (ClassInfo c : list) {
            if (c.getCampusId() != null) campusIds.add(c.getCampusId());
            if (c.getGradeId() != null) gradeIds.add(c.getGradeId());
        }
        Map<Long, String> campusNameMap = new HashMap<>();
        if (!campusIds.isEmpty()) {
            List<Campus> campuses = campusRepository.findAllById(campusIds);
            for (Campus cp : campuses) campusNameMap.put(cp.getId(), cp.getName());
        }
        Map<Long, String> gradeNameMap = new HashMap<>();
        if (!gradeIds.isEmpty()) {
            List<GradeDict> grades = gradeDictRepository.findAllById(gradeIds);
            for (GradeDict gd : grades) gradeNameMap.put(gd.getId(), gd.getName());
        }

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("班级信息");
            int rowIdx = 0;
            // 头部
            Row header = sheet.createRow(rowIdx++);
            String[] heads = new String[]{
                    "校区", "年级", "班级名称", "授课模式", "开课日期", "结课日期",
                    "学期", "班级状态", "教室", "班额", "学费标准", "收费状态",
                    "启用状态", "排序", "标签", "班主任IDs", "在读人数"
            };
            for (int i = 0; i < heads.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(heads[i]);
            }
            // 内容
            for (ClassInfo c : list) {
                Row r = sheet.createRow(rowIdx++);
                int col = 0;
                r.createCell(col++).setCellValue(safe(campusNameMap.get(c.getCampusId())));
                r.createCell(col++).setCellValue(safe(gradeNameMap.get(c.getGradeId())));
                r.createCell(col++).setCellValue(safe(c.getName()));
                r.createCell(col++).setCellValue(safe(c.getMode()));
                r.createCell(col++).setCellValue(safe(c.getStartDate()));
                r.createCell(col++).setCellValue(safe(c.getEndDate()));
                r.createCell(col++).setCellValue(safe(c.getTerm()));
                r.createCell(col++).setCellValue(safe(c.getState()));
                r.createCell(col++).setCellValue(safe(c.getClassroom()));
                r.createCell(col++).setCellValue(c.getCapacityLimit() == null ? 0 : c.getCapacityLimit());
                r.createCell(col++).setCellValue(safe(c.getFeeStandard()));
                r.createCell(col++).setCellValue(safe(c.getFeeStatus()));
                r.createCell(col++).setCellValue(safe(c.getStatus()));
                r.createCell(col++).setCellValue(c.getSortOrder() == null ? 0 : c.getSortOrder());
                r.createCell(col++).setCellValue(safe(c.getTags()));
                r.createCell(col++).setCellValue(safe(c.getHeadTeacherIds()));
                // 统计在读人数（active）
                int currentCount = 0;
                try { currentCount = (int) classEnrollmentRepository.countByClassIdAndStatus(c.getId(), "active"); } catch (Exception ignore) {}
                r.createCell(col++).setCellValue(currentCount);
            }
            for (int i = 0; i < heads.length; i++) sheet.autoSizeColumn(i);

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            wb.write(bos);
            byte[] bytes = bos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class_info.xlsx");
            headers.setContentLength(bytes.length);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(("导出失败: " + e.getMessage()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @PutMapping({"/info/update/{id}", "/update/{id}"})
    public ApiResponse<Map<String, Object>> infoUpdate(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<ClassInfo> opt = classInfoRepository.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "班级不存在");
        ClassInfo entity = opt.get();
        if (payload.containsKey("campusId")) entity.setCampusId(Long.valueOf(String.valueOf(payload.get("campusId"))));
        if (payload.containsKey("gradeId")) entity.setGradeId(Long.valueOf(String.valueOf(payload.get("gradeId"))));
        if (payload.containsKey("name")) {
            String name = String.valueOf(payload.get("name")).trim();
            if (name.isEmpty()) return ApiResponse.error(400, "班级名称不能为空");
            Long cId = payload.containsKey("campusId") ? Long.valueOf(String.valueOf(payload.get("campusId"))) : entity.getCampusId();
            Long gId = payload.containsKey("gradeId") ? Long.valueOf(String.valueOf(payload.get("gradeId"))) : entity.getGradeId();
            Optional<ClassInfo> dup = classInfoRepository.findByCampusIdAndGradeIdAndName(cId, gId, name);
            if (dup.isPresent() && !Objects.equals(dup.get().getId(), id)) {
                return ApiResponse.error(409, "同校同年级已存在该班级名称");
            }
            entity.setName(name);
        }
        if (payload.containsKey("status")) entity.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("sortOrder")) entity.setSortOrder(Integer.valueOf(String.valueOf(payload.get("sortOrder"))));
        // 扩展字段同步
        if (payload.containsKey("mode")) entity.setMode(String.valueOf(payload.get("mode")));
        if (payload.containsKey("startDate")) entity.setStartDate(String.valueOf(payload.get("startDate")));
        if (payload.containsKey("endDate")) entity.setEndDate(String.valueOf(payload.get("endDate")));
        if (payload.containsKey("term")) entity.setTerm(String.valueOf(payload.get("term")));
        if (payload.containsKey("state")) entity.setState(String.valueOf(payload.get("state")));
        if (payload.containsKey("classroom")) entity.setClassroom(String.valueOf(payload.get("classroom")));
        if (payload.containsKey("capacityLimit")) entity.setCapacityLimit(Integer.valueOf(String.valueOf(payload.get("capacityLimit"))));
        if (payload.containsKey("feeStandard")) entity.setFeeStandard(String.valueOf(payload.get("feeStandard")));
        if (payload.containsKey("feeStatus")) entity.setFeeStatus(String.valueOf(payload.get("feeStatus")));
        if (payload.containsKey("note")) entity.setNote(String.valueOf(payload.get("note")));
        if (payload.containsKey("parentGroup")) entity.setParentGroup(String.valueOf(payload.get("parentGroup")));
        if (payload.containsKey("contacts")) entity.setContacts(String.valueOf(payload.get("contacts")));
        if (payload.containsKey("subject")) {
            String subId = String.valueOf(payload.get("subject"));
            entity.setSubject(subId);
        }
        if (payload.containsKey("headTeacherIds")) entity.setHeadTeacherIds(toCsvFromListOfNumbers(payload.get("headTeacherIds")));
        if (payload.containsKey("tags")) entity.setTags(toCsvFromListOfStrings(payload.get("tags")));
        ClassInfo saved = classInfoRepository.save(entity);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("campusId", saved.getCampusId());
        out.put("campusName", saved.getCampusId() == null ? null : campusRepository.findById(saved.getCampusId()).map(Campus::getName).orElse(null));
        out.put("gradeId", saved.getGradeId());
        out.put("gradeName", saved.getGradeId() == null ? null : gradeDictRepository.findById(saved.getGradeId()).map(GradeDict::getName).orElse(null));
        out.put("name", saved.getName());
        out.put("status", saved.getStatus());
        out.put("sortOrder", saved.getSortOrder());
        // 扩展字段同步返回
        out.put("classId", saved.getId());
        out.put("mode", saved.getMode());
        out.put("startDate", saved.getStartDate());
        out.put("endDate", saved.getEndDate());
        out.put("term", saved.getTerm());
        out.put("state", saved.getState());
        out.put("classroom", saved.getClassroom());
        out.put("capacityLimit", saved.getCapacityLimit());
        out.put("feeStandard", saved.getFeeStandard());
        out.put("feeStatus", saved.getFeeStatus());
        out.put("note", saved.getNote());
        out.put("parentGroup", saved.getParentGroup());
        out.put("contacts", saved.getContacts());
        out.put("subject", saved.getSubject());
        try {
            String subjectName = (saved.getSubject() == null || saved.getSubject().trim().isEmpty()) ? null
                    : subjectDictRepository.findById(Long.valueOf(saved.getSubject())).map(s -> s.getName()).orElse(null);
            out.put("subjectName", subjectName);
        } catch (Exception ignore) {}
        out.put("headTeacherIds", parseCsvToLongList(saved.getHeadTeacherIds()));
        out.put("tags", parseCsv(saved.getTags()));
        return ApiResponse.success(out);
    }

    @DeleteMapping({"/info/delete/{id}", "/delete/{id}"})
    public ApiResponse<Map<String, Object>> infoDelete(@PathVariable Long id) {
        if (!classInfoRepository.findById(id).isPresent()) return ApiResponse.error(404, "班级不存在");
        classInfoRepository.deleteById(id);
        Map<String, Object> out = new HashMap<>();
        out.put("id", id);
        return ApiResponse.success(out);
    }

    @PostMapping({"/info/save", "/save"})
    public ApiResponse<Map<String, Object>> infoSave(@RequestBody Map<String, Object> payload) {
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        if (name.isEmpty()) return ApiResponse.error(400, "班级名称不能为空");
        Object campusIdObj = payload.get("campusId");
        Object gradeIdObj = payload.get("gradeId");
        if (campusIdObj == null) return ApiResponse.error(400, "请先选择学校（校区）");
        if (gradeIdObj == null) return ApiResponse.error(400, "请先选择年级");
        Long campusId = Long.valueOf(String.valueOf(campusIdObj));
        Long gradeId = Long.valueOf(String.valueOf(gradeIdObj));
        ClassInfo entity = classInfoRepository.findByCampusIdAndGradeIdAndName(campusId, gradeId, name).orElseGet(ClassInfo::new);
        entity.setCampusId(campusId);
        entity.setGradeId(gradeId);
        entity.setName(name);
        entity.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        Object sortOrder = payload.get("sortOrder");
        entity.setSortOrder(sortOrder == null ? 0 : Integer.parseInt(String.valueOf(sortOrder)));
        // 扩展字段写入
        entity.setMode(String.valueOf(payload.getOrDefault("mode", entity.getMode())));
        entity.setStartDate(String.valueOf(payload.getOrDefault("startDate", entity.getStartDate())));
        entity.setEndDate(String.valueOf(payload.getOrDefault("endDate", entity.getEndDate())));
        entity.setTerm(String.valueOf(payload.getOrDefault("term", entity.getTerm())));
        entity.setState(String.valueOf(payload.getOrDefault("state", entity.getState())));
        entity.setClassroom(String.valueOf(payload.getOrDefault("classroom", entity.getClassroom())));
        Object cap = payload.get("capacityLimit");
        if (cap != null) entity.setCapacityLimit(Integer.valueOf(String.valueOf(cap)));
        entity.setFeeStandard(String.valueOf(payload.getOrDefault("feeStandard", entity.getFeeStandard())));
        entity.setFeeStatus(String.valueOf(payload.getOrDefault("feeStatus", entity.getFeeStatus())));
        entity.setNote(String.valueOf(payload.getOrDefault("note", entity.getNote())));
        entity.setParentGroup(String.valueOf(payload.getOrDefault("parentGroup", entity.getParentGroup())));
        entity.setContacts(String.valueOf(payload.getOrDefault("contacts", entity.getContacts())));
        if (payload.containsKey("subject")) {
            String subId = String.valueOf(payload.get("subject"));
            entity.setSubject(subId);
        }
        entity.setHeadTeacherIds(toCsvFromListOfNumbers(payload.get("headTeacherIds")));
        entity.setTags(toCsvFromListOfStrings(payload.get("tags")));
        ClassInfo saved = classInfoRepository.save(entity);
        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("campusId", saved.getCampusId());
        out.put("campusName", saved.getCampusId() == null ? null : campusRepository.findById(saved.getCampusId()).map(Campus::getName).orElse(null));
        out.put("gradeId", saved.getGradeId());
        out.put("gradeName", saved.getGradeId() == null ? null : gradeDictRepository.findById(saved.getGradeId()).map(GradeDict::getName).orElse(null));
        out.put("name", saved.getName());
        out.put("status", saved.getStatus());
        out.put("sortOrder", saved.getSortOrder());
        // 扩展字段返回
        out.put("classId", saved.getId());
        out.put("mode", saved.getMode());
        out.put("startDate", saved.getStartDate());
        out.put("endDate", saved.getEndDate());
        out.put("term", saved.getTerm());
        out.put("state", saved.getState());
        out.put("classroom", saved.getClassroom());
        out.put("capacityLimit", saved.getCapacityLimit());
        out.put("feeStandard", saved.getFeeStandard());
        out.put("feeStatus", saved.getFeeStatus());
        out.put("note", saved.getNote());
        out.put("parentGroup", saved.getParentGroup());
        out.put("contacts", saved.getContacts());
        out.put("subject", saved.getSubject());
        try {
            String subjectName = (saved.getSubject() == null || saved.getSubject().trim().isEmpty()) ? null
                    : subjectDictRepository.findById(Long.valueOf(saved.getSubject())).map(s -> s.getName()).orElse(null);
            out.put("subjectName", subjectName);
        } catch (Exception ignore) {}
        out.put("headTeacherIds", parseCsvToLongList(saved.getHeadTeacherIds()));
        out.put("tags", parseCsv(saved.getTags()));
        return ApiResponse.success(out);
    }

    // ===== ClassInfo 辅助方法 =====
    private List<String> parseCsv(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptyList();
        String[] arr = s.split(",");
        List<String> out = new ArrayList<>();
        for (String it : arr) { if (it != null && !it.trim().isEmpty()) out.add(it.trim()); }
        return out;
    }

    private String safe(String s) { return s == null ? "" : s; }

    private List<Long> parseCsvToLongList(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptyList();
        String[] arr = s.split(",");
        List<Long> out = new ArrayList<>();
        for (String it : arr) {
            try { out.add(Long.valueOf(it.trim())); } catch (Exception ignore) {}
        }
        return out;
    }

    private String toCsvFromListOfNumbers(Object v) {
        if (v == null) return null;
        if (v instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object o : (Collection<?>) v) {
                if (o == null) continue;
                if (!first) sb.append(",");
                sb.append(String.valueOf(o));
                first = false;
            }
            return sb.toString();
        }
        return String.valueOf(v);
    }

    private String toCsvFromListOfStrings(Object v) {
        if (v == null) return null;
        if (v instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object o : (Collection<?>) v) {
                String s = o == null ? null : String.valueOf(o).trim();
                if (s == null || s.isEmpty()) continue;
                if (!first) sb.append(",");
                sb.append(s);
                first = false;
            }
            return sb.toString();
        }
        return String.valueOf(v);
    }

    private Map<String, Object> withCapacity(Map<String, Object> c) {
        Map<String, Object> out = new LinkedHashMap<>(c);
        Long id = ((Number) c.get("id")).longValue();
        int maxSize = ((Number) c.getOrDefault("maxSize", 0)).intValue();
        int enrolled = InMemoryCourseStore.classStudents.getOrDefault(id, Collections.emptySet()).size();
        // 优先使用数据库中的报名关系进行人数统计
        try {
            long dbCount = classEnrollmentRepository.countByClassIdAndStatus(id, "active");
            enrolled = (int) Math.max(enrolled, dbCount);
        } catch (Exception ignore) {}
        out.put("enrolledCount", enrolled);
        out.put("remaining", Math.max(0, maxSize - enrolled));
        return out;
    }

    private Map<String, Object> joinRecord(Long fromClassId, Long toClassId, String reason) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "join");
        r.put("fromClassId", fromClassId);
        r.put("toClassId", toClassId);
        r.put("time", new Date());
        r.put("reason", reason);
        return r;
    }

    private Map<String, Object> transferRecord(Long fromClassId, Long toClassId, String reason) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "transfer");
        r.put("fromClassId", fromClassId);
        r.put("toClassId", toClassId);
        r.put("time", new Date());
        r.put("reason", reason);
        return r;
    }

    private void addRecord(Long studentId, Map<String, Object> r) {
        InMemoryCourseStore.studentClassRecords.computeIfAbsent(studentId, k -> new ArrayList<>()).add(r);
    }
}