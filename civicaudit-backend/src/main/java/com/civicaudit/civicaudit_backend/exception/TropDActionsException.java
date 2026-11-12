package com.civicaudit.civicaudit_backend.exception;

/** Levée quand un utilisateur dépasse le quota anti-spam ; renvoyée en HTTP 429. */
public class TropDActionsException extends RuntimeException {
    public TropDActionsException(String message) {
        super(message);
    }
}
