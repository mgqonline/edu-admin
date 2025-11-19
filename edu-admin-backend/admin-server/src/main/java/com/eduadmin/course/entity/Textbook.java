package com.eduadmin.course.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "textbooks", indexes = {
        @Index(name = "idx_textbook_id", columnList = "textbookId", unique = true)
})
public class Textbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false, unique = true)
    private String textbookId;

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String publisher;

    private Double unitPrice;

    private Integer stock;

    @Column(name = "course_ids", length = 2000)
    private String courseIdsPlain; // 以逗号分隔的课程ID列表

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTextbookId() { return textbookId; }
    public void setTextbookId(String textbookId) { this.textbookId = textbookId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCourseIdsPlain() { return courseIdsPlain; }
    public void setCourseIdsPlain(String courseIdsPlain) { this.courseIdsPlain = courseIdsPlain; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}