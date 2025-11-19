package com.eduadmin.notify.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_message")
public class UserMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "text", length = 500)
    private String text;

    @Column(name = "is_read")
    private Boolean read = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}