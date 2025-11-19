package com.eduadmin.course.repo;

import com.eduadmin.course.entity.SubjectDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectDictRepository extends JpaRepository<SubjectDict, Long> {
    List<SubjectDict> findByStatusOrderBySortOrderAsc(String status);
    Optional<SubjectDict> findByName(String name);
}