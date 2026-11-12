package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Filtres combinables (ET logique) de la liste des marchés (CDC 3.2.1), partagés par la liste et l'export CSV. */
public record MarcheFiltre(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal montantMin,
        BigDecimal montantMax,
        String categorieCpv,
        String texte,
        String procedure,
        String titulaireId
) {
    public static MarcheFiltre vide() {
        return new MarcheFiltre(null, null, null, null, null, null, null, null);
    }
}
