package com.eduadmin.wechat.domain;

import java.util.Date;

public class SignRecord {
    private Long id;
    private Long studentId;
    private String classId;
    private String code; // QR code content or confirmation note
    private String method; // scan or confirm
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}