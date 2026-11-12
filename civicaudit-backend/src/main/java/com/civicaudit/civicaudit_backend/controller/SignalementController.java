package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.SignalementDTO;
import com.civicaudit.civicaudit_backend.dto.SignalementRequest;
import com.civicaudit.civicaudit_backend.service.SignalementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marches/{marcheId}/signalements")
@Tag(name = "Signalements", description = "Gestion des signalements citoyens")
@CrossOrigin(origins = "*")
public class SignalementController {

    private final SignalementService signalementService;

    public SignalementController(SignalementService signalementService) {
        this.signalementService = signalementService;
    }

    @PostMapping
    @Operation(summary = "Créer un signalement", description = "Signaler une anomalie sur un marché (authentification requise)")
    public ResponseEntity<SignalementDTO> creerSignalement(
            @PathVariable Long marcheId,
            @Valid @RequestBody SignalementRequest request,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            SignalementDTO signalement = signalementService.creerSignalement(
                    marcheId, request, authentication.getName());
            return ResponseEntity.ok(signalement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Lister les signalements", description = "Récupérer tous les signalements d'un marché, triés par votes")
    public ResponseEntity<List<SignalementDTO>> getSignalements(
            @PathVariable Long marcheId,
            Authentication authentication) {
        
        String userEmail = authentication != null ? authentication.getName() : null;
        List<SignalementDTO> signalements = signalementService.getSignalementsMarche(marcheId, userEmail);
        return ResponseEntity.ok(signalements);
    }

    @PostMapping("/{signalementId}/vote")
    @Operation(summary = "Voter pour un signalement", description = "Confirmer l'observation d'un signalement")
    public ResponseEntity<Void> voterSignalement(
            @PathVariable Long marcheId,
            @PathVariable Long signalementId,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            signalementService.voterSignalement(signalementId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
