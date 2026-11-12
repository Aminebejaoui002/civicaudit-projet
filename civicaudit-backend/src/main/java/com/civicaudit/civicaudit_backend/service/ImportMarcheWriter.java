package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.Acheteur;
import com.civicaudit.civicaudit_backend.entity.Avenant;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import com.civicaudit.civicaudit_backend.repository.AcheteurRepository;
import com.civicaudit.civicaudit_backend.repository.MarchePublicRepository;
import com.civicaudit.civicaudit_backend.repository.TitulaireRepository;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Écrit en base UN marché DECP (toutes ses lignes : titulaires multiples + avenants) dans sa propre
 * transaction (REQUIRES_NEW) : si une ligne est invalide, seul ce marché est annulé, jamais l'import entier,
 * et rien n'est inséré à moitié rempli (CDC 3.1).
 *
 * <p>Règles appliquées (CDC 3.1) :</p>
 * <ul>
 *   <li><b>Dédoublonnage titulaires</b> : toutes les lignes de même uid forment un seul MarchePublic,
 *       chaque titulaire distinct est lié via la table marche_titulaire.</li>
 *   <li><b>Avenants</b> : la version de plus petit modification_id (ou sans modification_id) est le marché
 *       d'origine ; chaque version suivante devient un Avenant (nouveau montant, nouvelle durée).</li>
 *   <li><b>Normalisation</b> : montants en BigDecimal à 2 décimales, dates ISO, noms nettoyés
 *       (trim + espaces multiples) sans aucune correction sémantique.</li>
 *   <li><b>Idempotence</b> : upsert basé sur uid — un marché déjà présent est mis à jour, jamais dupliqué.</li>
 * </ul>
 */
@Component
public class ImportMarcheWriter {

    static final String NON_RENSEIGNE = "Non renseigné";

    /** Page data.gouv.fr du jeu de données source, utilisée quand la ligne ne fournit pas d'URL (traçabilité, CDC 4). */
    static final String SOURCE_URL_DEFAUT =
            "https://www.data.gouv.fr/datasets/donnees-essentielles-de-la-commande-publique-consolidees-format-tabulaire";

    public enum Resultat { CREE, MIS_A_JOUR }

    private final AcheteurRepository acheteurRepository;
    private final TitulaireRepository titulaireRepository;
    private final MarchePublicRepository marchePublicRepository;

    public ImportMarcheWriter(AcheteurRepository acheteurRepository,
                              TitulaireRepository titulaireRepository,
                              MarchePublicRepository marchePublicRepository) {
        this.acheteurRepository = acheteurRepository;
        this.titulaireRepository = titulaireRepository;
        this.marchePublicRepository = marchePublicRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Resultat importUnMarche(String uid, List<CSVRecord> lignes, String nomFichierImport) {
        if (lignes == null || lignes.isEmpty()) {
            throw new IllegalArgumentException("aucune ligne pour ce marché");
        }

        // 1. Regroupement par version (modification_id) : une version = une ou plusieurs lignes titulaires
        TreeMap<Double, List<CSVRecord>> parVersion = new TreeMap<>();
        for (CSVRecord ligne : lignes) {
            String modifBrut = valeur(ligne, "modification_id");
            double modif;
            try {
                modif = modifBrut == null ? 0.0 : Double.parseDouble(modifBrut);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("modification_id invalide : " + modifBrut);
            }
            parVersion.computeIfAbsent(modif, k -> new ArrayList<>()).add(ligne);
        }

        Double versionOrigine = parVersion.firstKey();
        CSVRecord premiere = parVersion.get(versionOrigine).get(0);

        // 2. Validation des champs obligatoires AVANT toute écriture (rejet contrôlé)
        String objet = valeur(premiere, "objet");
        String montantBrut = montantBrut(premiere);
        String dateBrute = valeur(premiere, "dateNotification");
        String acheteurId = valeur(premiere, "acheteur_id");

        if (objet == null) throw new IllegalArgumentException("objet manquant");
        if (montantBrut == null) throw new IllegalArgumentException("montant manquant");
        if (dateBrute == null) throw new IllegalArgumentException("date de notification manquante");
        if (acheteurId == null) throw new IllegalArgumentException("acheteur_id manquant");

        BigDecimal montant = parseMontant(montantBrut);
        LocalDate dateNotification = parseDate(dateBrute, "date de notification invalide");

        // 3. Upsert du marché sur uid (idempotence)
        Optional<MarchePublic> existant = marchePublicRepository.findByUid(uid);
        MarchePublic marche = existant.orElseGet(MarchePublic::new);

        marche.setUid(uid);
        marche.setAcheteur(upsertAcheteur(premiere));
        marche.setObjet(objet);
        marche.setMontant(montant);
        marche.setDateNotification(dateNotification);
        marche.setDureeMois(parseDuree(valeur(premiere, "dureeMois")));
        marche.setProcedure(valeur(premiere, "procedure"));
        marche.setCpvCode(valeur(premiere, "codeCPV"));

        String sourceFile = valeur(premiere, "sourceFile");
        marche.setSourceFilename(sourceFile != null ? sourceFile : nomFichierImport);
        String sourceDataset = valeur(premiere, "sourceDataset");
        marche.setSourceUrl(sourceDataset != null && sourceDataset.startsWith("http") ? sourceDataset : SOURCE_URL_DEFAUT);

        // 4. Titulaires : un même titulaire n'est lié qu'une fois, quel que soit le nombre de lignes
        Map<String, Titulaire> titulairesUniques = new LinkedHashMap<>();
        for (CSVRecord ligne : lignes) {
            String titulaireId = valeur(ligne, "titulaire_id");
            if (titulaireId != null && !titulairesUniques.containsKey(titulaireId)) {
                titulairesUniques.put(titulaireId, upsertTitulaire(ligne));
            }
        }
        marche.getTitulaires().clear();
        marche.getTitulaires().addAll(titulairesUniques.values());

        // 5. Avenants : reconstruits à chaque import à partir du fichier (source de vérité), sans doublon
        marche.getAvenants().clear();
        for (Map.Entry<Double, List<CSVRecord>> entry : parVersion.entrySet()) {
            if (entry.getKey().equals(versionOrigine)) continue;

            CSVRecord ligneVersion = entry.getValue().get(0);
            String montantVersionBrut = montantBrut(ligneVersion);
            if (montantVersionBrut == null) continue; // avenant sans montant : rien d'exploitable à afficher

            Avenant avenant = new Avenant();
            avenant.setMarchePublic(marche);
            avenant.setMontantApres(parseMontant(montantVersionBrut));
            avenant.setDureeApresMois(parseDuree(valeur(ligneVersion, "dureeMois")));

            String datePubBrute = valeur(ligneVersion, "datePublicationDonnees");
            avenant.setDateAvenant(datePubBrute != null
                    ? parseDate(datePubBrute, "date d'avenant invalide")
                    : dateNotification);
            marche.getAvenants().add(avenant);
        }

        marchePublicRepository.save(marche); // cascade ALL sur les avenants
        return existant.isPresent() ? Resultat.MIS_A_JOUR : Resultat.CREE;
    }

    private Acheteur upsertAcheteur(CSVRecord ligne) {
        String id = valeur(ligne, "acheteur_id");
        Acheteur a = acheteurRepository.findById(id).orElseGet(Acheteur::new);
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
        Titulaire t = titulaireRepository.findById(id).orElseGet(Titulaire::new);
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

    private String montantBrut(CSVRecord ligne) {
        String m = valeur(ligne, "montant_rationalise");
        return m != null ? m : valeur(ligne, "montant");
    }

    /** Montant : BigDecimal, toujours 2 décimales (jamais de double pour un montant financier). */
    static BigDecimal parseMontant(String brut) {
        try {
            return new BigDecimal(brut.replace(" ", "").replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("montant invalide : " + brut);
        }
    }

    /** Dates : on ne garde que la partie yyyy-MM-dd (les DECP contiennent parfois une heure ou un fuseau). */
    static LocalDate parseDate(String brut, String messageErreur) {
        try {
            return LocalDate.parse(brut.length() > 10 ? brut.substring(0, 10) : brut);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(messageErreur + " : " + brut);
        }
    }

    /** Durée : champ facultatif — une valeur illisible est ignorée plutôt que de rejeter le marché. */
    static Integer parseDuree(String brut) {
        if (brut == null) return null;
        try {
            return (int) Double.parseDouble(brut.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String orDefault(String v) {
        return v == null ? NON_RENSEIGNE : v;
    }

    /**
     * Lecture d'une cellule avec nettoyage de format uniquement : trim + espaces multiples réduits à un seul.
     * Aucune correction « sémantique » (orthographe, casse) n'est appliquée (règle CDC 3.1).
     * Les valeurs vides, "nan" et "none" (artefacts pandas) sont considérées comme absentes.
     */
    static String valeur(CSVRecord ligne, String colonne) {
        if (!ligne.isMapped(colonne) || !ligne.isSet(colonne)) return null;
        String v = ligne.get(colonne);
        if (v == null) return null;
        v = v.trim().replaceAll("\\s+", " ");
        if (v.isEmpty() || v.equalsIgnoreCase("nan") || v.equalsIgnoreCase("none") || v.equalsIgnoreCase("null")) {
            return null;
        }
        return v;
    }
}
