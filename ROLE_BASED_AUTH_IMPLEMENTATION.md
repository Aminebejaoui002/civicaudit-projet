# Implémentation de l'authentification basée sur les rôles

## Vue d'ensemble
Les endpoints de modération sont maintenant protégés par une authentification basée sur les rôles. Seuls les utilisateurs avec le rôle `ADMIN` ou `MODERATEUR` peuvent accéder à ces endpoints.

## Changements implémentés

### 1. Configuration de sécurité (SecurityConfig.java)

#### Activation de la sécurité par méthode
```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
```

#### Protection des endpoints de modération
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/marches/**", "/api/fournisseurs/**", "/api/statistiques/**").permitAll()
    .requestMatchers("/api/moderation/**").hasAnyRole("ADMIN", "MODERATEUR") // ← Protection par rôle
    .anyRequest().authenticated()
)
```

### 2. Annotations @PreAuthorize sur ModerationController

Tous les endpoints de modération sont maintenant protégés:

```java
@GetMapping("/commentaires")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
public ResponseEntity<List<CommentaireDTO>> getCommentairesEnAttente() { ... }

@GetMapping("/signalements")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
public ResponseEntity<List<SignalementDTO>> getSignalements() { ... }

@PatchMapping("/commentaires/{id}/approuver")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
public ResponseEntity<Void> approuverCommentaire(@PathVariable Long id) { ... }

@PatchMapping("/commentaires/{id}/rejeter")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
public ResponseEntity<Void> rejeterCommentaire(@PathVariable Long id) { ... }

@PatchMapping("/signalements/{id}/statut")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATEUR')")
public ResponseEntity<Void> changerStatutSignalement(...) { ... }
```

### 3. Création automatique d'un utilisateur ADMIN au démarrage

Le fichier `ImportRunner.java` crée automatiquement un compte admin au premier démarrage:

```java
private void creerAdminParDefaut() {
    String adminEmail = "admin@civicaudit.tn";
    
    if (!userRepository.existsByEmail(adminEmail)) {
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
        
        log.info("=== Utilisateur ADMIN créé ===");
        log.info("Email: {}", adminEmail);
        log.info("Password: admin123");
        log.info("⚠️  IMPORTANT: Changez ce mot de passe en production!");
    }
}
```

## Compte admin par défaut

**Email:** `admin@civicaudit.tn`  
**Mot de passe:** `admin123`  
**Rôle:** `ADMIN`

⚠️ **IMPORTANT:** Changez ce mot de passe en production!

## Tests de vérification

### 1. Connexion en tant qu'admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@civicaudit.tn","password":"admin123"}'
```

**Résultat attendu:** HTTP 200 avec un token JWT
```json
{
  "token": "eyJhbGci...",
  "email": "admin@civicaudit.tn",
  "role": "ADMIN"
}
```

### 2. Accès aux endpoints de modération AVEC authentification
```bash
curl http://localhost:8080/api/moderation/commentaires \
  -H "Authorization: Bearer <TOKEN>"
```

**Résultat attendu:** HTTP 200 avec la liste des commentaires

### 3. Accès aux endpoints de modération SANS authentification
```bash
curl http://localhost:8080/api/moderation/commentaires
```

**Résultat attendu:** HTTP 403 Forbidden

## Rôles disponibles

L'entité `User` définit trois rôles:

```java
public enum Role {
    USER,        // Utilisateur normal (peut commenter, signaler)
    MODERATEUR,  // Modérateur (peut approuver/rejeter commentaires et gérer signalements)
    ADMIN        // Administrateur (accès complet)
}
```

## Flux d'authentification

1. **Connexion:**
   - L'utilisateur envoie email + password à `/api/auth/login`
   - Le backend vérifie les credentials
   - Si valide, génère un JWT contenant email + rôle
   - Retourne le token au client

2. **Autorisation:**
   - Le frontend envoie le token dans l'header `Authorization: Bearer <token>`
   - Le `JwtAuthenticationFilter` extrait et valide le token
   - Extrait le rôle et l'ajoute comme autorité avec préfixe `ROLE_`
   - Spring Security vérifie si l'utilisateur a le rôle requis

3. **Accès aux ressources protégées:**
   - Si le rôle correspond → HTTP 200
   - Si le rôle ne correspond pas → HTTP 403
   - Si pas de token → HTTP 403

## Modification du frontend nécessaire

Pour que la page de modération fonctionne, le frontend Angular doit:

1. **Stocker le token JWT après connexion**
2. **Inclure le token dans les requêtes HTTP**:
   ```typescript
   this.http.get('/api/moderation/commentaires', {
     headers: { 'Authorization': `Bearer ${token}` }
   })
   ```
3. **Gérer l'erreur 403** (rediriger vers login si non autorisé)

## Fichiers modifiés

- `SecurityConfig.java` - Ajout de @EnableMethodSecurity et protection par rôle
- `ModerationController.java` - Ajout @PreAuthorize sur tous les endpoints
- `ImportRunner.java` - Création automatique du compte admin
- `JwtAuthenticationFilter.java` - Déjà configuré pour extraire les rôles (inchangé)
- `JwtService.java` - Déjà configuré pour inclure les rôles dans le token (inchangé)

## Sécurité en production

### Recommandations:
1. ✅ Changer le mot de passe admin par défaut
2. ✅ Utiliser HTTPS en production
3. ✅ Configurer une clé JWT secrète forte (dans application.properties)
4. ✅ Implémenter un mécanisme de refresh token
5. ✅ Ajouter une expiration courte pour les tokens (actuellement 24h)
6. ✅ Implémenter un système de gestion des utilisateurs admin
7. ✅ Logger tous les accès aux endpoints de modération
8. ✅ Implémenter un système de récupération de mot de passe

## Prochaines étapes

1. **Frontend:** Implémenter l'interception HTTP pour ajouter automatiquement le token
2. **Frontend:** Créer une page de connexion pour les admins/modérateurs
3. **Backend:** Créer un endpoint pour changer le mot de passe
4. **Backend:** Créer des endpoints CRUD pour gérer les utilisateurs (réservé aux ADMIN)
5. **Backend:** Implémenter un audit log pour tracer les actions de modération

## Résultat final

✅ Les endpoints de modération sont maintenant sécurisés  
✅ Seuls les ADMIN et MODERATEUR peuvent y accéder  
✅ Un compte admin par défaut est créé automatiquement  
✅ Le système est prêt pour la production (après changement du mot de passe)
