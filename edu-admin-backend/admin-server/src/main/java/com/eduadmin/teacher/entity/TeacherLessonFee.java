package com.eduadmin.teacher.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "teacher_lesson_fee", indexes = {
        @Index(name = "idx_tlf_teacher_month", columnList = "teacherId,lessonDate"),
        @Index(name = "idx_tlf_schedule", columnList = "scheduleId", unique = true)
})
public class TeacherLessonFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private Long teacherId;

    private Long campusId;
    private Long classId;

    @Temporal(TemporalType.DATE)
    private Date lessonDate;

    @Column(length = 16)
    private String courseType; // 班课 / 一对一

    @Column(length = 32)
    private String courseCategory; // 常规英语 / 雅思（占位）

    private Integer classSize; // 班级有效人数或一对一=1

    private Boolean holiday; // 是否节假日/周末
    private Boolean substitute; // 是否代课
    private Boolean abnormal; // 异常课时（如一对一缺勤/全员缺勤）

    @Column(precision = 12, scale = 2)
    private BigDecimal baseAmount; // 基础金额（含各类乘数）

    @Column(precision = 12, scale = 2)
    private BigDecimal holidayExtra; // 节假日补贴（按规则：基础费的额外部分）

    @Column(precision = 12, scale = 2)
    private BigDecimal substituteExtra; // 代课补贴（固定额）

    @Column(precision = 12, scale = 2)
    private BigDecimal tierBonus; // 阶梯奖励（单课时默认为 0，按月汇总计算）

    @Column(precision = 12, scale = 2)
    private BigDecimal finalAmount; // 当次课时最终金额（base+substituteExtra）

    @Column(length = 16)
    private String status = "pending"; // 待结算/已结算

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = new Date();
        if (this.tierBonus == null) this.tierBonus = BigDecimal.ZERO;
        if (this.holidayExtra == null) this.holidayExtra = BigDecimal.ZERO;
        if (this.substituteExtra == null) this.substituteExtra = BigDecimal.ZERO;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Date getLessonDate() { return lessonDate; }
    public void setLessonDate(Date lessonDate) { this.lessonDate = lessonDate; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public String getCourseCategory() { return courseCategory; }
    public void setCourseCategory(String courseCategory) { this.courseCategory = courseCategory; }
    public Integer getClassSize() { return classSize; }
    public void setClassSize(Integer classSize) { this.classSize = classSize; }
    public Boolean getHoliday() { return holiday; }
    public void setHoliday(Boolean holiday) { this.holiday = holiday; }
    public Boolean getSubstitute() { return substitute; }
    public void setSubstitute(Boolean substitute) { this.substitute = substitute; }
    public Boolean getAbnormal() { return abnormal; }
    public void setAbnormal(Boolean abnormal) { this.abnormal = abnormal; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
    public BigDecimal getHolidayExtra() { return holidayExtra; }
    public void setHolidayExtra(BigDecimal holidayExtra) { this.holidayExtra = holidayExtra; }
    public BigDecimal getSubstituteExtra() { return substituteExtra; }
    public void setSubstituteExtra(BigDecimal substituteExtra) { this.substituteExtra = substituteExtra; }
    public BigDecimal getTierBonus() { return tierBonus; }
    public void setTierBonus(BigDecimal tierBonus) { this.tierBonus = tierBonus; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}