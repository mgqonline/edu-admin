package com.eduadmin.baseplatform.staff.entity;

import javax.persistence.*;

@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private Long deptId;

    @Column
    private Long campusId;

    @Column(nullable = false)
    private String status = "enabled";

    // 简化：单角色ID（与 /api/base/role/list 返回的 id 对应）
    @Column
    private String roleId;

    // 可选：为员工登录提供用户名/密码（演示用明文）
    @Column(unique = true)
    private String username;

    @Column
    private String password;

    @Column
    private String mobile;

    @Column
    private String roleIds;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getRoleIds() { return roleIds; }
    public void setRoleIds(String roleIds) { this.roleIds = roleIds; }
}