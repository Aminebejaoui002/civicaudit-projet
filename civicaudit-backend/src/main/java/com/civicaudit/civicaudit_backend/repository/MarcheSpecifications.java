package com.civicaudit.civicaudit_backend.repository;

import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MarcheSpecifications {

    public static Specification<MarchePublic> dateApres(LocalDate date) {
        return (root, query, cb) -> date == null ? null :
                cb.greaterThanOrEqualTo(root.get("dateNotification"), date);
    }

    public static Specification<MarchePublic> dateAvant(LocalDate date) {
        return (root, query, cb) -> date == null ? null :
                cb.lessThanOrEqualTo(root.get("dateNotification"), date);
    }

    public static Specification<MarchePublic> montantMin(BigDecimal montant) {
        return (root, query, cb) -> montant == null ? null :
                cb.greaterThanOrEqualTo(root.get("montant"), montant);
    }

    public static Specification<MarchePublic> montantMax(BigDecimal montant) {
        return (root, query, cb) -> montant == null ? null :
                cb.lessThanOrEqualTo(root.get("montant"), montant);
    }

    public static Specification<MarchePublic> categorieCpv(String codeCategorie) {
        return (root, query, cb) -> {
            if (codeCategorie == null || codeCategorie.isBlank()) return null;
            // catégorie CPV = 2 premiers chiffres du code complet (règle CDC p.5)
            return cb.equal(cb.substring(root.get("cpvCode"), 1, 2), codeCategorie);
        };
    }

    public static Specification<MarchePublic> procedure(String procedure) {
        return (root, query, cb) -> (procedure == null || procedure.isBlank()) ? null :
                cb.equal(root.get("procedure"), procedure);
    }

    public static Specification<MarchePublic> rechercheTexte(String texte) {
        return (root, query, cb) -> {
            if (texte == null || texte.isBlank()) return null;
            query.distinct(true); // évite les doublons dus au join sur titulaires
            String pattern = "%" + texte.toLowerCase() + "%";
            var joinTitulaires = root.join("titulaires", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("objet")), pattern),
                    cb.like(cb.lower(joinTitulaires.get("nom")), pattern)
            );
        };
    }
}