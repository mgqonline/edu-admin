package com.eduadmin.wechat.domain;

import java.math.BigDecimal;
import java.util.Date;

public class TeacherFeeRecord {
    private Long id;
    private Long teacherId;
    private String course;
    private BigDecimal amount;
    private Date date;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}