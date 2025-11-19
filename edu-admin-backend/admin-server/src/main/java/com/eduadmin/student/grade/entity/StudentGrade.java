package com.eduadmin.student.grade.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_grade")
public class StudentGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private String subject;

    @Column
    private String term; // 学期

    @Column
    private Double score;

    @Temporal(TemporalType.DATE)
    private Date examDate;

    @Column
    private String status; // passed/failed/unknown

    @Column(length = 512)
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}