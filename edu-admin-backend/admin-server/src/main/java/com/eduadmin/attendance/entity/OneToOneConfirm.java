package com.eduadmin.attendance.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "one_to_one_confirm", indexes = {
        @Index(name = "idx_o2o_schedule", columnList = "scheduleId")
})
public class OneToOneConfirm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date teacherConfirmedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date studentConfirmedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expireAt; // 10 分钟有效

    @Column(length = 32)
    private String status; // pending/finished/expired

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate() { if (createdAt == null) createdAt = new Date(); if (status == null) status = "pending"; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Date getTeacherConfirmedAt() { return teacherConfirmedAt; }
    public void setTeacherConfirmedAt(Date teacherConfirmedAt) { this.teacherConfirmedAt = teacherConfirmedAt; }
    public Date getStudentConfirmedAt() { return studentConfirmedAt; }
    public void setStudentConfirmedAt(Date studentConfirmedAt) { this.studentConfirmedAt = studentConfirmedAt; }
    public Date getExpireAt() { return expireAt; }
    public void setExpireAt(Date expireAt) { this.expireAt = expireAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}