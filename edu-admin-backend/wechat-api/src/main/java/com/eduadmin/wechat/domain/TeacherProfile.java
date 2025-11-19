package com.eduadmin.wechat.domain;

public class TeacherProfile {
    private Long id;
    private Long teacherId;
    private String name;
    private String subjects;
    private Integer years;
    private String phone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }
    public Integer getYears() { return years; }
    public void setYears(Integer years) { this.years = years; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}