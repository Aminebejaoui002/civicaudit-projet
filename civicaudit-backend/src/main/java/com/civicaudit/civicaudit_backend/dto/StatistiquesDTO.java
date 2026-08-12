package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StatistiquesDTO {
    private BigDecimal montantTotalDepenses;
    private long nombreTotalMarches;
    private long nombreFournisseursUniques;
    private Map<Integer, BigDecimal> montantParAnnee;       // année -> montant
    private List<TopFournisseurDTO> topFournisseurs;         // top 10
    private Map<String, Long> repartitionParProcedure;       // procédure -> nb marchés
    private Map<String, Long> repartitionParCategorieCpv;    // code 2 chiffres -> nb marchés

    public StatistiquesDTO(BigDecimal montantTotalDepenses, long nombreTotalMarches, long nombreFournisseursUniques,
                           Map<Integer, BigDecimal> montantParAnnee, List<TopFournisseurDTO> topFournisseurs,
                           Map<String, Long> repartitionParProcedure, Map<String, Long> repartitionParCategorieCpv) {
        this.montantTotalDepenses = montantTotalDepenses;
        this.nombreTotalMarches = nombreTotalMarches;
        this.nombreFournisseursUniques = nombreFournisseursUniques;
        this.montantParAnnee = montantParAnnee;
        this.topFournisseurs = topFournisseurs;
        this.repartitionParProcedure = repartitionParProcedure;
        this.repartitionParCategorieCpv = repartitionParCategorieCpv;
    }

    public BigDecimal getMontantTotalDepenses() { return montantTotalDepenses; }
    public long getNombreTotalMarches() { return nombreTotalMarches; }
    public long getNombreFournisseursUniques() { return nombreFournisseursUniques; }
    public Map<Integer, BigDecimal> getMontantParAnnee() { return montantParAnnee; }
    public List<TopFournisseurDTO> getTopFournisseurs() { return topFournisseurs; }
    public Map<String, Long> getRepartitionParProcedure() { return repartitionParProcedure; }
    public Map<String, Long> getRepartitionParCategorieCpv() { return repartitionParCategorieCpv; }
}