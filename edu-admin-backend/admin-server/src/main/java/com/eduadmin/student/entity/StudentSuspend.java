package com.eduadmin.student.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_suspend")
public class StudentSuspend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column
    private String reason;

    @Column(name = "expected_return_date")
    private String expectedReturnDate; // ISO date string

    @Column
    private String status; // suspended/resumed

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(String expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}