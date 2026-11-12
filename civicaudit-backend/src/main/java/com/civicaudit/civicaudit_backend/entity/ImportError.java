package com.civicaudit.civicaudit_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Ligne DECP rejetée à l'import (CDC 3.1 étape 5 « rejet contrôlé ») : la ligne brute et la raison
 * sont conservées, le marché n'est jamais inséré à moitié rempli.
 */
@Entity
@Table(name = "import_error")
public class ImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid_marche", length = 64)
    private String uidMarche;

    @Column(name = "ligne_brute", columnDefinition = "TEXT", nullable = false)
    private String ligneBrute;

    @Column(name = "raison", nullable = false, length = 1000)
    private String raison;

    @Column(name = "source_filename")
    private String sourceFilename;

    @Column(name = "date_import", nullable = false)
    private LocalDateTime dateImport = LocalDateTime.now();

    public ImportError() {
    }

    public ImportError(String uidMarche, String ligneBrute, String raison, String sourceFilename) {
        this.uidMarche = uidMarche;
        this.ligneBrute = ligneBrute;
        this.raison = raison;
        this.sourceFilename = sourceFilename;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUidMarche() { return uidMarche; }
    public void setUidMarche(String uidMarche) { this.uidMarche = uidMarche; }

    public String getLigneBrute() { return ligneBrute; }
    public void setLigneBrute(String ligneBrute) { this.ligneBrute = ligneBrute; }

    public String getRaison() { return raison; }
    public void setRaison(String raison) { this.raison = raison; }

    public String getSourceFilename() { return sourceFilename; }
    public void setSourceFilename(String sourceFilename) { this.sourceFilename = sourceFilename; }

    public LocalDateTime getDateImport() { return dateImport; }
    public void setDateImport(LocalDateTime dateImport) { this.dateImport = dateImport; }
}
