package com.eduadmin.wechat.domain;

import java.util.Date;

public class TeacherClassRecord {
    private Long id;
    private Long teacherId;
    private String classId;
    private String content;
    private String performance;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPerformance() { return performance; }
    public void setPerformance(String performance) { this.performance = performance; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}