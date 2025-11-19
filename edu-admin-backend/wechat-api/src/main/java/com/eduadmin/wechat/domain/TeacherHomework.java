package com.eduadmin.wechat.domain;

import java.util.Date;

public class TeacherHomework {
    private Long id;
    private Long teacherId;
    private String title;
    private String content;
    private String status;
    private Date publishAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getPublishAt() { return publishAt; }
    public void setPublishAt(Date publishAt) { this.publishAt = publishAt; }
}