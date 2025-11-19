package com.eduadmin.teacher.controller;

import com.eduadmin.common.api.ApiResponse;
import com.eduadmin.baseplatform.campus.entity.Campus;
import com.eduadmin.baseplatform.campus.repo.CampusRepository;
import com.eduadmin.teacher.entity.TeacherFeeRule;
import com.eduadmin.teacher.repository.TeacherFeeRuleRepository;
import com.eduadmin.teacher.service.TeacherFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/teacher/fee")
public class TeacherFeeController {

    @Autowired
    private TeacherFeeRuleRepository ruleRepo;

    @Autowired
    private TeacherFeeService feeService;

    @Autowired
    private CampusRepository campusRepository;

    // 联动建议：根据校区与教师等级返回基础课时费（无则给默认值）
    @GetMapping("/rule/suggest")
    public ApiResponse<Map<String, Object>> suggest(@RequestParam(value = "campusId", required = false) Long campusId,
                                                    @RequestParam("teacherLevel") String teacherLevel) {
        TeacherFeeRule rule = campusId == null
                ? ruleRepo.findFirstByTeacherLevel(teacherLevel).orElse(null)
                : ruleRepo.findFirstByCampusIdAndTeacherLevel(campusId, teacherLevel).orElse(null);
        Map<String, Object> out = new HashMap<>();
        if (rule != null && rule.getBaseFeePerLesson() != null) {
            out.put("baseFeePerLesson", rule.getBaseFeePerLesson());
            out.put("source", "rule");
        } else {
            out.put("baseFeePerLesson", new java.math.BigDecimal("120"));
            out.put("source", "default");
        }
        return ApiResponse.success(out);
    }

    // 规则配置保存
    @PostMapping("/rule/save")
    public ApiResponse<Map<String, Object>> saveRule(@RequestBody Map<String, Object> payload) {
        TeacherFeeRule rule = new TeacherFeeRule();
        Object campusId = payload.get("campusId");
        rule.setCampusId(campusId == null ? null : Long.valueOf(String.valueOf(campusId)));
        rule.setTeacherLevel(String.valueOf(payload.get("teacherLevel")));
        rule.setBaseFeePerLesson(new BigDecimal(String.valueOf(payload.getOrDefault("baseFeePerLesson", "0"))));
        rule.setOneToOneFactor(new BigDecimal(String.valueOf(payload.getOrDefault("oneToOneFactor", "1.50"))));
        rule.setIeltsFactor(new BigDecimal(String.valueOf(payload.getOrDefault("ieltsFactor", "1.20"))));
        rule.setLargeClassThreshold(Integer.valueOf(String.valueOf(payload.getOrDefault("largeClassThreshold", "20"))));
        rule.setLargeClassFactor(new BigDecimal(String.valueOf(payload.getOrDefault("largeClassFactor", "1.10"))));
        rule.setHolidayFactor(new BigDecimal(String.valueOf(payload.getOrDefault("holidayFactor", "2.00"))));
        rule.setSubstituteExtra(new BigDecimal(String.valueOf(payload.getOrDefault("substituteExtra", "30"))));
        rule.setTierMonthlyThreshold(Integer.valueOf(String.valueOf(payload.getOrDefault("tierMonthlyThreshold", "80"))));
        rule.setTierFactor(new BigDecimal(String.valueOf(payload.getOrDefault("tierFactor", "1.20"))));
        TeacherFeeRule saved = ruleRepo.save(rule);
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        return ApiResponse.success(resp);
    }

    // 规则列表：支持按校区筛选与分页（每页默认10条，可选10/20/50）
    @GetMapping("/rule/list")
    public ApiResponse<Map<String, Object>> listRules(
            @RequestParam(value = "campusId", required = false) Long campusId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        java.util.List<com.eduadmin.teacher.entity.TeacherFeeRule> all;
        if (campusId != null) all = ruleRepo.findByCampusId(campusId);
        else all = ruleRepo.findAll();
        // 按 id 倒序展示，最近配置在前
        all.sort((a, b) -> Long.compare(b.getId() == null ? 0L : b.getId(), a.getId() == null ? 0L : a.getId()));
        int total = all.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        java.util.List<com.eduadmin.teacher.entity.TeacherFeeRule> pageList = all.subList(from, to);

        java.util.Map<Long, String> campusNameMap = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        for (com.eduadmin.teacher.entity.TeacherFeeRule r : pageList) {
            Long cid = r.getCampusId();
            String cname = "全局";
            if (cid != null) {
                if (!campusNameMap.containsKey(cid)) {
                    cname = campusRepository.findById(cid).map(Campus::getName).orElse("");
                    campusNameMap.put(cid, cname);
                } else {
                    cname = campusNameMap.get(cid);
                }
            }
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("campusId", r.getCampusId());
            m.put("campusName", cname);
            m.put("teacherLevel", r.getTeacherLevel());
            m.put("baseFeePerLesson", r.getBaseFeePerLesson());
            m.put("oneToOneFactor", r.getOneToOneFactor());
            m.put("ieltsFactor", r.getIeltsFactor());
            m.put("largeClassThreshold", r.getLargeClassThreshold());
            m.put("largeClassFactor", r.getLargeClassFactor());
            m.put("holidayFactor", r.getHolidayFactor());
            m.put("substituteExtra", r.getSubstituteExtra());
            m.put("tierMonthlyThreshold", r.getTierMonthlyThreshold());
            m.put("tierFactor", r.getTierFactor());
            items.add(m);
        }
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("items", items);
        out.put("total", total);
        out.put("page", page);
        out.put("size", size);
        return ApiResponse.success(out);
    }

    @PutMapping("/rule/update/{id}")
    public ApiResponse<Map<String, Object>> updateRule(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Optional<TeacherFeeRule> opt = ruleRepo.findById(id);
        if (!opt.isPresent()) return ApiResponse.error(404, "规则不存在");
        TeacherFeeRule r = opt.get();
        if (payload.containsKey("campusId")) {
            Object v = payload.get("campusId");
            r.setCampusId(v == null ? null : Long.valueOf(String.valueOf(v)));
        }
        if (payload.containsKey("teacherLevel")) r.setTeacherLevel(String.valueOf(payload.get("teacherLevel")));
        if (payload.containsKey("baseFeePerLesson")) {
            Object v = payload.get("baseFeePerLesson");
            r.setBaseFeePerLesson(v == null ? null : new BigDecimal(String.valueOf(v)));
        }
        if (payload.containsKey("oneToOneFactor")) {
            Object v = payload.get("oneToOneFactor");
            r.setOneToOneFactor(v == null ? null : new BigDecimal(String.valueOf(v)));
        }
        if (payload.containsKey("ieltsFactor")) {
            Object v = payload.get("ieltsFactor");
            r.setIeltsFactor(v == null ? null : new BigDecimal(String.valueOf(v)));
        }
        if (payload.containsKey("largeClassThreshold")) {
            Object v = payload.get("largeClassThreshold");
            r.setLargeClassThreshold(v == null ? null : Integer.valueOf(String.valueOf(v)));
        }
        if (payload.containsKey("largeClassFactor")) {
            Object v = payload.get("largeClassFactor");
            r.setLargeClassFactor(v == null ? null : new BigDecimal(String.valueOf(v)));
        }
        if (payload.containsKey("holidayFactor")) {
            Object v = payload.get("holidayFactor");
            r.setHolidayFactor(v == null ? null : new BigDecimal(String.valueOf(v)));
        }
        if (payload.containsKey("substituteExtra")) {
            Object v = payload.get("substituteExtra");
            r.setSubstituteExtra(v == null ? null : new BigDecimal(String.valueOf(v)));
        }
        if (payload.containsKey("tierMonthlyThreshold")) {
            Object v = payload.get("tierMonthlyThreshold");
            r.setTierMonthlyThreshold(v == null ? null : Integer.valueOf(String.valueOf(v)));
        }
        if (payload.containsKey("tierFactor")) {
            Object v = payload.get("tierFactor");
            r.setTierFactor(v == null ? null : new BigDecimal(String.valueOf(v)));
        }
        TeacherFeeRule saved = ruleRepo.save(r);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", saved.getId());
        return ApiResponse.success(out);
    }

    // 课时费试算：支持传入一组课时，遵循优先级：单课时手动调整＞特殊规则＞基础规则
    @PostMapping("/calculate")
    public ApiResponse<Map<String, Object>> calculate(@RequestBody Map<String, Object> payload) {
        Object tid = payload.get("teacherId");
        Long teacherId = (tid == null || String.valueOf(tid).trim().isEmpty()) ? null : Long.valueOf(String.valueOf(tid));
        String month = String.valueOf(payload.getOrDefault("month", ""));
        String teacherLevel = String.valueOf(payload.getOrDefault("teacherLevel", "中级"));
        Long campusId = payload.get("campusId") == null ? null : Long.valueOf(String.valueOf(payload.get("campusId")));
        List<Map<String, Object>> lessons = (List<Map<String, Object>>) payload.getOrDefault("lessons", Collections.emptyList());
        int monthlyTotalLessons = Integer.parseInt(String.valueOf(payload.getOrDefault("monthlyTotalLessons", lessons.size())));

        Optional<TeacherFeeRule> optRule = campusId == null
                ? ruleRepo.findFirstByTeacherLevel(teacherLevel)
                : ruleRepo.findFirstByCampusIdAndTeacherLevel(campusId, teacherLevel);

        TeacherFeeRule rule = optRule.orElseGet(() -> {
            TeacherFeeRule r = new TeacherFeeRule();
            r.setBaseFeePerLesson(new BigDecimal("120"));
            r.setOneToOneFactor(new BigDecimal("1.50"));
            r.setIeltsFactor(new BigDecimal("1.20"));
            r.setLargeClassThreshold(20);
            r.setLargeClassFactor(new BigDecimal("1.10"));
            r.setHolidayFactor(new BigDecimal("2.00"));
            r.setSubstituteExtra(new BigDecimal("30"));
            r.setTierMonthlyThreshold(80);
            r.setTierFactor(new BigDecimal("1.20"));
            return r;
        });

        BigDecimal baseAmount = BigDecimal.ZERO;
        BigDecimal holidayExtra = BigDecimal.ZERO;
        BigDecimal substituteExtra = BigDecimal.ZERO;
        int totalLessons = 0;

        for (Map<String, Object> l : lessons) {
            totalLessons++;
            String courseType = String.valueOf(l.getOrDefault("courseType", "班课")); // 班课/一对一
            String courseCategory = String.valueOf(l.getOrDefault("courseCategory", "常规英语")); // 常规英语/雅思
            int classSize = Integer.parseInt(String.valueOf(l.getOrDefault("classSize", "0")));
            boolean isHoliday = Boolean.parseBoolean(String.valueOf(l.getOrDefault("isHoliday", false)));
            boolean isSubstitute = Boolean.parseBoolean(String.valueOf(l.getOrDefault("isSubstitute", false)));
            BigDecimal manualAdjusted = l.get("adjustedFee") == null ? null : new BigDecimal(String.valueOf(l.get("adjustedFee")));

            // 单课时手动调整优先
            if (manualAdjusted != null) {
                baseAmount = baseAmount.add(manualAdjusted);
                continue;
            }

            BigDecimal fee = rule.getBaseFeePerLesson() == null ? BigDecimal.ZERO : rule.getBaseFeePerLesson();
            if ("一对一".equals(courseType)) {
                BigDecimal f = rule.getOneToOneFactor() == null ? BigDecimal.ONE : rule.getOneToOneFactor();
                fee = fee.multiply(f);
            }
            if ("雅思".equals(courseCategory)) {
                BigDecimal f = rule.getIeltsFactor() == null ? BigDecimal.ONE : rule.getIeltsFactor();
                fee = fee.multiply(f);
            }
            if (classSize > (rule.getLargeClassThreshold() == null ? 0 : rule.getLargeClassThreshold())) {
                BigDecimal f = rule.getLargeClassFactor() == null ? BigDecimal.ONE : rule.getLargeClassFactor();
                fee = fee.multiply(f);
            }
            if (isHoliday) {
                BigDecimal f = rule.getHolidayFactor() == null ? BigDecimal.ONE : rule.getHolidayFactor();
                fee = fee.multiply(f);
                holidayExtra = holidayExtra.add(rule.getBaseFeePerLesson() == null ? BigDecimal.ZERO : rule.getBaseFeePerLesson());
            }
            if (isSubstitute) {
                substituteExtra = substituteExtra.add(rule.getSubstituteExtra() == null ? BigDecimal.ZERO : rule.getSubstituteExtra());
            }
            baseAmount = baseAmount.add(fee);
        }

        // 阶梯奖励：月课时 > 阈值，超额部分 ×(tierFactor - 1)
        BigDecimal tierBonus = BigDecimal.ZERO;
        int threshold = rule.getTierMonthlyThreshold() == null ? 0 : rule.getTierMonthlyThreshold();
        if (monthlyTotalLessons > threshold) {
            int extraLessons = monthlyTotalLessons - threshold;
            BigDecimal perLessonBase = rule.getBaseFeePerLesson() == null ? BigDecimal.ZERO : rule.getBaseFeePerLesson();
            BigDecimal tf = rule.getTierFactor() == null ? BigDecimal.ONE : rule.getTierFactor();
            BigDecimal extra = perLessonBase.multiply(new BigDecimal(extraLessons)).multiply(tf.subtract(BigDecimal.ONE));
            tierBonus = tierBonus.add(extra);
        }

        BigDecimal finalAmount = baseAmount.add(tierBonus).add(substituteExtra);
        Map<String, Object> result = new HashMap<>();
        result.put("teacherId", teacherId);
        result.put("month", month);
        result.put("totalLessons", totalLessons);
        result.put("baseAmount", baseAmount);
        result.put("tierBonus", tierBonus);
        result.put("holidayExtra", holidayExtra);
        result.put("substituteExtra", substituteExtra);
        result.put("finalAmount", finalAmount);
        return ApiResponse.success(result);
    }

    // 明细查询：按教师 / 月份 / 课程类型
    @GetMapping("/detail")
    public ApiResponse<Map<String, Object>> detail(@RequestParam("teacherId") Long teacherId,
                                                   @RequestParam("month") String month,
                                                   @RequestParam(value = "courseType", required = false) String courseType) {
        Map<String, Object> out = feeService.detail(teacherId, month, courseType);
        return ApiResponse.success(out);
    }

    // 薪资汇总：月度汇总（扣除异常课时）
    @GetMapping("/summary/monthly")
    public ApiResponse<Map<String, Object>> monthly(@RequestParam("teacherId") Long teacherId,
                                                    @RequestParam("month") String month) {
        Map<String, Object> out = feeService.monthlySummary(teacherId, month);
        return ApiResponse.success(out);
    }

    // 导出报表：教师薪资核算表（CSV）
    @GetMapping(value = "/export/monthly", produces = "text/csv")
    public String exportMonthly(@RequestParam("month") String month,
                                @RequestParam(value = "campusId", required = false) Long campusId) {
        return feeService.exportMonthlyCsv(month, campusId);
    }
}