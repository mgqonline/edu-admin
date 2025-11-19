package com.eduadmin.scheduling.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "system_holiday", indexes = {
        @Index(name = "idx_holiday_date", columnList = "dateOnly")
})
public class SystemHoliday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false, unique = true)
    private Date dateOnly;

    @Column(length = 128)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getDateOnly() { return dateOnly; }
    public void setDateOnly(Date dateOnly) { this.dateOnly = dateOnly; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}