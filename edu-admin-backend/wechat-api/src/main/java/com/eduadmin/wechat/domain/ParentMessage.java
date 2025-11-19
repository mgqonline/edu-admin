package com.eduadmin.wechat.domain;

import java.util.Date;

public class ParentMessage {
    private Long id;
    private Long studentId;
    private Long teacherId;
    private String parentName;
    private String content;
    private String reply;
    private String status;
    private Date createdAt;
    private Date repliedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getRepliedAt() { return repliedAt; }
    public void setRepliedAt(Date repliedAt) { this.repliedAt = repliedAt; }
}