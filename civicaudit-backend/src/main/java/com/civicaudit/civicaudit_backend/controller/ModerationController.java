package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.CommentaireDTO;
import com.civicaudit.civicaudit_backend.dto.SignalementDTO;
import com.civicaudit.civicaudit_backend.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
@Tag(name = "Modération", description = "Endpoints pour les modérateurs")
@CrossOrigin(origins = "*")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @GetMapping("/commentaires")
    @Operation(summary = "Lister les commentaires en attente", description = "Réservé aux modérateurs")
    public ResponseEntity<List<CommentaireDTO>> getCommentairesEnAttente() {
        List<CommentaireDTO> commentaires = moderationService.getCommentairesEnAttente();
        return ResponseEntity.ok(commentaires);
    }

    @GetMapping("/signalements")
    @Operation(summary = "Lister tous les signalements", description = "Réservé aux modérateurs")
    public ResponseEntity<List<SignalementDTO>> getSignalements() {
        List<SignalementDTO> signalements = moderationService.getTousSignalements();
        return ResponseEntity.ok(signalements);
    }

    @PostMapping("/commentaires/{id}/approuver")
    @Operation(summary = "Approuver un commentaire")
    public ResponseEntity<Void> approuverCommentaire(@PathVariable Long id) {
        try {
            moderationService.approuverCommentaire(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/commentaires/{id}/rejeter")
    @Operation(summary = "Rejeter un commentaire")
    public ResponseEntity<Void> rejeterCommentaire(@PathVariable Long id) {
        try {
            moderationService.rejeterCommentaire(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/signalements/{id}/statut")
    @Operation(summary = "Changer le statut d'un signalement")
    public ResponseEntity<Void> changerStatutSignalement(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String statut = body.get("statut");
        if (statut == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            moderationService.changerStatutSignalement(id, statut);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
