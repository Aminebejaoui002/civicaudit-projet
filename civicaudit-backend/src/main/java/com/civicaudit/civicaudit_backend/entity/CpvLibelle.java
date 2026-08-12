package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cpv_libelle")
public class CpvLibelle {

    @Id
    @Column(name = "code_2_chiffres", length = 2)
    private String code2Chiffres; // ex: "45" = Travaux de construction

    @Column(name = "libelle", nullable = false)
    private String libelle;

    // --- Constructeurs ---
    public CpvLibelle() {
    }

    public CpvLibelle(String code2Chiffres, String libelle) {
        this.code2Chiffres = code2Chiffres;
        this.libelle = libelle;
    }

    // --- Getters / Setters ---
    public String getCode2Chiffres() { return code2Chiffres; }
    public void setCode2Chiffres(String code2Chiffres) { this.code2Chiffres = code2Chiffres; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}