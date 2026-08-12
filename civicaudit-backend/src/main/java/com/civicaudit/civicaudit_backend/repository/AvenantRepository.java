package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Avenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvenantRepository extends JpaRepository<Avenant, Long> {
    List<Avenant> findByMarchePublicIdOrderByDateAvenantDesc(Long marcheId);
}