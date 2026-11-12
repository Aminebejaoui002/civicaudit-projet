package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Service
public class ExportService {

    public String exporterMarchesCSV(List<MarchePublic> marches) throws IOException {
        StringWriter writer = new StringWriter();
        
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Objet", "Montant", "Titulaire principal", "Date notification", 
                          "Procédure", "Code CPV", "Durée (mois)", "Acheteur")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
            for (MarchePublic marche : marches) {
                String titulairePrincipal = marche.getTitulaires().isEmpty() ? 
                        "" : marche.getTitulaires().get(0).getNom();
                
                printer.printRecord(
                    marche.getId(),
                    marche.getObjet(),
                    marche.getMontant(),
                    titulairePrincipal,
                    marche.getDateNotification(),
                    marche.getProcedure(),
                    marche.getCpvCode(),
                    marche.getDureeMois(),
                    marche.getAcheteur().getNom()
                );
            }
        }

        return writer.toString();
    }
}
