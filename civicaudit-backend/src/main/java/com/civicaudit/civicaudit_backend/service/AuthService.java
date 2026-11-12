package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.AuthResponse;
import com.civicaudit.civicaudit_backend.dto.LoginRequest;
import com.civicaudit.civicaudit_backend.dto.RegisterRequest;
import com.civicaudit.civicaudit_backend.entity.User;
import com.civicaudit.civicaudit_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        user.setDateConsentement(LocalDateTime.now());

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    /**
     * Droit à l'effacement (RGPD, CDC 3.6) : soft delete + anonymisation.
     * <ul>
     *   <li>le compte est marqué supprimé et ne peut plus se connecter ;</li>
     *   <li>l'email est remplacé par une adresse technique non identifiante et le mot de passe est invalidé ;</li>
     *   <li>les commentaires et signalements restent en base et affichés, avec l'auteur « Utilisateur supprimé ».</li>
     * </ul>
     */
    @Transactional
    public void supprimerCompte(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        if (Boolean.TRUE.equals(user.getSupprime())) {
            return;
        }
        user.setSupprime(true);
        user.setDateSuppression(LocalDateTime.now());
        user.setEmail("utilisateur-supprime-" + user.getId() + "@civicaudit.invalid");
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        if (user.getSupprime()) {
            throw new IllegalArgumentException("Ce compte a été supprimé");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
