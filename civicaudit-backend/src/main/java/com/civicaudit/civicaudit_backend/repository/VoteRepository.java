package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByUserIdAndSignalementId(Long userId, Long signalementId);
    long countBySignalementId(Long signalementId);

    // Anti-spam (CDC 4) : nombre d'actions d'un utilisateur depuis une date donnée
    long countByUserIdAndDateCreationAfter(Long userId, java.time.LocalDateTime depuis);
}
