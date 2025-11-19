package com.eduadmin.baseplatform.classroom;

import javax.persistence.*;

@Entity
@Table(name = "classroom_seat")
public class ClassroomSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "row_index", nullable = false)
    private Integer rowIndex;

    @Column(name = "col_index", nullable = false)
    private Integer colIndex;

    @Column(name = "label", length = 32)
    private String label;

    @Column(name = "usable", nullable = false)
    private Boolean usable = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }

    public Integer getColIndex() { return colIndex; }
    public void setColIndex(Integer colIndex) { this.colIndex = colIndex; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getUsable() { return usable; }
    public void setUsable(Boolean usable) { this.usable = usable; }
}