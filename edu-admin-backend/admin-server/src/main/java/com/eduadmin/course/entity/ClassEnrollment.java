package com.eduadmin.course.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "class_enrollment", indexes = {
        @Index(name = "idx_class_enroll_class", columnList = "classId"),
        @Index(name = "idx_class_enroll_student", columnList = "studentId")
})
public class ClassEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long classId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private String status = "active"; // active/withdrawn

    @Column
    private String source; // manual/select

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date enrolledAt;

    @PrePersist
    public void onCreate() {
        if (enrolledAt == null) enrolledAt = new Date();
        if (status == null || status.isEmpty()) status = "active";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Date getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Date enrolledAt) { this.enrolledAt = enrolledAt; }
}