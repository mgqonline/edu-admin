package com.eduadmin.finance.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "finance_refund_request")
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long classId;

    @Column(precision = 12, scale = 2)
    private BigDecimal remainingHours; // 剩余课时

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice; // 单价

    @Column(precision = 12, scale = 2)
    private BigDecimal serviceFee; // 手续费

    @Column(precision = 12, scale = 2)
    private BigDecimal autoAmount; // 自动核算金额（remainingHours * unitPrice - serviceFee）

    @Column(precision = 12, scale = 2)
    private BigDecimal finalAmount; // 实际退款金额（可手动调整，需审批）

    private String reason; // 退费原因（申请）
    private String status; // PENDING_L1 / PENDING_L2 / PENDING_FINANCE / APPROVED / REJECTED / REFUNDED

    // 审批轨迹
    private Long level1ApproverId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date level1At;

    private Long level2ApproverId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date level2At;

    private Long financeApproverId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date financeAt;

    // 执行退款信息
    private String refundMethod; // cash/wechat/alipay/bankcard
    private String refundTxnId; // 交易号或收据号
    @Temporal(TemporalType.TIMESTAMP)
    private Date refundAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public BigDecimal getRemainingHours() { return remainingHours; }
    public void setRemainingHours(BigDecimal remainingHours) { this.remainingHours = remainingHours; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
    public BigDecimal getAutoAmount() { return autoAmount; }
    public void setAutoAmount(BigDecimal autoAmount) { this.autoAmount = autoAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getLevel1ApproverId() { return level1ApproverId; }
    public void setLevel1ApproverId(Long level1ApproverId) { this.level1ApproverId = level1ApproverId; }
    public Date getLevel1At() { return level1At; }
    public void setLevel1At(Date level1At) { this.level1At = level1At; }
    public Long getLevel2ApproverId() { return level2ApproverId; }
    public void setLevel2ApproverId(Long level2ApproverId) { this.level2ApproverId = level2ApproverId; }
    public Date getLevel2At() { return level2At; }
    public void setLevel2At(Date level2At) { this.level2At = level2At; }
    public Long getFinanceApproverId() { return financeApproverId; }
    public void setFinanceApproverId(Long financeApproverId) { this.financeApproverId = financeApproverId; }
    public Date getFinanceAt() { return financeAt; }
    public void setFinanceAt(Date financeAt) { this.financeAt = financeAt; }
    public String getRefundMethod() { return refundMethod; }
    public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
    public String getRefundTxnId() { return refundTxnId; }
    public void setRefundTxnId(String refundTxnId) { this.refundTxnId = refundTxnId; }
    public Date getRefundAt() { return refundAt; }
    public void setRefundAt(Date refundAt) { this.refundAt = refundAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}