package com.civicaudit.civicaudit_backend.dto;

import java.time.LocalDateTime;

public class SignalementDTO {
    private Long id;
    private String categorie;
    private String description;
    private String statut;
    private String auteurEmail;
    private Long auteurId;
    private LocalDateTime dateCreation;
    private Long nombreVotes;
    private Boolean aVote; // l'utilisateur connecté a-t-il voté ?

    // Constructeurs
    public SignalementDTO() {}

    public SignalementDTO(Long id, String categorie, String description, String statut, 
                          String auteurEmail, Long auteurId, LocalDateTime dateCreation, Long nombreVotes) {
        this.id = id;
        this.categorie = categorie;
        this.description = description;
        this.statut = statut;
        this.auteurEmail = auteurEmail;
        this.auteurId = auteurId;
        this.dateCreation = dateCreation;
        this.nombreVotes = nombreVotes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Long getNombreVotes() {
        return nombreVotes;
    }

    public void setNombreVotes(Long nombreVotes) {
        this.nombreVotes = nombreVotes;
    }

    public Boolean getAVote() {
        return aVote;
    }

    public void setAVote(Boolean aVote) {
        this.aVote = aVote;
    }
}
