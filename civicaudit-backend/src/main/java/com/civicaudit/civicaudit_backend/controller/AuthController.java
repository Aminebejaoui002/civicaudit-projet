package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.AuthResponse;
import com.civicaudit.civicaudit_backend.dto.LoginRequest;
import com.civicaudit.civicaudit_backend.dto.RegisterRequest;
import com.civicaudit.civicaudit_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints de connexion et inscription")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Créer un nouveau compte utilisateur")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Se connecter avec email et mot de passe")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/compte")
    @Operation(summary = "Supprimer mon compte (RGPD)",
            description = "Soft delete + anonymisation : les contributions restent visibles sous « Utilisateur supprimé ».")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> supprimerCompte(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        authService.supprimerCompte(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
