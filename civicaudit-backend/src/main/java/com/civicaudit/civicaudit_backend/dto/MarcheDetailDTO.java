package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MarcheDetailDTO {
    private Long id;
    private String objet;
    private BigDecimal montant; // montant actuel = dernier avenant si existant, sinon montant d'origine
    private String acheteurNom;
    private List<TitulaireDTO> titulaires;
    private LocalDate dateNotification;
    private Integer dureeMois;
    private String procedure;
    private String cpvCode;
    private String sourceFilename;
    private boolean aDesAvenants;
    private List<AlerteDTO> alertes;

    public MarcheDetailDTO(Long id, String objet, BigDecimal montant, String acheteurNom,
                           List<TitulaireDTO> titulaires, LocalDate dateNotification, Integer dureeMois,
                           String procedure, String cpvCode, String sourceFilename, boolean aDesAvenants,
                           List<AlerteDTO> alertes) {
        this.id = id;
        this.objet = objet;
        this.montant = montant;
        this.acheteurNom = acheteurNom;
        this.titulaires = titulaires;
        this.dateNotification = dateNotification;
        this.dureeMois = dureeMois;
        this.procedure = procedure;
        this.cpvCode = cpvCode;
        this.sourceFilename = sourceFilename;
        this.aDesAvenants = aDesAvenants;
        this.alertes = alertes;
    }

    public Long getId() { return id; }
    public String getObjet() { return objet; }
    public BigDecimal getMontant() { return montant; }
    public String getAcheteurNom() { return acheteurNom; }
    public List<TitulaireDTO> getTitulaires() { return titulaires; }
    public LocalDate getDateNotification() { return dateNotification; }
    public Integer getDureeMois() { return dureeMois; }
    public String getProcedure() { return procedure; }
    public String getCpvCode() { return cpvCode; }
    public String getSourceFilename() { return sourceFilename; }
    public boolean isADesAvenants() { return aDesAvenants; }
    public List<AlerteDTO> getAlertes() { return alertes; }

    // --- Champs complémentaires (CDC 3.2.2) ---
    private String categorieCpvLibelle;   // libellé de la grande catégorie CPV, pas seulement le code
    private String sourceUrl;             // lien vers le jeu de données source (traçabilité, CDC 4)
    private long nombreCommentaires;      // commentaires approuvés
    private long nombreSignalements;

    public String getCategorieCpvLibelle() { return categorieCpvLibelle; }
    public void setCategorieCpvLibelle(String categorieCpvLibelle) { this.categorieCpvLibelle = categorieCpvLibelle; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public long getNombreCommentaires() { return nombreCommentaires; }
    public void setNombreCommentaires(long nombreCommentaires) { this.nombreCommentaires = nombreCommentaires; }

    public long getNombreSignalements() { return nombreSignalements; }
    public void setNombreSignalements(long nombreSignalements) { this.nombreSignalements = nombreSignalements; }
}
