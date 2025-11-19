package com.eduadmin.wechat.domain;

import java.util.Date;

public class GradeRecord {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String courseName;
    private String type;
    private Integer score;
    private Date examDate;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}