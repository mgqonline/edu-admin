package com.eduadmin.course.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "classes")
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String classNo;
    private String name;
    private Long courseId;

    // 分组课：主/助教
    private Long teacherMainId;
    private Long teacherAssistantId;

    // 一对一：固定教师
    private Long fixedTeacherId;
    private Boolean flexibleTeacher;

    private Long exclusiveStudentId;

    private Long campusId;
    private String room;

    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Temporal(TemporalType.DATE)
    private Date endDate;

    // 存储为逗号分隔的周几，如 1,3,5
    private String weekDaysText;

    private Integer durationMinutes;
    private Integer maxSize;
    private Double fee;

    private String status; // not_started/ongoing/ended/suspended
    private String suspendReason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = new Date();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClassNo() { return classNo; }
    public void setClassNo(String classNo) { this.classNo = classNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getTeacherMainId() { return teacherMainId; }
    public void setTeacherMainId(Long teacherMainId) { this.teacherMainId = teacherMainId; }
    public Long getTeacherAssistantId() { return teacherAssistantId; }
    public void setTeacherAssistantId(Long teacherAssistantId) { this.teacherAssistantId = teacherAssistantId; }
    public Long getFixedTeacherId() { return fixedTeacherId; }
    public void setFixedTeacherId(Long fixedTeacherId) { this.fixedTeacherId = fixedTeacherId; }
    public Boolean getFlexibleTeacher() { return flexibleTeacher; }
    public void setFlexibleTeacher(Boolean flexibleTeacher) { this.flexibleTeacher = flexibleTeacher; }
    public Long getExclusiveStudentId() { return exclusiveStudentId; }
    public void setExclusiveStudentId(Long exclusiveStudentId) { this.exclusiveStudentId = exclusiveStudentId; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getWeekDaysText() { return weekDaysText; }
    public void setWeekDaysText(String weekDaysText) { this.weekDaysText = weekDaysText; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getMaxSize() { return maxSize; }
    public void setMaxSize(Integer maxSize) { this.maxSize = maxSize; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSuspendReason() { return suspendReason; }
    public void setSuspendReason(String suspendReason) { this.suspendReason = suspendReason; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}