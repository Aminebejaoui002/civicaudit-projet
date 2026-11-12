package com.civicaudit.civicaudit_backend.exception;

/** Ressource demandée inexistante (marché, fournisseur...) ; renvoyée en HTTP 404. */
public class RessourceIntrouvableException extends RuntimeException {
    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
