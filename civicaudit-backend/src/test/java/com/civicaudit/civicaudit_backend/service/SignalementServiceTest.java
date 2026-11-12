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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Règles du module Signalement & Vote (CDC 3.3.2 et 3.3.3). */
class SignalementServiceTest {

    private SignalementRepository signalementRepository;
    private VoteRepository voteRepository;
    private SignalementService service;

    private final MarchePublic marche = new MarchePublic();
    private final User citoyen = new User();

    @BeforeEach
    void setUp() {
        signalementRepository = mock(SignalementRepository.class);
        voteRepository = mock(VoteRepository.class);
        MarchePublicRepository marcheRepository = mock(MarchePublicRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        LimiteActionsService limite = mock(LimiteActionsService.class);

        marche.setId(10L);
        citoyen.setId(1L);
        citoyen.setEmail("citoyen@example.fr");

        when(marcheRepository.findById(10L)).thenReturn(Optional.of(marche));
        when(userRepository.findByEmail("citoyen@example.fr")).thenReturn(Optional.of(citoyen));
        when(signalementRepository.save(any(Signalement.class))).thenAnswer(inv -> {
            Signalement s = inv.getArgument(0);
            if (s.getId() == null) s.setId(100L);
            return s;
        });

        service = new SignalementService(signalementRepository, marcheRepository, userRepository, voteRepository, limite);
    }

    private static SignalementRequest requete(String categorie) {
        SignalementRequest r = new SignalementRequest();
        r.setCategorie(categorie);
        r.setDescription("Le montant me semble élevé comparé aux marchés similaires de la commune.");
        return r;
    }

    @Test
    @DisplayName("Impossible de signaler deux fois la même catégorie sur le même marché")
    void refuseUnDoubleSignalementDansLaMemeCategorie() {
        when(signalementRepository.existsByUserIdAndMarcheIdAndCategorie(1L, 10L, Signalement.Categorie.MONTANT_ANORMAL))
                .thenReturn(true);

        assertThatThrownBy(() -> service.creerSignalement(10L, requete("MONTANT_ANORMAL"), "citoyen@example.fr"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà signalé");
        verify(signalementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Le même marché peut être signalé dans une autre catégorie")
    void accepteUneAutreCategorie() {
        when(signalementRepository.existsByUserIdAndMarcheIdAndCategorie(1L, 10L, Signalement.Categorie.DELAI_SUSPECT))
                .thenReturn(false);

        SignalementDTO dto = service.creerSignalement(10L, requete("DELAI_SUSPECT"), "citoyen@example.fr");

        assertThat(dto.getCategorie()).isEqualTo("DELAI_SUSPECT");
        assertThat(dto.getStatut()).isEqualTo("NOUVEAU");
        assertThat(dto.getAuteurEmail()).isNull(); // l'email n'est jamais exposé publiquement
        assertThat(dto.getAuteurNom()).isEqualTo("cit***");
    }

    @Test
    @DisplayName("Une catégorie hors liste fermée est refusée")
    void refuseUneCategorieInconnue() {
        assertThatThrownBy(() -> service.creerSignalement(10L, requete("CORRUPTION"), "citoyen@example.fr"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Catégorie invalide");
    }

    @Test
    @DisplayName("Un utilisateur ne vote qu'une fois par signalement")
    void refuseUnDoubleVote() {
        Signalement s = new Signalement();
        s.setId(5L);
        when(signalementRepository.findById(5L)).thenReturn(Optional.of(s));
        when(voteRepository.existsByUserIdAndSignalementId(1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.voterSignalement(5L, "citoyen@example.fr"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(voteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Au 3e vote, un signalement NOUVEAU passe automatiquement EN_DISCUSSION")
    void passeEnDiscussionAuTroisiemeVote() {
        Signalement s = new Signalement();
        s.setId(5L);
        s.setStatut(Signalement.Statut.NOUVEAU);
        when(signalementRepository.findById(5L)).thenReturn(Optional.of(s));
        when(voteRepository.existsByUserIdAndSignalementId(anyLong(), eq(5L))).thenReturn(false);
        when(voteRepository.countBySignalementId(5L)).thenReturn(3L);

        service.voterSignalement(5L, "citoyen@example.fr");

        verify(voteRepository).save(any(Vote.class));
        assertThat(s.getStatut()).isEqualTo(Signalement.Statut.EN_DISCUSSION);
    }

    @Test
    @DisplayName("Avant 3 votes, le statut ne change pas")
    void resteNouveauAvantTroisVotes() {
        Signalement s = new Signalement();
        s.setId(5L);
        s.setStatut(Signalement.Statut.NOUVEAU);
        when(signalementRepository.findById(5L)).thenReturn(Optional.of(s));
        when(voteRepository.countBySignalementId(5L)).thenReturn(2L);

        service.voterSignalement(5L, "citoyen@example.fr");

        assertThat(s.getStatut()).isEqualTo(Signalement.Statut.NOUVEAU);
    }
}
