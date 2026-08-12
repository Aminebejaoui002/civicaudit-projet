package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface MarchePublicRepository extends JpaRepository<MarchePublic, Long>,
        JpaSpecificationExecutor<MarchePublic> {
    boolean existsByUid(String uid);
    Optional<MarchePublic> findByUid(String uid);
}