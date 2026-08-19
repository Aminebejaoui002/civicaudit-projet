package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByUserIdAndSignalementId(Long userId, Long signalementId);
    long countBySignalementId(Long signalementId);
}
