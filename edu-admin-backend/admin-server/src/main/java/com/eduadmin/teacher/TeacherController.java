package com.eduadmin.teacher;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.teacher.entity.Teacher;
import com.eduadmin.teacher.repo.TeacherRepository;
import com.eduadmin.baseplatform.campus.entity.Campus;
import com.eduadmin.baseplatform.campus.repo.CampusRepository;
import com.eduadmin.baseplatform.org.entity.Dept;
import com.eduadmin.baseplatform.org.repo.DeptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private DeptRepository deptRepository;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam(value = "status", required = false) String status,
                                                 @RequestParam(value = "q", required = false) String keyword,
                                                 @RequestParam(value = "campusId", required = false) Long campusId,
                                                 @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<Teacher> found;
        if (campusId != null) {
            if (status != null && !status.isEmpty()) {
                found = teacherRepo.findByCampusIdAndStatus(campusId, status);
            } else {
                found = teacherRepo.findByCampusId(campusId);
            }
            String k = keyword == null ? null : keyword.trim();
            if (k != null && !k.isEmpty()) {
                List<Teacher> filtered = new ArrayList<>();
                for (Teacher t : found) {
                    String name = t.getName() == null ? "" : t.getName();
                    if (name.contains(k)) filtered.add(t);
                }
                found = filtered;
            }
        } else if (keyword != null && !keyword.isEmpty()) {
            found = teacherRepo.findByNameContaining(keyword);
        } else if (status != null && !status.isEmpty()) {
            found = teacherRepo.findByStatus(status);
        } else {
            found = teacherRepo.findAll();
        }
        // 按新增时间降序（使用自增ID近似）
        found.sort((a, b) -> Long.compare(b.getId() == null ? 0L : b.getId(), a.getId() == null ? 0L : a.getId()));
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int total = found.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Teacher> pageList = found.subList(from, to);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Teacher t : pageList) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("staffId", t.getStaffId());
            m.put("name", t.getName());
            m.put("subjects", t.getSubjects());
            m.put("classes", t.getClasses());
            m.put("hourlyRate", t.getHourlyRate());
            m.put("status", t.getStatus());
            m.put("gender", t.getGender());
            m.put("mobile", t.getMobile());
            m.put("joinDate", t.getJoinDate());
            m.put("campusId", t.getCampusId());
            m.put("level", t.getLevel());
            m.put("qualificationCertNo", t.getQualificationCertNo());
            m.put("trainingExperience", t.getTrainingExperience());
            m.put("deptId", t.getDeptId());
            String campusName = null;
            if (t.getCampusId() != null) {
                Optional<Campus> c = campusRepository.findById(t.getCampusId());
                campusName = c.map(Campus::getName).orElse(null);
            }
            String deptName = null;
            if (t.getDeptId() != null) {
                Optional<Dept> d = deptRepository.findById(t.getDeptId());
                deptName = d.map(Dept::getTitle).orElse(null);
            }
            m.put("campusName", campusName);
            m.put("deptName", deptName);
            items.add(m);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("items", items);
        out.put("total", total);
        out.put("page", page);
        out.put("size", size);
        out.put("pages", (int) Math.ceil((double) total / size));
        return ApiResponse.success(out);
    }

    @PostMapping("/save")
    public ApiResponse<Map<String, Object>> save(@RequestBody Map<String, Object> payload) {
        Teacher t = new Teacher();
        t.setName(String.valueOf(payload.getOrDefault("name", "")));
        Object staffIdObj = payload.get("staffId");
        t.setStaffId(staffIdObj == null ? null : Long.valueOf(String.valueOf(staffIdObj)));
        t.setSubjects(String.valueOf(payload.getOrDefault("subjects", "")));
        t.setClasses(String.valueOf(payload.getOrDefault("classes", "")));
        Object hrObj = payload.get("hourlyRate");
        if (hrObj != null && !String.valueOf(hrObj).isEmpty()) {
            t.setHourlyRate(new BigDecimal(String.valueOf(hrObj)));
        }
        t.setStatus(String.valueOf(payload.getOrDefault("status", "enabled")));
        // 新增字段
        t.setGender(String.valueOf(payload.getOrDefault("gender", null)));
        t.setMobile(String.valueOf(payload.getOrDefault("mobile", null)));
        Object jdObj = payload.get("joinDate");
        if (jdObj != null) {
            try {
                if (jdObj instanceof Date) t.setJoinDate((Date) jdObj);
                else {
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    t.setJoinDate(df.parse(String.valueOf(jdObj)));
                }
            } catch (Exception ignore) {}
        }
        Object campusObj = payload.get("campusId");
        t.setCampusId(campusObj == null ? null : Long.valueOf(String.valueOf(campusObj)));
        t.setLevel(String.valueOf(payload.getOrDefault("level", null)));
        t.setQualificationCertNo(String.valueOf(payload.getOrDefault("qualificationCertNo", null)));
        t.setTrainingExperience(String.valueOf(payload.getOrDefault("trainingExperience", null)));
        Object deptObj = payload.get("deptId");
        if (deptObj != null) {
            try { t.setDeptId(Long.valueOf(String.valueOf(deptObj))); } catch (Exception ignore) {}
        }
        Teacher saved = teacherRepo.save(t);
        return ApiResponse.success(toView(saved));
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<Teacher> opt = teacherRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "教师不存在");
        Teacher t = opt.get();
        if (payload.containsKey("name")) t.setName(String.valueOf(payload.get("name")));
        if (payload.containsKey("staffId")) {
            Object staffIdObj = payload.get("staffId");
            t.setStaffId(staffIdObj == null ? null : Long.valueOf(String.valueOf(staffIdObj)));
        }
        if (payload.containsKey("subjects")) t.setSubjects(String.valueOf(payload.get("subjects")));
        if (payload.containsKey("classes")) t.setClasses(String.valueOf(payload.get("classes")));
        if (payload.containsKey("hourlyRate")) {
            Object hrObj = payload.get("hourlyRate");
            if (hrObj == null || String.valueOf(hrObj).isEmpty()) t.setHourlyRate(null);
            else t.setHourlyRate(new BigDecimal(String.valueOf(hrObj)));
        }
        if (payload.containsKey("status")) t.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("gender")) t.setGender(String.valueOf(payload.get("gender")));
        if (payload.containsKey("mobile")) t.setMobile(String.valueOf(payload.get("mobile")));
        if (payload.containsKey("joinDate")) {
            Object jdObj = payload.get("joinDate");
            try {
                if (jdObj instanceof Date) t.setJoinDate((Date) jdObj);
                else {
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    t.setJoinDate(df.parse(String.valueOf(jdObj)));
                }
            } catch (Exception ignore) {}
        }
        if (payload.containsKey("campusId")) {
            Object campusObj = payload.get("campusId");
            t.setCampusId(campusObj == null ? null : Long.valueOf(String.valueOf(campusObj)));
        }
        if (payload.containsKey("level")) t.setLevel(String.valueOf(payload.get("level")));
        if (payload.containsKey("qualificationCertNo")) t.setQualificationCertNo(String.valueOf(payload.get("qualificationCertNo")));
        if (payload.containsKey("trainingExperience")) t.setTrainingExperience(String.valueOf(payload.get("trainingExperience")));
        if (payload.containsKey("deptId")) {
            Object deptObj = payload.get("deptId");
            try { t.setDeptId(deptObj == null ? null : Long.valueOf(String.valueOf(deptObj))); } catch (Exception ignore) {}
        }
        Teacher saved = teacherRepo.save(t);
        return ApiResponse.success(toView(saved));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        if (!teacherRepo.existsById(id)) return ApiResponse.error(404, "教师不存在");
        teacherRepo.deleteById(id);
        return ApiResponse.success(true);
    }

    @PostMapping("/clear")
    public ApiResponse<Integer> clearAll() {
        int count = (int) teacherRepo.count();
        teacherRepo.deleteAll();
        return ApiResponse.success(count);
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Optional<Teacher> opt = teacherRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "教师不存在");
        return ApiResponse.success(toView(opt.get()));
    }

    @PostMapping("/status/enable-all")
    public ApiResponse<Integer> enableAll() {
        List<Teacher> list = teacherRepo.findAll();
        int count = 0;
        for (Teacher t : list) {
            if (!"enabled".equals(t.getStatus())) {
                t.setStatus("enabled");
                count++;
            }
        }
        teacherRepo.saveAll(list);
        return ApiResponse.success(count);
    }

    @PostMapping("/import/excel")
    public ApiResponse<Map<String, Object>> importTeachersExcel(@RequestParam("file") MultipartFile file) {
        int ok = 0; int fail = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rows; i++) {
                org.apache.poi.ss.usermodel.Row r = sheet.getRow(i);
                if (r == null) continue;
                try {
                    String name = getCellStr(r, 0);
                    String staffIdStr = getCellStr(r, 1);
                    String subjects = getCellStr(r, 2);
                    String classes = getCellStr(r, 3);
                    String mobile = getCellStr(r, 4);
                    String gender = getCellStr(r, 5);
                    String level = getCellStr(r, 6);
                    String campusIdStr = getCellStr(r, 7);
                    String joinDateStr = getCellStr(r, 8);
                    java.math.BigDecimal hourlyRate = null;
                    try { hourlyRate = new java.math.BigDecimal(getCellStr(r, 9)); } catch (Exception ignore) {}

                    if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("姓名不能为空");
                    if (mobile != null && !mobile.isEmpty() && !mobile.matches("^1[3-9]\\d{9}$")) throw new IllegalArgumentException("手机号格式不合法");
                    Teacher t = new Teacher();
                    t.setName(name);
                    if (staffIdStr != null && !staffIdStr.trim().isEmpty()) {
                        try { t.setStaffId(Long.valueOf(staffIdStr.trim())); } catch (Exception ignore) {}
                    }
                    t.setSubjects(subjects);
                    t.setClasses(classes);
                    t.setMobile(mobile);
                    t.setGender(gender);
                    t.setLevel(level);
                    if (campusIdStr != null && !campusIdStr.trim().isEmpty()) {
                        try { t.setCampusId(Long.valueOf(campusIdStr.trim())); } catch (Exception ignore) {}
                    }
                    if (hourlyRate != null) t.setHourlyRate(hourlyRate);
                    if (joinDateStr != null && !joinDateStr.trim().isEmpty()) {
                        try {
                            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                            t.setJoinDate(df.parse(joinDateStr.trim()));
                        } catch (Exception ignore) {}
                    }
                    teacherRepo.save(t);
                    ok++;
                } catch (Exception e) {
                    fail++;
                    Map<String, Object> err = new HashMap<>();
                    err.put("row", i + 1);
                    err.put("error", e.getMessage());
                    errors.add(err);
                }
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "导入失败: " + e.getMessage());
        }
        Map<String, Object> out = new HashMap<>();
        out.put("success", ok);
        out.put("failed", fail);
        out.put("errors", errors);
        return ApiResponse.success(out);
    }

    private String getCellStr(org.apache.poi.ss.usermodel.Row r, int idx) {
        org.apache.poi.ss.usermodel.Cell c = r.getCell(idx);
        if (c == null) return null;
        switch (c.getCellType()) {
            case STRING: return c.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(c)) {
                    java.util.Date d = c.getDateCellValue();
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return df.format(d);
                }
                return new java.math.BigDecimal(c.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
            case FORMULA:
                try { return c.getStringCellValue(); } catch (Exception ignore) { return String.valueOf(c.getNumericCellValue()); }
            default: return null;
        }
    }

    private Map<String, Object> toView(Teacher t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("staffId", t.getStaffId());
        m.put("name", t.getName());
        m.put("subjects", t.getSubjects());
        m.put("classes", t.getClasses());
        m.put("hourlyRate", t.getHourlyRate());
        m.put("status", t.getStatus());
        m.put("gender", t.getGender());
        m.put("mobile", t.getMobile());
        m.put("joinDate", t.getJoinDate());
        m.put("campusId", t.getCampusId());
        m.put("level", t.getLevel());
        m.put("qualificationCertNo", t.getQualificationCertNo());
        m.put("trainingExperience", t.getTrainingExperience());
        m.put("deptId", t.getDeptId());
        String campusName = null;
        if (t.getCampusId() != null) {
            Optional<Campus> c = campusRepository.findById(t.getCampusId());
            campusName = c.map(Campus::getName).orElse(null);
        }
        String deptName = null;
        if (t.getDeptId() != null) {
            Optional<Dept> d = deptRepository.findById(t.getDeptId());
            deptName = d.map(Dept::getTitle).orElse(null);
        }
        m.put("campusName", campusName);
        m.put("deptName", deptName);
        return m;
    }
}