package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.dto.MarcheListeDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Export CSV (CDC 3.7) : mêmes colonnes que le tableau à l'écran + code CPV brut + lien source.
 * Séparateur « ; » et BOM UTF-8 pour une ouverture correcte dans Excel en français.
 */
@Service
public class ExportService {

    static final String[] ENTETES = {
            "Objet", "Montant (EUR)", "Titulaire principal", "Date de notification", "Procédure",
            "Catégorie CPV", "Code CPV", "Lien source"
    };

    public byte[] exporterMarchesCSV(List<MarcheListeDTO> marches) throws IOException {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader(ENTETES)
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (MarcheListeDTO m : marches) {
                printer.printRecord(
                        m.getObjet(),
                        m.getMontant() != null ? m.getMontant().toPlainString() : "",
                        m.getTitulairePrincipal(),
                        m.getDateNotification(),
                        m.getProcedure(),
                        m.getCategorieCpvLibelle(),
                        m.getCpvCode() != null ? m.getCpvCode() : "",
                        m.getSourceUrl() != null ? m.getSourceUrl() : ""
                );
            }
        }
        return ("﻿" + writer).getBytes(StandardCharsets.UTF_8);
    }
}
