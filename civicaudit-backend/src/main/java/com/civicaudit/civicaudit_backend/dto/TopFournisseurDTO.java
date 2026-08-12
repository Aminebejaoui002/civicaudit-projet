package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;

public class TopFournisseurDTO {
    private String siret;
    private String nom;
    private BigDecimal montantCumule;

    public TopFournisseurDTO(String siret, String nom, BigDecimal montantCumule) {
        this.siret = siret;
        this.nom = nom;
        this.montantCumule = montantCumule;
    }

    public String getSiret() { return siret; }
    public String getNom() { return nom; }
    public BigDecimal getMontantCumule() { return montantCumule; }
}