package com.eduadmin.course.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "class_info", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"campusId", "gradeId", "name"})
})
public class ClassInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long campusId;

    @Column(nullable = false)
    private Long gradeId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private String status = "enabled"; // enabled/disabled

    @Column(nullable = false)
    private Integer sortOrder = 0;

    // 业务扩展字段（前端需要展示）
    @Column(length = 32)
    private String mode; // 授课模式：线上/线下

    @Column(length = 32)
    private String startDate; // 信息展示字段：开班日期（YYYY-MM-DD）

    @Column(length = 32)
    private String endDate; // 信息展示字段：结课日期（YYYY-MM-DD）

    @Column(length = 64)
    private String term; // 学期描述，如 2024-2025 第一学期

    @Column(length = 32)
    private String state; // 班级状态：normal/preparing/paused/finished/dissolved

    @Column(length = 256)
    private String headTeacherIds; // 逗号分隔的教师ID，如 "12,15"

    @Column(length = 128)
    private String classroom; // 主教室名称（信息展示）

    // ===== 排课与教学相关字段（兼容原 classes 表） =====
    @Column
    private Long courseId; // 课程ID

    @Column(length = 64)
    private String subject; // 科目名称（冗余字段，便于直接展示）

    @Column
    private Long teacherMainId; // 主教师

    @Column
    private Long teacherAssistantId; // 助教

    @Column
    private Long fixedTeacherId; // 固定教师（一对一）

    @Column
    private Boolean flexibleTeacher; // 是否灵活调配教师

    @Column
    private Long exclusiveStudentId; // 一对一学员ID

    @Column(length = 64)
    private String room; // 排课教室

    @Temporal(TemporalType.DATE)
    private Date startDateAt; // 教学起始日期（用于排课范围）

    @Temporal(TemporalType.DATE)
    private Date endDateAt; // 教学结束日期（用于排课范围）

    @Column
    private Integer durationMinutes; // 单次课时长

    @Column
    private Integer maxSize; // 班额上限

    @Column
    private Integer enrolledCount; // 开班人数（持久化缓存，便于排序展示）

    @Column
    private Double fee; // 学费/课时费参考

    private Integer capacityLimit; // 班额限制

    @Column(length = 128)
    private String feeStandard; // 学费标准，如 "5000元/学期"

    @Column(length = 64)
    private String feeStatus; // 收费状态，如 "未设置"

    @Column(length = 256)
    private String tags; // 逗号分隔的标签，如 "实验班,平行班"

    @Column(length = 512)
    private String note; // 备注

    @Column(length = 128)
    private String parentGroup; // 家长群信息

    @Column(length = 128)
    private String contacts; // 联系人信息

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getHeadTeacherIds() { return headTeacherIds; }
    public void setHeadTeacherIds(String headTeacherIds) { this.headTeacherIds = headTeacherIds; }
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
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
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public Date getStartDateAt() { return startDateAt; }
    public void setStartDateAt(Date startDateAt) { this.startDateAt = startDateAt; }
    public Date getEndDateAt() { return endDateAt; }
    public void setEndDateAt(Date endDateAt) { this.endDateAt = endDateAt; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getMaxSize() { return maxSize; }
    public void setMaxSize(Integer maxSize) { this.maxSize = maxSize; }
    public Integer getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    public Integer getCapacityLimit() { return capacityLimit; }
    public void setCapacityLimit(Integer capacityLimit) { this.capacityLimit = capacityLimit; }
    public String getFeeStandard() { return feeStandard; }
    public void setFeeStandard(String feeStandard) { this.feeStandard = feeStandard; }
    public String getFeeStatus() { return feeStatus; }
    public void setFeeStatus(String feeStatus) { this.feeStatus = feeStatus; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getParentGroup() { return parentGroup; }
    public void setParentGroup(String parentGroup) { this.parentGroup = parentGroup; }
    public String getContacts() { return contacts; }
    public void setContacts(String contacts) { this.contacts = contacts; }
}