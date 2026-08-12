package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.MarcheListeDTO;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.MarcheSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class MarcheConsultationService {

    private static final String NON_RENSEIGNE = "Non renseigné";

    private final MarchePublicRepository marchePublicRepository;

    public MarcheConsultationService(MarchePublicRepository marchePublicRepository) {
        this.marchePublicRepository = marchePublicRepository;
    }

    public Page<MarcheListeDTO> rechercher(
            int page, int taille, String tri,
            LocalDate dateDebut, LocalDate dateFin,
            BigDecimal montantMin, BigDecimal montantMax,
            String categorieCpv, String texte, String procedure) {

        Specification<MarchePublic> spec = Specification
                .where(MarcheSpecifications.dateApres(dateDebut))
                .and(MarcheSpecifications.dateAvant(dateFin))
                .and(MarcheSpecifications.montantMin(montantMin))
                .and(MarcheSpecifications.montantMax(montantMax))
                .and(MarcheSpecifications.categorieCpv(categorieCpv))
                .and(MarcheSpecifications.procedure(procedure))
                .and(MarcheSpecifications.rechercheTexte(texte));

        // Tri par défaut : date décroissante (règle CDC p.4). Tris possibles : montant, date.
        Sort sort = switch (tri == null ? "date" : tri) {
            case "montant" -> Sort.by(Sort.Direction.DESC, "montant");
            default -> Sort.by(Sort.Direction.DESC, "dateNotification");
        };

        Pageable pageable = PageRequest.of(page, taille, sort);

        return marchePublicRepository.findAll(spec, pageable).map(this::versDTO);
    }

    private MarcheListeDTO versDTO(MarchePublic marche) {
        String titulairePrincipal = marche.getTitulaires().isEmpty()
                ? NON_RENSEIGNE
                : marche.getTitulaires().get(0).getNom();

        return new MarcheListeDTO(
                marche.getId(),
                marche.getObjet(),
                marche.getMontant(),
                titulairePrincipal,
                marche.getDateNotification(),
                marche.getProcedure() == null ? NON_RENSEIGNE : marche.getProcedure()
        );
    }
}