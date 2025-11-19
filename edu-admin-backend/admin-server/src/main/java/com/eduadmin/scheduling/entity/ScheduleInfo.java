package com.eduadmin.scheduling.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "schedule_info", indexes = {
        @Index(name = "idx_schedule_teacher", columnList = "teacherId,startAt"),
        @Index(name = "idx_schedule_room", columnList = "campusId,room,startAt"),
        @Index(name = "idx_schedule_student", columnList = "studentId,startAt"),
        @Index(name = "idx_schedule_class", columnList = "classId,startAt")
})
public class ScheduleInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long classId;

    @Column
    private Long courseId;

    @Column
    private Long teacherId;

    @Column
    private Long campusId;

    @Column(length = 64)
    private String room;

    @Column
    private Long studentId; // 一对一场景可填写

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date startAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date endAt;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dateOnly; // 冗余，便于按日期查询

    @Column(length = 8)
    private String startTimeText; // HH:mm

    @Column(length = 8)
    private String endTimeText; // HH:mm

    @Column(length = 32)
    private String status = "scheduled"; // scheduled/adjusted/cancelled/completed

    @Column(length = 32)
    private String source; // batch_month / batch_week / manual

    @Column(length = 16)
    private String classType; // 1=班课, 2=一对一

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Date getStartAt() { return startAt; }
    public void setStartAt(Date startAt) { this.startAt = startAt; }
    public Date getEndAt() { return endAt; }
    public void setEndAt(Date endAt) { this.endAt = endAt; }
    public Date getDateOnly() { return dateOnly; }
    public void setDateOnly(Date dateOnly) { this.dateOnly = dateOnly; }
    public String getStartTimeText() { return startTimeText; }
    public void setStartTimeText(String startTimeText) { this.startTimeText = startTimeText; }
    public String getEndTimeText() { return endTimeText; }
    public void setEndTimeText(String endTimeText) { this.endTimeText = endTimeText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}