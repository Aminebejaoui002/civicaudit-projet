package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MarcheListeDTO {
    private Long id;
    private String objet;
    private BigDecimal montant;
    private String titulairePrincipal;
    private LocalDate dateNotification;
    private String procedure;

    public MarcheListeDTO(Long id, String objet, BigDecimal montant, String titulairePrincipal,
                          LocalDate dateNotification, String procedure) {
        this.id = id;
        this.objet = objet;
        this.montant = montant;
        this.titulairePrincipal = titulairePrincipal;
        this.dateNotification = dateNotification;
        this.procedure = procedure;
    }

    public Long getId() { return id; }
    public String getObjet() { return objet; }
    public BigDecimal getMontant() { return montant; }
    public String getTitulairePrincipal() { return titulairePrincipal; }
    public LocalDate getDateNotification() { return dateNotification; }
    public String getProcedure() { return procedure; }

    // --- Champs complémentaires (catégorie CPV lisible, code brut et source pour l'export CSV) ---
    private String cpvCode;
    private String categorieCpvLibelle;
    private String sourceUrl;

    public String getCpvCode() { return cpvCode; }
    public void setCpvCode(String cpvCode) { this.cpvCode = cpvCode; }

    public String getCategorieCpvLibelle() { return categorieCpvLibelle; }
    public void setCategorieCpvLibelle(String categorieCpvLibelle) { this.categorieCpvLibelle = categorieCpvLibelle; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
