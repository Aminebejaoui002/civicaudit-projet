package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Acheteur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcheteurRepository extends JpaRepository<Acheteur, String> {
}