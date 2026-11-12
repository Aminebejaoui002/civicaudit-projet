package com.civicaudit.civicaudit_backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "L'email est requis")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = ".*\\d.*", message = "Le mot de passe doit contenir au moins 1 chiffre")
    private String password;

    // Consentement RGPD explicite : case à cocher non pré-cochée côté frontend (CDC section 4)
    // @NotNull est indispensable : @AssertTrue considère une valeur absente (null) comme valide
    @NotNull(message = "Vous devez accepter la politique de confidentialité")
    @AssertTrue(message = "Vous devez accepter la politique de confidentialité")
    private Boolean consentementRgpd;

    public Boolean getConsentementRgpd() {
        return consentementRgpd;
    }

    public void setConsentementRgpd(Boolean consentementRgpd) {
        this.consentementRgpd = consentementRgpd;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
