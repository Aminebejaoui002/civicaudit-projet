package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "avenant")
public class Avenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marche_id", nullable = false)
    private MarchePublic marchePublic;

    @Column(name = "montant_apres", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantApres;

    @Column(name = "duree_apres_mois")
    private Integer dureeApresMois;

    @Column(name = "date_avenant", nullable = false)
    private LocalDate dateAvenant;

    // --- Constructeurs ---
    public Avenant() {
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MarchePublic getMarchePublic() { return marchePublic; }
    public void setMarchePublic(MarchePublic marchePublic) { this.marchePublic = marchePublic; }

    public BigDecimal getMontantApres() { return montantApres; }
    public void setMontantApres(BigDecimal montantApres) { this.montantApres = montantApres; }

    public Integer getDureeApresMois() { return dureeApresMois; }
    public void setDureeApresMois(Integer dureeApresMois) { this.dureeApresMois = dureeApresMois; }

    public LocalDate getDateAvenant() { return dateAvenant; }
    public void setDateAvenant(LocalDate dateAvenant) { this.dateAvenant = dateAvenant; }
}