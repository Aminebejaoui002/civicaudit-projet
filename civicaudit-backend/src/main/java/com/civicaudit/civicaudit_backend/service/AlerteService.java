package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.Alerte;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.AlerteRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final MarchePublicRepository marcheRepository;

    public AlerteService(AlerteRepository alerteRepository, MarchePublicRepository marcheRepository) {
        this.alerteRepository = alerteRepository;
        this.marcheRepository = marcheRepository;
    }

    @Transactional
    public void genererAlertes() {
        // Supprimer les anciennes alertes
        alerteRepository.deleteAll();

        List<MarchePublic> marches = marcheRepository.findAll();

        // Règle 1 : Concentration fournisseur
        genererAlertesConcentration(marches);

        // Règle 2 : Montant atypique par catégorie CPV
        genererAlertesMontantAtypique(marches);

        // Règle 3 : Procédure sans mise en concurrence proche du seuil
        genererAlertesSeuil(marches);
    }

    private void genererAlertesConcentration(List<MarchePublic> marches) {
        LocalDate troishAnsAuparavant = LocalDate.now().minusYears(3);
        
        // Filtrer les marchés des 3 dernières années
        List<MarchePublic> marchesRecents = marches.stream()
                .filter(m -> m.getDateNotification() != null && 
                            m.getDateNotification().isAfter(troishAnsAuparavant))
                .collect(Collectors.toList());

        // Calculer le montant total
        BigDecimal montantTotal = marchesRecents.stream()
                .map(MarchePublic::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (montantTotal.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // Calculer la part de chaque fournisseur
        Map<String, BigDecimal> montantParFournisseur = new HashMap<>();
        for (MarchePublic marche : marchesRecents) {
            for (Titulaire titulaire : marche.getTitulaires()) {
                montantParFournisseur.merge(
                    titulaire.getId(),
                    marche.getMontant(),
                    BigDecimal::add
                );
            }
        }

        // Identifier les fournisseurs avec concentration > 40%
        for (Map.Entry<String, BigDecimal> entry : montantParFournisseur.entrySet()) {
            BigDecimal pourcentage = entry.getValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(montantTotal, 2, RoundingMode.HALF_UP);

            if (pourcentage.compareTo(BigDecimal.valueOf(40)) > 0) {
                // Créer des alertes pour les marchés récents de ce fournisseur
                String titulaireId = entry.getKey();
                LocalDate unAnAuparavant = LocalDate.now().minusYears(1);
                
                for (MarchePublic marche : marchesRecents) {
                    if (marche.getDateNotification().isAfter(unAnAuparavant) &&
                        marche.getTitulaires().stream().anyMatch(t -> t.getId().equals(titulaireId))) {
                        
                        Alerte alerte = new Alerte();
                        alerte.setMarche(marche);
                        alerte.setType(Alerte.Type.CONCENTRATION_ELEVEE);
                        alerte.setValeurCalculee(pourcentage);
                        alerteRepository.save(alerte);
                    }
                }
            }
        }
    }

    private void genererAlertesMontantAtypique(List<MarchePublic> marches) {
        // Grouper par catégorie CPV (2 premiers chiffres)
        Map<String, List<MarchePublic>> marcheParCategorie = marches.stream()
                .filter(m -> m.getCpvCode() != null && m.getCpvCode().length() >= 2)
                .collect(Collectors.groupingBy(m -> m.getCpvCode().substring(0, 2)));

        for (Map.Entry<String, List<MarchePublic>> entry : marcheParCategorie.entrySet()) {
            List<MarchePublic> marchesCategorie = entry.getValue();
            
            // Minimum 5 marchés pour calculer l'écart-type
            if (marchesCategorie.size() < 5) {
                continue;
            }

            // Calculer moyenne et écart-type
            List<BigDecimal> montants = marchesCategorie.stream()
                    .map(MarchePublic::getMontant)
                    .collect(Collectors.toList());

            BigDecimal moyenne = montants.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(montants.size()), 2, RoundingMode.HALF_UP);

            // Calcul de l'écart-type
            BigDecimal variance = montants.stream()
                    .map(m -> m.subtract(moyenne).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(montants.size()), 2, RoundingMode.HALF_UP);

            double ecartType = Math.sqrt(variance.doubleValue());
            BigDecimal seuil = moyenne.add(BigDecimal.valueOf(2 * ecartType));

            // Créer des alertes pour les marchés au-dessus du seuil
            for (MarchePublic marche : marchesCategorie) {
                if (marche.getMontant().compareTo(seuil) > 0) {
                    Alerte alerte = new Alerte();
                    alerte.setMarche(marche);
                    alerte.setType(Alerte.Type.MONTANT_ATYPIQUE);
                    alerte.setValeurCalculee(marche.getMontant());
                    alerteRepository.save(alerte);
                }
            }
        }
    }

    private void genererAlertesSeuil(List<MarchePublic> marches) {
        BigDecimal seuilMin = BigDecimal.valueOf(35000);
        BigDecimal seuilMax = BigDecimal.valueOf(40000);

        for (MarchePublic marche : marches) {
            String procedure = marche.getProcedure();
            if (procedure != null && 
                (procedure.toLowerCase().contains("sans publicité") || 
                 procedure.toLowerCase().contains("négocié sans"))) {
                
                if (marche.getMontant().compareTo(seuilMin) >= 0 && 
                    marche.getMontant().compareTo(seuilMax) <= 0) {
                    
                    Alerte alerte = new Alerte();
                    alerte.setMarche(marche);
                    alerte.setType(Alerte.Type.SEUIL_SUSPECT);
                    alerte.setValeurCalculee(marche.getMontant());
                    alerteRepository.save(alerte);
                }
            }
        }
    }

    public List<Alerte> getAlertesByMarche(Long marcheId) {
        return alerteRepository.findByMarcheId(marcheId);
    }
}
