package com.eduadmin.baseplatform.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomSeatRepository extends JpaRepository<ClassroomSeat, Long> {
    List<ClassroomSeat> findByClassroomIdOrderByRowIndexAscColIndexAsc(Long classroomId);
    void deleteByClassroomId(Long classroomId);
}