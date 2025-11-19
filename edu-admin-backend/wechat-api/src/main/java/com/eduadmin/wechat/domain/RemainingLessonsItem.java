package com.eduadmin.wechat.domain;

public class RemainingLessonsItem {
    private Long courseId;
    private String courseName;
    private Integer remaining;
    private Integer attended;
    private Integer purchased;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Integer getRemaining() { return remaining; }
    public void setRemaining(Integer remaining) { this.remaining = remaining; }
    public Integer getAttended() { return attended; }
    public void setAttended(Integer attended) { this.attended = attended; }
    public Integer getPurchased() { return purchased; }
    public void setPurchased(Integer purchased) { this.purchased = purchased; }
}