package com.eduadmin.student.grade.repo;

import com.eduadmin.student.grade.entity.StudentGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {
    List<StudentGrade> findByStudentId(Long studentId);
    List<StudentGrade> findBySubject(String subject);
    List<StudentGrade> findByTerm(String term);
    Page<StudentGrade> findByStudentIdAndSubjectContainingIgnoreCaseAndTermContainingIgnoreCase(Long studentId, String subject, String term, Pageable pageable);
    Page<StudentGrade> findBySubjectContainingIgnoreCaseAndTermContainingIgnoreCase(String subject, String term, Pageable pageable);
    List<StudentGrade> findByStudentIdAndSubjectContainingIgnoreCaseAndTermContainingIgnoreCase(Long studentId, String subject, String term);
    List<StudentGrade> findBySubjectContainingIgnoreCaseAndTermContainingIgnoreCase(String subject, String term);
}