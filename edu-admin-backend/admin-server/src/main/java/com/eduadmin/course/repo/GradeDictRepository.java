package com.eduadmin.course.repo;

import com.eduadmin.course.entity.GradeDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeDictRepository extends JpaRepository<GradeDict, Long> {
    List<GradeDict> findByStatusOrderBySortOrderAsc(String status);
    List<GradeDict> findByCampusIdOrderBySortOrderAsc(Long campusId);
    List<GradeDict> findByCampusIdAndStatusOrderBySortOrderAsc(Long campusId, String status);
    Optional<GradeDict> findByCampusIdAndName(Long campusId, String name);
    List<GradeDict> findByNameContainingOrderBySortOrderAsc(String keyword);
    List<GradeDict> findByCampusIdAndNameContainingOrderBySortOrderAsc(Long campusId, String keyword);
}