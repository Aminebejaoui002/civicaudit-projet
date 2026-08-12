package com.civicaudit.civicaudit_backend.dto;

public class TitulaireDTO {
    private String siret;
    private String nom;

    public TitulaireDTO(String siret, String nom) {
        this.siret = siret;
        this.nom = nom;
    }

    public String getSiret() { return siret; }
    public String getNom() { return nom; }
}