package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    
    @Query("SELECT s FROM Signalement s LEFT JOIN Vote v ON s.id = v.signalement.id " +
           "WHERE s.marche.id = :marcheId " +
           "GROUP BY s.id " +
           "ORDER BY COUNT(v.id) DESC, s.dateCreation DESC")
    List<Signalement> findByMarcheIdOrderByVotesDesc(@Param("marcheId") Long marcheId);
    
    boolean existsByUserIdAndMarcheIdAndCategorie(Long userId, Long marcheId, Signalement.Categorie categorie);
}
