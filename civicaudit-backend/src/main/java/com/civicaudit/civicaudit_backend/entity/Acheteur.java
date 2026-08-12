package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "acheteur")
public class Acheteur {

    @Id
    @Column(name = "id", length = 20)
    private String id; // acheteur_id du DECP (ex: SIRET), pas d'auto-génération

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "categorie")
    private String categorie; // ex: "Commune"

    @Column(name = "commune_code")
    private String communeCode;

    @Column(name = "commune_nom")
    private String communeNom;

    @Column(name = "departement_code")
    private String departementCode;

    @Column(name = "departement_nom")
    private String departementNom;

    // --- Constructeurs ---
    public Acheteur() {
    }

    // --- Getters / Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getCommuneCode() { return communeCode; }
    public void setCommuneCode(String communeCode) { this.communeCode = communeCode; }

    public String getCommuneNom() { return communeNom; }
    public void setCommuneNom(String communeNom) { this.communeNom = communeNom; }

    public String getDepartementCode() { return departementCode; }
    public void setDepartementCode(String departementCode) { this.departementCode = departementCode; }

    public String getDepartementNom() { return departementNom; }
    public void setDepartementNom(String departementNom) { this.departementNom = departementNom; }
}