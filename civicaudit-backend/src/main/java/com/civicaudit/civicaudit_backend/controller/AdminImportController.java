package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.entity.ImportError;
import com.civicaudit.civicaudit_backend.service.AlerteService;
import com.civicaudit.civicaudit_backend.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Import manuel des DECP (CDC 3.1) : l'administrateur télécharge le fichier consolidé sur data.gouv.fr
 * puis l'envoie ici. Les alertes automatiques sont recalculées après chaque import (CDC 3.5).
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Import des données DECP (rôle ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImportController {

    private final ImportService importService;
    private final AlerteService alerteService;

    public AdminImportController(ImportService importService, AlerteService alerteService) {
        this.importService = importService;
        this.alerteService = alerteService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer un fichier DECP (CSV)",
            description = "Import idempotent (upsert sur uid) ; retourne le rapport d'import et le nombre d'alertes recalculées.")
    public Map<String, Object> importer(@RequestParam("fichier") MultipartFile fichier) throws IOException {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier reçu");
        }
        String nom = fichier.getOriginalFilename() != null ? fichier.getOriginalFilename() : "import.csv";
        if (!nom.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Seuls les fichiers CSV sont acceptés");
        }

        ImportService.ImportReport rapport;
        try (Reader reader = new InputStreamReader(fichier.getInputStream(), StandardCharsets.UTF_8)) {
            rapport = importService.importCsv(reader, nom);
        }
        int nombreAlertes = alerteService.genererAlertes();

        return Map.of(
                "rapport", rapport,
                "alertesGenerees", nombreAlertes
        );
    }

    @GetMapping("/import/erreurs")
    @Operation(summary = "Dernières lignes rejetées à l'import (100 max)")
    public List<ImportError> erreurs() {
        return importService.dernieresErreurs();
    }
}
