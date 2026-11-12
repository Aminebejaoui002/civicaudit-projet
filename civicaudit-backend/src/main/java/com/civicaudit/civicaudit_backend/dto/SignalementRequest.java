package com.civicaudit.civicaudit_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignalementRequest {
    @NotNull(message = "La catégorie est requise")
    // Liste fermée (CDC 3.3.2) — doit correspondre exactement à l'enum Signalement.Categorie
    @Pattern(regexp = "^(MONTANT_ANORMAL|CONCENTRATION_FOURNISSEUR|PROCEDURE_INHABITUELLE|DELAI_SUSPECT|AUTRE)$",
             message = "Catégorie invalide")
    private String categorie;

    @NotBlank(message = "La description est requise")
    @Size(min = 20, max = 1000, message = "La description doit contenir entre 20 et 1000 caractères")
    @Pattern(regexp = "(?s)^(?!.*<script).*$", message = "Contenu suspect détecté") // (?s) : autorise les retours à la ligne
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
