package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.CommentaireDTO;
import com.civicaudit.civicaudit_backend.dto.CommentaireRequest;
import com.civicaudit.civicaudit_backend.entity.Commentaire;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.User;
import com.civicaudit.civicaudit_backend.repository.CommentaireRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final MarchePublicRepository marcheRepository;
    private final UserRepository userRepository;
    private final LimiteActionsService limiteActionsService;

    public CommentaireService(CommentaireRepository commentaireRepository,
                             MarchePublicRepository marcheRepository,
                             UserRepository userRepository,
                             LimiteActionsService limiteActionsService) {
        this.commentaireRepository = commentaireRepository;
        this.marcheRepository = marcheRepository;
        this.userRepository = userRepository;
        this.limiteActionsService = limiteActionsService;
    }

    public CommentaireDTO creerCommentaire(Long marcheId, CommentaireRequest request, String userEmail) {
        MarchePublic marche = marcheRepository.findById(marcheId)
                .orElseThrow(() -> new IllegalArgumentException("Marché introuvable"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        limiteActionsService.verifierQuota(user.getId());

        Commentaire commentaire = new Commentaire();
        commentaire.setMarche(marche);
        commentaire.setUser(user);
        commentaire.setContenu(request.getContenu().trim());
        commentaire.setStatut(Commentaire.Statut.EN_ATTENTE);

        commentaire = commentaireRepository.save(commentaire);
        return toDTO(commentaire, user.getId());
    }

    public List<CommentaireDTO> getCommentairesMarche(Long marcheId, String userEmail) {
        List<Commentaire> commentaires = commentaireRepository
                .findByMarcheIdAndSupprimeOrderByDateCreationDesc(marcheId, false);
        
        Long currentUserId = null;
        if (userEmail != null) {
            currentUserId = userRepository.findByEmail(userEmail)
                    .map(User::getId)
                    .orElse(null);
        }

        final Long userId = currentUserId;
        return commentaires.stream()
                .filter(c -> c.getStatut() == Commentaire.Statut.APPROUVE || 
                            (userId != null && c.getUser().getId().equals(userId)))
                .map(c -> toDTO(c, userId))
                .collect(Collectors.toList());
    }

    public void supprimerCommentaire(Long commentaireId, String userEmail) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!commentaire.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez supprimer que vos propres commentaires");
        }

        commentaire.setSupprime(true);
        commentaireRepository.save(commentaire);
    }

    // Vue publique : l'email de l'auteur n'est jamais exposé, seulement un pseudonyme
    // (ou « Utilisateur supprimé » si le compte a été supprimé — CDC 3.6).
    private CommentaireDTO toDTO(Commentaire commentaire, Long currentUserId) {
        CommentaireDTO dto = new CommentaireDTO(
                commentaire.getId(),
                commentaire.getContenu(),
                commentaire.getStatut().name(),
                null,
                commentaire.getUser().getId(),
                commentaire.getDateCreation()
        );
        dto.setAuteurNom(AuteurAffichage.nom(commentaire.getUser()));
        dto.setEstAuteur(currentUserId != null && currentUserId.equals(commentaire.getUser().getId()));
        return dto;
    }
}
