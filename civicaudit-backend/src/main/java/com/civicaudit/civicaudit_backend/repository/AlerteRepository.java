package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    List<Alerte> findByMarcheId(Long marcheId);
    void deleteByMarcheId(Long marcheId);
}
