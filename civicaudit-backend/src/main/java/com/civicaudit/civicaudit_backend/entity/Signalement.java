package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signalement", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "marche_id", "categorie"})
})
public class Signalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "marche_id", nullable = false)
    private MarchePublic marche;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categorie categorie;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.NOUVEAU;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public enum Categorie {
        MONTANT_ANORMAL,
        CONCENTRATION_FOURNISSEUR,
        PROCEDURE_INHABITUELLE,
        DELAI_SUSPECT,
        AUTRE
    }

    public enum Statut {
        NOUVEAU, EN_DISCUSSION, VERIFIE, CLOS
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
