package com.eduadmin.wechat.domain;

import java.util.Date;

public class TeacherRoomRequest {
    private Long id;
    private Long teacherId;
    private Date dateOnly;
    private String startTimeText;
    private String endTimeText;
    private Integer capacity;
    private String reason;
    private String status;
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Date getDateOnly() { return dateOnly; }
    public void setDateOnly(Date dateOnly) { this.dateOnly = dateOnly; }
    public String getStartTimeText() { return startTimeText; }
    public void setStartTimeText(String startTimeText) { this.startTimeText = startTimeText; }
    public String getEndTimeText() { return endTimeText; }
    public void setEndTimeText(String endTimeText) { this.endTimeText = endTimeText; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}