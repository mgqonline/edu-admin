package com.eduadmin.student.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "auditions")
public class Audition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;
    private Long teacherId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    private String status; // booked/finished
    @Column(columnDefinition = "TEXT")
    private String feedback;
    private String result; // 适合报班/需再评估

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}