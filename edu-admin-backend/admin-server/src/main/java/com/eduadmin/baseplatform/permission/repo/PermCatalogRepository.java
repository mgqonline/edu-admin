package com.eduadmin.baseplatform.permission.repo;

import com.eduadmin.baseplatform.permission.entity.PermCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermCatalogRepository extends JpaRepository<PermCatalog, Long> {
    List<PermCatalog> findByTypeOrderBySortOrderAsc(String type);
    Optional<PermCatalog> findByValue(String value);
}