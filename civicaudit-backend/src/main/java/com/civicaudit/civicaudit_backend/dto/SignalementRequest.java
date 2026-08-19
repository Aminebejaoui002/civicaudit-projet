package com.civicaudit.civicaudit_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SignalementRequest {
    @NotNull(message = "La catégorie est requise")
    private String categorie;

    @NotBlank(message = "La description est requise")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
