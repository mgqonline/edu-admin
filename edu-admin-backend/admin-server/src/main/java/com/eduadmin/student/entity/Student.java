package com.eduadmin.student.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String gender; // male/female
    private String birthDate; // yyyy-MM-dd
    private String idType;
    private String idNumber;
    private String photoUrl;
    private Long campusId;

    @Column(columnDefinition = "TEXT")
    private String guardiansJson; // JSON 数组
    @Column(columnDefinition = "TEXT")
    private String originChannelsJson; // JSON 数组
    @Column(columnDefinition = "TEXT")
    private String interestsJson; // JSON 数组
    @Column(columnDefinition = "TEXT")
    private String studyTagsJson; // JSON 数组

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public String getGuardiansJson() { return guardiansJson; }
    public void setGuardiansJson(String guardiansJson) { this.guardiansJson = guardiansJson; }
    public String getOriginChannelsJson() { return originChannelsJson; }
    public void setOriginChannelsJson(String originChannelsJson) { this.originChannelsJson = originChannelsJson; }
    public String getInterestsJson() { return interestsJson; }
    public void setInterestsJson(String interestsJson) { this.interestsJson = interestsJson; }
    public String getStudyTagsJson() { return studyTagsJson; }
    public void setStudyTagsJson(String studyTagsJson) { this.studyTagsJson = studyTagsJson; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}