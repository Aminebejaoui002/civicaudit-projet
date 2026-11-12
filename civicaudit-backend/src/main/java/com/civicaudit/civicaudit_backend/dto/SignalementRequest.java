package com.civicaudit.civicaudit_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignalementRequest {
    @NotNull(message = "La catégorie est requise")
    @Pattern(regexp = "^(PRIX_ELEVE|DELAI_COURT|FRACTIONNEMENT|PROCEDURE_IRREGULIERE|AUTRE)$", 
             message = "Catégorie invalide")
    private String categorie;

    @NotBlank(message = "La description est requise")
    @Size(min = 20, max = 1000, message = "La description doit contenir entre 20 et 1000 caractères")
    @Pattern(regexp = "^(?!.*<script).*$", message = "Contenu suspect détecté")
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
