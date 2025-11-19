package com.eduadmin.wechat.domain;

public class ScheduleItem {
    private Long id;
    private String date;
    private String className;
    private String startTime;
    private String endTime;
    private Long classId;
    private Long courseId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}