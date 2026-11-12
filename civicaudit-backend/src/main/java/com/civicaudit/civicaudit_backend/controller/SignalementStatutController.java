package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Changement de statut d'un signalement (CDC 3.3.2 et 5.4 : PATCH /api/signalements/{id}/statut [MODERATEUR]).
 */
@RestController
@RequestMapping("/api/signalements")
@Tag(name = "Modération", description = "Endpoints réservés aux modérateurs (rôles MODERATEUR ou ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class SignalementStatutController {

    private final ModerationService moderationService;

    public SignalementStatutController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('MODERATEUR', 'ADMIN')")
    @Operation(summary = "Changer le statut d'un signalement",
            description = "Corps attendu : {\"statut\": \"NOUVEAU\" | \"EN_DISCUSSION\" | \"VERIFIE\" | \"CLOS\"}")
    public ResponseEntity<Void> changerStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        moderationService.changerStatutSignalement(id, body.get("statut"));
        return ResponseEntity.ok().build();
    }
}
