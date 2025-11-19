package com.eduadmin.finance.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "finance_settlement")
public class FinanceSettlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long classId;
    // 操作类型：enroll(新生报名) / renew(老生续费)
    private String opType;
    // 报班时选定的教室与座位（用于占用校验与座位标注）
    private Long classroomId;
    private Integer seatNo;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal hours;

    @Column(precision = 12, scale = 2)
    private BigDecimal textbookFee;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalFee;

    private String method; // cash/wechat/alipay/bankcard
    private String receiptNo;
    private String receiver;
    private String voucherUrl;
    private String status; // paid/unpaid
    private Boolean arrears = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public Integer getSeatNo() { return seatNo; }
    public void setSeatNo(Integer seatNo) { this.seatNo = seatNo; }
    public String getOpType() { return opType; }
    public void setOpType(String opType) { this.opType = opType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }
    public BigDecimal getTextbookFee() { return textbookFee; }
    public void setTextbookFee(BigDecimal textbookFee) { this.textbookFee = textbookFee; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getVoucherUrl() { return voucherUrl; }
    public void setVoucherUrl(String voucherUrl) { this.voucherUrl = voucherUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getArrears() { return arrears; }
    public void setArrears(Boolean arrears) { this.arrears = arrears; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}