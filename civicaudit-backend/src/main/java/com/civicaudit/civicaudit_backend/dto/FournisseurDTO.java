package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class FournisseurDTO {
    private String siret;
    private String nom;
    private int nombreMarches;
    private BigDecimal montantTotalCumule;
    private double pourcentageConcentration; // % du total des dépenses de la collectivité
    private List<MarcheListeDTO> marches;

    public FournisseurDTO(String siret, String nom, int nombreMarches, BigDecimal montantTotalCumule,
                          double pourcentageConcentration, List<MarcheListeDTO> marches) {
        this.siret = siret;
        this.nom = nom;
        this.nombreMarches = nombreMarches;
        this.montantTotalCumule = montantTotalCumule;
        this.pourcentageConcentration = pourcentageConcentration;
        this.marches = marches;
    }

    public String getSiret() { return siret; }
    public String getNom() { return nom; }
    public int getNombreMarches() { return nombreMarches; }
    public BigDecimal getMontantTotalCumule() { return montantTotalCumule; }
    public double getPourcentageConcentration() { return pourcentageConcentration; }
    public List<MarcheListeDTO> getMarches() { return marches; }
}