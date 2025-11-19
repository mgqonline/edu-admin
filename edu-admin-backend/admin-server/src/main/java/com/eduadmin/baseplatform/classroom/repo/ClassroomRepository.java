package com.eduadmin.baseplatform.classroom.repo;

import com.eduadmin.baseplatform.classroom.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long>, JpaSpecificationExecutor<Classroom> {
    List<Classroom> findByCampusId(Long campusId);
    List<Classroom> findByNameContaining(String name);
    List<Classroom> findByRoomCodeContaining(String roomCode);
}