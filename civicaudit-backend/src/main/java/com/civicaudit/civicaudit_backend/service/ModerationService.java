package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.CommentaireDTO;
import com.civicaudit.civicaudit_backend.dto.SignalementDTO;
import com.civicaudit.civicaudit_backend.entity.Commentaire;
import com.civicaudit.civicaudit_backend.entity.Signalement;
import com.civicaudit.civicaudit_backend.repository.CommentaireRepository;
import com.civicaudit.civicaudit_backend.repository.SignalementRepository;
import com.civicaudit.civicaudit_backend.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModerationService {

    private final CommentaireRepository commentaireRepository;
    private final SignalementRepository signalementRepository;
    private final VoteRepository voteRepository;

    public ModerationService(CommentaireRepository commentaireRepository,
                             SignalementRepository signalementRepository,
                             VoteRepository voteRepository) {
        this.commentaireRepository = commentaireRepository;
        this.signalementRepository = signalementRepository;
        this.voteRepository = voteRepository;
    }

    public List<CommentaireDTO> getCommentairesEnAttente() {
        return commentaireRepository.findByStatutOrderByDateCreationDesc(Commentaire.Statut.EN_ATTENTE)
                .stream()
                .filter(c -> !Boolean.TRUE.equals(c.getSupprime()))
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
        changerStatutCommentaire(commentaireId, Commentaire.Statut.APPROUVE.name());
    }

    /** Un commentaire rejeté n'est jamais affiché mais reste en base pour traçabilité (CDC 3.4). */
    public void rejeterCommentaire(Long commentaireId) {
        changerStatutCommentaire(commentaireId, Commentaire.Statut.REJETE.name());
    }

    public void changerStatutCommentaire(Long commentaireId, String nouveauStatut) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));
        Commentaire.Statut statut = parseEnum(Commentaire.Statut.class, nouveauStatut, "Statut de commentaire invalide");
        commentaire.setStatut(statut);
        commentaireRepository.save(commentaire);
    }

    public void changerStatutSignalement(Long signalementId, String nouveauStatut) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new IllegalArgumentException("Signalement introuvable"));
        Signalement.Statut statut = parseEnum(Signalement.Statut.class, nouveauStatut, "Statut de signalement invalide");
        signalement.setStatut(statut);
        signalementRepository.save(signalement);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String valeur, String messageErreur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(messageErreur);
        }
        try {
            return Enum.valueOf(type, valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(messageErreur);
        }
    }

    // Les modérateurs voient l'email de l'auteur (nécessaire pour traiter une contestation) ;
    // le public ne voit qu'un pseudonyme (voir AuteurAffichage).
    private CommentaireDTO toDTO(Commentaire commentaire) {
        CommentaireDTO dto = new CommentaireDTO(
                commentaire.getId(),
                commentaire.getContenu(),
                commentaire.getStatut().name(),
                commentaire.getUser().getEmail(),
                commentaire.getUser().getId(),
                commentaire.getDateCreation()
        );
        dto.setAuteurNom(AuteurAffichage.nom(commentaire.getUser()));
        dto.setMarcheId(commentaire.getMarche().getId());
        dto.setMarcheObjet(commentaire.getMarche().getObjet());
        return dto;
    }

    private SignalementDTO toSignalementDTO(Signalement signalement) {
        SignalementDTO dto = new SignalementDTO(
                signalement.getId(),
                signalement.getCategorie().name(),
                signalement.getDescription(),
                signalement.getStatut().name(),
                signalement.getUser().getEmail(),
                signalement.getUser().getId(),
                signalement.getDateCreation(),
                voteRepository.countBySignalementId(signalement.getId())
        );
        dto.setAuteurNom(AuteurAffichage.nom(signalement.getUser()));
        dto.setMarcheId(signalement.getMarche().getId());
        dto.setMarcheObjet(signalement.getMarche().getObjet());
        return dto;
    }
}
