package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.Acheteur;
import com.civicaudit.civicaudit_backend.entity.Avenant;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.AcheteurRepository;
import com.civicaudit.civicaudit_backend.repository.AvenantRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.TitulaireRepository;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
public class ImportMarcheWriter {

    private static final String NON_RENSEIGNE = "Non renseigné";

    private final AcheteurRepository acheteurRepository;
    private final TitulaireRepository titulaireRepository;
    private final MarchePublicRepository marchePublicRepository;
    private final AvenantRepository avenantRepository;

    public ImportMarcheWriter(AcheteurRepository acheteurRepository,
                              TitulaireRepository titulaireRepository,
                              MarchePublicRepository marchePublicRepository,
                              AvenantRepository avenantRepository) {
        this.acheteurRepository = acheteurRepository;
        this.titulaireRepository = titulaireRepository;
        this.marchePublicRepository = marchePublicRepository;
        this.avenantRepository = avenantRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importUnMarche(String idMarche, List<CSVRecord> lignes) {

        // Regroupement par modification_id (une version = une ou plusieurs lignes titulaires)
        Map<Double, List<CSVRecord>> parVersion = new TreeMap<>();
        for (CSVRecord ligne : lignes) {
            String modifBrut = valeur(ligne, "modification_id");
            double modif = modifBrut == null ? 0.0 : Double.parseDouble(modifBrut);
            parVersion.computeIfAbsent(modif, k -> new ArrayList<>()).add(ligne);
        }

        Double versionOrigine = parVersion.keySet().iterator().next();
        CSVRecord premiere = parVersion.get(versionOrigine).get(0);

        String objet = valeur(premiere, "objet");
        String montantBrut = valeur(premiere, "montant_rationalise");
        if (montantBrut == null) montantBrut = valeur(premiere, "montant");
        String dateBrute = valeur(premiere, "dateNotification");

        if (objet == null) throw new IllegalArgumentException("objet manquant");
        if (montantBrut == null) throw new IllegalArgumentException("montant manquant");
        if (dateBrute == null) throw new IllegalArgumentException("date de notification manquante");

        BigDecimal montant = new BigDecimal(montantBrut);
        LocalDate dateNotification = LocalDate.parse(dateBrute.substring(0, 10));

        Acheteur acheteur = upsertAcheteur(premiere);

        MarchePublic marche = new MarchePublic();
        marche.setUid(idMarche);
        marche.setAcheteur(acheteur);
        marche.setObjet(objet);
        marche.setMontant(montant);
        marche.setDateNotification(dateNotification);
        marche.setProcedure(valeur(premiere, "procedure"));
        marche.setCpvCode(valeur(premiere, "codeCPV"));
        marche.setSourceFilename(valeur(premiere, "sourceFile"));
        marche.setSourceUrl(valeur(premiere, "sourceDataset"));

        String dureeBrute = valeur(premiere, "dureeMois");
        if (dureeBrute != null) {
            marche.setDureeMois((int) Double.parseDouble(dureeBrute));
        }

        Map<String, Titulaire> titulairesUniques = new LinkedHashMap<>();
        for (CSVRecord ligne : lignes) {
            String titulaireId = valeur(ligne, "titulaire_id");
            if (titulaireId == null) continue;
            if (!titulairesUniques.containsKey(titulaireId)) {
                titulairesUniques.put(titulaireId, upsertTitulaire(ligne));
            }
        }
        marche.setTitulaires(new ArrayList<>(titulairesUniques.values()));

        marchePublicRepository.save(marche);

        for (Map.Entry<Double, List<CSVRecord>> entry : parVersion.entrySet()) {
            if (entry.getKey().equals(versionOrigine)) continue;

            CSVRecord ligneVersion = entry.getValue().get(0);
            String montantVersionBrut = valeur(ligneVersion, "montant_rationalise");
            if (montantVersionBrut == null) montantVersionBrut = valeur(ligneVersion, "montant");
            if (montantVersionBrut == null) continue;

            Avenant avenant = new Avenant();
            avenant.setMarchePublic(marche);
            avenant.setMontantApres(new BigDecimal(montantVersionBrut));

            String dureeVersionBrute = valeur(ligneVersion, "dureeMois");
            if (dureeVersionBrute != null) {
                avenant.setDureeApresMois((int) Double.parseDouble(dureeVersionBrute));
            }

            String datePubBrute = valeur(ligneVersion, "datePublicationDonnees");
            avenant.setDateAvenant(datePubBrute != null
                    ? LocalDate.parse(datePubBrute.substring(0, 10))
                    : dateNotification);

            avenantRepository.save(avenant);
        }
    }

    private Acheteur upsertAcheteur(CSVRecord ligne) {
        String id = valeur(ligne, "acheteur_id");
        Acheteur a = acheteurRepository.findById(id).orElse(new Acheteur());
        a.setId(id);
        a.setNom(orDefault(valeur(ligne, "acheteur_nom")));
        a.setCategorie(valeur(ligne, "acheteur_categorie"));
        a.setCommuneCode(valeur(ligne, "acheteur_commune_code"));
        a.setCommuneNom(valeur(ligne, "acheteur_commune_nom"));
        a.setDepartementCode(valeur(ligne, "acheteur_departement_code"));
        a.setDepartementNom(valeur(ligne, "acheteur_departement_nom"));
        return acheteurRepository.save(a);
    }

    private Titulaire upsertTitulaire(CSVRecord ligne) {
        String id = valeur(ligne, "titulaire_id");
        Titulaire t = titulaireRepository.findById(id).orElse(new Titulaire());
        t.setId(id);
        t.setTypeIdentifiant(valeur(ligne, "titulaire_typeIdentifiant"));
        t.setNom(orDefault(valeur(ligne, "titulaire_nom")));
        t.setCategorie(valeur(ligne, "titulaire_categorie"));
        t.setActiviteCode(valeur(ligne, "titulaire_activite_code"));
        t.setActiviteLibelle(valeur(ligne, "titulaire_activite_libelle"));
        t.setCommuneCode(valeur(ligne, "titulaire_commune_code"));
        t.setCommuneNom(valeur(ligne, "titulaire_commune_nom"));
        t.setDepartementCode(valeur(ligne, "titulaire_departement_code"));
        t.setDepartementNom(valeur(ligne, "titulaire_departement_nom"));
        return titulaireRepository.save(t);
    }

    private String orDefault(String v) {
        return v == null ? NON_RENSEIGNE : v;
    }

    private String valeur(CSVRecord ligne, String colonne) {
        if (!ligne.isMapped(colonne)) return null;
        String v = ligne.get(colonne);
        if (v == null || v.isBlank() || v.equalsIgnoreCase("nan") || v.equalsIgnoreCase("none")) return null;
        return v;
    }
}