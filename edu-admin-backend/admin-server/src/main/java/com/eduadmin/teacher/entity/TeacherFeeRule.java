package com.eduadmin.teacher.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "teacher_fee_rule")
public class TeacherFeeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long campusId; // 可为空表示全局

    @Column(length = 16, nullable = false)
    private String teacherLevel; // 初级/中级/高级

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal baseFeePerLesson; // 基础规则：按教师等级

    // 课程类型系数
    @Column(precision = 5, scale = 2)
    private BigDecimal oneToOneFactor; // 一对一 = 班课 ×1.5

    @Column(precision = 5, scale = 2)
    private BigDecimal ieltsFactor; // 雅思 = 常规英语 ×1.2

    // 班级规模上浮
    @Column
    private Integer largeClassThreshold; // > N 人

    @Column(precision = 5, scale = 2)
    private BigDecimal largeClassFactor; // 上浮系数，如 1.10

    // 特殊规则
    @Column(precision = 5, scale = 2)
    private BigDecimal holidayFactor; // 节假日 ×2.0

    @Column(precision = 10, scale = 2)
    private BigDecimal substituteExtra; // 代课 +30 元/课时

    // 阶梯奖励
    @Column
    private Integer tierMonthlyThreshold; // 月课时 > 80

    @Column(precision = 5, scale = 2)
    private BigDecimal tierFactor; // 超额部分 ×1.2

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public String getTeacherLevel() { return teacherLevel; }
    public void setTeacherLevel(String teacherLevel) { this.teacherLevel = teacherLevel; }
    public BigDecimal getBaseFeePerLesson() { return baseFeePerLesson; }
    public void setBaseFeePerLesson(BigDecimal baseFeePerLesson) { this.baseFeePerLesson = baseFeePerLesson; }
    public BigDecimal getOneToOneFactor() { return oneToOneFactor; }
    public void setOneToOneFactor(BigDecimal oneToOneFactor) { this.oneToOneFactor = oneToOneFactor; }
    public BigDecimal getIeltsFactor() { return ieltsFactor; }
    public void setIeltsFactor(BigDecimal ieltsFactor) { this.ieltsFactor = ieltsFactor; }
    public Integer getLargeClassThreshold() { return largeClassThreshold; }
    public void setLargeClassThreshold(Integer largeClassThreshold) { this.largeClassThreshold = largeClassThreshold; }
    public BigDecimal getLargeClassFactor() { return largeClassFactor; }
    public void setLargeClassFactor(BigDecimal largeClassFactor) { this.largeClassFactor = largeClassFactor; }
    public BigDecimal getHolidayFactor() { return holidayFactor; }
    public void setHolidayFactor(BigDecimal holidayFactor) { this.holidayFactor = holidayFactor; }
    public BigDecimal getSubstituteExtra() { return substituteExtra; }
    public void setSubstituteExtra(BigDecimal substituteExtra) { this.substituteExtra = substituteExtra; }
    public Integer getTierMonthlyThreshold() { return tierMonthlyThreshold; }
    public void setTierMonthlyThreshold(Integer tierMonthlyThreshold) { this.tierMonthlyThreshold = tierMonthlyThreshold; }
    public BigDecimal getTierFactor() { return tierFactor; }
    public void setTierFactor(BigDecimal tierFactor) { this.tierFactor = tierFactor; }
}