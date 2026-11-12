package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.User;

/**
 * Nom d'auteur affiché publiquement sous un commentaire ou un signalement.
 * <ul>
 *   <li>Compte supprimé (droit RGPD, CDC 3.6) : « Utilisateur supprimé ».</li>
 *   <li>Sinon : pseudonyme dérivé de l'email (3 premiers caractères + « *** »),
 *       pour ne jamais exposer l'adresse email complète au public.</li>
 * </ul>
 */
public final class AuteurAffichage {

    public static final String UTILISATEUR_SUPPRIME = "Utilisateur supprimé";

    private AuteurAffichage() {
    }

    public static String nom(User user) {
        if (user == null || Boolean.TRUE.equals(user.getSupprime())) {
            return UTILISATEUR_SUPPRIME;
        }
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            return "Citoyen";
        }
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String debut = local.length() <= 3 ? local : local.substring(0, 3);
        return debut + "***";
    }
}
