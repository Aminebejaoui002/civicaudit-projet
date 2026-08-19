package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commentaire")
public class Commentaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "marche_id", nullable = false)
    private MarchePublic marche;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.EN_ATTENTE;

    @Column(nullable = false)
    private Boolean supprime = false;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public enum Statut {
        EN_ATTENTE, APPROUVE, REJETE
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

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Boolean getSupprime() {
        return supprime;
    }

    public void setSupprime(Boolean supprime) {
        this.supprime = supprime;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
