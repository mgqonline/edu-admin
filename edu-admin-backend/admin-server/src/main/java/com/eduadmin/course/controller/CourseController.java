package com.eduadmin.course.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.course.entity.Course;
import com.eduadmin.course.repo.CourseRepository;
import com.eduadmin.course.store.InMemoryCourseStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> course) {
        // 兼容前端：如果传入 courseId 则按 ID 更新，否则新建
        String cid = course.get("courseId") != null ? String.valueOf(course.get("courseId")) : null;
        Course entity = null;
        if (cid != null && !cid.trim().isEmpty()) {
            try {
                Long id = Long.valueOf(cid);
                entity = courseRepository.findById(id).orElse(null);
            } catch (NumberFormatException ignore) {}
        }
        if (entity == null) {
            entity = new Course();
            entity.setCreatedAt(new Date());
        }
        // 映射字段（兼容前端命名）：仅当传参非空时更新，避免产生字符串"null"
        Object nameObj = course.get("name");
        if (nameObj != null) entity.setName(String.valueOf(nameObj));
        Object categoryObj = course.get("category");
        if (categoryObj != null) entity.setCategory(String.valueOf(categoryObj));
        Object typeObj = course.get("type");
        if (typeObj != null) entity.setType(String.valueOf(typeObj));
        Object ageRangeObj = course.get("ageRange");
        if (ageRangeObj != null) entity.setAgeRange(String.valueOf(ageRangeObj));
        Object subjectObj = course.get("subject");
        if (subjectObj != null) entity.setSubject(String.valueOf(subjectObj));
        Object gradeObj = course.get("grade");
        if (gradeObj != null) entity.setGrade(String.valueOf(gradeObj));
        Object campusIdObj = course.get("campusId");
        if (campusIdObj != null) entity.setCampusId(Long.valueOf(String.valueOf(campusIdObj)));
        Object teacherIdObj = course.get("teacherId");
        if (teacherIdObj != null) entity.setTeacherId(Long.valueOf(String.valueOf(teacherIdObj)));
        // 单价/课时数映射到 price/lessons
        Object unitPrice = course.get("unitPrice");
        if (unitPrice != null) entity.setPrice(Double.valueOf(String.valueOf(unitPrice)));
        Object lessonCount = course.get("lessonCount");
        if (lessonCount != null) entity.setLessons(Integer.valueOf(String.valueOf(lessonCount)));
        // 大纲与教学目标
        Object outlineObj = course.get("outline");
        if (outlineObj != null) entity.setOutline(String.valueOf(outlineObj));
        Object objectiveObj = course.get("objective");
        if (objectiveObj != null) entity.setObjective(String.valueOf(objectiveObj));
        // 起止日期（yyyy-MM-dd）
        Object startDateObj = course.get("startDate");
        Object endDateObj = course.get("endDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (startDateObj != null && String.valueOf(startDateObj).length() >= 8) {
                entity.setStartDate(sdf.parse(String.valueOf(startDateObj)));
            }
            if (endDateObj != null && String.valueOf(endDateObj).length() >= 8) {
                entity.setEndDate(sdf.parse(String.valueOf(endDateObj)));
            }
        } catch (ParseException ignore) {}
        // 状态
        Object statusObj = course.get("status");
        if (statusObj != null) {
            entity.setStatus(String.valueOf(statusObj));
        } else if (entity.getStatus() == null || entity.getStatus().isEmpty()) {
            entity.setStatus("draft");
        }
        entity.setUpdatedAt(new Date());

        Course saved = courseRepository.save(entity);
        Map<String, Object> out = toMap(saved);
        return ApiResponse.success(out);
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable("id") String id) {
        try {
            Long lid = Long.valueOf(id);
            Optional<Course> opt = courseRepository.findById(lid);
            if (!opt.isPresent()) return ApiResponse.success(new HashMap<>());
            return ApiResponse.success(toMap(opt.get()));
        } catch (NumberFormatException e) {
            return ApiResponse.success(new HashMap<>());
        }
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(value = "status", required = false) String status,
                                                       @RequestParam(value = "keyword", required = false) String keyword) {
        List<Course> found;
        boolean hasStatus = status != null && !status.isEmpty();
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        if (hasStatus && hasKeyword) {
            found = courseRepository.findByStatusAndNameContaining(status, keyword);
        } else if (hasStatus) {
            found = courseRepository.findByStatus(status);
        } else if (hasKeyword) {
            found = courseRepository.findByNameContaining(keyword);
        } else {
            found = courseRepository.findAll();
        }
        List<Map<String, Object>> out = found.stream().map(this::toMap).collect(Collectors.toList());
        return ApiResponse.success(out);
    }

    @PostMapping("/dev/seed")
    public ApiResponse<Map<String, Object>> seedDev() {
        if (courseRepository.count() > 0) {
            return ApiResponse.success(Collections.singletonMap("message", "courses already exist"));
        }
        List<Course> list = sampleCourses();
        courseRepository.saveAll(list);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("inserted", list.size());
        return ApiResponse.success(out);
    }

    @PostMapping("/status/{id}")
    public ApiResponse<Map<String, Object>> changeStatus(@PathVariable("id") String id,
                                                         @RequestParam("status") String status) {
        try {
            Long lid = Long.valueOf(id);
            Optional<Course> opt = courseRepository.findById(lid);
            if (!opt.isPresent()) return ApiResponse.success(new HashMap<>());
            Course c = opt.get();
            c.setStatus(status);
            c.setUpdatedAt(new Date());
            Course saved = courseRepository.save(c);
            return ApiResponse.success(toMap(saved));
        } catch (NumberFormatException e) {
            return ApiResponse.success(new HashMap<>());
        }
    }

    @PostMapping("/materials/bind")
    public ApiResponse<Map<String, Object>> bindMaterials(@RequestBody Map<String, Object> payload) {
        String courseId = String.valueOf(payload.get("courseId"));
        List<String> materialIds = (List<String>) payload.getOrDefault("materialIds", Collections.emptyList());
        Map<String, Object> c = InMemoryCourseStore.courses.get(courseId);
        if (c == null) return ApiResponse.success(new HashMap<>());
        c.put("materials", new ArrayList<>(materialIds));
        return ApiResponse.success(c);
    }

    @GetMapping("/materials/{courseId}")
    public ApiResponse<List<Map<String, Object>>> listMaterials(@PathVariable("courseId") String courseId) {
        Map<String, Object> c = InMemoryCourseStore.courses.get(courseId);
        if (c == null) return ApiResponse.success(Collections.emptyList());
        List<String> mids = (List<String>) c.getOrDefault("materials", Collections.emptyList());
        List<Map<String, Object>> out = new ArrayList<>();
        for (String mid : mids) {
            Map<String, Object> tb = InMemoryCourseStore.textbooks.get(mid);
            if (tb != null) out.add(tb);
        }
        return ApiResponse.success(out);
    }

    private List<Course> sampleCourses() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Course> list = new ArrayList<>();
        list.add(buildCourse("数学提高班（七年级）", "提升班", "数学", "七年级", 1L, 101L, "published", 2980.0, 16, "2025-11-10", "2026-01-20", "夯实函数与方程基础，提升解题速度与准确率。", sdf));
        list.add(buildCourse("英语语法精讲（八年级）", "基础班", "英语", "八年级", 2L, 102L, "published", 2680.0, 12, "2025-11-12", "2026-01-15", "系统梳理从时态到从句的核心语法点。", sdf));
        list.add(buildCourse("物理实验与探究（九年级）", "实验班", "物理", "九年级", 1L, 103L, "published", 3280.0, 10, "2025-11-18", "2026-02-01", "力学与电学核心实验设计与数据分析。", sdf));
        list.add(buildCourse("化学方程式与化学计算（九年级）", "提升班", "化学", "九年级", 3L, 104L, "published", 3080.0, 14, "2025-11-15", "2026-01-30", "方程式书写规范与常见化学计算专题训练。", sdf));
        list.add(buildCourse("语文阅读与写作（七年级）", "综合班", "语文", "七年级", 2L, 105L, "published", 2580.0, 12, "2025-11-20", "2026-01-25", "阅读理解技巧与记叙文、说明文写作训练。", sdf));
        list.add(buildCourse("生物基础知识梳理（八年级）", "基础班", "生物", "八年级", 1L, 106L, "published", 2180.0, 10, "2025-11-22", "2026-01-28", "细胞、遗传与生态基础知识系统梳理。", sdf));
        list.add(buildCourse("历史专题突破（八年级）", "专题班", "历史", "八年级", 3L, 107L, "published", 2380.0, 8, "2025-11-25", "2026-01-18", "近现代史重点事件与线索梳理，提升历史观。", sdf));
        list.add(buildCourse("地理地图技能与综合题（九年级）", "提升班", "地理", "九年级", 2L, 108L, "published", 2480.0, 10, "2025-11-28", "2026-01-31", "地图判读、坐标定位与综合题解题方法。", sdf));
        list.add(buildCourse("数学竞赛基础（八年级）", "竞赛班", "数学", "八年级", 1L, 101L, "published", 3680.0, 12, "2025-12-01", "2026-02-05", "数论、组合与不等式入门，打好竞赛基础。", sdf));
        list.add(buildCourse("英语词汇专项（七年级）", "专题班", "英语", "七年级", 3L, 102L, "published", 1980.0, 8, "2025-12-03", "2026-01-22", "高频词根词缀、词族记忆法与场景应用。", sdf));
        return list;
    }

    private Course buildCourse(String name, String type, String subject, String grade, Long campusId, Long teacherId,
                               String status, Double price, Integer lessons, String startDate, String endDate,
                               String description, SimpleDateFormat sdf) {
        Course c = new Course();
        c.setName(name);
        c.setType(type);
        c.setSubject(subject);
        c.setGrade(grade);
        c.setCampusId(campusId);
        c.setTeacherId(teacherId);
        c.setStatus(status);
        c.setPrice(price);
        c.setLessons(lessons);
        try {
            c.setStartDate(sdf.parse(startDate));
            c.setEndDate(sdf.parse(endDate));
        } catch (ParseException e) {
            c.setStartDate(null);
            c.setEndDate(null);
        }
        c.setDescription(description);
        Date now = new Date();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        return c;
    }

    private Map<String, Object> toMap(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        // 兼容前端：提供 courseId 字段作为字符串
        m.put("courseId", c.getId() == null ? null : String.valueOf(c.getId()));
        m.put("name", c.getName());
        m.put("category", c.getCategory());
        m.put("type", c.getType());
        m.put("subject", c.getSubject());
        m.put("grade", c.getGrade());
        m.put("ageRange", c.getAgeRange());
        m.put("campusId", c.getCampusId());
        m.put("teacherId", c.getTeacherId());
        m.put("status", c.getStatus());
        // 兼容前端展示字段命名
        m.put("unitPrice", c.getPrice());
        m.put("lessonCount", c.getLessons());
        m.put("startDate", c.getStartDate());
        m.put("endDate", c.getEndDate());
        m.put("outline", c.getOutline());
        m.put("objective", c.getObjective());
        m.put("description", c.getDescription());
        return m;
    }
}