package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.RegisterRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Consentement RGPD obligatoire à l'inscription (CDC 4) : absent ou false → refusé. */
class RegisterRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static RegisterRequest requete(Boolean consentement) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("citoyen@example.fr");
        r.setPassword("motdepasse1");
        r.setConsentementRgpd(consentement);
        return r;
    }

    @Test
    void consentementAbsentRefuse() {
        assertThat(validator.validate(requete(null))).isNotEmpty();
    }

    @Test
    void consentementFalseRefuse() {
        assertThat(validator.validate(requete(false))).isNotEmpty();
    }

    @Test
    void consentementDonneAccepte() {
        assertThat(validator.validate(requete(true))).isEmpty();
    }
}
