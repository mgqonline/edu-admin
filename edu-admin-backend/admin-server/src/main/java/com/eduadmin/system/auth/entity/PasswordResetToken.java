package com.eduadmin.system.auth.entity;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Instant expireAt;

    @Column(nullable = false)
    private boolean used = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Instant getExpireAt() { return expireAt; }
    public void setExpireAt(Instant expireAt) { this.expireAt = expireAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}