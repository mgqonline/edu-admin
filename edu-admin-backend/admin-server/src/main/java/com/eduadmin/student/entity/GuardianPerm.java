package com.eduadmin.student.entity;

import javax.persistence.*;

@Entity
@Table(name = "guardian_perm")
public class GuardianPerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private String guardianName;

    @Column(nullable = false)
    private String guardianPhone;

    @Column(nullable = true)
    private String relation;

    @Column(name = "view_perms_json", length = 2048)
    private String viewPermsJson;

    @Column(name = "action_perms_json", length = 2048)
    private String actionPermsJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getViewPermsJson() { return viewPermsJson; }
    public void setViewPermsJson(String viewPermsJson) { this.viewPermsJson = viewPermsJson; }
    public String getActionPermsJson() { return actionPermsJson; }
    public void setActionPermsJson(String actionPermsJson) { this.actionPermsJson = actionPermsJson; }
}