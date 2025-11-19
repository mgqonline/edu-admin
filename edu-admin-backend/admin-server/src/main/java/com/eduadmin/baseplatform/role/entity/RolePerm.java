package com.eduadmin.baseplatform.role.entity;

import javax.persistence.*;

@Entity
@Table(name = "role_perm")
public class RolePerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private String value; // e.g. "menu:attendance" or "*"

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}