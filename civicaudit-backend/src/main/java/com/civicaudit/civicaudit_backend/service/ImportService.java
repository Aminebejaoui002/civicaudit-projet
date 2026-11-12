package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.ImportError;
import com.civicaudit.civicaudit_backend.repository.ImportErrorRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Module decp-import (CDC 3.1) : lecture du fichier DECP consolidé (CSV), filtrage sur l'acheteur choisi,
 * regroupement des lignes par uid, puis écriture marché par marché via {@link ImportMarcheWriter}.
 * Chaque marché rejeté est journalisé dans la table import_error avec sa ligne brute et la raison.
 */
@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);
    private static final int TAILLE_MAX_LIGNE_BRUTE = 4000;

    private final ImportMarcheWriter writer;
    private final ImportErrorRepository importErrorRepository;

    /**
     * SIRET (acheteur_id) de la collectivité retenue (CDC 1.4). Vide = pas de filtrage
     * (cas d'un fichier déjà pré-filtré sur la commune).
     */
    private String acheteurIdFiltre;

    public ImportService(ImportMarcheWriter writer,
                         ImportErrorRepository importErrorRepository,
                         @Value("${civicaudit.import.acheteur-id:}") String acheteurIdFiltre) {
        this.writer = writer;
        this.importErrorRepository = importErrorRepository;
        this.acheteurIdFiltre = acheteurIdFiltre;
    }

    /** Rapport affiché après chaque import (CDC 3.1 étape 5). */
    public static class ImportReport {
        public String fichier;
        public int lignesLues = 0;
        public int lignesHorsPerimetre = 0;
        public int marchesTrouves = 0;
        public int marchesImportes = 0;
        public int marchesMisAJour = 0;
        public int marchesRejetes = 0;
        public final List<String> raisonsRejet = new ArrayList<>();

        public String getFichier() { return fichier; }
        public int getLignesLues() { return lignesLues; }
        public int getLignesHorsPerimetre() { return lignesHorsPerimetre; }
        public int getMarchesTrouves() { return marchesTrouves; }
        public int getMarchesImportes() { return marchesImportes; }
        public int getMarchesMisAJour() { return marchesMisAJour; }
        public int getMarchesRejetes() { return marchesRejetes; }
        public List<String> getRaisonsRejet() { return raisonsRejet; }
    }

    public ImportReport importCsv(Path fichier) throws IOException {
        try (Reader in = Files.newBufferedReader(fichier, StandardCharsets.UTF_8)) {
            return importCsv(in, fichier.getFileName().toString());
        }
    }

    public ImportReport importCsv(Reader in, String nomFichier) throws IOException {
        ImportReport rapport = new ImportReport();
        rapport.fichier = nomFichier;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (CSVParser parser = format.parse(sansBom(in))) {
            if (!parser.getHeaderMap().containsKey("id")) {
                throw new IllegalArgumentException("Format de fichier non reconnu : colonne 'id' (uid du marché) absente");
            }

            // Étape 2 (filtrage) + étape 3 (regroupement par uid)
            Map<String, List<CSVRecord>> parUid = new LinkedHashMap<>();
            for (CSVRecord record : parser) {
                rapport.lignesLues++;

                if (!estDansLePerimetre(record)) {
                    rapport.lignesHorsPerimetre++;
                    continue;
                }

                String uid = ImportMarcheWriter.valeur(record, "id");
                if (uid == null) {
                    journaliserRejet(null, List.of(record), "identifiant (id) du marché manquant", nomFichier, rapport);
                    continue;
                }
                parUid.computeIfAbsent(uid, k -> new ArrayList<>()).add(record);
            }
            rapport.marchesTrouves = parUid.size();

            // Étapes 4 à 6 : normalisation, rejet contrôlé, upsert
            for (Map.Entry<String, List<CSVRecord>> entry : parUid.entrySet()) {
                String uid = entry.getKey();
                try {
                    ImportMarcheWriter.Resultat resultat = writer.importUnMarche(uid, entry.getValue(), nomFichier);
                    if (resultat == ImportMarcheWriter.Resultat.CREE) {
                        rapport.marchesImportes++;
                    } else {
                        rapport.marchesMisAJour++;
                    }
                } catch (Exception e) {
                    String raison = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                    journaliserRejet(uid, entry.getValue(), raison, nomFichier, rapport);
                }
            }
        }

        log.info("Import {} : {} lignes lues, {} hors périmètre, {} marchés créés, {} mis à jour, {} rejetés",
                nomFichier, rapport.lignesLues, rapport.lignesHorsPerimetre,
                rapport.marchesImportes, rapport.marchesMisAJour, rapport.marchesRejetes);
        return rapport;
    }

    /** Ignore un éventuel BOM UTF-8 en tête de fichier (fréquent sur les CSV enregistrés depuis Excel). */
    private static Reader sansBom(Reader in) throws IOException {
        PushbackReader pushback = new PushbackReader(in, 1);
        int premier = pushback.read();
        if (premier != -1 && premier != '\uFEFF') {
            pushback.unread(premier);
        }
        return pushback;
    }

    private boolean estDansLePerimetre(CSVRecord record) {
        if (acheteurIdFiltre == null || acheteurIdFiltre.isBlank()) {
            return true;
        }
        return acheteurIdFiltre.trim().equals(ImportMarcheWriter.valeur(record, "acheteur_id"));
    }

    private void journaliserRejet(String uid, List<CSVRecord> lignes, String raison, String nomFichier,
                                  ImportReport rapport) {
        rapport.marchesRejetes++;
        rapport.raisonsRejet.add("uid=" + uid + " -> " + raison);
        log.warn("Marché rejeté (uid={}) : {}", uid, raison);

        String ligneBrute = String.join(",", lignes.get(0));
        if (ligneBrute.length() > TAILLE_MAX_LIGNE_BRUTE) {
            ligneBrute = ligneBrute.substring(0, TAILLE_MAX_LIGNE_BRUTE) + "…";
        }
        String raisonTronquee = raison.length() > 1000 ? raison.substring(0, 1000) : raison;
        try {
            importErrorRepository.save(new ImportError(uid, ligneBrute, raisonTronquee, nomFichier));
        } catch (Exception e) {
            log.error("Impossible d'enregistrer l'erreur d'import (uid={})", uid, e);
        }
    }

    public List<ImportError> dernieresErreurs() {
        return importErrorRepository.findTop100ByOrderByDateImportDesc();
    }

    // Utilisé par les tests unitaires
    void setAcheteurIdFiltre(String acheteurIdFiltre) {
        this.acheteurIdFiltre = acheteurIdFiltre;
    }
}
