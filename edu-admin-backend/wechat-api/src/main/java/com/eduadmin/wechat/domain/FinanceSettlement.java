package com.eduadmin.wechat.domain;

import java.util.Date;
import java.math.BigDecimal;

public class FinanceSettlement {
    private Long id;
    private Long studentId;
    private Long classId;
    private BigDecimal totalFee;
    private Integer hours;
    private String status;
    private String opType;
    private String method;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOpType() { return opType; }
    public void setOpType(String opType) { this.opType = opType; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}