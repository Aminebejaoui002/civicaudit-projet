package com.civicaudit.civicaudit_backend.dto;

import java.time.LocalDateTime;

public class CommentaireDTO {
    private Long id;
    private String contenu;
    private String statut;
    private String auteurEmail;
    private Long auteurId;
    private LocalDateTime dateCreation;
    private Boolean estAuteur; // pour savoir si l'utilisateur connecté est l'auteur

    // Constructeurs
    public CommentaireDTO() {}

    public CommentaireDTO(Long id, String contenu, String statut, String auteurEmail, Long auteurId, LocalDateTime dateCreation) {
        this.id = id;
        this.contenu = contenu;
        this.statut = statut;
        this.auteurEmail = auteurEmail;
        this.auteurId = auteurId;
        this.dateCreation = dateCreation;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getAuteurEmail() {
        return auteurEmail;
    }

    public void setAuteurEmail(String auteurEmail) {
        this.auteurEmail = auteurEmail;
    }

    public Long getAuteurId() {
        return auteurId;
    }

    public void setAuteurId(Long auteurId) {
        this.auteurId = auteurId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Boolean getEstAuteur() {
        return estAuteur;
    }

    public void setEstAuteur(Boolean estAuteur) {
        this.estAuteur = estAuteur;
    }
}
