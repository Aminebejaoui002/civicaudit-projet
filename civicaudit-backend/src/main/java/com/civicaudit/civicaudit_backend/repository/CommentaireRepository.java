package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByMarcheIdAndSupprimeOrderByDateCreationDesc(Long marcheId, Boolean supprime);
    List<Commentaire> findByStatutOrderByDateCreationDesc(Commentaire.Statut statut);
    List<Commentaire> findByUserIdAndSupprimeOrderByDateCreationDesc(Long userId, Boolean supprime);
}
