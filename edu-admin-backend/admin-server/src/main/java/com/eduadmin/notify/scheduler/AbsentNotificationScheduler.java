package com.eduadmin.notify.scheduler;

import com.eduadmin.attendance.entity.AttendanceRecord;
import com.eduadmin.attendance.repo.AttendanceRecordRepository;
import com.eduadmin.notify.service.NotificationSender;
import com.eduadmin.student.entity.GuardianPerm;
import com.eduadmin.student.service.GuardianService;
import com.eduadmin.scheduling.entity.ScheduleInfo;
import com.eduadmin.scheduling.repo.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AbsentNotificationScheduler {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    @Autowired
    private NotificationSender notificationSender;
    @Autowired
    private GuardianService guardianService;

    // 每15分钟检查已结束的课时是否有缺勤，通知家长
    @Scheduled(cron = "0 */15 * * * *")
    public void notifyAbsences() {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.MINUTE, -60); // 过去1小时内结束的课时
        Date oneHourAgo = c.getTime();

        List<ScheduleInfo> all = scheduleRepository.findAll();
        for (ScheduleInfo s : all) {
            Date end = s.getEndAt();
            if (end == null) continue;
            if (end.after(oneHourAgo) && end.before(now)) {
                List<AttendanceRecord> records = attendanceRecordRepository.findByScheduleId(s.getId());
                List<Long> absentStudents = new ArrayList<>();
                for (AttendanceRecord r : records) {
                    if (Integer.valueOf(4).equals(r.getSignType())) {
                        absentStudents.add(r.getStudentId());
                    }
                }
                if (absentStudents.isEmpty()) continue;

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String title = "缺勤通知";
                String content = String.format("课时(%s)结束，以下学员缺勤：%s。请及时沟通。",
                        df.format(end), absentStudents.toString());
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("scheduleId", s.getId());
                meta.put("classId", s.getClassId());
                meta.put("teacherId", s.getTeacherId());

                // 先通知到学生ID（日志占位），并通知家长手机号（若已维护）
                for (Long stuId : absentStudents) {
                    notificationSender.sendToStudent(stuId, title, content, meta);
                    List<GuardianPerm> guardians = guardianService.listByStudent(stuId);
                    for (GuardianPerm g : guardians) {
                        if (g.getGuardianPhone() != null && !g.getGuardianPhone().trim().isEmpty()) {
                            notificationSender.sendToGuardian(g.getGuardianPhone(), title, content, meta);
                        }
                    }
                }
            }
        }
    }
}