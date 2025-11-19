package com.eduadmin.student.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_referral", indexes = {
        @Index(name = "idx_referrerId", columnList = "referrerId")
})
@org.hibernate.annotations.Table(appliesTo = "student_referral", comment = "学员转介绍关系表")
public class StudentReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint COMMENT '推荐人老学员ID'")
    private Long referrerId;

    @Column(nullable = false, columnDefinition = "bigint COMMENT '新学员ID'")
    private Long newStudentId;

    @Lob
    @Column(nullable = false, columnDefinition = "text COMMENT '奖励规则JSON字符串'")
    private String rewardRuleJson;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, columnDefinition = "datetime COMMENT '建立关系时间'")
    private Date time;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReferrerId() { return referrerId; }
    public void setReferrerId(Long referrerId) { this.referrerId = referrerId; }

    public Long getNewStudentId() { return newStudentId; }
    public void setNewStudentId(Long newStudentId) { this.newStudentId = newStudentId; }

    public String getRewardRuleJson() { return rewardRuleJson; }
    public void setRewardRuleJson(String rewardRuleJson) { this.rewardRuleJson = rewardRuleJson; }

    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }
}