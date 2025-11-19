package com.eduadmin.student.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_histories")
public class StudentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String type; // 报班/缴费/请假/调班
    @Column(columnDefinition = "TEXT")
    private String detail;
    private Double feeAmount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Double getFeeAmount() { return feeAmount; }
    public void setFeeAmount(Double feeAmount) { this.feeAmount = feeAmount; }
    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }
}