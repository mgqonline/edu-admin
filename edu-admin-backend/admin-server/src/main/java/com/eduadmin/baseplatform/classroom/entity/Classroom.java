package com.eduadmin.baseplatform.classroom.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "classroom", indexes = {
        @Index(name = "idx_classroom_campus", columnList = "campus_id")
})
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campus_id", nullable = false)
    private Long campusId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "room_code", length = 255)
    private String roomCode;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "usable_seats")
    private Integer usableSeats;

    @Column(name = "seat_rows")
    private Integer seatRows; // 座位行数

    @Column(name = "seat_cols")
    private Integer seatCols; // 座位列数

    @Column(name = "seat_map", length = 10000)
    private String seatMap; // JSON 字符串，二维数组，1=可用座位，0=禁用

    @Column(name = "status", length = 32, nullable = false)
    private String status = "enabled";

    @Column(name = "note", length = 2000)
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getUsableSeats() { return usableSeats; }
    public void setUsableSeats(Integer usableSeats) { this.usableSeats = usableSeats; }

    public Integer getSeatRows() { return seatRows; }
    public void setSeatRows(Integer seatRows) { this.seatRows = seatRows; }

    public Integer getSeatCols() { return seatCols; }
    public void setSeatCols(Integer seatCols) { this.seatCols = seatCols; }

    public String getSeatMap() { return seatMap; }
    public void setSeatMap(String seatMap) { this.seatMap = seatMap; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}