package com.civicaudit.civicaudit_backend.runner;

import com.civicaudit.civicaudit_backend.entity.User;
import com.civicaudit.civicaudit_backend.repository.UserRepository;
import com.civicaudit.civicaudit_backend.service.AlerteService;
import com.civicaudit.civicaudit_backend.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportRunner.class);
    private final ImportService importService;
    private final AlerteService alerteService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${civicaudit.import.file:}")
    private String cheminFichier;

    @Value("${civicaudit.admin.email:admin@civicaudit.tn}")
    private String adminEmail;

    // Mot de passe initial de l'admin : à fournir par variable d'environnement ADMIN_PASSWORD en dehors du poste de dev
    @Value("${civicaudit.admin.password:}")
    private String adminPassword;

    public ImportRunner(ImportService importService, AlerteService alerteService, 
                        UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.importService = importService;
        this.alerteService = alerteService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Créer un utilisateur admin par défaut s'il n'existe pas
        creerAdminParDefaut();
        
        if (cheminFichier == null || cheminFichier.isBlank()) {
            return; // pas de fichier configuré → on ne fait rien au démarrage
        }
        log.info("=== Démarrage import DECP depuis {} ===", cheminFichier);
        ImportService.ImportReport rapport = importService.importCsv(Path.of(cheminFichier));

        log.info("=== Rapport d'import ===");
        log.info("Lignes lues        : {}", rapport.lignesLues);
        log.info("Hors périmètre     : {}", rapport.lignesHorsPerimetre);
        log.info("Marchés importés   : {}", rapport.marchesImportes);
        log.info("Marchés mis à jour : {}", rapport.marchesMisAJour);
        log.info("Marchés rejetés    : {}", rapport.marchesRejetes);
        rapport.raisonsRejet.forEach(r -> log.warn("  Rejet : {}", r));

        // Générer les alertes après l'import (CDC 3.5)
        log.info("=== Génération des alertes automatiques ===");
        int nombreAlertes = alerteService.genererAlertes();
        log.info("{} alertes générées", nombreAlertes);
    }
    
    private void creerAdminParDefaut() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("Aucun mot de passe admin configuré (civicaudit.admin.password) : pas de création de compte admin");
            return;
        }
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            log.info("=== Utilisateur ADMIN créé ===");
            log.info("Email: {} (mot de passe : valeur de civicaudit.admin.password)", adminEmail);
        } else {
            log.info("Utilisateur admin déjà existant");
        }
    }
}