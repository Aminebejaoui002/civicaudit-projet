package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.CommentaireDTO;
import com.civicaudit.civicaudit_backend.dto.CommentaireRequest;
import com.civicaudit.civicaudit_backend.service.CommentaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marches/{marcheId}/commentaires")
@Tag(name = "Commentaires", description = "Gestion des commentaires sur les marchés")
@CrossOrigin(origins = "*")
public class CommentaireController {

    private final CommentaireService commentaireService;

    public CommentaireController(CommentaireService commentaireService) {
        this.commentaireService = commentaireService;
    }

    @PostMapping
    @Operation(summary = "Créer un commentaire", description = "Poster un commentaire sur un marché (authentification requise)")
    public ResponseEntity<CommentaireDTO> creerCommentaire(
            @PathVariable Long marcheId,
            @Valid @RequestBody CommentaireRequest request,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            CommentaireDTO commentaire = commentaireService.creerCommentaire(
                    marcheId, request, authentication.getName());
            return ResponseEntity.ok(commentaire);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Lister les commentaires", description = "Récupérer tous les commentaires d'un marché")
    public ResponseEntity<List<CommentaireDTO>> getCommentaires(
            @PathVariable Long marcheId,
            Authentication authentication) {
        
        String userEmail = authentication != null ? authentication.getName() : null;
        List<CommentaireDTO> commentaires = commentaireService.getCommentairesMarche(marcheId, userEmail);
        return ResponseEntity.ok(commentaires);
    }

    @DeleteMapping("/{commentaireId}")
    @Operation(summary = "Supprimer un commentaire", description = "Supprimer son propre commentaire")
    public ResponseEntity<Void> supprimerCommentaire(
            @PathVariable Long marcheId,
            @PathVariable Long commentaireId,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            commentaireService.supprimerCommentaire(commentaireId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
