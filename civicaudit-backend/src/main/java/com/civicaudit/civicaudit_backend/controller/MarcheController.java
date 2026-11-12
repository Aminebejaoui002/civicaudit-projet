package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.AvenantDTO;
import com.civicaudit.civicaudit_backend.dto.MarcheDetailDTO;
import com.civicaudit.civicaudit_backend.dto.MarcheListeDTO;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.service.ExportService;
import com.civicaudit.civicaudit_backend.service.MarcheConsultationService;
import com.civicaudit.civicaudit_backend.service.MarcheDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/marches")
@Tag(name = "Marchés publics", description = "Consultation des marchés publics de la commune (module 3.2 du CDC)")
public class MarcheController {

    private final MarcheConsultationService consultationService;
    private final MarcheDetailService detailService;
    private final ExportService exportService;
    private final MarchePublicRepository marcheRepository;

    public MarcheController(MarcheConsultationService consultationService,
                            MarcheDetailService detailService,
                            ExportService exportService,
                            MarchePublicRepository marcheRepository) {
        this.consultationService = consultationService;
        this.detailService = detailService;
        this.exportService = exportService;
        this.marcheRepository = marcheRepository;
    }

    @Operation(summary = "Liste paginée des marchés publics",
            description = "Retourne les marchés triés par date de notification décroissante par défaut. "
                    + "Tous les filtres sont combinables entre eux (ET logique).")
    @GetMapping
    public Page<MarcheListeDTO> lister(
            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Nombre de résultats par page")
            @RequestParam(defaultValue = "20") int taille,

            @Parameter(description = "Tri : 'date' (défaut) ou 'montant'")
            @RequestParam(required = false) String tri,

            @Parameter(description = "Date de notification minimale (incluse), format YYYY-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,

            @Parameter(description = "Date de notification maximale (incluse), format YYYY-MM-DD")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,

            @Parameter(description = "Montant minimum")
            @RequestParam(required = false) BigDecimal montantMin,

            @Parameter(description = "Montant maximum")
            @RequestParam(required = false) BigDecimal montantMax,

            @Parameter(description = "Grande catégorie CPV, 2 chiffres (ex: '45' = Travaux de construction)")
            @RequestParam(required = false) String categorieCpv,

            @Parameter(description = "Recherche texte libre sur l'objet et le nom du titulaire")
            @RequestParam(required = false) String texte,

            @Parameter(description = "Type de procédure (ex: 'Appel d'offres ouvert')")
            @RequestParam(required = false) String procedure
    ) {
        return consultationService.rechercher(page, taille, tri, dateDebut, dateFin,
                montantMin, montantMax, categorieCpv, texte, procedure);
    }

    @Operation(summary = "Fiche détaillée d'un marché",
            description = "Le montant retourné est le montant actuel (dernier avenant en date si applicable, "
                    + "sinon montant d'origine). Les champs non renseignés affichent explicitement 'Non renseigné'.")
    @GetMapping("/{id}")
    public MarcheDetailDTO detail(@PathVariable Long id) {
        return detailService.getDetail(id);
    }

    @Operation(summary = "Historique des avenants d'un marché",
            description = "Liste tous les avenants du marché, triés du plus récent au plus ancien.")
    @GetMapping("/{id}/avenants")
    public List<AvenantDTO> avenants(@PathVariable Long id) {
        return detailService.getAvenants(id);
    }

    @Operation(summary = "Exporter les marchés en CSV",
            description = "Exporte tous les marchés publics au format CSV (CDC 3.7)")
    @GetMapping("/export")
    public ResponseEntity<String> exporterCSV() {
        try {
            List<MarchePublic> marches = marcheRepository.findAll();
            String csv = exportService.exporterMarchesCSV(marches);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));
            headers.setContentDispositionFormData("attachment", "marches_publics.csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}