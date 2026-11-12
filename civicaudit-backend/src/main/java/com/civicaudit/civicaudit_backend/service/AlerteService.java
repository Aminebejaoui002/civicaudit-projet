package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.Alerte;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.AlerteRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module alert-engine (CDC 3.5) : heuristiques simples, sans machine learning, qui signalent des marchés
 * « à examiner ». Ce sont des signaux statistiques, jamais des accusations.
 *
 * <p>Le calcul ({@link #calculerAlertes(List, LocalDate)}) est une fonction pure, sans accès base,
 * pour être testé unitairement sur des jeux de données synthétiques. Le recalcul complet a lieu
 * après chaque import (pas en temps réel à la consultation).</p>
 */
@Service
public class AlerteService {

    /** Règle 1 : part maximale d'un fournisseur dans les dépenses de la collectivité. */
    static final BigDecimal SEUIL_CONCENTRATION_POURCENT = BigDecimal.valueOf(40);
    static final int FENETRE_CONCENTRATION_ANNEES = 3;
    static final int ANCIENNETE_MAX_MARCHE_ALERTE_ANNEES = 1;

    /** Règle 2 : nombre d'écarts-types au-delà de la moyenne, et garde-fou statistique. */
    static final int NOMBRE_ECARTS_TYPES = 2;
    static final int MIN_MARCHES_PAR_CATEGORIE = 5;

    /** Règle 3 : zone juste sous le seuil réglementaire de 40 000 € HT. */
    static final BigDecimal SEUIL_BAS = BigDecimal.valueOf(35000);
    static final BigDecimal SEUIL_REGLEMENTAIRE = BigDecimal.valueOf(40000);

    private final AlerteRepository alerteRepository;
    private final MarchePublicRepository marcheRepository;

    public AlerteService(AlerteRepository alerteRepository, MarchePublicRepository marcheRepository) {
        this.alerteRepository = alerteRepository;
        this.marcheRepository = marcheRepository;
    }

    /** Recalcule toutes les alertes (appelé après chaque import). Retourne le nombre d'alertes générées. */
    @Transactional
    public int genererAlertes() {
        alerteRepository.deleteAllInBatch();
        List<Alerte> alertes = calculerAlertes(marcheRepository.findAll(), LocalDate.now());
        alerteRepository.saveAll(alertes);
        return alertes.size();
    }

    public List<Alerte> calculerAlertes(List<MarchePublic> marches, LocalDate aujourdhui) {
        List<Alerte> alertes = new ArrayList<>();
        alertes.addAll(alertesConcentration(marches, aujourdhui));
        alertes.addAll(alertesMontantAtypique(marches));
        alertes.addAll(alertesSeuil(marches));
        return alertes;
    }

    /**
     * Règle 1 — Concentration fournisseur : si un titulaire représente plus de 40 % du montant total des marchés
     * des 3 dernières années, alerte CONCENTRATION_ELEVEE sur chacun de ses marchés de moins d'un an.
     */
    List<Alerte> alertesConcentration(List<MarchePublic> marches, LocalDate aujourdhui) {
        LocalDate debutFenetre = aujourdhui.minusYears(FENETRE_CONCENTRATION_ANNEES);
        LocalDate debutMarchesRecents = aujourdhui.minusYears(ANCIENNETE_MAX_MARCHE_ALERTE_ANNEES);

        List<MarchePublic> marchesFenetre = marches.stream()
                .filter(m -> m.getDateNotification() != null && m.getMontant() != null)
                .filter(m -> m.getDateNotification().isAfter(debutFenetre))
                .collect(Collectors.toList());

        BigDecimal montantTotal = marchesFenetre.stream()
                .map(MarchePublic::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (montantTotal.signum() == 0) {
            return List.of();
        }

        // Montant cumulé par titulaire (un marché en groupement compte pour chacun de ses titulaires)
        Map<String, BigDecimal> montantParTitulaire = new HashMap<>();
        for (MarchePublic marche : marchesFenetre) {
            for (Titulaire titulaire : marche.getTitulaires()) {
                montantParTitulaire.merge(titulaire.getId(), marche.getMontant(), BigDecimal::add);
            }
        }

        List<Alerte> alertes = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : montantParTitulaire.entrySet()) {
            BigDecimal pourcentage = entry.getValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(montantTotal, 2, RoundingMode.HALF_UP);
            if (pourcentage.compareTo(SEUIL_CONCENTRATION_POURCENT) <= 0) {
                continue;
            }
            String titulaireId = entry.getKey();
            for (MarchePublic marche : marchesFenetre) {
                boolean recent = marche.getDateNotification().isAfter(debutMarchesRecents);
                boolean duTitulaire = marche.getTitulaires().stream().anyMatch(t -> t.getId().equals(titulaireId));
                if (recent && duTitulaire) {
                    alertes.add(nouvelleAlerte(marche, Alerte.Type.CONCENTRATION_ELEVEE, pourcentage));
                }
            }
        }
        return alertes;
    }

    /**
     * Règle 2 — Montant hors norme : par grande catégorie CPV (2 premiers chiffres), alerte MONTANT_ATYPIQUE
     * si le montant dépasse moyenne + 2 écarts-types. Garde-fou : catégorie d'au moins 5 marchés,
     * sinon aucune alerte (base statistique insuffisante).
     */
    List<Alerte> alertesMontantAtypique(List<MarchePublic> marches) {
        Map<String, List<MarchePublic>> parCategorie = marches.stream()
                .filter(m -> m.getMontant() != null && m.getCpvCode() != null && m.getCpvCode().length() >= 2)
                .collect(Collectors.groupingBy(m -> m.getCpvCode().substring(0, 2)));

        List<Alerte> alertes = new ArrayList<>();
        for (List<MarchePublic> marchesCategorie : parCategorie.values()) {
            if (marchesCategorie.size() < MIN_MARCHES_PAR_CATEGORIE) {
                continue; // garde-fou statistique
            }

            BigDecimal n = BigDecimal.valueOf(marchesCategorie.size());
            BigDecimal moyenne = marchesCategorie.stream()
                    .map(MarchePublic::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(n, 10, RoundingMode.HALF_UP);

            // Écart-type de population : racine de la moyenne des carrés des écarts
            BigDecimal variance = marchesCategorie.stream()
                    .map(m -> m.getMontant().subtract(moyenne).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(n, 10, RoundingMode.HALF_UP);
            BigDecimal ecartType = variance.sqrt(MathContext.DECIMAL64);

            BigDecimal seuil = moyenne.add(ecartType.multiply(BigDecimal.valueOf(NOMBRE_ECARTS_TYPES)));

            for (MarchePublic marche : marchesCategorie) {
                if (marche.getMontant().compareTo(seuil) > 0) {
                    alertes.add(nouvelleAlerte(marche, Alerte.Type.MONTANT_ATYPIQUE, marche.getMontant()));
                }
            }
        }
        return alertes;
    }

    /**
     * Règle 3 — Procédure sans mise en concurrence proche du seuil : procédure dont le libellé contient
     * « sans publicité » ou « négocié sans », ET montant compris entre 35 000 € et 40 000 €.
     */
    List<Alerte> alertesSeuil(List<MarchePublic> marches) {
        List<Alerte> alertes = new ArrayList<>();
        for (MarchePublic marche : marches) {
            if (marche.getMontant() == null || !estProcedureSansMiseEnConcurrence(marche.getProcedure())) {
                continue;
            }
            if (marche.getMontant().compareTo(SEUIL_BAS) >= 0
                    && marche.getMontant().compareTo(SEUIL_REGLEMENTAIRE) <= 0) {
                alertes.add(nouvelleAlerte(marche, Alerte.Type.SEUIL_SUSPECT, marche.getMontant()));
            }
        }
        return alertes;
    }

    static boolean estProcedureSansMiseEnConcurrence(String procedure) {
        if (procedure == null) return false;
        String p = procedure.toLowerCase(Locale.FRENCH);
        return p.contains("sans publicité") || p.contains("sans publicite")
                || p.contains("négocié sans") || p.contains("negocie sans");
    }

    private static Alerte nouvelleAlerte(MarchePublic marche, Alerte.Type type, BigDecimal valeur) {
        Alerte alerte = new Alerte();
        alerte.setMarche(marche);
        alerte.setType(type);
        alerte.setValeurCalculee(valeur.setScale(2, RoundingMode.HALF_UP));
        return alerte;
    }

    public List<Alerte> getAlertesByMarche(Long marcheId) {
        return alerteRepository.findByMarcheId(marcheId);
    }
}
