package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.MarcheFiltre;
import com.civicaudit.civicaudit_backend.dto.MarcheListeDTO;
import com.civicaudit.civicaudit_backend.entity.Avenant;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.repository.AvenantRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.MarcheSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarcheConsultationService {

    private static final String NON_RENSEIGNE = "Non renseigné";
    private static final int TAILLE_MAX_PAGE = 100;

    private final MarchePublicRepository marchePublicRepository;
    private final AvenantRepository avenantRepository;
    private final CpvLibelleService cpvLibelleService;

    public MarcheConsultationService(MarchePublicRepository marchePublicRepository,
                                     AvenantRepository avenantRepository,
                                     CpvLibelleService cpvLibelleService) {
        this.marchePublicRepository = marchePublicRepository;
        this.avenantRepository = avenantRepository;
        this.cpvLibelleService = cpvLibelleService;
    }

    /** Liste paginée et filtrée (CDC 3.2.1). */
    public Page<MarcheListeDTO> rechercher(int page, int taille, String tri, MarcheFiltre filtre) {
        int tailleBornee = Math.min(Math.max(taille, 1), TAILLE_MAX_PAGE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), tailleBornee, tri(tri));
        Page<MarchePublic> resultat = marchePublicRepository.findAll(specification(filtre), pageable);
        Map<Long, BigDecimal> montantsActuels = montantsActuels(resultat.getContent());
        return resultat.map(m -> versDTO(m, montantsActuels));
    }

    /** Toutes les lignes correspondant aux filtres actifs, dans le même ordre que la liste (export CSV, CDC 3.7). */
    public List<MarcheListeDTO> rechercherTout(String tri, MarcheFiltre filtre) {
        List<MarchePublic> marches = marchePublicRepository.findAll(specification(filtre), tri(tri));
        Map<Long, BigDecimal> montantsActuels = montantsActuels(marches);
        return marches.stream().map(m -> versDTO(m, montantsActuels)).collect(Collectors.toList());
    }

    /** Valeurs proposées dans les listes déroulantes de filtres : uniquement celles présentes dans les données. */
    public Map<String, Object> valeursFiltres() {
        List<Map<String, String>> categories = marchePublicRepository.findDistinctCategoriesCpv().stream()
                .filter(Objects::nonNull)
                .sorted()
                .map(code -> Map.of("code", code, "libelle", cpvLibelleService.libelle(code)))
                .collect(Collectors.toList());
        return Map.of(
                "categoriesCpv", categories,
                "procedures", marchePublicRepository.findDistinctProcedures()
        );
    }

    static Specification<MarchePublic> specification(MarcheFiltre f) {
        MarcheFiltre filtre = f != null ? f : MarcheFiltre.vide();
        return Specification
                .where(MarcheSpecifications.dateApres(filtre.dateDebut()))
                .and(MarcheSpecifications.dateAvant(filtre.dateFin()))
                .and(MarcheSpecifications.montantMin(filtre.montantMin()))
                .and(MarcheSpecifications.montantMax(filtre.montantMax()))
                .and(MarcheSpecifications.categorieCpv(filtre.categorieCpv()))
                .and(MarcheSpecifications.procedure(filtre.procedure()))
                .and(MarcheSpecifications.rechercheTexte(filtre.texte()))
                .and(MarcheSpecifications.titulaire(filtre.titulaireId()));
    }

    // Tri par défaut : date de notification décroissante (CDC 3.2.1). Tris possibles : montant, date.
    static Sort tri(String tri) {
        String t = tri == null ? "date" : tri;
        return switch (t) {
            case "montant" -> Sort.by(Sort.Direction.DESC, "montant").and(Sort.by(Sort.Direction.DESC, "id"));
            case "montant_asc" -> Sort.by(Sort.Direction.ASC, "montant").and(Sort.by(Sort.Direction.ASC, "id"));
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "dateNotification").and(Sort.by(Sort.Direction.ASC, "id"));
            default -> Sort.by(Sort.Direction.DESC, "dateNotification").and(Sort.by(Sort.Direction.DESC, "id"));
        };
    }

    /** Montant affiché au citoyen = celui du dernier avenant en date, sinon le montant d'origine (CDC 3.1). */
    private Map<Long, BigDecimal> montantsActuels(List<MarchePublic> marches) {
        Map<Long, BigDecimal> resultat = new HashMap<>();
        if (marches.isEmpty()) return resultat;
        List<Long> ids = marches.stream().map(MarchePublic::getId).collect(Collectors.toList());
        Map<Long, Avenant> dernierAvenant = new HashMap<>();
        for (Avenant a : avenantRepository.findByMarchePublicIdIn(ids)) {
            Long id = a.getMarchePublic().getId();
            Avenant actuel = dernierAvenant.get(id);
            if (actuel == null || a.getDateAvenant().isAfter(actuel.getDateAvenant())) {
                dernierAvenant.put(id, a);
            }
        }
        for (MarchePublic m : marches) {
            Avenant a = dernierAvenant.get(m.getId());
            resultat.put(m.getId(), a != null ? a.getMontantApres() : m.getMontant());
        }
        return resultat;
    }

    private MarcheListeDTO versDTO(MarchePublic marche, Map<Long, BigDecimal> montantsActuels) {
        String titulairePrincipal = marche.getTitulaires().isEmpty()
                ? NON_RENSEIGNE
                : marche.getTitulaires().get(0).getNom();

        MarcheListeDTO dto = new MarcheListeDTO(
                marche.getId(),
                marche.getObjet(),
                montantsActuels.getOrDefault(marche.getId(), marche.getMontant()),
                titulairePrincipal,
                marche.getDateNotification(),
                marche.getProcedure() == null ? NON_RENSEIGNE : marche.getProcedure()
        );
        dto.setCpvCode(marche.getCpvCode());
        dto.setCategorieCpvLibelle(cpvLibelleService.libelle(marche.getCpvCode()));
        dto.setSourceUrl(ImportMarcheWriter.lienSource(marche));
        return dto;
    }
}
