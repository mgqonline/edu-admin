package com.eduadmin.baseplatform.dict.entity;

import javax.persistence.*;

@Entity
@Table(name = "dict_item")
public class DictItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // e.g. courseCategory, classType, studentStatus, commonStatus

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private Integer sortOrder = 0;

    @Column(nullable = false)
    private String status = "enabled";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}