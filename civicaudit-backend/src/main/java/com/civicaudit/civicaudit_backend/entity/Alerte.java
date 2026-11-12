package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerte", indexes = @Index(name = "idx_alerte_marche", columnList = "marche_id"))
public class Alerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "marche_id", nullable = false)
    private MarchePublic marche;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valeurCalculee;

    @Column(nullable = false)
    private LocalDateTime dateGeneration = LocalDateTime.now();

    public enum Type {
        CONCENTRATION_ELEVEE,
        MONTANT_ATYPIQUE,
        SEUIL_SUSPECT
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarchePublic getMarche() {
        return marche;
    }

    public void setMarche(MarchePublic marche) {
        this.marche = marche;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BigDecimal getValeurCalculee() {
        return valeurCalculee;
    }

    public void setValeurCalculee(BigDecimal valeurCalculee) {
        this.valeurCalculee = valeurCalculee;
    }

    public LocalDateTime getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(LocalDateTime dateGeneration) {
        this.dateGeneration = dateGeneration;
    }
}
