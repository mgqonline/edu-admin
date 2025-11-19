package com.eduadmin.scheduling.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "schedule_share", indexes = {
        @Index(name = "idx_share_target", columnList = "dimension,targetId"),
        @Index(name = "idx_share_token", columnList = "shareToken", unique = true)
})
public class ScheduleShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * class/teacher/student/room
     */
    @Column(nullable = false, length = 32)
    private String dimension;

    @Column(nullable = false, length = 64)
    private String targetId;

    /**
     * yyyy-MM 或 yyyy-MM-dd~yyyy-MM-dd
     */
    @Column(nullable = false, length = 64)
    private String rangeText;

    @Column(nullable = false, length = 64)
    private String shareToken; // 用于生成分享链接或校验

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    @Column(length = 64)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate(){ if (createdAt == null) createdAt = new Date(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getRangeText() { return rangeText; }
    public void setRangeText(String rangeText) { this.rangeText = rangeText; }
    public String getShareToken() { return shareToken; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }
    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}