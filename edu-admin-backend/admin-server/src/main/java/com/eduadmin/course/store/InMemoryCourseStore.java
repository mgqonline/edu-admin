package com.eduadmin.course.store;

import java.util.*;

/**
 * 简易内存存储：课程、教材、班级与库存记录。
 * 仅用于联调与演示，重启后数据将重置。
 */
public class InMemoryCourseStore {
    // 课程：courseId -> course map
    public static final Map<String, Map<String, Object>> courses = new LinkedHashMap<>();
    // 教材：textbookId -> textbook map
    public static final Map<String, Map<String, Object>> textbooks = new LinkedHashMap<>();
    // 班级：classId -> class map（包含 courseId 关联与班级学费）
    public static final Map<Long, Map<String, Object>> classes = new LinkedHashMap<>();
    // 班级学生：classId -> set(studentId)
    public static final Map<Long, Set<Long>> classStudents = new HashMap<>();
    // 学员分班/调班记录：studentId -> list of records {type: join/transfer, classId, fromClassId, toClassId, time, reason}
    public static final Map<Long, List<Map<String, Object>>> studentClassRecords = new HashMap<>();
    // 学员选班申请：applicationId -> {id, classId, studentId, status: pending/approved/rejected, note, reason, time}
    public static final Map<Long, Map<String, Object>> selectApplications = new LinkedHashMap<>();
    public static long selectAppSeq = 1L;
    // 库存记录：textbookId -> list of records {type: in/out, qty, time}
    public static final Map<String, List<Map<String, Object>>> inventoryRecords = new HashMap<>();

    static {
        // 初始化示例课程
        String mathCourseId = UUID.randomUUID().toString();
        Map<String, Object> mathCourse = new LinkedHashMap<>();
        mathCourse.put("courseId", mathCourseId);
        mathCourse.put("name", "数学提高班");
        mathCourse.put("category", "文化课");
        mathCourse.put("type", "班课"); // 班课 / 一对一
        mathCourse.put("ageRange", "8-12");
        mathCourse.put("lessonCount", 32);
        mathCourse.put("unitPrice", 120.0);
        mathCourse.put("outline", "分数、方程、几何综合提升");
        mathCourse.put("objective", "夯实基础并提高解题能力");
        mathCourse.put("status", "draft"); // draft/published/offline
        mathCourse.put("materials", new ArrayList<String>());
        courses.put(mathCourseId, mathCourse);

        String engCourseId = UUID.randomUUID().toString();
        Map<String, Object> engCourse = new LinkedHashMap<>();
        engCourse.put("courseId", engCourseId);
        engCourse.put("name", "小学英语口语");
        engCourse.put("category", "语言类");
        engCourse.put("type", "一对一");
        engCourse.put("ageRange", "7-12");
        engCourse.put("lessonCount", 24);
        engCourse.put("unitPrice", 180.0);
        engCourse.put("outline", "日常交流与情景会话");
        engCourse.put("objective", "提升口语表达与听力理解");
        engCourse.put("status", "published");
        engCourse.put("materials", new ArrayList<String>());
        courses.put(engCourseId, engCourse);

        // 初始化示例教材
        String mathTextbookId = UUID.randomUUID().toString();
        Map<String, Object> mathTextbook = new LinkedHashMap<>();
        mathTextbook.put("textbookId", mathTextbookId);
        mathTextbook.put("name", "数学提高训练册");
        mathTextbook.put("publisher", "教育出版社");
        mathTextbook.put("unitPrice", 45.0);
        mathTextbook.put("courseIds", Arrays.asList(mathCourseId));
        mathTextbook.put("stock", 30);
        textbooks.put(mathTextbookId, mathTextbook);
        inventoryRecords.put(mathTextbookId, new ArrayList<>());
        inventoryRecords.get(mathTextbookId).add(record("in", 30));

        String engTextbookId = UUID.randomUUID().toString();
        Map<String, Object> engTextbook = new LinkedHashMap<>();
        engTextbook.put("textbookId", engTextbookId);
        engTextbook.put("name", "少儿英语口语教程");
        engTextbook.put("publisher", "外语教学与研究出版社");
        engTextbook.put("unitPrice", 58.0);
        engTextbook.put("courseIds", Arrays.asList(engCourseId));
        engTextbook.put("stock", 15);
        textbooks.put(engTextbookId, engTextbook);
        inventoryRecords.put(engTextbookId, new ArrayList<>());
        inventoryRecords.get(engTextbookId).add(record("in", 15));

        // 将教材绑定到课程 materials 列表
        ((List<String>) mathCourse.get("materials")).add(mathTextbookId);
        ((List<String>) engCourse.get("materials")).add(engTextbookId);

        // 初始化示例班级（含 courseId 与班级学费）
        classes.put(1001L, classItem(1001L, "数学提高班A班", mathCourseId, 3600.0));
        classes.put(1002L, classItem(1002L, "数学提高班B班", mathCourseId, 3600.0));
        classes.put(2001L, classItem(2001L, "英语口语一对一-张老师", engCourseId, 4320.0));

        classStudents.put(1001L, new LinkedHashSet<>(Arrays.asList(5001L, 5002L)));
        classStudents.put(1002L, new LinkedHashSet<>());
        classStudents.put(2001L, new LinkedHashSet<>(Collections.singletonList(6001L)));
    }

    private static Map<String, Object> classItem(Long id, String name, String courseId, Double fee) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("courseId", courseId);
        m.put("fee", fee);
        m.put("classNo", String.valueOf(id));
        m.put("campusId", 1L);
        m.put("room", "A-101");
        m.put("startDate", new Date());
        m.put("endDate", null);
        m.put("weekDays", Arrays.asList("Mon", "Wed"));
        m.put("durationMinutes", 90);
        m.put("maxSize", 30);
        m.put("teacherMainId", 3001L);
        m.put("teacherAssistantId", 3002L);
        m.put("status", "not_started"); // not_started/ongoing/ended/suspended
        m.put("suspendReason", null);
        return m;
    }

    private static Map<String, Object> record(String type, int qty) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", type);
        r.put("qty", qty);
        r.put("time", new Date());
        return r;
    }
}