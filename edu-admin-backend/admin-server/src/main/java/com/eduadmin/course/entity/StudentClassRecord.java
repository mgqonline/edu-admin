package com.eduadmin.course.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_class_record", indexes = {
        @Index(name = "idx_scr_student", columnList = "studentId")
})
public class StudentClassRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private String type; // join/transfer

    @Column
    private Long fromClassId;

    @Column
    private Long toClassId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date time;

    @Column
    private String reason;

    @PrePersist
    public void onCreate() { if (time == null) time = new Date(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getFromClassId() { return fromClassId; }
    public void setFromClassId(Long fromClassId) { this.fromClassId = fromClassId; }
    public Long getToClassId() { return toClassId; }
    public void setToClassId(Long toClassId) { this.toClassId = toClassId; }
    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}