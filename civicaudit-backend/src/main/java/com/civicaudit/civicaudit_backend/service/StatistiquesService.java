package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.StatistiquesDTO;
import com.civicaudit.civicaudit_backend.dto.TopFournisseurDTO;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.TitulaireRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatistiquesService {

    private static final String NON_RENSEIGNE = "Non renseigné";

    private final MarchePublicRepository marchePublicRepository;
    private final TitulaireRepository titulaireRepository;
    private final MontantActuelService montantActuelService;

    public StatistiquesService(MarchePublicRepository marchePublicRepository,
                               TitulaireRepository titulaireRepository,
                               MontantActuelService montantActuelService) {
        this.marchePublicRepository = marchePublicRepository;
        this.titulaireRepository = titulaireRepository;
        this.montantActuelService = montantActuelService;
    }

    public StatistiquesDTO calculer() {
        List<MarchePublic> marches = marchePublicRepository.findAll();
        Map<Long, BigDecimal> montantsActuels = montantActuelService.calculerMontantsActuels(marches);

        BigDecimal montantTotal = montantsActuels.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombreFournisseurs = titulaireRepository.count();

        // Montant total par année
        Map<Integer, BigDecimal> parAnnee = new TreeMap<>();
        for (MarchePublic m : marches) {
            int annee = m.getDateNotification().getYear();
            parAnnee.merge(annee, montantsActuels.get(m.getId()), BigDecimal::add);
        }

        // Top 10 fournisseurs par montant cumulé
        Map<String, BigDecimal> cumulParTitulaire = new HashMap<>();
        Map<String, String> nomParTitulaire = new HashMap<>();
        for (MarchePublic m : marches) {
            BigDecimal montant = montantsActuels.get(m.getId());
            for (Titulaire t : m.getTitulaires()) {
                cumulParTitulaire.merge(t.getId(), montant, BigDecimal::add);
                nomParTitulaire.put(t.getId(), t.getNom());
            }
        }
        List<TopFournisseurDTO> top10 = cumulParTitulaire.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(e -> new TopFournisseurDTO(e.getKey(), nomParTitulaire.get(e.getKey()), e.getValue()))
                .collect(Collectors.toList());

        // Répartition par type de procédure (nombre de marchés)
        Map<String, Long> parProcedure = marches.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getProcedure() == null ? NON_RENSEIGNE : m.getProcedure(),
                        Collectors.counting()));

        // Répartition par grande catégorie CPV (2 premiers chiffres)
        Map<String, Long> parCpv = marches.stream()
                .collect(Collectors.groupingBy(
                        m -> (m.getCpvCode() == null || m.getCpvCode().length() < 2)
                                ? NON_RENSEIGNE : m.getCpvCode().substring(0, 2),
                        Collectors.counting()));

        return new StatistiquesDTO(montantTotal, marches.size(), nombreFournisseurs,
                parAnnee, top10, parProcedure, parCpv);
    }
}