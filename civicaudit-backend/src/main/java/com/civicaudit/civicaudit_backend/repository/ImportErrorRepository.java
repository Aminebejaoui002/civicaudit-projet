package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.ImportError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportErrorRepository extends JpaRepository<ImportError, Long> {
    List<ImportError> findTop100ByOrderByDateImportDesc();
}
