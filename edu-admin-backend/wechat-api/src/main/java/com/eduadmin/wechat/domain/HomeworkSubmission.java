package com.eduadmin.wechat.domain;

import java.util.Date;

public class HomeworkSubmission {
    private Long id;
    private Long studentId;
    private Long homeworkId;
    private String filename;
    private Long size;
    private String url;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}