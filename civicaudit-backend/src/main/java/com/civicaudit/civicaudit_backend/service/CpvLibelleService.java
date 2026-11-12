package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.CpvLibelle;
import com.civicaudit.civicaudit_backend.repository.CpvLibelleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Table de correspondance CPV « grandes catégories » (2 premiers chiffres) → libellé français (CDC 3.1 étape 4).
 * Chargée une seule fois au démarrage depuis le fichier de référence referentiel/cpv-divisions.csv
 * (divisions de la nomenclature CPV 2008), persistée dans la table cpv_libelle puis gardée en mémoire.
 */
@Service
@Order(0)
public class CpvLibelleService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CpvLibelleService.class);
    static final String FICHIER_REFERENTIEL = "referentiel/cpv-divisions.csv";

    private final CpvLibelleRepository repository;
    private volatile Map<String, String> libelles = Map.of();

    public CpvLibelleService(CpvLibelleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Map<String, String> referentiel = lireReferentiel();
        if (repository.count() < referentiel.size()) {
            List<CpvLibelle> entites = new ArrayList<>();
            referentiel.forEach((code, libelle) -> entites.add(new CpvLibelle(code, libelle)));
            repository.saveAll(entites);
            log.info("Référentiel CPV chargé en base : {} grandes catégories", entites.size());
        }
        Map<String, String> cache = new TreeMap<>();
        repository.findAll().forEach(c -> cache.put(c.getCode2Chiffres(), c.getLibelle()));
        this.libelles = Collections.unmodifiableMap(cache);
    }

    static Map<String, String> lireReferentiel() throws IOException {
        Map<String, String> referentiel = new LinkedHashMap<>();
        ClassPathResource ressource = new ClassPathResource(FICHIER_REFERENTIEL);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(ressource.getInputStream(), StandardCharsets.UTF_8))) {
            String ligne;
            boolean entete = true;
            while ((ligne = reader.readLine()) != null) {
                if (entete) { entete = false; continue; }
                int sep = ligne.indexOf(';');
                if (sep > 0) {
                    referentiel.put(ligne.substring(0, sep).trim(), ligne.substring(sep + 1).trim());
                }
            }
        }
        return referentiel;
    }

    /** Code de catégorie (2 chiffres) d'un code CPV complet, ou null. */
    public static String categorie(String cpvCode) {
        return (cpvCode == null || cpvCode.length() < 2) ? null : cpvCode.substring(0, 2);
    }

    /** Libellé de la grande catégorie d'un code CPV (complet ou à 2 chiffres). */
    public String libelle(String cpvCode) {
        String code = categorie(cpvCode);
        if (code == null) return ImportMarcheWriter.NON_RENSEIGNE;
        return libelles.getOrDefault(code, "Catégorie CPV " + code);
    }
}
