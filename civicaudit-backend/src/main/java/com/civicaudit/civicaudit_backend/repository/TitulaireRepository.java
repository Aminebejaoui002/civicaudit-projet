package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Titulaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TitulaireRepository extends JpaRepository<Titulaire, String> {
}