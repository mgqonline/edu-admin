package com.eduadmin.course.repo;

import com.eduadmin.course.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
    List<ClassInfo> findByStatusOrderBySortOrderAsc(String status);
    List<ClassInfo> findByCampusIdOrderBySortOrderAsc(Long campusId);
    List<ClassInfo> findByCampusIdAndStatusOrderBySortOrderAsc(Long campusId, String status);
    List<ClassInfo> findByCampusIdAndGradeIdOrderBySortOrderAsc(Long campusId, Long gradeId);
    List<ClassInfo> findByCampusIdAndGradeIdAndStatusOrderBySortOrderAsc(Long campusId, Long gradeId, String status);
    Optional<ClassInfo> findByCampusIdAndGradeIdAndName(Long campusId, Long gradeId, String name);
    List<ClassInfo> findByNameContainingOrderBySortOrderAsc(String keyword);
    List<ClassInfo> findByCampusIdAndNameContainingOrderBySortOrderAsc(Long campusId, String keyword);
    List<ClassInfo> findByCampusIdAndGradeIdAndNameContainingOrderBySortOrderAsc(Long campusId, Long gradeId, String keyword);

    @Modifying
    @Transactional
    @Query("update ClassInfo c set " +
            "c.mode = coalesce(c.mode, '线下'), " +
            "c.startDate = coalesce(c.startDate, ''), " +
            "c.endDate = coalesce(c.endDate, ''), " +
            "c.term = coalesce(c.term, ''), " +
            "c.state = coalesce(c.state, 'normal'), " +
            "c.headTeacherIds = coalesce(c.headTeacherIds, ''), " +
            "c.classroom = coalesce(c.classroom, ''), " +
            "c.capacityLimit = coalesce(c.capacityLimit, 0), " +
            "c.feeStandard = coalesce(c.feeStandard, ''), " +
            "c.feeStatus = coalesce(c.feeStatus, '未设置'), " +
            "c.tags = coalesce(c.tags, ''), " +
            "c.note = coalesce(c.note, ''), " +
            "c.parentGroup = coalesce(c.parentGroup, ''), " +
            "c.contacts = coalesce(c.contacts, '')")
    int backfillNullsWithDefaults();
}