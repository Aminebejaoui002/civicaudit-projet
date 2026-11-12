package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MarchePublicRepository extends JpaRepository<MarchePublic, Long>,
        JpaSpecificationExecutor<MarchePublic> {
    boolean existsByUid(String uid);
    Optional<MarchePublic> findByUid(String uid);

    // Valeurs présentes dans les données, pour alimenter les listes déroulantes de filtres (CDC 3.2.1)
    @Query("SELECT DISTINCT m.procedure FROM MarchePublic m WHERE m.procedure IS NOT NULL ORDER BY m.procedure")
    List<String> findDistinctProcedures();

    @Query("SELECT DISTINCT SUBSTRING(m.cpvCode, 1, 2) FROM MarchePublic m WHERE m.cpvCode IS NOT NULL")
    List<String> findDistinctCategoriesCpv();
}