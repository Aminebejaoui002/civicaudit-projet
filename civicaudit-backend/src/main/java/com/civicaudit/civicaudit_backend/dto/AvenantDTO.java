package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvenantDTO {
    private Long id;
    private BigDecimal montantApres;
    private Integer dureeApresMois;
    private LocalDate dateAvenant;

    public AvenantDTO(Long id, BigDecimal montantApres, Integer dureeApresMois, LocalDate dateAvenant) {
        this.id = id;
        this.montantApres = montantApres;
        this.dureeApresMois = dureeApresMois;
        this.dateAvenant = dateAvenant;
    }

    public Long getId() { return id; }
    public BigDecimal getMontantApres() { return montantApres; }
    public Integer getDureeApresMois() { return dureeApresMois; }
    public LocalDate getDateAvenant() { return dateAvenant; }
}