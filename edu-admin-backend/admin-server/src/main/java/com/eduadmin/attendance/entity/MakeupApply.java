package com.eduadmin.attendance.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "makeup_apply", indexes = {
        @Index(name = "idx_makeup_schedule", columnList = "scheduleId"),
        @Index(name = "idx_makeup_student", columnList = "studentId")
})
public class MakeupApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private Long studentId;

    @Column(length = 32)
    private String status; // pending/approved/rejected

    @Column(length = 255)
    private String reason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date auditedAt;

    @PrePersist
    public void onCreate(){ if (createdAt == null) createdAt = new Date(); if (status == null) status = "pending"; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getAuditedAt() { return auditedAt; }
    public void setAuditedAt(Date auditedAt) { this.auditedAt = auditedAt; }
}