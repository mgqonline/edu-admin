package com.eduadmin.finance.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "finance_invoice_record", indexes = {
        @Index(name = "idx_invoice_date", columnList = "invoiceDate"),
        @Index(name = "idx_invoice_campus", columnList = "campusId")
})
public class InvoiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationId; // 可为空，直接开票场景
    private Long settlementId; // 关联缴费记录
    private Long studentId; // 冗余便于查询
    private Long campusId; // 校区

    @Column(length = 64)
    private String invoiceNo;

    @Temporal(TemporalType.DATE)
    private Date invoiceDate;

    @Column(length = 32)
    private String deliveryStatus; // pending/shipped/received

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public Date getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(Date invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}