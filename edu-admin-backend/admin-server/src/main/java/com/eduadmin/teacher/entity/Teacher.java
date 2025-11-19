package com.eduadmin.teacher.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "teacher")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联教职工（staff）ID，可为空表示仅教师信息
    @Column
    private Long staffId;

    @Column(nullable = false)
    private String name;

    // 授课科目（逗号分隔或JSON存储的简化字符串）
    @Column(length = 1000)
    private String subjects; // 如："语文,数学,英语"

    // 授课班级（存储班级ID或名称的逗号分隔字符串）
    @Column(length = 1000)
    private String classes; // 如："一年级一班,一年级二班" 或 "101,102"

    // 课时费单价
    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column
    private String status = "enabled";

    // ===== 新增基础信息字段 =====
    @Column(length = 8)
    private String gender; // 男/女

    @Column(length = 20)
    private String mobile;

    @Temporal(TemporalType.DATE)
    private Date joinDate;

    @Column
    private Long campusId;

    @Column
    private Long deptId;

    @Column(length = 16)
    private String level; // 初级/中级/高级

    // ===== 资质信息 =====
    @Column(length = 64)
    private String qualificationCertNo; // 教师资格证编号

    @Column(length = 2000)
    private String trainingExperience; // 培训经历描述

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public String getClasses() { return classes; }
    public void setClasses(String classes) { this.classes = classes; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getQualificationCertNo() { return qualificationCertNo; }
    public void setQualificationCertNo(String qualificationCertNo) { this.qualificationCertNo = qualificationCertNo; }
    public String getTrainingExperience() { return trainingExperience; }
    public void setTrainingExperience(String trainingExperience) { this.trainingExperience = trainingExperience; }
}