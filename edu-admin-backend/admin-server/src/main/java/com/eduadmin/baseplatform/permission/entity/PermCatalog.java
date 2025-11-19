package com.eduadmin.baseplatform.permission.entity;

import javax.persistence.*;

@Entity
@Table(name = "perm_catalog")
public class PermCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 类型：menu / button / data
    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "label", nullable = false, length = 128)
    private String label;

    @Column(name = "value", nullable = false, length = 128, unique = true)
    private String value;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}