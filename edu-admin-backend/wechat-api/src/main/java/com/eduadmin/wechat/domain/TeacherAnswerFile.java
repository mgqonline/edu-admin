package com.eduadmin.wechat.domain;

import java.util.Date;

public class TeacherAnswerFile {
    private Long id;
    private Long teacherId;
    private Long homeworkId;
    private String title;
    private String filename;
    private Long size;
    private String url;
    private Date uploadedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }
}