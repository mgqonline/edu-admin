package com.eduadmin.scheduling.repo;

import com.eduadmin.scheduling.entity.ScheduleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleInfo, Long> {
    List<ScheduleInfo> findByClassId(Long classId);
    List<ScheduleInfo> findByTeacherId(Long teacherId);
    List<ScheduleInfo> findByCampusIdAndRoom(Long campusId, String room);
    List<ScheduleInfo> findByStudentId(Long studentId);
    List<ScheduleInfo> findByDateOnlyBetween(Date startDate, Date endDate);

    @Query("SELECT s FROM ScheduleInfo s WHERE s.teacherId = :teacherId AND s.startAt < :endAt AND s.endAt > :startAt")
    List<ScheduleInfo> findTeacherConflicts(@Param("teacherId") Long teacherId,
                                            @Param("startAt") Date startAt,
                                            @Param("endAt") Date endAt);

    @Query("SELECT s FROM ScheduleInfo s WHERE s.campusId = :campusId AND s.room = :room AND s.startAt < :endAt AND s.endAt > :startAt")
    List<ScheduleInfo> findRoomConflicts(@Param("campusId") Long campusId,
                                         @Param("room") String room,
                                         @Param("startAt") Date startAt,
                                         @Param("endAt") Date endAt);

    @Query("SELECT s FROM ScheduleInfo s WHERE s.studentId = :studentId AND s.startAt < :endAt AND s.endAt > :startAt")
    List<ScheduleInfo> findStudentConflicts(@Param("studentId") Long studentId,
                                            @Param("startAt") Date startAt,
                                            @Param("endAt") Date endAt);
}