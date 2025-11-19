package com.eduadmin.teacher.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "teacher_lesson_adjustment")
public class TeacherLessonAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private Long scheduleId; // 对应排课/出勤记录

    @Column(precision = 10, scale = 2)
    private BigDecimal adjustedFee; // 手动调整后的课时费（优先级最高）

    @Column(length = 128)
    private String reason;

    @Column
    private Boolean substitute; // 是否代课（额外+30）

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public BigDecimal getAdjustedFee() { return adjustedFee; }
    public void setAdjustedFee(BigDecimal adjustedFee) { this.adjustedFee = adjustedFee; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Boolean getSubstitute() { return substitute; }
    public void setSubstitute(Boolean substitute) { this.substitute = substitute; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}