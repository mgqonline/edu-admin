package com.eduadmin.system.init;

import com.eduadmin.baseplatform.permission.entity.PermCatalog;
import com.eduadmin.baseplatform.permission.repo.PermCatalogRepository;
import com.eduadmin.baseplatform.role.entity.Role;
import com.eduadmin.baseplatform.role.entity.RolePerm;
import com.eduadmin.baseplatform.role.repo.RolePermRepository;
import com.eduadmin.baseplatform.role.repo.RoleRepository;
import com.eduadmin.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/system/init")
public class DataInitController {

    @Autowired
    private PermCatalogRepository permCatalogRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermRepository rolePermRepository;

    @Autowired
    private com.eduadmin.scheduling.repo.ScheduleRepository scheduleRepository;


    @Autowired
    private com.eduadmin.attendance.repo.AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private com.eduadmin.scheduling.repo.ScheduleShareRepository scheduleShareRepository;

    @Autowired
    private com.eduadmin.course.repo.ClassInfoRepository classInfoRepository;
    @Autowired
    private com.eduadmin.baseplatform.campus.repo.CampusRepository campusRepository;
    @Autowired
    private com.eduadmin.course.repo.GradeDictRepository gradeDictRepository;
    @Autowired
    private com.eduadmin.course.repo.SubjectDictRepository subjectDictRepository;

    @Autowired
    private com.eduadmin.student.grade.repo.StudentGradeRepository studentGradeRepository;

    @Autowired
    private com.eduadmin.student.repo.StudentRepository studentRepository;

    /**
     * 初始化权限目录数据
     */
    @PostMapping("/permissions")
    public ApiResponse<String> initPermissions() {
        // 清空现有权限目录数据
        permCatalogRepository.deleteAll();

        List<PermCatalog> permissions = new ArrayList<>();

        permissions.addAll(Arrays.asList(
            createPermCatalog("menu", "首页", "menu:dashboard", 1),
            createPermCatalog("menu", "考勤管理", "menu:attendance", 2),
            createPermCatalog("menu", "财务管理", "menu:finance", 25),
            createPermCatalog("menu", "报表管理", "menu:report", 26),
            createPermCatalog("menu", "招生报表", "menu:report:enroll", 261),
            createPermCatalog("menu", "教学报表", "menu:report:teaching", 262),
            createPermCatalog("menu", "财务报表", "menu:report:finance", 263),
            createPermCatalog("menu", "试听转化报表", "menu:report:audition", 264),
            createPermCatalog("menu", "收费管理", "menu:finance:settlement:fee", 251),
            createPermCatalog("menu", "续费管理", "menu:finance:settlement:renew", 252),
            createPermCatalog("menu", "退费管理", "menu:finance:settlement:refund", 253),
            createPermCatalog("menu", "审批人配置", "menu:finance:settlement:approver-config", 254),
            createPermCatalog("menu", "开票申请", "menu:finance:invoice:apply", 255),
            createPermCatalog("menu", "发票管理", "menu:finance:invoice:manage", 256),
            createPermCatalog("menu", "开票统计", "menu:finance:invoice:stat", 257),
            createPermCatalog("menu", "课时费规则", "menu:finance:teacher-fee:rule", 258),
            createPermCatalog("menu", "课时费试算", "menu:finance:teacher-fee:trial", 259),
            createPermCatalog("menu", "基础平台", "menu:base", 3),
            createPermCatalog("menu", "校区管理", "menu:base:campus", 5),
            createPermCatalog("menu", "部门管理", "menu:base:dept", 6),
            createPermCatalog("menu", "用户管理", "menu:base:user", 7),
            createPermCatalog("menu", "教师管理", "menu:base:teacher", 8),
            createPermCatalog("menu", "教室管理", "menu:base:classroom", 8),
            createPermCatalog("menu", "角色权限管理", "menu:base:role", 8),
            createPermCatalog("menu", "权限目录管理", "menu:base:perm", 9),
            createPermCatalog("menu", "节假日管理", "menu:base:holiday", 9),
            createPermCatalog("menu", "学生管理", "menu:student", 10),
            createPermCatalog("menu", "学生信息", "menu:student:info", 11),
            createPermCatalog("menu", "学生成绩", "menu:student:grade", 12),
            createPermCatalog("menu", "课程管理", "menu:course", 13),
            createPermCatalog("menu", "课程安排", "menu:course:schedule", 14),
            createPermCatalog("menu", "教学计划", "menu:course:plan", 15),
            createPermCatalog("menu", "家长沟通", "menu:parent", 16),
            createPermCatalog("menu", "通知公告", "menu:notice", 17),
            createPermCatalog("menu", "系统设置", "menu:system", 18),
            createPermCatalog("menu", "用户管理", "menu:system:user", 19),
            createPermCatalog("menu", "系统日志", "menu:system:log", 20)
        ));

        // 功能权限 - 操作权限
        permissions.addAll(Arrays.asList(
            // 基础平台功能权限
            createPermCatalog("button", "新增校区", "base:campus:add", 101),
            createPermCatalog("button", "编辑校区", "base:campus:edit", 102),
            createPermCatalog("button", "删除校区", "base:campus:delete", 103),
            createPermCatalog("button", "启禁用校区", "base:campus:toggle", 104),
            createPermCatalog("button", "查看校区", "base:campus:view", 105),

            createPermCatalog("button", "新增部门", "base:dept:add", 106),
            createPermCatalog("button", "编辑部门", "base:dept:edit", 107),
            createPermCatalog("button", "删除部门", "base:dept:delete", 108),
            createPermCatalog("button", "启禁用部门", "base:dept:toggle", 109),
            createPermCatalog("button", "查看部门", "base:dept:view", 110),

            createPermCatalog("button", "新增教职工", "base:staff:add", 111),
            createPermCatalog("button", "编辑教职工", "base:staff:edit", 112),
            createPermCatalog("button", "删除教职工", "base:staff:delete", 113),
            createPermCatalog("button", "启禁用教职工", "base:staff:toggle", 114),
            createPermCatalog("button", "查看教职工", "base:staff:view", 115),
            createPermCatalog("button", "重置密码", "base:staff:reset", 116),

            // 节假日维护功能权限
            createPermCatalog("button", "查看节假日", "base:holiday:view", 130),
            createPermCatalog("button", "新增节假日", "base:holiday:add", 131),
            createPermCatalog("button", "编辑节假日", "base:holiday:edit", 132),
            createPermCatalog("button", "删除节假日", "base:holiday:delete", 133),

            // 教师管理功能权限
            createPermCatalog("button", "新增教师", "base:teacher:add", 122),
            createPermCatalog("button", "编辑教师", "base:teacher:edit", 123),
            createPermCatalog("button", "删除教师", "base:teacher:delete", 124),
            createPermCatalog("button", "启禁用教师", "base:teacher:toggle", 125),
            createPermCatalog("button", "查看教师", "base:teacher:view", 126),
            createPermCatalog("button", "设置科目", "base:teacher:subjects", 127),
            createPermCatalog("button", "设置班级", "base:teacher:classes", 128),
            createPermCatalog("button", "设置课时费", "base:teacher:rate", 129),
            // 课时费与状态细粒度权限（教师与课时费管理模块）
            createPermCatalog("button", "课时费调整（细粒度）", "teacher:rate:update", 222),
            createPermCatalog("button", "状态切换（细粒度）", "teacher:status:update", 223),

            createPermCatalog("button", "新增角色", "base:role:add", 117),
            createPermCatalog("button", "编辑角色", "base:role:edit", 118),
            createPermCatalog("button", "删除角色", "base:role:delete", 119),
            createPermCatalog("button", "启禁用角色", "base:role:toggle", 120),
            createPermCatalog("button", "分配权限", "base:role:perm", 121),

            // 学生管理功能权限
            createPermCatalog("button", "新增学生", "student:add", 201),
            createPermCatalog("button", "编辑学生", "student:edit", 202),
            createPermCatalog("button", "删除学生", "student:delete", 203),
            createPermCatalog("button", "查看学生", "student:view", 204),
            createPermCatalog("button", "学生转班", "student:transfer", 205),
            createPermCatalog("button", "学生升级", "student:upgrade", 206),

            // 成绩管理功能权限
            createPermCatalog("button", "录入成绩", "grade:add", 207),
            createPermCatalog("button", "修改成绩", "grade:edit", 208),
            createPermCatalog("button", "删除成绩", "grade:delete", 209),
            createPermCatalog("button", "查看成绩", "grade:view", 210),
            createPermCatalog("button", "导出成绩", "grade:export", 211),

            // 课程管理功能权限
            createPermCatalog("button", "新增课程", "course:add", 301),
            createPermCatalog("button", "编辑课程", "course:edit", 302),
            createPermCatalog("button", "删除课程", "course:delete", 303),
            createPermCatalog("button", "查看课程", "course:view", 304),
            createPermCatalog("button", "排课", "course:schedule", 305),
            createPermCatalog("button", "调课", "course:reschedule", 306),

            // 班级管理功能权限（新增）
            createPermCatalog("button", "查看班级", "class:view", 311),
            createPermCatalog("button", "班级管理", "class:manage", 312),
            createPermCatalog("button", "选班申请", "class:select", 313),

            // 考勤管理功能权限
            createPermCatalog("button", "考勤打卡", "attendance:checkin", 401),
            createPermCatalog("button", "考勤查询", "attendance:view", 402),
            createPermCatalog("button", "考勤统计", "attendance:stat", 403),
            createPermCatalog("button", "考勤导出", "attendance:export", 404),

            // 通知公告功能权限
            createPermCatalog("button", "发布通知", "notice:add", 501),
            createPermCatalog("button", "编辑通知", "notice:edit", 502),
            createPermCatalog("button", "删除通知", "notice:delete", 503),
            createPermCatalog("button", "查看通知", "notice:view", 504)
        ));

        // 财务管理-退费相关功能权限
        permissions.addAll(Arrays.asList(
            createPermCatalog("button", "退费申请", "finance:refund:apply", 801),
            createPermCatalog("button", "退费调整", "finance:refund:adjust", 802),
            createPermCatalog("button", "退费审批", "finance:refund:approve", 803),
            createPermCatalog("button", "执行退款", "finance:refund:execute", 804),
            createPermCatalog("button", "查看退费", "finance:refund:view", 805)
        ));

        // 数据范围权限
        permissions.addAll(Arrays.asList(
            createPermCatalog("data", "全部数据", "data:all", 601),
            createPermCatalog("data", "本校区数据", "data:campus", 602),
            createPermCatalog("data", "本部门数据", "data:dept", 603),
            createPermCatalog("data", "个人数据", "data:self", 604)
        ));

        permCatalogRepository.saveAll(permissions);
        return ApiResponse.success("权限目录初始化完成，共创建 " + permissions.size() + " 个权限项");
    }

    @PostMapping("/permissions/finance-fix")
    public ApiResponse<Map<String, Object>> fixFinanceMenuCodes() {
        Map<String, Object> out = new LinkedHashMap<>();
        int created = 0, updated = 0;
        class Item { String label; String value; int sort; Item(String l, String v, int s){label=l;value=v;sort=s;} }
        List<Item> targets = Arrays.asList(
                new Item("收费管理", "menu:finance:settlement:fee", 251),
                new Item("续费管理", "menu:finance:settlement:renew", 252),
                new Item("退费管理", "menu:finance:settlement:refund", 253),
                new Item("审批人配置", "menu:finance:settlement:approver-config", 254),
                new Item("开票申请", "menu:finance:invoice:apply", 255),
                new Item("发票管理", "menu:finance:invoice:manage", 256),
                new Item("开票统计", "menu:finance:invoice:stat", 257),
                new Item("课时费规则", "menu:finance:teacher-fee:rule", 258),
                new Item("课时费试算", "menu:finance:teacher-fee:trial", 259)
        );
        for (Item it : targets) {
            try {
                Optional<PermCatalog> opt = permCatalogRepository.findByValue(it.value);
                if (opt.isPresent()) {
                    PermCatalog pc = opt.get();
                    boolean need = false;
                    if (!"menu".equals(pc.getType())) { pc.setType("menu"); need = true; }
                    if (!Objects.equals(pc.getLabel(), it.label)) { pc.setLabel(it.label); need = true; }
                    if (pc.getSortOrder() == null || pc.getSortOrder() != it.sort) { pc.setSortOrder(it.sort); need = true; }
                    if (need) { permCatalogRepository.save(pc); updated++; }
                } else {
                    PermCatalog pc = new PermCatalog();
                    pc.setType("menu"); pc.setLabel(it.label); pc.setValue(it.value); pc.setSortOrder(it.sort);
                    permCatalogRepository.save(pc); created++;
                }
            } catch (Exception ignore) {}
        }
        out.put("created", created);
        out.put("updated", updated);
        return ApiResponse.success(out);
    }
    @PostMapping("/classinfo-seed")
    public ApiResponse<Map<String, Object>> seedClassInfo(@RequestParam(value = "count", required = false, defaultValue = "20") Integer count) {
        if (count == null || count <= 0) count = 20;
        List<com.eduadmin.baseplatform.campus.entity.Campus> campuses = campusRepository.findAll();
        if (campuses.isEmpty()) {
            com.eduadmin.baseplatform.campus.entity.Campus cp = new com.eduadmin.baseplatform.campus.entity.Campus();
            cp.setName("示例校区"); cp.setStatus("enabled"); campusRepository.save(cp);
            campuses = campusRepository.findAll();
        }
        List<com.eduadmin.course.entity.GradeDict> grades = gradeDictRepository.findAll();
        if (grades.isEmpty()) {
            com.eduadmin.course.entity.GradeDict g1 = new com.eduadmin.course.entity.GradeDict(); g1.setName("一年级"); g1.setCampusId(campuses.get(0).getId()); g1.setStatus("enabled"); g1.setSortOrder(1); gradeDictRepository.save(g1);
            com.eduadmin.course.entity.GradeDict g2 = new com.eduadmin.course.entity.GradeDict(); g2.setName("二年级"); g2.setCampusId(campuses.get(0).getId()); g2.setStatus("enabled"); g2.setSortOrder(2); gradeDictRepository.save(g2);
            grades = gradeDictRepository.findAll();
        }
        String[] rooms = new String[]{"A101","A102","B201","B202"};
        String[] names = new String[]{"数学一班","英语提高班","语文精品班","科学探索班","编程启蒙班","美术基础班"};
        // 科目字典：若为空则初始化常用科目
        List<com.eduadmin.course.entity.SubjectDict> subjects = subjectDictRepository.findAll();
        if (subjects.isEmpty()) {
            String[] subs = new String[]{"语文","数学","英语","科学","编程","美术"};
            int so = 1;
            for (String s : subs) {
                com.eduadmin.course.entity.SubjectDict sd = new com.eduadmin.course.entity.SubjectDict();
                sd.setName(s); sd.setStatus("enabled"); sd.setSortOrder(so++);
                subjectDictRepository.save(sd);
            }
            subjects = subjectDictRepository.findAll();
        }
        java.util.Random rand = new java.util.Random();
        int created = 0;
        for (int i = 0; i < count; i++) {
            com.eduadmin.course.entity.ClassInfo ci = new com.eduadmin.course.entity.ClassInfo();
            com.eduadmin.baseplatform.campus.entity.Campus cp = campuses.get(i % campuses.size());
            com.eduadmin.course.entity.GradeDict gd = grades.get(i % grades.size());
            ci.setCampusId(cp.getId());
            ci.setGradeId(gd.getId());
            ci.setName(names[i % names.length] + "-" + (i+1));
            ci.setStatus("enabled");
            ci.setSortOrder(i+1);
            ci.setMode("线下");
            ci.setStartDate("2025-03-0" + ((i%9)+1));
            ci.setEndDate("2025-07-1" + ((i%8)+1));
            ci.setTerm("2025春季");
            ci.setState("normal");
            ci.setClassroom(rooms[i % rooms.length]);
            ci.setRoom(rooms[i % rooms.length]);
            com.eduadmin.course.entity.SubjectDict sd = subjects.get(i % subjects.size());
            ci.setCourseId(sd.getId());
            ci.setSubject(sd.getName());
            ci.setMaxSize(25 + rand.nextInt(10));
            ci.setFeeStandard((3000 + rand.nextInt(1500)) + "/学期");
            ci.setFeeStatus("未设置");
            classInfoRepository.save(ci);
            created++;
        }
        Map<String, Object> out = new java.util.LinkedHashMap<>(); out.put("created", created); return ApiResponse.success(out);
    }

    /**
     * 生成示例：课表与考勤数据，并记录一条课表分享
     */
    @PostMapping("/schedule-attendance")
    public ApiResponse<Map<String, Object>> initScheduleAndAttendance() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            // 1) 找一个班级或创建示例班级
            com.eduadmin.course.entity.ClassInfo cls = classInfoRepository.findAll().stream().findFirst().orElse(null);
            if (cls == null) {
                cls = new com.eduadmin.course.entity.ClassInfo();
                cls.setName("示例班级-数学一对一");
                cls.setCourseId(1L);
                cls.setFixedTeacherId(1L);
                cls.setFlexibleTeacher(false);
                cls.setExclusiveStudentId(1001L);
                cls.setCampusId(1L);
                cls.setRoom("A101");
                cls.setDurationMinutes(60);
                cls.setMaxSize(1);
                cls.setFee(0.0);
                cls.setStatus("enabled");
                cls = classInfoRepository.save(cls);
            }

            // 2) 生成本周3条示例课表
            int createdSchedules = 0;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            // 设为周一
            cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
            java.text.SimpleDateFormat dFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat dtFmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (int i = 0; i < 3; i++) {
                java.util.Date d = cal.getTime();
                String base = dFmt.format(d);
                java.util.Date startAt = dtFmt.parse(base + " 09:00");
                java.util.Date endAt = dtFmt.parse(base + " 10:00");
                com.eduadmin.scheduling.entity.ScheduleInfo s = new com.eduadmin.scheduling.entity.ScheduleInfo();
                s.setClassId(cls.getId());
                s.setCourseId(cls.getCourseId());
                s.setTeacherId(cls.getFixedTeacherId());
                s.setCampusId(cls.getCampusId());
                s.setRoom(cls.getRoom());
                s.setStudentId(cls.getExclusiveStudentId());
                s.setStartAt(startAt);
                s.setEndAt(endAt);
                s.setDateOnly(d);
                s.setStartTimeText("09:00");
                s.setEndTimeText("10:00");
                s.setStatus("scheduled");
                s.setSource("init_seed");
                s.setCreatedAt(new java.util.Date());
                scheduleRepository.save(s);
                createdSchedules++;
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }

            // 3) 为第一条课表创建3条考勤记录
            java.util.List<com.eduadmin.scheduling.entity.ScheduleInfo> saved = scheduleRepository.findByClassId(cls.getId());
            int createdAttendances = 0;
            if (!saved.isEmpty()) {
                Long firstScheduleId = saved.get(0).getId();
                createdAttendances += createAttend(firstScheduleId, 1001L, 1, "正常");
                createdAttendances += createAttend(firstScheduleId, 1002L, 2, "迟到5分钟");
                createdAttendances += createAttend(firstScheduleId, 1003L, 4, "未到课");
            }

            // 4) 记录一条分享信息
            com.eduadmin.scheduling.entity.ScheduleShare share = new com.eduadmin.scheduling.entity.ScheduleShare();
            share.setDimension("class");
            share.setTargetId(String.valueOf(cls.getId()));
            share.setRangeText(new java.text.SimpleDateFormat("yyyy-MM").format(new java.util.Date()));
            share.setShareToken(java.util.UUID.randomUUID().toString());
            java.util.Calendar exp = java.util.Calendar.getInstance();
            exp.add(java.util.Calendar.DAY_OF_MONTH, 7);
            share.setExpiresAt(exp.getTime());
            share.setCreatedBy("system");
            scheduleShareRepository.save(share);

            out.put("classId", cls.getId());
            out.put("schedulesCreated", createdSchedules);
            out.put("attendanceCreated", createdAttendances);
            out.put("shareToken", share.getShareToken());
            return ApiResponse.success(out);
        } catch (Exception e) {
            out.put("error", e.getMessage());
            return ApiResponse.error(500, "初始化课表/考勤失败: " + e.getMessage());
        }
    }

    private int createAttend(Long scheduleId, Long studentId, int signType, String remark) {
        com.eduadmin.attendance.entity.AttendanceRecord ar = new com.eduadmin.attendance.entity.AttendanceRecord();
        ar.setScheduleId(scheduleId);
        ar.setStudentId(studentId);
        ar.setSignType(signType);
        ar.setRemark(remark);
        ar.setSource("init_seed");
        ar.setSignTime(new java.util.Date());
        attendanceRecordRepository.save(ar);
        return 1;
    }

    @PostMapping("/classinfo-fix")
    public ApiResponse<Map<String, Object>> fixClassInfo() {
        List<com.eduadmin.course.entity.ClassInfo> list = classInfoRepository.findAll();
        int updated = 0;
        for (com.eduadmin.course.entity.ClassInfo ci : list) {
            boolean need = false;
            if (ci.getCourseId() == null) { ci.setCourseId(ci.getId()); need = true; }
            if (ci.getMaxSize() == null) { Integer cap = ci.getCapacityLimit(); ci.setMaxSize(cap == null ? 30 : cap); need = true; }
            if ((ci.getRoom() == null || String.valueOf(ci.getRoom()).trim().isEmpty()) && ci.getClassroom() != null) { ci.setRoom(ci.getClassroom()); need = true; }
            if ((ci.getSubject() == null || ci.getSubject().trim().isEmpty())) {
                try {
                    if (ci.getCourseId() != null) {
                        String sub = subjectDictRepository.findById(ci.getCourseId()).map(s -> s.getName()).orElse(null);
                        if (sub != null) { ci.setSubject(sub); need = true; }
                    } else {
                        // 简单从名称猜测科目
                        String nm = ci.getName() == null ? "" : ci.getName();
                        String[] ks = new String[]{"语文","数学","英语","科学","编程","美术"};
                        for (String k : ks) { if (nm.contains(k)) { ci.setSubject(k); need = true; break; } }
                    }
                } catch (Exception ignore) {}
            }
            if (need) { classInfoRepository.save(ci); updated++; }
        }
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("updated", updated);
        return ApiResponse.success(out);
    }

    @PostMapping("/student-grades")
    public ApiResponse<Map<String, Object>> initStudentGrades(@RequestParam(value = "count", required = false, defaultValue = "20") Integer count) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (count == null || count <= 0) count = 20;
        List<com.eduadmin.student.entity.Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            out.put("created", 0);
            out.put("message", "no students");
            return ApiResponse.success(out);
        }
        String[] subjects = new String[] { "数学", "英语", "语文", "科学" };
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int created = 0;
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < count; i++) {
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
            created++;
        }
        out.put("created", created);
        return ApiResponse.success(out);
    }

    /**
     * 初始化角色数据
     */
    @PostMapping("/roles")
    public ApiResponse<String> initRoles() {
        // 清空现有角色数据
        roleRepository.deleteAll();

        List<Role> roles = Arrays.asList(
            createRole("system_admin", "系统管理员", "enabled"),
            createRole("academic_director", "教务处长", "enabled"),
            createRole("academic_staff", "教务员", "enabled"),
            createRole("teacher", "教师", "enabled"),
            createRole("student", "学生", "enabled"),
            createRole("parent", "家长", "enabled")
        );

        roleRepository.saveAll(roles);
        return ApiResponse.success("角色初始化完成，共创建 " + roles.size() + " 个角色");
    }

    /**
     * 初始化角色权限分配
     */
    @PostMapping("/role-permissions")
    public ApiResponse<String> initRolePermissions() {
        // 清空现有角色权限关联
        rolePermRepository.deleteAll();

        // 获取所有角色
        List<Role> roles = roleRepository.findAll();
        Map<String, Long> roleCodeToId = new HashMap<>();
        for (Role role : roles) {
            roleCodeToId.put(role.getCode(), role.getId());
        }

        List<RolePerm> rolePerms = new ArrayList<>();

        // 系统管理员 - 拥有所有权限
        Long adminRoleId = roleCodeToId.get("system_admin");
        if (adminRoleId != null) {
            rolePerms.add(createRolePerm(adminRoleId, "*"));
        }

        // 教务处长 - 拥有大部分管理权限
        Long directorRoleId = roleCodeToId.get("academic_director");
        if (directorRoleId != null) {
            String[] directorPerms = {
                // 菜单权限
                "menu:dashboard", "menu:attendance", "menu:finance", "menu:report", "menu:base", "menu:base:org", 
                "menu:base:campus", "menu:base:dept", "menu:base:staff", "menu:base:teacher", "menu:base:classroom", "menu:base:role", "menu:base:holiday",
                "menu:student", "menu:student:info", "menu:student:grade",
                "menu:course", "menu:course:schedule", "menu:course:plan",
                "menu:notice", "menu:system", "menu:system:log",
                // 财务管理子菜单
                "menu:finance:settlement:fee", "menu:finance:settlement:renew", "menu:finance:settlement:refund", "menu:finance:settlement:approver-config",
                "menu:finance:teacher-fee:rule", "menu:finance:teacher-fee:trial",
                // 报表管理子菜单
                "menu:report", "menu:report:enroll", "menu:report:teaching", "menu:report:finance", "menu:report:audition",
                // 发票管理菜单
                "menu:finance:invoice:apply", "menu:finance:invoice:manage", "menu:finance:invoice:stat",
                // 功能权限
                "base:campus:add", "base:campus:edit", "base:campus:view", "base:campus:toggle",
                "base:dept:add", "base:dept:edit", "base:dept:view", "base:dept:toggle",
                "base:staff:add", "base:staff:edit", "base:staff:view", "base:staff:toggle", "base:staff:reset",
                "base:teacher:add", "base:teacher:edit", "base:teacher:view", "base:teacher:toggle", "base:teacher:subjects", "base:teacher:classes", "base:teacher:rate",
                // 教师与课时费管理模块的细粒度操作
                "teacher:rate:update", "teacher:status:update",
                "base:role:add", "base:role:edit", "base:role:view", "base:role:perm",
                "student:add", "student:edit", "student:view", "student:transfer", "student:upgrade",
                "grade:view", "grade:export",
                "course:add", "course:edit", "course:view", "course:schedule", "course:reschedule",
                "class:view", "class:manage", "class:select",
                "attendance:view", "attendance:stat", "attendance:export",
                "notice:add", "notice:edit", "notice:view",
                // 退费管理操作
                "finance:refund:view", "finance:refund:approve", "finance:refund:execute", "finance:refund:adjust",
                // 发票管理操作
                "finance:invoice:apply", "finance:invoice:manage", "finance:invoice:stat", "finance:invoice:export",
                // 数据权限
                "data:all"
            };
            for (String perm : directorPerms) {
                rolePerms.add(createRolePerm(directorRoleId, perm));
            }
        }

        // 教务员 - 日常教务管理权限
        Long staffRoleId = roleCodeToId.get("academic_staff");
        if (staffRoleId != null) {
            String[] staffPerms = {
                // 菜单权限
                "menu:dashboard", "menu:attendance", "menu:finance", "menu:report", "menu:base", "menu:base:org", "menu:base:teacher", "menu:base:classroom",
                "menu:student", "menu:student:info", "menu:student:grade",
                "menu:course", "menu:course:schedule",
                "menu:notice",
                // 财务管理子菜单
                "menu:finance:settlement:fee", "menu:finance:settlement:renew", "menu:finance:settlement:refund",
                "menu:finance:teacher-fee:rule", "menu:finance:teacher-fee:trial",
                // 报表管理子菜单
                "menu:report", "menu:report:enroll", "menu:report:teaching", "menu:report:finance", "menu:report:audition",
                // 功能权限
                "base:staff:view", "base:dept:view", "base:campus:view",
                "base:teacher:view", "base:teacher:subjects", "base:teacher:classes",
                "base:holiday:view", "base:holiday:add", "base:holiday:edit", "base:holiday:delete",
                "student:add", "student:edit", "student:view", "student:transfer",
                "grade:add", "grade:edit", "grade:view", "grade:export",
                "course:view", "course:schedule",
                "class:view", "class:manage", "class:select",
                "attendance:view", "attendance:stat",
                "notice:view",
                // 退费管理操作
                "finance:refund:view", "finance:refund:apply", "finance:refund:adjust",
                // 数据权限
                "data:campus"
            };
            for (String perm : staffPerms) {
                rolePerms.add(createRolePerm(staffRoleId, perm));
            }
        }

        // 教师 - 教学相关权限
        Long teacherRoleId = roleCodeToId.get("teacher");
        if (teacherRoleId != null) {
            String[] teacherPerms = {
                // 菜单权限
                "menu:dashboard", "menu:attendance",
                "menu:student", "menu:student:info", "menu:student:grade",
                "menu:course", "menu:course:schedule",
                "menu:notice",
                // 功能权限
                "student:view",
                "grade:add", "grade:edit", "grade:view",
                "course:view", "class:view", "base:holiday:view",
                "attendance:checkin", "attendance:view",
                "notice:view",
                // 数据权限
                "data:dept"
            };
            for (String perm : teacherPerms) {
                rolePerms.add(createRolePerm(teacherRoleId, perm));
            }
        }

        // 学生 - 基本查看权限
        Long studentRoleId = roleCodeToId.get("student");
        if (studentRoleId != null) {
            String[] studentPerms = {
                // 菜单权限
                "menu:dashboard", "menu:attendance",
                "menu:student:grade", "menu:course:schedule",
                "menu:notice",
                // 功能权限
                "grade:view", "course:view", "class:select",
                "attendance:checkin", "attendance:view",
                "notice:view",
                // 数据权限
                "data:self"
            };
            for (String perm : studentPerms) {
                rolePerms.add(createRolePerm(studentRoleId, perm));
            }
        }

        // 家长 - 查看子女相关信息
        Long parentRoleId = roleCodeToId.get("parent");
        if (parentRoleId != null) {
            String[] parentPerms = {
                // 菜单权限
                "menu:dashboard", "menu:attendance",
                "menu:student:grade", "menu:course:schedule",
                "menu:notice", "menu:parent",
                // 功能权限
                "grade:view", "course:view",
                "attendance:view",
                "notice:view",
                // 数据权限
                "data:self"
            };
            for (String perm : parentPerms) {
                rolePerms.add(createRolePerm(parentRoleId, perm));
            }
        }

        rolePermRepository.saveAll(rolePerms);
        return ApiResponse.success("角色权限分配完成，共创建 " + rolePerms.size() + " 个权限关联");
    }

    /**
     * 一键初始化所有数据
     */
    @PostMapping("/all")
    public ApiResponse<Map<String, String>> initAll() {
        Map<String, String> results = new HashMap<>();
        // 先进行一次清理，移除历史遗留的顶栏“教师管理”权限及角色授权
        try {
            Optional<PermCatalog> legacy = permCatalogRepository.findByValue("menu:teacher");
            if (legacy.isPresent()) {
                rolePermRepository.deleteByValue("menu:teacher");
                permCatalogRepository.deleteById(legacy.get().getId());
            }
            results.put("cleanup", "已移除历史权限：menu:teacher 及其角色授权");
        } catch (Exception e) {
            results.put("cleanup", "清理失败: " + e.getMessage());
        }
        
        try {
            ApiResponse<String> permResult = initPermissions();
            results.put("permissions", permResult.getData());
        } catch (Exception e) {
            results.put("permissions", "初始化失败: " + e.getMessage());
        }

        try {
            Optional<PermCatalog> legacyFee = permCatalogRepository.findByValue("menu:teacher-fee");
            if (legacyFee.isPresent()) {
                rolePermRepository.deleteByValue("menu:teacher-fee");
                permCatalogRepository.deleteById(legacyFee.get().getId());
                results.put("cleanup_teacher_fee", "已移除历史权限：menu:teacher-fee 及其角色授权");
            }
        } catch (Exception e) {
            results.put("cleanup_teacher_fee", "清理失败: " + e.getMessage());
        }

        try {
            ApiResponse<String> roleResult = initRoles();
            results.put("roles", roleResult.getData());
        } catch (Exception e) {
            results.put("roles", "初始化失败: " + e.getMessage());
        }

        try {
            ApiResponse<String> rolePermResult = initRolePermissions();
            results.put("rolePermissions", rolePermResult.getData());
        } catch (Exception e) {
            results.put("rolePermissions", "初始化失败: " + e.getMessage());
        }

        return ApiResponse.success(results);
    }

    private PermCatalog createPermCatalog(String type, String label, String value, int sortOrder) {
        PermCatalog perm = new PermCatalog();
        perm.setType(type);
        perm.setLabel(label);
        perm.setValue(value);
        perm.setSortOrder(sortOrder);
        return perm;
    }

    private Role createRole(String code, String name, String status) {
        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setStatus(status);
        return role;
    }

    private RolePerm createRolePerm(Long roleId, String value) {
        RolePerm rolePerm = new RolePerm();
        rolePerm.setRoleId(roleId);
        rolePerm.setValue(value);
        return rolePerm;
    }

    /**
     * 生成未来N天的示例排课数据（用于首页展示更饱满）
     */
    @PostMapping("/upcoming-schedules")
    public ApiResponse<Map<String, Object>> initUpcomingSchedules(
            @RequestParam(value = "days", required = false, defaultValue = "7") Integer days
    ) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            if (days == null || days <= 0) days = 7;
            // 找一个班级或创建示例班级
            com.eduadmin.course.entity.ClassInfo cls = classInfoRepository.findAll().stream().findFirst().orElse(null);
            if (cls == null) {
                cls = new com.eduadmin.course.entity.ClassInfo();
                cls.setName("示例班级-数学一对一");
                cls.setCourseId(1L);
                cls.setFixedTeacherId(1L);
                cls.setFlexibleTeacher(false);
                cls.setExclusiveStudentId(1001L);
                cls.setCampusId(1L);
                cls.setRoom("A101");
                cls.setDurationMinutes(60);
                cls.setMaxSize(1);
                cls.setFee(0.0);
                cls.setStatus("enabled");
                cls = classInfoRepository.save(cls);
            }

            java.text.SimpleDateFormat dayFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat dtFmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);

            int created = 0;
            for (int i = 0; i < days; i++) {
                java.util.Date dateOnly = cal.getTime();
                String base = dayFmt.format(dateOnly);
                // 设置每天固定时段（示例：14:00~15:00）
                java.util.Date startAt = dtFmt.parse(base + " 14:00");
                java.util.Date endAt = dtFmt.parse(base + " 15:00");

                com.eduadmin.scheduling.entity.ScheduleInfo s = new com.eduadmin.scheduling.entity.ScheduleInfo();
                s.setClassId(cls.getId());
                s.setCourseId(cls.getCourseId());
                s.setTeacherId(cls.getFixedTeacherId());
                s.setCampusId(cls.getCampusId());
                s.setRoom(cls.getRoom());
                s.setStudentId(cls.getExclusiveStudentId());
                s.setStartAt(startAt);
                s.setEndAt(endAt);
                s.setDateOnly(dateOnly);
                s.setStartTimeText("14:00");
                s.setEndTimeText("15:00");
                s.setStatus("scheduled");
                s.setSource("init_upcoming_seed");
                s.setCreatedAt(new java.util.Date());
                scheduleRepository.save(s);
                created++;
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }

            out.put("classId", cls.getId());
            out.put("schedulesCreated", created);
            out.put("range", days + "天");
            return ApiResponse.success(out);
        } catch (Exception e) {
            out.put("error", e.getMessage());
            return ApiResponse.error(500, "初始化未来排课失败: " + e.getMessage());
        }
    }

    /**
     * 回填 class_dict 表中的空字段为默认值，避免前端列表出现空白
     */
    @PostMapping("/fix/class-info-backfill")
    public ApiResponse<Map<String, Object>> backfillClassInfoNulls() {
        int updated = classInfoRepository.backfillNullsWithDefaults();
        Map<String, Object> out = new HashMap<>();
        out.put("updated", updated);
        return ApiResponse.success(out);
    }

}