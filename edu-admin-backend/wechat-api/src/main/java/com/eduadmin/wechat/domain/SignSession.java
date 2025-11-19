package com.eduadmin.wechat.domain;

import java.util.Date;

public class SignSession {
    private Long id;
    private Long teacherId;
    private String classId;
    private String code;
    private Integer expireMinutes;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getExpireMinutes() { return expireMinutes; }
    public void setExpireMinutes(Integer expireMinutes) { this.expireMinutes = expireMinutes; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}