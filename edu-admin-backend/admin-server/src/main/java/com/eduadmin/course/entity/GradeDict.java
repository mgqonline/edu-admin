package com.eduadmin.course.entity;

import javax.persistence.*;

@Entity
@Table(name = "grade_dict", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"campusId", "name"})
})
public class GradeDict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long campusId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private String status = "enabled"; // enabled/disabled

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}