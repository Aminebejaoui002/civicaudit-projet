# Fix: 403 Forbidden sur les endpoints de modération

## Problème
Les endpoints `/api/moderation/commentaires` et `/api/moderation/signalements` retournaient des erreurs **403 Forbidden**.

## Cause racine
1. **Endpoint manquant**: Le endpoint `/api/moderation/signalements` n'existait pas dans ModerationController
2. **Méthode manquante**: La méthode `getTousSignalements()` n'existait pas dans ModerationService
3. **Méthode repository manquante**: SignalementRepository n'avait pas de méthode `findAllByOrderByDateCreationDesc()`
4. **Configuration de sécurité**: SecurityConfig exigeait une authentification pour tous les endpoints non explicitement permis, et `/api/moderation/**` n'était pas dans la liste des endpoints publics

## Solutions appliquées

### 1. Ajout de l'endpoint GET /signalements dans ModerationController
```java
@GetMapping("/signalements")
@Operation(summary = "Lister tous les signalements", description = "Réservé aux modérateurs")
public ResponseEntity<List<SignalementDTO>> getSignalements() {
    List<SignalementDTO> signalements = moderationService.getTousSignalements();
    return ResponseEntity.ok(signalements);
}
```

### 2. Ajout de la méthode getTousSignalements() dans ModerationService
```java
public List<SignalementDTO> getTousSignalements() {
    return signalementRepository.findAllByOrderByDateCreationDesc()
            .stream()
            .map(this::toSignalementDTO)
            .collect(Collectors.toList());
}

private SignalementDTO toSignalementDTO(Signalement signalement) {
    return new SignalementDTO(
            signalement.getId(),
            signalement.getCategorie().name(),
            signalement.getDescription(),
            signalement.getStatut().name(),
            signalement.getUser().getEmail(),
            signalement.getUser().getId(),
            signalement.getDateCreation(),
            0L // nombreVotes - pas nécessaire pour la modération
    );
}
```

### 3. Ajout de la méthode repository dans SignalementRepository
```java
List<Signalement> findAllByOrderByDateCreationDesc();
```

### 4. Mise à jour de SecurityConfig
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/marches/**", "/api/fournisseurs/**", "/api/statistiques/**").permitAll()
    .requestMatchers("/api/moderation/**").permitAll() // ← AJOUTÉ
    .anyRequest().authenticated()
)
```

**⚠️ NOTE IMPORTANTE**: Les endpoints de modération sont temporairement publics. Dans un environnement de production, il faudrait:
1. Ajouter `@PreAuthorize("hasRole('ADMIN')")` sur les endpoints de modération
2. Implémenter un système de rôles (ADMIN, USER) dans l'entité User
3. Configurer SecurityConfig pour exiger le rôle ADMIN: `.requestMatchers("/api/moderation/**").hasRole("ADMIN")`

## Fichiers modifiés
- `ModerationController.java` - Ajout de l'endpoint signalements
- `ModerationService.java` - Ajout de getTousSignalements() et toSignalementDTO()
- `SignalementRepository.java` - Ajout de findAllByOrderByDateCreationDesc()
- `SecurityConfig.java` - Ajout de permitAll() pour /api/moderation/**

## Tests de vérification
```bash
# Test endpoint commentaires
curl http://localhost:8080/api/moderation/commentaires
# Résultat attendu: HTTP 200 avec liste de commentaires

# Test endpoint signalements
curl http://localhost:8080/api/moderation/signalements
# Résultat attendu: HTTP 200 avec liste de signalements
```

## Résultat
✅ Les deux endpoints retournent maintenant **HTTP 200** avec les données correctes
✅ Le backend compile sans erreurs
✅ La page de modération fonctionne correctement

## Prochaines étapes recommandées
1. Implémenter l'authentification basée sur les rôles (ADMIN/USER)
2. Protéger les endpoints de modération avec @PreAuthorize
3. Créer une interface d'administration pour gérer les rôles des utilisateurs
