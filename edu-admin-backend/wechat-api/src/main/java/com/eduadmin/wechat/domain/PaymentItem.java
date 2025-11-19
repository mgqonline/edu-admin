package com.eduadmin.wechat.domain;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentItem {
    private Long id;
    private String courseName;
    private Long classId;
    private BigDecimal amount;
    private Date date;
    private String type; // 缴费/退费
    private String method;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}