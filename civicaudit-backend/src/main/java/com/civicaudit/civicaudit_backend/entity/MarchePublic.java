package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "marche_public")
public class MarchePublic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", nullable = false, unique = true, length = 64)
    private String uid; // identifiant métier du marché dans le DECP (regroupe les lignes multi-titulaires)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acheteur_id", nullable = false)
    private Acheteur acheteur;

    @Column(name = "objet", columnDefinition = "TEXT", nullable = false)
    private String objet;

    @Column(name = "montant", precision = 15, scale = 2, nullable = false)
    private BigDecimal montant; // BigDecimal obligatoire pour un montant financier (jamais de double/float)

    @Column(name = "date_notification", nullable = false)
    private LocalDate dateNotification;

    @Column(name = "duree_mois")
    private Integer dureeMois;

    @Column(name = "procedure")
    private String procedure;

    @Column(name = "cpv_code", length = 10)
    private String cpvCode;

    @Column(name = "source_filename")
    private String sourceFilename;

    @Column(name = "source_url")
    private String sourceUrl;

    // --- Relation MarcheTitulaire : générée automatiquement par JPA via @JoinTable,
    // pas besoin d'écrire une entité MarcheTitulaire séparée ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "marche_titulaire",
            joinColumns = @JoinColumn(name = "marche_id"),
            inverseJoinColumns = @JoinColumn(name = "titulaire_id")
    )

    private List<Titulaire> titulaires = new ArrayList<>();
    @OneToMany(mappedBy = "marchePublic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Avenant> avenants = new ArrayList<>();


    // --- Constructeurs ---
    public MarchePublic() {
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public Acheteur getAcheteur() { return acheteur; }
    public void setAcheteur(Acheteur acheteur) { this.acheteur = acheteur; }

    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public LocalDate getDateNotification() { return dateNotification; }
    public void setDateNotification(LocalDate dateNotification) { this.dateNotification = dateNotification; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public String getProcedure() { return procedure; }
    public void setProcedure(String procedure) { this.procedure = procedure; }

    public String getCpvCode() { return cpvCode; }
    public void setCpvCode(String cpvCode) { this.cpvCode = cpvCode; }

    public String getSourceFilename() { return sourceFilename; }
    public void setSourceFilename(String sourceFilename) { this.sourceFilename = sourceFilename; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public List<Titulaire> getTitulaires() { return titulaires; }
    public void setTitulaires(List<Titulaire> titulaires) { this.titulaires = titulaires; }
    public List<Avenant> getAvenants() { return avenants; }
    public void setAvenants(List<Avenant> avenants) { this.avenants = avenants; }
}