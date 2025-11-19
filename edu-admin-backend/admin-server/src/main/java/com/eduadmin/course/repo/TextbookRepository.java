package com.eduadmin.course.repo;

import com.eduadmin.course.entity.Textbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TextbookRepository extends JpaRepository<Textbook, Long> {
    Optional<Textbook> findByTextbookId(String textbookId);
    List<Textbook> findByNameContaining(String keyword);
    List<Textbook> findByPublisherContaining(String keyword);
    List<Textbook> findByNameContainingOrPublisherContaining(String name, String publisher);
    List<Textbook> findByStockLessThanEqual(Integer threshold);
}