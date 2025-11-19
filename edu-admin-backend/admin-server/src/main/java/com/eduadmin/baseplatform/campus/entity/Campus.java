package com.eduadmin.baseplatform.campus.entity;

import javax.persistence.*;

@Entity
@Table(name = "campus")
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;
    private String manager;
    private String region;

    @Column(length = 2048)
    private String configJson; // 存储前端的 config 对象的 JSON 文本

    @Column(nullable = false)
    private String status = "enabled";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}