package com.eduadmin.finance.entity;

import javax.persistence.*;

@Entity
@Table(name = "finance_refund_approver_config")
public class RefundApproverConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 允许的一审审批人ID列表，逗号分隔（例如："101,102"）
    @Column(length = 1024)
    private String level1ApproverIds;

    // 允许的二审审批人ID列表
    @Column(length = 1024)
    private String level2ApproverIds;

    // 允许的财务审批人ID列表
    @Column(length = 1024)
    private String financeApproverIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevel1ApproverIds() { return level1ApproverIds; }
    public void setLevel1ApproverIds(String level1ApproverIds) { this.level1ApproverIds = level1ApproverIds; }
    public String getLevel2ApproverIds() { return level2ApproverIds; }
    public void setLevel2ApproverIds(String level2ApproverIds) { this.level2ApproverIds = level2ApproverIds; }
    public String getFinanceApproverIds() { return financeApproverIds; }
    public void setFinanceApproverIds(String financeApproverIds) { this.financeApproverIds = financeApproverIds; }
}