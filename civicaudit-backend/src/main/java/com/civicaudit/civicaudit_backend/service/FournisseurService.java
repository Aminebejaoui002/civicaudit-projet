package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.FournisseurDTO;
import com.civicaudit.civicaudit_backend.dto.MarcheListeDTO;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.TitulaireRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class FournisseurService {

    private static final String NON_RENSEIGNE = "Non renseigné";

    private final TitulaireRepository titulaireRepository;
    private final MarchePublicRepository marchePublicRepository;
    private final MontantActuelService montantActuelService;

    public FournisseurService(TitulaireRepository titulaireRepository,
                              MarchePublicRepository marchePublicRepository,
                              MontantActuelService montantActuelService) {
        this.titulaireRepository = titulaireRepository;
        this.marchePublicRepository = marchePublicRepository;
        this.montantActuelService = montantActuelService;
    }

    public FournisseurDTO getFournisseur(String siret) {
        Titulaire titulaire = titulaireRepository.findById(siret)
                .orElseThrow(() -> new NoSuchElementException("Fournisseur introuvable : " + siret));

        List<MarchePublic> tousLesMarches = marchePublicRepository.findAll();
        Map<Long, BigDecimal> montantsActuels = montantActuelService.calculerMontantsActuels(tousLesMarches);

        // Marchés de ce fournisseur
        List<MarchePublic> marchesFournisseur = tousLesMarches.stream()
                .filter(m -> m.getTitulaires().stream().anyMatch(t -> t.getId().equals(siret)))
                .collect(Collectors.toList());

        BigDecimal montantTotalFournisseur = marchesFournisseur.stream()
                .map(m -> montantsActuels.get(m.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantTotalCollectivite = tousLesMarches.stream()
                .map(m -> montantsActuels.get(m.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double pourcentage = montantTotalCollectivite.signum() == 0 ? 0.0 :
                montantTotalFournisseur
                        .divide(montantTotalCollectivite, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

        List<MarcheListeDTO> marchesDTO = marchesFournisseur.stream()
                .map(m -> new MarcheListeDTO(
                        m.getId(), m.getObjet(), montantsActuels.get(m.getId()),
                        titulaire.getNom(), m.getDateNotification(),
                        m.getProcedure() == null ? NON_RENSEIGNE : m.getProcedure()))
                .collect(Collectors.toList());

        return new FournisseurDTO(
                titulaire.getId(), titulaire.getNom(), marchesFournisseur.size(),
                montantTotalFournisseur, pourcentage, marchesDTO
        );
    }
}