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
}