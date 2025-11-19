package com.eduadmin.attendance.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attendance_sign_code", indexes = {
        @Index(name = "idx_signcode_schedule", columnList = "scheduleId"),
        @Index(name = "idx_signcode_code", columnList = "code")
})
public class AttendanceSignCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false, length = 32)
    private String code; // 简短字符串，供 QR 展示

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expireAt; // 10 分钟有效

    @Column
    private Long teacherId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Date getExpireAt() { return expireAt; }
    public void setExpireAt(Date expireAt) { this.expireAt = expireAt; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}