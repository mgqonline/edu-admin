package com.eduadmin.wechat.domain;

import java.util.Date;

public class AdjustRequest {
    private Long id;
    private Long teacherId;
    private String course;
    private String originalTime;
    private String newTime;
    private String reason;
    private String status;
    private Date submittedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getOriginalTime() { return originalTime; }
    public void setOriginalTime(String originalTime) { this.originalTime = originalTime; }
    public String getNewTime() { return newTime; }
    public void setNewTime(String newTime) { this.newTime = newTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
}