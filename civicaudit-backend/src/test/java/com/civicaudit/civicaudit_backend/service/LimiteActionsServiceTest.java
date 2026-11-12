package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.exception.TropDActionsException;
import com.civicaudit.civicaudit_backend.repository.CommentaireRepository;
import com.civicaudit.civicaudit_backend.repository.SignalementRepository;
import com.civicaudit.civicaudit_backend.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Anti-spam : 10 actions par utilisateur et par heure (CDC 4). */
class LimiteActionsServiceTest {

    private CommentaireRepository commentaires;
    private SignalementRepository signalements;
    private VoteRepository votes;
    private LimiteActionsService service;

    @BeforeEach
    void setUp() {
        commentaires = mock(CommentaireRepository.class);
        signalements = mock(SignalementRepository.class);
        votes = mock(VoteRepository.class);
        service = new LimiteActionsService(commentaires, signalements, votes, 10);
    }

    private void actions(long nbCommentaires, long nbSignalements, long nbVotes) {
        when(commentaires.countByUserIdAndDateCreationAfter(eq(1L), any(LocalDateTime.class))).thenReturn(nbCommentaires);
        when(signalements.countByUserIdAndDateCreationAfter(eq(1L), any(LocalDateTime.class))).thenReturn(nbSignalements);
        when(votes.countByUserIdAndDateCreationAfter(eq(1L), any(LocalDateTime.class))).thenReturn(nbVotes);
    }

    @Test
    void neufActionsAutorisees() {
        actions(4, 3, 2);
        assertThatCode(() -> service.verifierQuota(1L)).doesNotThrowAnyException();
    }

    @Test
    void dixiemeActionRefusee() {
        actions(4, 3, 3);
        assertThatThrownBy(() -> service.verifierQuota(1L)).isInstanceOf(TropDActionsException.class);
    }
}
