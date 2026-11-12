package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.CommentaireDTO;
import com.civicaudit.civicaudit_backend.dto.SignalementDTO;
import com.civicaudit.civicaudit_backend.entity.Commentaire;
import com.civicaudit.civicaudit_backend.entity.Signalement;
import com.civicaudit.civicaudit_backend.repository.CommentaireRepository;
import com.civicaudit.civicaudit_backend.repository.SignalementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModerationService {

    private final CommentaireRepository commentaireRepository;
    private final SignalementRepository signalementRepository;

    public ModerationService(CommentaireRepository commentaireRepository,
                            SignalementRepository signalementRepository) {
        this.commentaireRepository = commentaireRepository;
        this.signalementRepository = signalementRepository;
    }

    public List<CommentaireDTO> getCommentairesEnAttente() {
        return commentaireRepository.findByStatutOrderByDateCreationDesc(Commentaire.Statut.EN_ATTENTE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<SignalementDTO> getTousSignalements() {
        return signalementRepository.findAllByOrderByDateCreationDesc()
                .stream()
                .map(this::toSignalementDTO)
                .collect(Collectors.toList());
    }

    public void approuverCommentaire(Long commentaireId) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));
        
        commentaire.setStatut(Commentaire.Statut.APPROUVE);
        commentaireRepository.save(commentaire);
    }

    public void rejeterCommentaire(Long commentaireId) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));
        
        commentaire.setStatut(Commentaire.Statut.REJETE);
        commentaireRepository.save(commentaire);
    }

    public void changerStatutSignalement(Long signalementId, String nouveauStatut) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new IllegalArgumentException("Signalement introuvable"));
        
        try {
            Signalement.Statut statut = Signalement.Statut.valueOf(nouveauStatut);
            signalement.setStatut(statut);
            signalementRepository.save(signalement);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide");
        }
    }

    private CommentaireDTO toDTO(Commentaire commentaire) {
        return new CommentaireDTO(
                commentaire.getId(),
                commentaire.getContenu(),
                commentaire.getStatut().name(),
                commentaire.getUser().getEmail(),
                commentaire.getUser().getId(),
                commentaire.getDateCreation()
        );
    }

    private SignalementDTO toSignalementDTO(Signalement signalement) {
        return new SignalementDTO(
                signalement.getId(),
                signalement.getCategorie().name(),
                signalement.getDescription(),
                signalement.getStatut().name(),
                signalement.getUser().getEmail(),
                signalement.getUser().getId(),
                signalement.getDateCreation(),
                0L // nombreVotes - pas nécessaire pour la modération
        );
    }
}
