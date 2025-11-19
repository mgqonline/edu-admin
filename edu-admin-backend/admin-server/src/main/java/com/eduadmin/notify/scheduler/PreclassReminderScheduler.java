package com.eduadmin.notify.scheduler;

import com.eduadmin.course.repo.ClassEnrollmentRepository;
import com.eduadmin.notify.service.NotificationSender;
import com.eduadmin.scheduling.entity.ScheduleInfo;
import com.eduadmin.scheduling.repo.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class PreclassReminderScheduler {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ClassEnrollmentRepository classEnrollmentRepository;
    @Autowired
    private NotificationSender notificationSender;

    // 每5分钟扫描一次，找出30分钟内即将开始的课时进行提醒
    @Scheduled(cron = "0 */5 * * * *")
    public void remindUpcoming() {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.MINUTE, 30);
        Date soon = c.getTime();
        List<ScheduleInfo> all = scheduleRepository.findAll();
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
        for (ScheduleInfo s : all) {
            Date start = s.getStartAt();
            if (start == null) continue;
            if (start.after(now) && start.before(soon) && "scheduled".equalsIgnoreCase(s.getStatus())) {
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("scheduleId", s.getId());
                meta.put("classId", s.getClassId());
                meta.put("teacherId", s.getTeacherId());
                String dateStr = s.getDateOnly() != null ? dfDate.format(s.getDateOnly()) : dfDate.format(start);
                String startStr = dfTime.format(start);
                String endStr = s.getEndAt() != null ? dfTime.format(s.getEndAt()) : s.getEndTimeText();
                String title = "课前提醒";
                String classLabel = s.getClassId() != null ? ("班级#" + s.getClassId()) : (s.getStudentId() != null ? ("学员#" + s.getStudentId()) : "");
                String content = String.format("您有即将开始的课时：%s %s %s-%s，请按时到课并发起签到。",
                        classLabel, dateStr, startStr, endStr);

                // 通知教师
                if (s.getTeacherId() != null) {
                    notificationSender.sendToTeacher(s.getTeacherId(), title, content, meta);
                }

                // 班课：通知在读学员（仅记录为日志）
                if (s.getClassId() != null) {
                    classEnrollmentRepository.findByClassIdAndStatus(s.getClassId(), "active")
                            .forEach(en -> notificationSender.sendToStudent(en.getStudentId(), title, content, meta));
                } else if (s.getStudentId() != null) {
                    // 一对一：通知该学员
                    notificationSender.sendToStudent(s.getStudentId(), title, content, meta);
                }
            }
        }
    }
}