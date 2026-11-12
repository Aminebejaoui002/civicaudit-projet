package com.civicaudit.civicaudit_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CommentaireRequest {
    @NotBlank(message = "Le contenu est requis")
    @Size(min = 10, max = 1000, message = "Le commentaire doit contenir entre 10 et 1000 caractères")
    @Pattern(regexp = "^(?!.*<script).*$", message = "Contenu suspect détecté")
    private String contenu;

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}
