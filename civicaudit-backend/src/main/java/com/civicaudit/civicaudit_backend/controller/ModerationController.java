package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.CommentaireDTO;
import com.civicaudit.civicaudit_backend.dto.SignalementDTO;
import com.civicaudit.civicaudit_backend.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Module Modération (CDC 3.4). Toutes les routes sont réservées aux rôles MODERATEUR et ADMIN :
 * double protection, au niveau de la SecurityFilterChain (/api/moderation/**) et ici via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/moderation")
@Tag(name = "Modération", description = "Endpoints réservés aux modérateurs (rôles MODERATEUR ou ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MODERATEUR', 'ADMIN')")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @GetMapping("/commentaires")
    @Operation(summary = "Lister les commentaires en attente de modération")
    public List<CommentaireDTO> getCommentairesEnAttente() {
        return moderationService.getCommentairesEnAttente();
    }

    @GetMapping("/signalements")
    @Operation(summary = "Lister tous les signalements (avec nombre de votes)")
    public List<SignalementDTO> getSignalements() {
        return moderationService.getTousSignalements();
    }

    @PatchMapping("/commentaires/{id}")
    @Operation(summary = "Approuver ou rejeter un commentaire",
            description = "Corps attendu : {\"statut\": \"APPROUVE\"} ou {\"statut\": \"REJETE\"}")
    public ResponseEntity<Void> modererCommentaire(@PathVariable Long id, @RequestBody Map<String, String> body) {
        moderationService.changerStatutCommentaire(id, body.get("statut"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/commentaires/{id}/approuver")
    @Operation(summary = "Approuver un commentaire (raccourci)")
    public ResponseEntity<Void> approuverCommentaire(@PathVariable Long id) {
        moderationService.approuverCommentaire(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/commentaires/{id}/rejeter")
    @Operation(summary = "Rejeter un commentaire (raccourci)")
    public ResponseEntity<Void> rejeterCommentaire(@PathVariable Long id) {
        moderationService.rejeterCommentaire(id);
        return ResponseEntity.ok().build();
    }
}
