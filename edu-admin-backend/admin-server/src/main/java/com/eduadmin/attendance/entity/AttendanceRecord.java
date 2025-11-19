package com.eduadmin.attendance.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attendance_record", indexes = {
        @Index(name = "idx_attend_schedule", columnList = "scheduleId"),
        @Index(name = "idx_attend_student", columnList = "studentId"),
        @Index(name = "idx_attend_time", columnList = "signTime")
})
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private Long studentId;

    /**
     * 1: 正常/到课, 2: 迟到, 3: 早退, 4: 缺勤
     */
    @Column(nullable = false)
    private Integer signType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date signTime;

    @Column(length = 255)
    private String remark;

    @Column(length = 32)
    private String source; // device/app/manual

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate(){
        if (createdAt == null) createdAt = new Date();
        if (signTime == null) signTime = new Date();
        if (signType == null) signType = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Integer getSignType() { return signType; }
    public void setSignType(Integer signType) { this.signType = signType; }
    public Date getSignTime() { return signTime; }
    public void setSignTime(Date signTime) { this.signTime = signTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}