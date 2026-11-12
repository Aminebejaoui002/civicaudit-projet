package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.LoginRequest;
import com.civicaudit.civicaudit_backend.dto.RegisterRequest;
import com.civicaudit.civicaudit_backend.entity.User;
import com.civicaudit.civicaudit_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Inscription avec consentement et droit à l'effacement (CDC 3.6 et 4). */
class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService service;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        JwtService jwt = new JwtService("cle-de-test-suffisamment-longue-pour-hmac-sha-256", 3_600_000L);
        service = new AuthService(userRepository, encoder, jwt);
    }

    @Test
    void lInscriptionEnregistreLaDateDeConsentement() {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("nouveau@example.fr");
        r.setPassword("motdepasse1");
        r.setConsentementRgpd(true);

        var reponse = service.register(r);

        assertThat(reponse.getRole()).isEqualTo("USER");
        assertThat(reponse.getToken()).isNotBlank();
    }

    @Test
    void laSuppressionDeCompteAnonymiseEtEmpecheLaConnexion() {
        User u = new User();
        u.setId(7L);
        u.setEmail("partant@example.fr");
        u.setPasswordHash(encoder.encode("motdepasse1"));
        when(userRepository.findByEmail("partant@example.fr")).thenReturn(Optional.of(u));

        service.supprimerCompte("partant@example.fr");

        assertThat(u.getSupprime()).isTrue();
        assertThat(u.getDateSuppression()).isNotNull();
        assertThat(u.getEmail()).isEqualTo("utilisateur-supprime-7@civicaudit.invalid");
        assertThat(encoder.matches("motdepasse1", u.getPasswordHash())).isFalse();
        assertThat(AuteurAffichage.nom(u)).isEqualTo("Utilisateur supprimé");

        // L'ancien email ne permet plus de se connecter
        when(userRepository.findByEmail("partant@example.fr")).thenReturn(Optional.empty());
        LoginRequest login = new LoginRequest();
        login.setEmail("partant@example.fr");
        login.setPassword("motdepasse1");
        assertThatThrownBy(() -> service.login(login)).isInstanceOf(IllegalArgumentException.class);
    }
}
