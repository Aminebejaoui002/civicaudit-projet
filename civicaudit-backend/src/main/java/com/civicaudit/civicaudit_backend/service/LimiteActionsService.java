package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.exception.TropDActionsException;
import com.civicaudit.civicaudit_backend.repository.CommentaireRepository;
import com.civicaudit.civicaudit_backend.repository.SignalementRepository;
import com.civicaudit.civicaudit_backend.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Protection anti-spam (CDC section 4) : au plus N actions (commentaires + signalements + votes)
 * par utilisateur et par heure. Implémentation volontairement simple, par comptage en base
 * des actions horodatées de l'utilisateur sur la dernière heure (pas de solution externe).
 */
@Service
public class LimiteActionsService {

    private final CommentaireRepository commentaireRepository;
    private final SignalementRepository signalementRepository;
    private final VoteRepository voteRepository;
    private final int maxActionsParHeure;

    public LimiteActionsService(CommentaireRepository commentaireRepository,
                                SignalementRepository signalementRepository,
                                VoteRepository voteRepository,
                                @Value("${civicaudit.anti-spam.max-actions-par-heure:10}") int maxActionsParHeure) {
        this.commentaireRepository = commentaireRepository;
        this.signalementRepository = signalementRepository;
        this.voteRepository = voteRepository;
        this.maxActionsParHeure = maxActionsParHeure;
    }

    public void verifierQuota(Long userId) {
        LocalDateTime ilYAUneHeure = LocalDateTime.now().minusHours(1);
        long actions = commentaireRepository.countByUserIdAndDateCreationAfter(userId, ilYAUneHeure)
                + signalementRepository.countByUserIdAndDateCreationAfter(userId, ilYAUneHeure)
                + voteRepository.countByUserIdAndDateCreationAfter(userId, ilYAUneHeure);
        if (actions >= maxActionsParHeure) {
            throw new TropDActionsException("Limite atteinte : " + maxActionsParHeure
                    + " actions (commentaires, signalements, votes) par heure. Réessayez plus tard.");
        }
    }
}
