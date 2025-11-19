package com.eduadmin.system.init;

import com.eduadmin.baseplatform.campus.entity.Campus;
import com.eduadmin.baseplatform.campus.repo.CampusRepository;
import com.eduadmin.baseplatform.classroom.entity.Classroom;
import com.eduadmin.baseplatform.classroom.repo.ClassroomRepository;
import com.eduadmin.baseplatform.staff.entity.Staff;
import com.eduadmin.baseplatform.staff.repo.StaffRepository;
import com.eduadmin.baseplatform.permission.entity.PermCatalog;
import com.eduadmin.baseplatform.permission.repo.PermCatalogRepository;
import com.eduadmin.course.entity.ClassInfo;
import com.eduadmin.course.entity.GradeDict;
import com.eduadmin.course.repo.ClassInfoRepository;
import com.eduadmin.course.repo.GradeDictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.*;

@Component
public class SampleDataSeeder {

    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private GradeDictRepository gradeRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private com.eduadmin.student.grade.repo.StudentGradeRepository studentGradeRepository;
    @Autowired
    private com.eduadmin.student.repo.StudentRepository studentRepository;

    @Autowired
    private PermCatalogRepository permCatalogRepository;

    @Autowired
    private com.eduadmin.scheduling.repo.SystemHolidayRepository holidayRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        // 校区
        List<Campus> campuses = campusRepository.findAll();
        if (campuses.isEmpty()) {
            Campus east = new Campus();
            east.setName("东校区");
            east.setAddress("海淀中路1号");
            east.setPhone("010-12345678");
            east.setManager("张三");
            east.setRegion("北京");
            east.setConfigJson("{\"signRule\":\"qrcode\",\"conflictCheckLevel\":\"strict\"}");
            east.setStatus("enabled");
            Campus savedEast = campusRepository.save(east);

            Campus west = new Campus();
            west.setName("西校区");
            west.setAddress("中关村西路88号");
            west.setPhone("010-87654321");
            west.setManager("李四");
            west.setRegion("北京");
            west.setConfigJson("{\"signRule\":\"qrcode\",\"conflictCheckLevel\":\"strict\"}");
            west.setStatus("enabled");
            Campus savedWest = campusRepository.save(west);

            campuses = Arrays.asList(savedEast, savedWest);
        }

        // 年级
        if (gradeRepository.count() == 0) {
            for (Campus cp : campuses) {
                GradeDict g1 = new GradeDict();
                g1.setCampusId(cp.getId());
                g1.setName("一年级");
                g1.setStatus("enabled");
                g1.setSortOrder(1);
                gradeRepository.save(g1);

                GradeDict g2 = new GradeDict();
                g2.setCampusId(cp.getId());
                g2.setName("二年级");
                g2.setStatus("enabled");
                g2.setSortOrder(2);
                gradeRepository.save(g2);
            }
        }

        // 教室（持久化）
        if (classroomRepository.count() == 0) {
            Campus first = campuses.get(0);
            Classroom a101 = new Classroom();
            a101.setCampusId(first.getId());
            a101.setName("A101");
            a101.setRoomCode("A101");
            a101.setCapacity(40);
            a101.setUsableSeats(38);
            a101.setStatus("enabled");
            a101.setNote("一楼东侧");
            classroomRepository.save(a101);

            Classroom a102 = new Classroom();
            a102.setCampusId(first.getId());
            a102.setName("A102");
            a102.setRoomCode("A102");
            a102.setCapacity(36);
            a102.setUsableSeats(34);
            a102.setStatus("enabled");
            a102.setNote("一楼东侧");
            classroomRepository.save(a102);

            Campus second = campuses.size() > 1 ? campuses.get(1) : first;
            Classroom b201 = new Classroom();
            b201.setCampusId(second.getId());
            b201.setName("B201");
            b201.setRoomCode("B201");
            b201.setCapacity(42);
            b201.setUsableSeats(40);
            b201.setStatus("enabled");
            b201.setNote("二楼西侧");
            classroomRepository.save(b201);
        }

        // 教职工（用于班主任）
        if (staffRepository.count() == 0) {
            Staff t1 = new Staff();
            t1.setName("王老师");
            t1.setCampusId(campuses.get(0).getId());
            t1.setStatus("enabled");
            t1.setRoleId("role_teacher");
            t1.setUsername("wanglaoshi");
            t1.setPassword("123456");
            Staff s1 = staffRepository.save(t1);

            Staff t2 = new Staff();
            t2.setName("赵老师");
            t2.setCampusId(campuses.get(0).getId());
            t2.setStatus("enabled");
            t2.setRoleId("role_teacher");
            t2.setUsername("zhaolaoshi");
            t2.setPassword("123456");
            Staff s2 = staffRepository.save(t2);

            Staff t3 = new Staff();
            t3.setName("陈老师");
            t3.setCampusId(campuses.size() > 1 ? campuses.get(1).getId() : campuses.get(0).getId());
            t3.setStatus("enabled");
            t3.setRoleId("role_teacher");
            t3.setUsername("chenlaoshi");
            t3.setPassword("123456");
            staffRepository.save(t3);
        }

        // 学生成绩种子（表为空时生成约20条）
        if (studentGradeRepository.count() == 0) {
            java.util.List<com.eduadmin.student.entity.Student> students = studentRepository.findAll();
            if (!students.isEmpty()) {
                String[] subjects = new String[] { "数学", "英语", "语文", "科学" };
                java.util.Random rand = new java.util.Random();
                java.util.Calendar cal = java.util.Calendar.getInstance();
                for (int i = 0; i < 20; i++) {
                    com.eduadmin.student.grade.entity.StudentGrade g = new com.eduadmin.student.grade.entity.StudentGrade();
                    com.eduadmin.student.entity.Student s = students.get(i % students.size());
                    g.setStudentId(s.getId());
                    g.setSubject(subjects[i % subjects.length]);
                    g.setTerm("2025春季");
                    double base = 60 + rand.nextDouble() * 40;
                    g.setScore(Math.round(base * 100.0) / 100.0);
                    cal.set(java.util.Calendar.MONTH, rand.nextInt(6));
                    cal.set(java.util.Calendar.DAY_OF_MONTH, 1 + rand.nextInt(27));
                    g.setExamDate(cal.getTime());
                    g.setStatus(g.getScore() >= 60 ? "passed" : "failed");
                    g.setRemark("");
                    studentGradeRepository.save(g);
                }
            }
        }

        // 节假日种子（表为空时插入常见法定节日示例）
        if (holidayRepository.count() == 0) {
            try {
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.List<com.eduadmin.scheduling.entity.SystemHoliday> hs = new java.util.ArrayList<>();
                hs.add(makeHoliday(df.parse("2025-01-01"), "元旦"));
                hs.add(makeHoliday(df.parse("2025-02-01"), "春节假期(示例)"));
                hs.add(makeHoliday(df.parse("2025-04-05"), "清明节"));
                hs.add(makeHoliday(df.parse("2025-05-01"), "劳动节"));
                hs.add(makeHoliday(df.parse("2025-10-01"), "国庆节"));
                for (com.eduadmin.scheduling.entity.SystemHoliday h : hs) holidayRepository.save(h);
            } catch (Exception ignore) {}
        }

        if (permCatalogRepository.count() == 0) {
            saveMenu("签到统计", "menu:attendance", 10);
            saveMenu("财务管理", "menu:finance", 400);
            saveMenu("收费管理", "menu:finance:settlement:fee", 401);
            saveMenu("续费管理", "menu:finance:settlement:renew", 402);
            saveMenu("退费管理", "menu:finance:settlement:refund", 403);
            saveMenu("审批人配置", "menu:finance:settlement:approver-config", 404);
            saveMenu("开票申请", "menu:finance:invoice:apply", 405);
            saveMenu("发票管理", "menu:finance:invoice:manage", 406);
            saveMenu("开票统计", "menu:finance:invoice:stat", 407);
            saveMenu("课时费规则", "menu:finance:teacher-fee:rule", 408);
            saveMenu("课时费试算", "menu:finance:teacher-fee:trial", 409);

            saveMenu("招生报表", "menu:report:enroll", 201);
            saveMenu("教学报表", "menu:report:teaching", 202);
            saveMenu("财务报表", "menu:report:finance", 203);
            saveMenu("试听转化", "menu:report:audition", 204);

            saveMenu("校区管理", "menu:base:campus", 301);
            saveMenu("部门管理", "menu:base:dept", 302);
            saveMenu("角色与权限", "menu:base:role", 304);
            saveMenu("用户管理", "menu:base:user", 3041);
            saveMenu("教师管理", "menu:base:teacher", 305);
            saveMenu("教室管理", "menu:base:classroom", 306);
            saveMenu("节假日管理", "menu:base:holiday", 307);
            saveMenu("科目维护", "menu:base:subject-dict", 308);
            saveMenu("班级管理", "menu:base:class-dict", 309);
            saveMenu("年级维护", "menu:base:grade-dict", 310);
        }

        // 班级字典：保证示例班级存在（按校区+年级+名称去重插入）
        {
            // 获取必要的映射
            List<GradeDict> grades = gradeRepository.findAll();
            Map<String, Long> gradeIdByKey = new HashMap<>();
            for (GradeDict g : grades) {
                String key = g.getCampusId() + "::" + g.getName();
                gradeIdByKey.put(key, g.getId());
            }

            List<Staff> staffList = staffRepository.findAll();
            List<Long> teacherIds = new ArrayList<>();
            for (Staff s : staffList) teacherIds.add(s.getId());
            List<Long> headPair = teacherIds.size() >= 2 ? teacherIds.subList(0, 2) : teacherIds;

            Campus east = campuses.get(0);
            Campus west = campuses.size() > 1 ? campuses.get(1) : campuses.get(0);

            ensureClassExists(classInfoRepository, east.getId(), gradeIdByKey.get(east.getId() + "::一年级"),
                    "数学一班", headPair, "线下", "2025-03-01", "2025-07-15", "2025春季",
                    "in_session", "A101", 30, "3500/学期", "unpaid",
                    Arrays.asList("数学", "春季"), 1001L, 30);

            ensureClassExists(classInfoRepository, east.getId(), gradeIdByKey.get(east.getId() + "::二年级"),
                    "英语提高班", headPair, "线下", "2025-03-05", "2025-07-10", "2025春季",
                    "enrolling", "A102", 28, "3600/学期", "unpaid",
                    Arrays.asList("英语", "提高"), 1002L, 28);

            ensureClassExists(classInfoRepository, west.getId(), gradeIdByKey.get(west.getId() + "::一年级"),
                    "语文精品班", headPair, "线下", "2025-03-02", "2025-07-12", "2025春季",
                    "in_session", "B201", 32, "3400/学期", "paid",
                    Arrays.asList("语文", "精品"), 1003L, 32);

            ensureClassExists(classInfoRepository, west.getId(), gradeIdByKey.get(west.getId() + "::二年级"),
                    "科学探索班", headPair, "线下", "2025-03-08", "2025-07-18", "2025春季",
                    "planning", "B201", 26, "3300/学期", "unpaid",
                    Arrays.asList("科学", "探索"), 1004L, 26);
        }
    }

    private void saveMenu(String label, String value, int sort) {
        PermCatalog pc = new PermCatalog();
        pc.setType("menu");
        pc.setLabel(label);
        pc.setValue(value);
        pc.setSortOrder(sort);
        permCatalogRepository.save(pc);
    }

    private com.eduadmin.scheduling.entity.SystemHoliday makeHoliday(java.util.Date date, String name) {
        com.eduadmin.scheduling.entity.SystemHoliday h = new com.eduadmin.scheduling.entity.SystemHoliday();
        h.setDateOnly(date);
        h.setName(name);
        return h;
    }

    private void createClass(ClassInfoRepository repo, Long campusId, Long gradeId, String name,
                             List<Long> headTeacherIds, String mode, String startDate, String endDate,
                             String term, String state, String classroom, Integer capacityLimit,
                             String feeStandard, String feeStatus, List<String> tags,
                             Long courseId, Integer maxSize) {
        if (campusId == null || gradeId == null) return;
        ClassInfo ci = new ClassInfo();
        ci.setCampusId(campusId);
        ci.setGradeId(gradeId);
        ci.setName(name);
        ci.setStatus("enabled");
        ci.setSortOrder(1);
        ci.setMode(mode);
        ci.setStartDate(startDate);
        ci.setEndDate(endDate);
        ci.setTerm(term);
        ci.setState(state);
        ci.setClassroom(classroom);
        ci.setCapacityLimit(capacityLimit);
        if (courseId != null) ci.setCourseId(courseId);
        if (maxSize != null) ci.setMaxSize(maxSize);
        ci.setFeeStandard(feeStandard);
        ci.setFeeStatus(feeStatus);
        ci.setHeadTeacherIds(headTeacherIds == null ? null : join(headTeacherIds));
        ci.setTags(tags == null ? null : String.join(",", tags));
        repo.save(ci);
    }

    private String join(List<Long> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(String.valueOf(ids.get(i)));
        }
        return sb.toString();
    }

    private void ensureClassExists(ClassInfoRepository repo, Long campusId, Long gradeId, String name,
                                   List<Long> headTeacherIds, String mode, String startDate, String endDate,
                                   String term, String state, String classroom, Integer capacityLimit,
                                   String feeStandard, String feeStatus, List<String> tags,
                                   Long courseId, Integer maxSize) {
        if (campusId == null || gradeId == null) return;
        Optional<ClassInfo> exists = repo.findByCampusIdAndGradeIdAndName(campusId, gradeId, name);
        if (exists.isPresent()) return;
        createClass(repo, campusId, gradeId, name, headTeacherIds, mode, startDate, endDate,
                term, state, classroom, capacityLimit, feeStandard, feeStatus, tags, courseId, maxSize);
    }
}