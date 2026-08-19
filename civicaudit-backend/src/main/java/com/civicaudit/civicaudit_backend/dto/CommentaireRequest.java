package com.civicaudit.civicaudit_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentaireRequest {
    @NotBlank(message = "Le contenu est requis")
    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String contenu;

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}
