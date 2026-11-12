package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.MarcheListeDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Export CSV (CDC 3.7) : colonnes du tableau + code CPV brut + lien source. */
class ExportServiceTest {

    @Test
    void exporteLesColonnesAttendues() throws Exception {
        MarcheListeDTO m = new MarcheListeDTO(1L, "Travaux; voirie", new BigDecimal("12345.60"), "SARL DUPONT",
                LocalDate.of(2024, 3, 1), "Procédure adaptée");
        m.setCpvCode("45233140");
        m.setCategorieCpvLibelle("Travaux de construction");
        m.setSourceUrl("https://www.data.gouv.fr/datasets/decp");

        String csv = new String(new ExportService().exporterMarchesCSV(List.of(m)), StandardCharsets.UTF_8);
        String[] lignes = csv.split("\r\n");

        assertThat(csv).startsWith("﻿"); // BOM pour Excel
        assertThat(lignes[0]).contains("Objet;Montant (EUR);Titulaire principal;Date de notification;Procédure;"
                + "Catégorie CPV;Code CPV;Lien source");
        assertThat(lignes[1]).isEqualTo("\"Travaux; voirie\";12345.60;SARL DUPONT;2024-03-01;Procédure adaptée;"
                + "Travaux de construction;45233140;https://www.data.gouv.fr/datasets/decp");
        assertThat(lignes).hasSize(2);
    }
}
