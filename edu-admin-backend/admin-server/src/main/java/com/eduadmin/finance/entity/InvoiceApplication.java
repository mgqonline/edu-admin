package com.eduadmin.finance.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "finance_invoice_application")
public class InvoiceApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long settlementId; // 关联缴费记录（finance_settlement.id）

    @Column(length = 128)
    private String title; // 发票抬头

    @Column(length = 64)
    private String taxNo; // 税号

    @Column(precision = 12, scale = 2)
    private BigDecimal amount; // 开票金额

    @Column(length = 32)
    private String status; // APPLIED / INVOICED

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTaxNo() { return taxNo; }
    public void setTaxNo(String taxNo) { this.taxNo = taxNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}