package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.CommentaireDTO;
import com.civicaudit.civicaudit_backend.entity.Commentaire;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.User;
import com.civicaudit.civicaudit_backend.repository.CommentaireRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Règle d'affichage des commentaires (CDC 3.3.1) et anonymisation (CDC 3.6). */
class CommentaireServiceTest {

    private final User alice = utilisateur(1L, "alice@example.fr");
    private final User bob = utilisateur(2L, "bob@example.fr");
    private final User carole = utilisateur(3L, "carole@example.fr");

    private CommentaireService service;

    private static User utilisateur(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    private static Commentaire commentaire(long id, User auteur, Commentaire.Statut statut) {
        Commentaire c = new Commentaire();
        c.setId(id);
        c.setUser(auteur);
        c.setStatut(statut);
        c.setContenu("Commentaire numéro " + id);
        c.setMarche(new MarchePublic());
        return c;
    }

    @BeforeEach
    void setUp() {
        CommentaireRepository commentaireRepository = mock(CommentaireRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(commentaireRepository.findByMarcheIdAndSupprimeOrderByDateCreationDesc(10L, false)).thenReturn(List.of(
                commentaire(1, alice, Commentaire.Statut.APPROUVE),
                commentaire(2, bob, Commentaire.Statut.EN_ATTENTE),
                commentaire(3, bob, Commentaire.Statut.REJETE)));
        when(userRepository.findByEmail("bob@example.fr")).thenReturn(Optional.of(bob));
        when(userRepository.findByEmail("carole@example.fr")).thenReturn(Optional.of(carole));

        service = new CommentaireService(commentaireRepository, mock(MarchePublicRepository.class),
                userRepository, mock(LimiteActionsService.class));
    }

    @Test
    @DisplayName("Un visiteur anonyme ne voit que les commentaires approuvés")
    void anonymeNeVoitQueLesApprouves() {
        assertThat(service.getCommentairesMarche(10L, null))
                .extracting(CommentaireDTO::getId).containsExactly(1L);
    }

    @Test
    @DisplayName("Un commentaire non approuvé n'apparaît jamais pour un autre utilisateur que son auteur")
    void unAutreUtilisateurNeVoitPasLesCommentairesEnAttente() {
        assertThat(service.getCommentairesMarche(10L, "carole@example.fr"))
                .extracting(CommentaireDTO::getId).containsExactly(1L);
    }

    @Test
    @DisplayName("L'auteur voit toujours ses propres commentaires, quel que soit leur statut")
    void lAuteurVoitSesCommentaires() {
        List<CommentaireDTO> vus = service.getCommentairesMarche(10L, "bob@example.fr");

        assertThat(vus).extracting(CommentaireDTO::getId).containsExactly(1L, 2L, 3L);
        assertThat(vus.get(1).getEstAuteur()).isTrue();
        assertThat(vus.get(1).getStatut()).isEqualTo("EN_ATTENTE");
    }

    @Test
    @DisplayName("Les commentaires d'un compte supprimé restent affichés sous « Utilisateur supprimé »")
    void compteSupprimeAnonymise() {
        alice.setSupprime(true);

        CommentaireDTO dto = service.getCommentairesMarche(10L, null).get(0);

        assertThat(dto.getContenu()).isEqualTo("Commentaire numéro 1");
        assertThat(dto.getAuteurNom()).isEqualTo("Utilisateur supprimé");
        assertThat(dto.getAuteurEmail()).isNull();
    }
}
