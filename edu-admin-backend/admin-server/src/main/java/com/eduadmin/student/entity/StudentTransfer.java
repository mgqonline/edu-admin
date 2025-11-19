package com.eduadmin.student.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_transfer")
public class StudentTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long fromClassId;

    @Column(nullable = false)
    private Long toClassId;

    @Column
    private String reason;

    @Column
    private String status; // pending/approved/rejected

    @Column
    private Long approverFrom;

    @Column
    private Long approverTo;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getFromClassId() { return fromClassId; }
    public void setFromClassId(Long fromClassId) { this.fromClassId = fromClassId; }
    public Long getToClassId() { return toClassId; }
    public void setToClassId(Long toClassId) { this.toClassId = toClassId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getApproverFrom() { return approverFrom; }
    public void setApproverFrom(Long approverFrom) { this.approverFrom = approverFrom; }
    public Long getApproverTo() { return approverTo; }
    public void setApproverTo(Long approverTo) { this.approverTo = approverTo; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}