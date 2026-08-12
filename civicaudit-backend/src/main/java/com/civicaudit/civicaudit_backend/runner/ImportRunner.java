package com.civicaudit.civicaudit_backend.runner;

import com.civicaudit.civicaudit_backend.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportRunner.class);
    private final ImportService importService;

    @Value("${civicaudit.import.file:}")
    private String cheminFichier;

    public ImportRunner(ImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (cheminFichier == null || cheminFichier.isBlank()) {
            return; // pas de fichier configuré → on ne fait rien au démarrage
        }
        log.info("=== Démarrage import DECP depuis {} ===", cheminFichier);
        ImportService.ImportReport rapport = importService.importCsv(Path.of(cheminFichier));

        log.info("=== Rapport d'import ===");
        log.info("Lignes lues        : {}", rapport.lignesLues);
        log.info("Marchés importés   : {}", rapport.marchesImportes);
        log.info("Déjà existants     : {}", rapport.marchesIgnoresDejaExistants);
        log.info("Marchés rejetés    : {}", rapport.marchesRejetes);
        rapport.raisonsRejet.forEach(r -> log.warn("  Rejet : {}", r));
    }
}