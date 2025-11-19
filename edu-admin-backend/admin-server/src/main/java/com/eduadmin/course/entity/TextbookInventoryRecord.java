package com.eduadmin.course.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "textbook_inventory_records", indexes = {
        @Index(name = "idx_tb_rec_textbookId", columnList = "textbookId")
})
public class TextbookInventoryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false)
    private String textbookId;

    @Column(length = 8)
    private String type; // in/out

    private Integer qty;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTextbookId() { return textbookId; }
    public void setTextbookId(String textbookId) { this.textbookId = textbookId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public Date getTime() { return time; }
    public void setTime(Date time) { this.time = time; }
}