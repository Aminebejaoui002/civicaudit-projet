package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.SignalementDTO;
import com.civicaudit.civicaudit_backend.dto.SignalementRequest;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Signalement;
import com.civicaudit.civicaudit_backend.entity.User;
import com.civicaudit.civicaudit_backend.entity.Vote;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.SignalementRepository;
import com.civicaudit.civicaudit_backend.repository.UserRepository;
import com.civicaudit.civicaudit_backend.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SignalementService {

    private final SignalementRepository signalementRepository;
    private final MarchePublicRepository marcheRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final LimiteActionsService limiteActionsService;

    /** Nombre de votes à partir duquel un signalement NOUVEAU passe automatiquement EN_DISCUSSION (CDC 3.3.2). */
    static final long SEUIL_VOTES_EN_DISCUSSION = 3;

    public SignalementService(SignalementRepository signalementRepository,
                             MarchePublicRepository marcheRepository,
                             UserRepository userRepository,
                             VoteRepository voteRepository,
                             LimiteActionsService limiteActionsService) {
        this.signalementRepository = signalementRepository;
        this.marcheRepository = marcheRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.limiteActionsService = limiteActionsService;
    }

    public SignalementDTO creerSignalement(Long marcheId, SignalementRequest request, String userEmail) {
        MarchePublic marche = marcheRepository.findById(marcheId)
                .orElseThrow(() -> new IllegalArgumentException("Marché introuvable"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Signalement.Categorie categorie;
        try {
            categorie = Signalement.Categorie.valueOf(request.getCategorie());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Catégorie invalide");
        }

        // Vérifier si l'utilisateur n'a pas déjà signalé ce marché dans cette catégorie
        if (signalementRepository.existsByUserIdAndMarcheIdAndCategorie(user.getId(), marcheId, categorie)) {
            throw new IllegalArgumentException("Vous avez déjà signalé ce marché dans cette catégorie");
        }

        limiteActionsService.verifierQuota(user.getId());

        Signalement signalement = new Signalement();
        signalement.setMarche(marche);
        signalement.setUser(user);
        signalement.setCategorie(categorie);
        signalement.setDescription(request.getDescription().trim());
        signalement.setStatut(Signalement.Statut.NOUVEAU);

        signalement = signalementRepository.save(signalement);
        return toDTO(signalement, user.getId());
    }

    public List<SignalementDTO> getSignalementsMarche(Long marcheId, String userEmail) {
        List<Signalement> signalements = signalementRepository.findByMarcheIdOrderByVotesDesc(marcheId);
        
        Long currentUserId = null;
        if (userEmail != null) {
            currentUserId = userRepository.findByEmail(userEmail)
                    .map(User::getId)
                    .orElse(null);
        }

        final Long userId = currentUserId;
        return signalements.stream()
                .map(s -> toDTO(s, userId))
                .collect(Collectors.toList());
    }

    public void voterSignalement(Long signalementId, String userEmail) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new IllegalArgumentException("Signalement introuvable"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Vérifier si l'utilisateur n'a pas déjà voté
        if (voteRepository.existsByUserIdAndSignalementId(user.getId(), signalementId)) {
            throw new IllegalArgumentException("Vous avez déjà voté pour ce signalement");
        }

        limiteActionsService.verifierQuota(user.getId());

        Vote vote = new Vote();
        vote.setSignalement(signalement);
        vote.setUser(user);
        voteRepository.save(vote);

        // Passer automatiquement EN_DISCUSSION si 3+ votes
        long nombreVotes = voteRepository.countBySignalementId(signalementId);
        if (nombreVotes >= SEUIL_VOTES_EN_DISCUSSION && signalement.getStatut() == Signalement.Statut.NOUVEAU) {
            signalement.setStatut(Signalement.Statut.EN_DISCUSSION);
            signalementRepository.save(signalement);
        }
    }

    private SignalementDTO toDTO(Signalement signalement, Long currentUserId) {
        long nombreVotes = voteRepository.countBySignalementId(signalement.getId());
        boolean aVote = currentUserId != null && 
                       voteRepository.existsByUserIdAndSignalementId(currentUserId, signalement.getId());

        SignalementDTO dto = new SignalementDTO(
                signalement.getId(),
                signalement.getCategorie().name(),
                signalement.getDescription(),
                signalement.getStatut().name(),
                null, // email jamais exposé publiquement
                signalement.getUser().getId(),
                signalement.getDateCreation(),
                nombreVotes
        );
        dto.setAVote(aVote);
        dto.setAuteurNom(AuteurAffichage.nom(signalement.getUser()));
        return dto;
    }
}
