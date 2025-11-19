package com.eduadmin.course.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "class_select_apply")
public class ClassSelectApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long classId;
    private Long studentId;
    private String status; // pending/approved/rejected
    private String note;
    private String reason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate(){ this.createdAt = new Date(); }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}