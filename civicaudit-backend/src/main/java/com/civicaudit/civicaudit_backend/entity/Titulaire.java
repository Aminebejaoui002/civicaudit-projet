package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "titulaire")
public class Titulaire {

    @Id
    @Column(name = "id", length = 20)
    private String id; // titulaire_id du DECP (SIRET), pas d'auto-génération

    @Column(name = "type_identifiant")
    private String typeIdentifiant; // ex: "SIRET"

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "categorie")
    private String categorie; // ex: PME, ETI, GE

    @Column(name = "activite_code")
    private String activiteCode; // code NAF/APE

    @Column(name = "activite_libelle")
    private String activiteLibelle;

    @Column(name = "commune_code")
    private String communeCode;

    @Column(name = "commune_nom")
    private String communeNom;

    @Column(name = "departement_code")
    private String departementCode;

    @Column(name = "departement_nom")
    private String departementNom;

    // --- Constructeurs ---
    public Titulaire() {
    }

    // --- Getters / Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTypeIdentifiant() { return typeIdentifiant; }
    public void setTypeIdentifiant(String typeIdentifiant) { this.typeIdentifiant = typeIdentifiant; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getActiviteCode() { return activiteCode; }
    public void setActiviteCode(String activiteCode) { this.activiteCode = activiteCode; }

    public String getActiviteLibelle() { return activiteLibelle; }
    public void setActiviteLibelle(String activiteLibelle) { this.activiteLibelle = activiteLibelle; }

    public String getCommuneCode() { return communeCode; }
    public void setCommuneCode(String communeCode) { this.communeCode = communeCode; }

    public String getCommuneNom() { return communeNom; }
    public void setCommuneNom(String communeNom) { this.communeNom = communeNom; }

    public String getDepartementCode() { return departementCode; }
    public void setDepartementCode(String departementCode) { this.departementCode = departementCode; }

    public String getDepartementNom() { return departementNom; }
    public void setDepartementNom(String departementNom) { this.departementNom = departementNom; }
}