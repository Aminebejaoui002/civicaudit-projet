package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.AlerteDTO;
import com.civicaudit.civicaudit_backend.dto.AvenantDTO;
import com.civicaudit.civicaudit_backend.dto.MarcheDetailDTO;
import com.civicaudit.civicaudit_backend.dto.TitulaireDTO;
import com.civicaudit.civicaudit_backend.entity.Alerte;
import com.civicaudit.civicaudit_backend.entity.Avenant;
import com.civicaudit.civicaudit_backend.entity.Commentaire;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.repository.AvenantRepository;
import com.civicaudit.civicaudit_backend.repository.CommentaireRepository;
import com.civicaudit.civicaudit_backend.repository.SignalementRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class MarcheDetailService {

    private static final String NON_RENSEIGNE = "Non renseigné";

    private final MarchePublicRepository marchePublicRepository;
    private final AvenantRepository avenantRepository;
    private final AlerteService alerteService;
    private final CpvLibelleService cpvLibelleService;
    private final CommentaireRepository commentaireRepository;
    private final SignalementRepository signalementRepository;

    public MarcheDetailService(MarchePublicRepository marchePublicRepository,
                              AvenantRepository avenantRepository,
                              AlerteService alerteService,
                              CpvLibelleService cpvLibelleService,
                              CommentaireRepository commentaireRepository,
                              SignalementRepository signalementRepository) {
        this.marchePublicRepository = marchePublicRepository;
        this.avenantRepository = avenantRepository;
        this.alerteService = alerteService;
        this.cpvLibelleService = cpvLibelleService;
        this.commentaireRepository = commentaireRepository;
        this.signalementRepository = signalementRepository;
    }

    public MarcheDetailDTO getDetail(Long marcheId) {
        MarchePublic marche = marchePublicRepository.findById(marcheId)
                .orElseThrow(() -> new NoSuchElementException("Marché introuvable : " + marcheId));

        List<Avenant> avenants = avenantRepository.findByMarchePublicIdOrderByDateAvenantDesc(marcheId);

        // Règle CDC (3.2.2, p.5) : montant "actuel" = dernier avenant en date, sinon montant d'origine
        BigDecimal montantActuel = avenants.isEmpty() ? marche.getMontant() : avenants.get(0).getMontantApres();

        List<TitulaireDTO> titulaires = marche.getTitulaires().stream()
                .map(t -> new TitulaireDTO(t.getId(), t.getNom()))
                .collect(Collectors.toList());

        // Récupérer les alertes associées au marché
        List<AlerteDTO> alertes = alerteService.getAlertesByMarche(marcheId).stream()
                .map(a -> new AlerteDTO(a.getId(), a.getType().name(), a.getValeurCalculee(), a.getDateGeneration()))
                .collect(Collectors.toList());

        MarcheDetailDTO dto = new MarcheDetailDTO(
                marche.getId(),
                marche.getObjet(),
                montantActuel,
                marche.getAcheteur() != null ? marche.getAcheteur().getNom() : NON_RENSEIGNE,
                titulaires,
                marche.getDateNotification(),
                marche.getDureeMois(),
                marche.getProcedure() == null ? NON_RENSEIGNE : marche.getProcedure(),
                marche.getCpvCode() == null ? NON_RENSEIGNE : marche.getCpvCode(),
                marche.getSourceFilename() == null ? NON_RENSEIGNE : marche.getSourceFilename(),
                !avenants.isEmpty(),
                alertes
        );
        dto.setCategorieCpvLibelle(cpvLibelleService.libelle(marche.getCpvCode()));
        dto.setSourceUrl(marche.getSourceUrl() == null ? ImportMarcheWriter.SOURCE_URL_DEFAUT : marche.getSourceUrl());
        dto.setNombreCommentaires(commentaireRepository.countByMarcheIdAndStatutAndSupprimeFalse(
                marcheId, Commentaire.Statut.APPROUVE));
        dto.setNombreSignalements(signalementRepository.countByMarcheId(marcheId));
        return dto;
    }

    public List<AvenantDTO> getAvenants(Long marcheId) {
        return avenantRepository.findByMarchePublicIdOrderByDateAvenantDesc(marcheId).stream()
                .map(a -> new AvenantDTO(a.getId(), a.getMontantApres(), a.getDureeApresMois(), a.getDateAvenant()))
                .collect(Collectors.toList());
    }
}