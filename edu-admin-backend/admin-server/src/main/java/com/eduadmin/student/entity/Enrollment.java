package com.eduadmin.student.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_enrollment", indexes = {
        @Index(name = "idx_enroll_student", columnList = "studentId"),
        @Index(name = "idx_enroll_class", columnList = "classId")
})
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long classId;

    @Column(length = 2000)
    private String applyInfo;

    @Column
    private Double fee;

    @Column
    private Double materialsFee;

    @Column(nullable = false)
    private String status; // pending/approved/rejected/approved(default)

    @Column
    private Boolean approvalRequired;

    @Column
    private String contractUrl;

    @Column
    private Boolean signed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = new Date();
        if (signed == null) signed = false;
        if (approvalRequired == null) approvalRequired = false;
        if (status == null || status.isEmpty()) status = approvalRequired ? "pending" : "approved";
        if (fee == null) fee = 0.0;
        if (materialsFee == null) materialsFee = 0.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getApplyInfo() { return applyInfo; }
    public void setApplyInfo(String applyInfo) { this.applyInfo = applyInfo; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    public Double getMaterialsFee() { return materialsFee; }
    public void setMaterialsFee(Double materialsFee) { this.materialsFee = materialsFee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }
    public String getContractUrl() { return contractUrl; }
    public void setContractUrl(String contractUrl) { this.contractUrl = contractUrl; }
    public Boolean getSigned() { return signed; }
    public void setSigned(Boolean signed) { this.signed = signed; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}