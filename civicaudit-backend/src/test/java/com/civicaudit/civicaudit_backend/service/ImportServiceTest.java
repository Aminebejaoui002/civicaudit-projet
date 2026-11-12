package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.Acheteur;
import com.civicaudit.civicaudit_backend.entity.ImportError;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.AcheteurRepository;
import com.civicaudit.civicaudit_backend.repository.ImportErrorRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.TitulaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du module decp-import (CDC 3.1) sur des fichiers CSV synthétiques.
 * Les repositories sont simulés par des maps en mémoire : on teste la logique de regroupement,
 * de dédoublonnage, de gestion des avenants, de normalisation, de rejet et d'idempotence.
 */
class ImportServiceTest {

    private static final String SIRET_COMMUNE = "21830129700014";
    private static final String ENTETE =
            "id,acheteur_id,acheteur_nom,objet,montant,dateNotification,procedure,codeCPV,dureeMois,"
                    + "titulaire_id,titulaire_nom,modification_id,datePublicationDonnees\n";

    private final Map<String, MarchePublic> marches = new LinkedHashMap<>();
    private final Map<String, Titulaire> titulaires = new HashMap<>();
    private final Map<String, Acheteur> acheteurs = new HashMap<>();
    private final List<ImportError> erreurs = new ArrayList<>();
    private long sequence = 1;

    private ImportService importService;

    @BeforeEach
    void setUp() {
        MarchePublicRepository marcheRepo = mock(MarchePublicRepository.class);
        when(marcheRepo.findByUid(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(marches.get(inv.<String>getArgument(0))));
        when(marcheRepo.save(any(MarchePublic.class))).thenAnswer(inv -> {
            MarchePublic m = inv.getArgument(0);
            if (m.getId() == null) m.setId(sequence++);
            marches.put(m.getUid(), m);
            return m;
        });

        TitulaireRepository titulaireRepo = mock(TitulaireRepository.class);
        when(titulaireRepo.findById(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(titulaires.get(inv.<String>getArgument(0))));
        when(titulaireRepo.save(any(Titulaire.class))).thenAnswer(inv -> {
            Titulaire t = inv.getArgument(0);
            titulaires.put(t.getId(), t);
            return t;
        });

        AcheteurRepository acheteurRepo = mock(AcheteurRepository.class);
        when(acheteurRepo.findById(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(acheteurs.get(inv.<String>getArgument(0))));
        when(acheteurRepo.save(any(Acheteur.class))).thenAnswer(inv -> {
            Acheteur a = inv.getArgument(0);
            acheteurs.put(a.getId(), a);
            return a;
        });

        ImportErrorRepository erreurRepo = mock(ImportErrorRepository.class);
        when(erreurRepo.save(any(ImportError.class))).thenAnswer(inv -> {
            erreurs.add(inv.getArgument(0));
            return inv.getArgument(0);
        });

        ImportMarcheWriter writer = new ImportMarcheWriter(acheteurRepo, titulaireRepo, marcheRepo);
        importService = new ImportService(writer, erreurRepo, "");
    }

    private ImportService.ImportReport importer(String lignes) throws Exception {
        return importService.importCsv(new StringReader(ENTETE + lignes), "decp-test.csv");
    }

    private static String ligne(String uid, String montant, String date, String titulaireId, String titulaireNom,
                                String modificationId, String datePublication) {
        return String.join(",", uid, SIRET_COMMUNE, "Commune de Test", "\"Travaux de voirie, rue des Lilas\"",
                montant, date, "Procédure adaptée", "45233140", "12", titulaireId, titulaireNom,
                modificationId, datePublication) + "\n";
    }

    private static final String FICHIER_REFERENCE =
            // M1 : un marché avec 2 titulaires (groupement)
            ligne("M1", "100000", "2023-03-01", "11111111111111", "SARL DUPONT", "", "")
            + ligne("M1", "100000", "2023-03-01", "22222222222222", "SAS MARTIN", "", "")
            // M2 : un marché d'origine + 1 avenant (modification_id = 1)
            + ligne("M2", "50000", "2022-01-10", "33333333333333", "ESPACES VERTS SA", "", "")
            + ligne("M2", "60000", "2022-01-10", "33333333333333", "ESPACES VERTS SA", "1", "2023-02-15");

    @Test
    @DisplayName("Un marché à 2 titulaires n'apparaît qu'une fois, avec ses 2 titulaires")
    void regroupeLesTitulairesDUnMemeMarche() throws Exception {
        ImportService.ImportReport rapport = importer(FICHIER_REFERENCE);

        assertThat(rapport.lignesLues).isEqualTo(4);
        assertThat(rapport.marchesImportes).isEqualTo(2);
        assertThat(marches).hasSize(2);
        assertThat(marches.get("M1").getTitulaires())
                .extracting(Titulaire::getNom)
                .containsExactly("SARL DUPONT", "SAS MARTIN");
    }

    @Test
    @DisplayName("Un avenant est stocké séparément, le montant d'origine est conservé")
    void gereLesAvenants() throws Exception {
        importer(FICHIER_REFERENCE);

        MarchePublic m2 = marches.get("M2");
        assertThat(m2.getMontant()).isEqualByComparingTo("50000.00");
        assertThat(m2.getAvenants()).hasSize(1);
        assertThat(m2.getAvenants().get(0).getMontantApres()).isEqualByComparingTo("60000.00");
        assertThat(m2.getAvenants().get(0).getDateAvenant()).isEqualTo(LocalDate.of(2023, 2, 15));
        assertThat(m2.getTitulaires()).hasSize(1);
    }

    @Test
    @DisplayName("Idempotence : relancer l'import du même fichier ne duplique rien")
    void relancerLImportNeCreeAucunDoublon() throws Exception {
        importer(FICHIER_REFERENCE);
        ImportService.ImportReport second = importer(FICHIER_REFERENCE);

        assertThat(second.marchesImportes).isZero();
        assertThat(second.marchesMisAJour).isEqualTo(2);
        assertThat(marches).hasSize(2);
        assertThat(titulaires).hasSize(3);
        assertThat(marches.get("M1").getTitulaires()).hasSize(2);
        assertThat(marches.get("M2").getAvenants()).hasSize(1);
    }

    @Test
    @DisplayName("Rejet contrôlé : une ligne sans montant est journalisée et jamais insérée")
    void rejetteUneLigneSansMontant() throws Exception {
        ImportService.ImportReport rapport = importer(
                ligne("M3", "", "2023-05-01", "44444444444444", "SARL X", "", "")
                + ligne("M4", "20000", "2023-05-01", "44444444444444", "SARL X", "", ""));

        assertThat(rapport.marchesRejetes).isEqualTo(1);
        assertThat(rapport.marchesImportes).isEqualTo(1);
        assertThat(marches).containsOnlyKeys("M4");
        assertThat(erreurs).hasSize(1);
        assertThat(erreurs.get(0).getUidMarche()).isEqualTo("M3");
        assertThat(erreurs.get(0).getRaison()).isEqualTo("montant manquant");
        assertThat(erreurs.get(0).getLigneBrute()).contains("M3");
    }

    @Test
    @DisplayName("Rejet contrôlé : date de notification invalide")
    void rejetteUneDateInvalide() throws Exception {
        ImportService.ImportReport rapport = importer(
                ligne("M5", "20000", "2023-13-45", "44444444444444", "SARL X", "", ""));

        assertThat(rapport.marchesRejetes).isEqualTo(1);
        assertThat(marches).isEmpty();
        assertThat(erreurs.get(0).getRaison()).startsWith("date de notification invalide");
    }

    @Test
    @DisplayName("Normalisation : montant à 2 décimales, noms nettoyés sans correction sémantique")
    void normaliseLesChamps() throws Exception {
        importer(ligne("M6", "1234.5", "2024-07-08T00:00:00+02:00", "55555555555555", "\"  sarl   Dupont  & fils \"", "", ""));

        MarchePublic m = marches.get("M6");
        assertThat(m.getMontant().scale()).isEqualTo(2);
        assertThat(m.getMontant()).isEqualByComparingTo("1234.50");
        assertThat(m.getDateNotification()).isEqualTo(LocalDate.of(2024, 7, 8));
        assertThat(m.getObjet()).isEqualTo("Travaux de voirie, rue des Lilas");
        // trim + espaces multiples réduits, casse d'origine conservée
        assertThat(m.getTitulaires().get(0).getNom()).isEqualTo("sarl Dupont & fils");
        assertThat(m.getSourceFilename()).isEqualTo("decp-test.csv");
        assertThat(m.getSourceUrl()).startsWith("https://www.data.gouv.fr/");
    }

    @Test
    @DisplayName("Filtrage : seules les lignes de l'acheteur choisi sont importées")
    void filtreSurLAcheteurChoisi() throws Exception {
        importService.setAcheteurIdFiltre(SIRET_COMMUNE);
        String autreAcheteur = ligne("M7", "30000", "2023-01-01", "66666666666666", "SARL Y", "", "")
                .replace(SIRET_COMMUNE, "99999999999999");

        ImportService.ImportReport rapport = importer(FICHIER_REFERENCE + autreAcheteur);

        assertThat(rapport.lignesHorsPerimetre).isEqualTo(1);
        assertThat(marches).containsOnlyKeys("M1", "M2");
    }

    @Test
    @DisplayName("Un BOM UTF-8 en tête de fichier est ignoré")
    void ignoreLeBomUtf8() throws Exception {
        ImportService.ImportReport rapport = importService.importCsv(
                new StringReader("﻿" + ENTETE + FICHIER_REFERENCE), "avec-bom.csv");

        assertThat(rapport.marchesImportes).isEqualTo(2);
    }

    @Test
    @DisplayName("Un fichier sans colonne 'id' est refusé")
    void refuseUnFichierAuMauvaisFormat() {
        assertThatThrownBy(() -> importService.importCsv(new StringReader("a,b,c\n1,2,3\n"), "mauvais.csv"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }
}
