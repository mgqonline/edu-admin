package com.eduadmin.baseplatform.dict.repo;

import com.eduadmin.baseplatform.dict.entity.DictItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictItemRepository extends JpaRepository<DictItem, Long> {
    List<DictItem> findByTypeOrderBySortOrderAsc(String type);
}