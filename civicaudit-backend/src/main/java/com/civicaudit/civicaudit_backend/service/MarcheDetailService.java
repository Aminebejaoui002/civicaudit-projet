package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.AvenantDTO;
import com.civicaudit.civicaudit_backend.dto.MarcheDetailDTO;
import com.civicaudit.civicaudit_backend.dto.TitulaireDTO;
import com.civicaudit.civicaudit_backend.entity.Avenant;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.repository.AvenantRepository;
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

    public MarcheDetailService(MarchePublicRepository marchePublicRepository, AvenantRepository avenantRepository) {
        this.marchePublicRepository = marchePublicRepository;
        this.avenantRepository = avenantRepository;
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

        return new MarcheDetailDTO(
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
                !avenants.isEmpty()
        );
    }

    public List<AvenantDTO> getAvenants(Long marcheId) {
        return avenantRepository.findByMarchePublicIdOrderByDateAvenantDesc(marcheId).stream()
                .map(a -> new AvenantDTO(a.getId(), a.getMontantApres(), a.getDureeApresMois(), a.getDateAvenant()))
                .collect(Collectors.toList());
    }
}