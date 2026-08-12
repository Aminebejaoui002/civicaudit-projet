package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final MarchePublicRepository marchePublicRepository;
    private final ImportMarcheWriter writer;

    public ImportService(MarchePublicRepository marchePublicRepository, ImportMarcheWriter writer) {
        this.marchePublicRepository = marchePublicRepository;
        this.writer = writer;
    }

    public static class ImportReport {
        public int lignesLues = 0;
        public int marchesImportes = 0;
        public int marchesIgnoresDejaExistants = 0;
        public int marchesRejetes = 0;
        public final List<String> raisonsRejet = new ArrayList<>();
    }

    public ImportReport importCsv(Path fichier) throws Exception {
        ImportReport rapport = new ImportReport();

        try (Reader in = new FileReader(fichier.toFile());
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).build().parse(in)) {

            Map<String, List<CSVRecord>> parUid = new LinkedHashMap<>();
            for (CSVRecord record : parser) {
                rapport.lignesLues++;
                parUid.computeIfAbsent(record.get("id"), k -> new ArrayList<>()).add(record);            }

            for (Map.Entry<String, List<CSVRecord>> entry : parUid.entrySet()) {
                String uid = entry.getKey();

                if (marchePublicRepository.existsByUid(uid)) {
                    rapport.marchesIgnoresDejaExistants++;
                    continue;
                }

                try {
                    writer.importUnMarche(uid, entry.getValue()); // sa propre transaction
                    rapport.marchesImportes++;
                } catch (Exception e) {
                    rapport.marchesRejetes++;
                    rapport.raisonsRejet.add("uid=" + uid + " -> " + e.getMessage());
                    log.warn("Marché rejeté (uid={}) : {}", uid, e.getMessage());
                }
            }
        }

        return rapport;
    }
}