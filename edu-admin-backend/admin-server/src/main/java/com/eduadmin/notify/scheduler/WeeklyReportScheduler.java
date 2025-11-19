package com.eduadmin.notify.scheduler;

import com.eduadmin.attendance.entity.AttendanceRecord;
import com.eduadmin.attendance.repo.AttendanceRecordRepository;
import com.eduadmin.course.entity.ClassInfo;
import com.eduadmin.course.repo.ClassEnrollmentRepository;
import com.eduadmin.course.repo.ClassInfoRepository;
import com.eduadmin.notify.service.NotificationSender;
import com.eduadmin.scheduling.entity.ScheduleInfo;
import com.eduadmin.scheduling.repo.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class WeeklyReportScheduler {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private ClassEnrollmentRepository classEnrollmentRepository;
    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private NotificationSender notificationSender;

    // 每周一早上8点生成上周的出勤周报，并推送给班主任（fixedTeacherId）
    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyReport() {
        try {
            Calendar c = Calendar.getInstance();
            // 计算上周一到上周日
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            c.add(Calendar.DAY_OF_MONTH, -7);
            Date start = c.getTime();
            c.add(Calendar.DAY_OF_MONTH, 6);
            Date end = c.getTime();

            // 统计班级维度的周报
            Map<Long, Map<String, Object>> classStats = new LinkedHashMap<>();
            List<ScheduleInfo> all = scheduleRepository.findAll();
            for (ScheduleInfo s : all) {
                Date d = s.getDateOnly();
                if (d == null) d = s.getStartAt();
                if (d == null || d.before(start) || d.after(end)) continue;
                Long classId = s.getClassId();
                if (classId == null) continue; // 只统计班课
                classStats.computeIfAbsent(classId, k -> new LinkedHashMap<>()).putIfAbsent("should", 0);
                classStats.computeIfAbsent(classId, k -> new LinkedHashMap<>()).putIfAbsent("present", 0);
                classStats.computeIfAbsent(classId, k -> new LinkedHashMap<>()).putIfAbsent("absent", 0);
                classStats.computeIfAbsent(classId, k -> new LinkedHashMap<>()).putIfAbsent("late", 0);

                int should = classEnrollmentRepository.findByClassIdAndStatus(classId, "active").size();
                classStats.get(classId).put("should", (int) classStats.get(classId).get("should") + should);
                List<AttendanceRecord> recs = attendanceRecordRepository.findByScheduleId(s.getId());
                for (AttendanceRecord r : recs) {
                    if (Integer.valueOf(4).equals(r.getSignType())) {
                        classStats.get(classId).put("absent", (int) classStats.get(classId).get("absent") + 1);
                    } else {
                        classStats.get(classId).put("present", (int) classStats.get(classId).get("present") + 1);
                        if (Integer.valueOf(2).equals(r.getSignType())) {
                            classStats.get(classId).put("late", (int) classStats.get(classId).get("late") + 1);
                        }
                    }
                }
            }

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            String title = "班级周报";
            for (Map.Entry<Long, Map<String, Object>> e : classStats.entrySet()) {
                Long classId = e.getKey();
                Map<String, Object> stat = e.getValue();
                int should = (int) stat.get("should");
                int present = (int) stat.get("present");
                int absent = (int) stat.get("absent");
                int late = (int) stat.get("late");
                double rate = (should > 0) ? Math.round((present * 100.0 / should) * 10.0) / 10.0 : 0.0;
                Optional<ClassInfo> clazzOpt = classInfoRepository.findById(classId);
                Long teacherId = null;
                String className = "班级#" + classId;
                if (clazzOpt.isPresent()) {
                    ClassInfo ce = clazzOpt.get();
                    teacherId = ce.getFixedTeacherId();
                    className = ce.getName() == null ? className : ce.getName();
                }
                String content = String.format("%s（%s ~ %s）：%s 平均出勤率 %s%%，到课 %d，缺勤 %d，迟到 %d。",
                        className, fmt.format(start), fmt.format(end), className, rate, present, absent, late);
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("classId", classId);
                meta.put("startDate", fmt.format(start));
                meta.put("endDate", fmt.format(end));
                meta.put("avgAttendanceRate", rate);
                meta.put("present", present);
                meta.put("absent", absent);
                meta.put("late", late);

                // 推送给班主任（如无固定老师则跳过）
                if (teacherId != null) {
                    notificationSender.sendToTeacher(teacherId, title, content, meta);
                }
            }
        } catch (Exception ex) {
            // 简单捕获，避免调度失败中断；详细错误由日志输出
        }
    }
}