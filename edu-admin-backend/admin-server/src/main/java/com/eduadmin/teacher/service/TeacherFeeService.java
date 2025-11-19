package com.eduadmin.teacher.service;

import com.eduadmin.attendance.entity.AttendanceRecord;
import com.eduadmin.attendance.repo.AttendanceRecordRepository;
import com.eduadmin.course.entity.ClassInfo;
import com.eduadmin.course.repo.ClassEnrollmentRepository;
import com.eduadmin.course.repo.ClassInfoRepository;
import com.eduadmin.scheduling.entity.ScheduleInfo;
import com.eduadmin.scheduling.repo.ScheduleRepository;
import com.eduadmin.teacher.entity.Teacher;
import com.eduadmin.teacher.entity.TeacherFeeRule;
import com.eduadmin.teacher.entity.TeacherLessonFee;
import com.eduadmin.teacher.repo.TeacherRepository;
import com.eduadmin.teacher.repository.TeacherFeeRuleRepository;
import com.eduadmin.teacher.repository.TeacherLessonAdjustmentRepository;
import com.eduadmin.teacher.repository.TeacherLessonFeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TeacherFeeService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private TeacherFeeRuleRepository ruleRepository;
    @Autowired
    private TeacherLessonFeeRepository feeRepository;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private ClassEnrollmentRepository classEnrollmentRepository;
    @Autowired
    private TeacherLessonAdjustmentRepository adjustmentRepository;

    public void autoCalcForSchedule(Long scheduleId) {
        if (scheduleId == null) return;
        try {
            Optional<TeacherLessonFee> exists = feeRepository.findByScheduleId(scheduleId);
            if (exists.isPresent()) return; // 避免重复生成

            Optional<ScheduleInfo> sOpt = scheduleRepository.findById(scheduleId);
            if (!sOpt.isPresent()) return;
            ScheduleInfo s = sOpt.get();
            Long teacherId = s.getTeacherId();
            if (teacherId == null) return;

            Optional<Teacher> tOpt = teacherRepository.findById(teacherId);
            if (!tOpt.isPresent()) return;
            Teacher t = tOpt.get();
            String level = t.getLevel() == null ? "中级" : t.getLevel();
            Long campusId = (s.getCampusId() != null) ? s.getCampusId() : t.getCampusId();

            Optional<TeacherFeeRule> ruleOpt = (campusId == null)
                    ? ruleRepository.findFirstByTeacherLevel(level)
                    : ruleRepository.findFirstByCampusIdAndTeacherLevel(campusId, level);
            TeacherFeeRule rule = ruleOpt.orElseGet(() -> defaultRule());

            // 课程属性
            String courseType = "2".equals(s.getClassType()) ? "一对一" : "班课";
            String courseCategory = "常规英语"; // 暂未区分科目，后续可由 courseId 反查

            // 班级规模：班课取报名有效人数；一对一=1
            int classSize = 1;
            if (!"2".equals(s.getClassType()) && s.getClassId() != null) {
                classSize = (int) classEnrollmentRepository.countByClassIdAndStatus(s.getClassId(), "active");
            }

            // 节假日：按周末视为节假日
            boolean isHoliday = false;
            if (s.getDateOnly() != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(s.getDateOnly());
                int dow = c.get(Calendar.DAY_OF_WEEK);
                isHoliday = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
            }

            // 是否代课：与班级固定/主教师不一致视为代课
            boolean isSubstitute = false;
            if (s.getClassId() != null) {
                Optional<ClassInfo> ceOpt = classInfoRepository.findById(s.getClassId());
                if (ceOpt.isPresent()) {
                    ClassInfo ce = ceOpt.get();
                    if (ce.getFixedTeacherId() != null && !ce.getFixedTeacherId().equals(teacherId)) isSubstitute = true;
                    if (ce.getTeacherMainId() != null && !ce.getTeacherMainId().equals(teacherId)) isSubstitute = true;
                }
            }

            // 单课时手动调整/代课状态覆盖
            BigDecimal adjustedFee = null;
            boolean adjustedSubstitute = false;
            try {
                adjustmentRepository.findFirstByScheduleId(scheduleId).ifPresent(adj -> {
                    // 闭包中更新外部变量
                });
                Optional<com.eduadmin.teacher.entity.TeacherLessonAdjustment> adjOpt = adjustmentRepository.findFirstByScheduleId(scheduleId);
                if (adjOpt.isPresent()) {
                    com.eduadmin.teacher.entity.TeacherLessonAdjustment adj = adjOpt.get();
                    if (adj.getAdjustedFee() != null) adjustedFee = adj.getAdjustedFee();
                    if (Boolean.TRUE.equals(adj.getSubstitute())) adjustedSubstitute = true;
                }
            } catch (Exception ignore) {}
            if (adjustedSubstitute) isSubstitute = true;

            // 计算金额
            BigDecimal baseAmount = BigDecimal.ZERO;
            BigDecimal holidayExtra = BigDecimal.ZERO;
            BigDecimal substituteExtra = BigDecimal.ZERO;

            if (adjustedFee != null) {
                baseAmount = adjustedFee;
                // 手动调整时不再叠加补贴项
            } else {
                BigDecimal fee = rule.getBaseFeePerLesson();
                if ("一对一".equals(courseType)) fee = fee.multiply(rule.getOneToOneFactor());
                if ("雅思".equals(courseCategory)) fee = fee.multiply(rule.getIeltsFactor());
                Integer threshold = (rule.getLargeClassThreshold() == null) ? 0 : rule.getLargeClassThreshold();
                if (classSize > threshold) fee = fee.multiply(rule.getLargeClassFactor());
                if (isHoliday) {
                    fee = fee.multiply(rule.getHolidayFactor());
                    holidayExtra = holidayExtra.add(rule.getBaseFeePerLesson());
                }
                if (isSubstitute) substituteExtra = substituteExtra.add(rule.getSubstituteExtra());
                baseAmount = fee;
            }

            BigDecimal finalAmount = baseAmount.add(substituteExtra);

            // 异常判定：一对一缺勤 或 全员缺勤
            boolean abnormal = false;
            try {
                List<AttendanceRecord> recs = attendanceRecordRepository.findByScheduleId(scheduleId);
                int present = 0;
                for (AttendanceRecord r : recs) {
                    if (r.getSource() != null && "makeup".equalsIgnoreCase(r.getSource())) continue;
                    if (!Integer.valueOf(4).equals(r.getSignType())) present++;
                }
                if ("2".equals(s.getClassType())) {
                    abnormal = (present == 0); // 一对一缺勤
                } else {
                    // 班课：若有效报名>0 且 present==0 视为异常
                    if (classSize > 0 && present == 0) abnormal = true;
                }
            } catch (Exception ignore) {}

            TeacherLessonFee tlf = new TeacherLessonFee();
            tlf.setScheduleId(scheduleId);
            tlf.setTeacherId(teacherId);
            tlf.setCampusId(campusId);
            tlf.setClassId(s.getClassId());
            tlf.setLessonDate(s.getDateOnly());
            tlf.setCourseType(courseType);
            tlf.setCourseCategory(courseCategory);
            tlf.setClassSize(classSize);
            tlf.setHoliday(isHoliday);
            tlf.setSubstitute(isSubstitute);
            tlf.setAbnormal(abnormal);
            tlf.setBaseAmount(baseAmount);
            tlf.setHolidayExtra(holidayExtra);
            tlf.setSubstituteExtra(substituteExtra);
            tlf.setTierBonus(BigDecimal.ZERO);
            tlf.setFinalAmount(finalAmount);
            tlf.setStatus("pending");
            feeRepository.save(tlf);
        } catch (Exception ignore) {
        }
    }

    private TeacherFeeRule defaultRule() {
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
    }

    public Map<String, Object> detail(Long teacherId, String month, String courseType) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("teacherId", teacherId);
        out.put("month", month);
        try {
            SimpleDateFormat ym = new SimpleDateFormat("yyyy-MM");
            ym.setLenient(false);
            Date m = ym.parse(month);
            Calendar c = Calendar.getInstance();
            c.setTime(m);
            c.set(Calendar.DAY_OF_MONTH, 1);
            Date start = c.getTime();
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DAY_OF_MONTH, -1);
            Date end = c.getTime();

            List<TeacherLessonFee> list;
            if (courseType != null && !courseType.trim().isEmpty()) {
                list = feeRepository.findByTeacherIdAndLessonDateBetweenAndCourseType(teacherId, start, end, courseType);
            } else {
                list = feeRepository.findByTeacherIdAndLessonDateBetween(teacherId, start, end);
            }
            List<Map<String, Object>> rows = new ArrayList<>();
            for (TeacherLessonFee r : list) {
                Map<String, Object> mrow = new LinkedHashMap<>();
                mrow.put("scheduleId", r.getScheduleId());
                mrow.put("date", r.getLessonDate());
                mrow.put("courseType", r.getCourseType());
                mrow.put("classSize", r.getClassSize());
                mrow.put("holiday", r.getHoliday());
                mrow.put("substitute", r.getSubstitute());
                mrow.put("abnormal", r.getAbnormal());
                mrow.put("baseAmount", r.getBaseAmount());
                mrow.put("holidayExtra", r.getHolidayExtra());
                mrow.put("substituteExtra", r.getSubstituteExtra());
                mrow.put("finalAmount", r.getFinalAmount());
                rows.add(mrow);
            }
            out.put("items", rows);
            return out;
        } catch (Exception e) {
            out.put("items", Collections.emptyList());
            return out;
        }
    }

    public Map<String, Object> monthlySummary(Long teacherId, String month) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("teacherId", teacherId);
        out.put("month", month);
        try {
            SimpleDateFormat ym = new SimpleDateFormat("yyyy-MM");
            ym.setLenient(false);
            Date m = ym.parse(month);
            Calendar c = Calendar.getInstance();
            c.setTime(m);
            c.set(Calendar.DAY_OF_MONTH, 1);
            Date start = c.getTime();
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DAY_OF_MONTH, -1);
            Date end = c.getTime();

            List<TeacherLessonFee> list = feeRepository.findByTeacherIdAndLessonDateBetween(teacherId, start, end);
            // 排除异常课时
            List<TeacherLessonFee> normal = new ArrayList<>();
            for (TeacherLessonFee r : list) if (!Boolean.TRUE.equals(r.getAbnormal())) normal.add(r);

            BigDecimal baseAmount = BigDecimal.ZERO;
            BigDecimal holidayExtra = BigDecimal.ZERO;
            BigDecimal substituteExtra = BigDecimal.ZERO;
            for (TeacherLessonFee r : normal) {
                baseAmount = baseAmount.add(nvl(r.getBaseAmount()));
                holidayExtra = holidayExtra.add(nvl(r.getHolidayExtra()));
                substituteExtra = substituteExtra.add(nvl(r.getSubstituteExtra()));
            }

            // 阶梯奖励：使用规则与当月有效课时数
            Teacher t = teacherRepository.findById(teacherId).orElse(null);
            String level = (t == null || t.getLevel() == null) ? "中级" : t.getLevel();
            Long campusId = (t == null) ? null : t.getCampusId();
            TeacherFeeRule rule = ruleRepository.findFirstByCampusIdAndTeacherLevel(campusId, level)
                    .orElseGet(() -> ruleRepository.findFirstByTeacherLevel(level).orElseGet(this::defaultRule));
            int count = normal.size();
            BigDecimal tierBonus = BigDecimal.ZERO;
            int threshold = rule.getTierMonthlyThreshold() == null ? 0 : rule.getTierMonthlyThreshold();
            if (count > threshold) {
                int extraLessons = count - threshold;
                BigDecimal perLessonBase = rule.getBaseFeePerLesson();
                tierBonus = perLessonBase.multiply(new BigDecimal(extraLessons)).multiply(rule.getTierFactor().subtract(BigDecimal.ONE));
            }

            BigDecimal finalAmount = baseAmount.add(tierBonus).add(substituteExtra);
            out.put("totalLessons", count);
            out.put("baseAmount", baseAmount);
            out.put("holidayExtra", holidayExtra);
            out.put("substituteExtra", substituteExtra);
            out.put("tierBonus", tierBonus);
            out.put("finalAmount", finalAmount);
            return out;
        } catch (Exception e) {
            out.put("totalLessons", 0);
            out.put("baseAmount", BigDecimal.ZERO);
            out.put("holidayExtra", BigDecimal.ZERO);
            out.put("substituteExtra", BigDecimal.ZERO);
            out.put("tierBonus", BigDecimal.ZERO);
            out.put("finalAmount", BigDecimal.ZERO);
            return out;
        }
    }

    public String exportMonthlyCsv(String month, Long campusId) {
        try {
            SimpleDateFormat ym = new SimpleDateFormat("yyyy-MM");
            ym.setLenient(false);
            Date m = ym.parse(month);
            Calendar c = Calendar.getInstance();
            c.setTime(m);
            c.set(Calendar.DAY_OF_MONTH, 1);
            Date start = c.getTime();
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DAY_OF_MONTH, -1);
            Date end = c.getTime();

            List<TeacherLessonFee> monthFees = feeRepository.findByLessonDateBetween(start, end);
            // 分组按教师
            Map<Long, List<TeacherLessonFee>> byTeacher = new LinkedHashMap<>();
            for (TeacherLessonFee r : monthFees) {
                if (campusId != null && r.getCampusId() != null && !campusId.equals(r.getCampusId())) continue;
                byTeacher.computeIfAbsent(r.getTeacherId(), k -> new ArrayList<>()).add(r);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("教师ID,教师姓名,月份,有效课时,基础金额,节假日补贴,代课补贴,阶梯奖励,最终金额\n");
            for (Map.Entry<Long, List<TeacherLessonFee>> e : byTeacher.entrySet()) {
                Long tid = e.getKey();
                Teacher t = teacherRepository.findById(tid).orElse(null);
                String tname = (t == null || t.getName() == null) ? "" : t.getName();
                List<TeacherLessonFee> list = new ArrayList<>();
                for (TeacherLessonFee r : e.getValue()) if (!Boolean.TRUE.equals(r.getAbnormal())) list.add(r);
                BigDecimal baseAmount = BigDecimal.ZERO;
                BigDecimal holidayExtra = BigDecimal.ZERO;
                BigDecimal substituteExtra = BigDecimal.ZERO;
                for (TeacherLessonFee r : list) {
                    baseAmount = baseAmount.add(nvl(r.getBaseAmount()));
                    holidayExtra = holidayExtra.add(nvl(r.getHolidayExtra()));
                    substituteExtra = substituteExtra.add(nvl(r.getSubstituteExtra()));
                }
                // 阶梯奖励
                String level = (t == null || t.getLevel() == null) ? "中级" : t.getLevel();
                Long cap = (t == null) ? null : t.getCampusId();
                TeacherFeeRule rule = ruleRepository.findFirstByCampusIdAndTeacherLevel(cap, level)
                        .orElseGet(() -> ruleRepository.findFirstByTeacherLevel(level).orElseGet(this::defaultRule));
                int count = list.size();
                BigDecimal tierBonus = BigDecimal.ZERO;
                int threshold = rule.getTierMonthlyThreshold() == null ? 0 : rule.getTierMonthlyThreshold();
                if (count > threshold) {
                    int extraLessons = count - threshold;
                    tierBonus = rule.getBaseFeePerLesson().multiply(new BigDecimal(extraLessons)).multiply(rule.getTierFactor().subtract(BigDecimal.ONE));
                }
                BigDecimal finalAmount = baseAmount.add(tierBonus).add(substituteExtra);
                sb.append(tid).append(',').append(escapeCsv(tname)).append(',').append(month).append(',')
                        .append(count).append(',').append(baseAmount).append(',').append(holidayExtra).append(',')
                        .append(substituteExtra).append(',').append(tierBonus).append(',').append(finalAmount).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) return '"' + s.replace("\"", "\"\"") + '"';
        return s;
    }
}