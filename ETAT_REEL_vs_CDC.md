# Audit : État réel du projet vs Cahier Des Charges

**Date de l'audit** : 27 août 2026  
**Version du CDC** : Finale  
**Auditeur** : Auto-audit basé sur inspection du code source

---

## ✅ Points CONFORMES au CDC

### Module 3.1 - Import DECP
| Exigence CDC | État réel | Conforme |
|--------------|-----------|----------|
| Normalisation des dates | ✅ Implémenté | ✅ |
| Règle de dédoublonnage (un marché = plusieurs titulaires) | ✅ Table `marche_titulaire` | ✅ |
| Rejet contrôlé (transaction par marché) | ✅ `REQUIRES_NEW` | ✅ |
| Substitution "Non renseigné" pour valeurs manquantes | ✅ Implémenté | ✅ |
| Idempotence (uid unique) | ✅ Contrainte UNIQUE | ✅ |

### Module 3.2 - Consultation publique
| Exigence CDC | État réel | Conforme |
|--------------|-----------|----------|
| Liste paginée (20 par défaut) | ✅ Implémenté | ✅ |
| Filtres (dates, montant, CPV, texte, procédure) | ✅ Via `Specification` | ✅ |
| Tri par date et montant | ✅ Implémenté | ✅ |
| Fiche détaillée avec montant actuel | ✅ Dernier avenant ou origine | ✅ |
| Vue fournisseur avec concentration | ✅ Implémenté | ✅ |
| Statistiques globales | ✅ Implémenté | ✅ |

### Module 3.3 - Participation citoyenne
| Exigence CDC | État réel | Conforme |
|--------------|-----------|----------|
| Catégories de signalement | `MONTANT_ANORMAL`, `CONCENTRATION_FOURNISSEUR`, `PROCEDURE_INHABITUELLE`, `DELAI_SUSPECT`, `AUTRE` | ✅ |
| Statuts de signalement | `NOUVEAU`, `EN_DISCUSSION`, `VERIFIE`, `CLOS` | ✅ |
| Statuts de commentaire | `EN_ATTENTE`, `APPROUVE`, `REJETE` | ✅ |
| Passage automatique à EN_DISCUSSION à 3 votes | ✅ Implémenté dans `SignalementService` | ✅ |
| Contrainte unicité signalement (user+marche+categorie) | ✅ `@UniqueConstraint` | ✅ |
| Commentaire max 1000 caractères | ✅ `@Size(max=1000)` | ✅ |
| Soft delete des commentaires | ✅ Champ `supprime` boolean | ✅ |

### Module 3.5 - Alertes automatiques
| Exigence CDC | État réel | Conforme |
|--------------|-----------|----------|
| Types d'alertes | `CONCENTRATION_ELEVEE`, `MONTANT_ATYPIQUE`, `SEUIL_SUSPECT` | ✅ |
| Règle 1: Concentration >40% sur 3 ans, alerte sur marchés <1 an | ✅ Implémenté correctement | ✅ |
| Règle 2: µ+2σ avec minimum 5 marchés par CPV | ✅ Implémenté correctement | ✅ |
| Règle 3: Procédure "sans publicité" ou "négocié sans", 35-40k€ | ✅ Implémenté correctement | ✅ |

### Module 3.6 - Authentification
| Exigence CDC | État réel | Conforme |
|--------------|-----------|----------|
| Mot de passe min 8 caractères + 1 chiffre | ✅ `@Size(min=8)` + `@Pattern(.*\\d.*)` | ✅ |
| Hashage BCrypt | ✅ `BCryptPasswordEncoder` | ✅ |
| JWT avec email + rôle | ✅ `JwtService` | ✅ |
| 3 rôles (USER, MODERATEUR, ADMIN) | ✅ Enum `Role` | ✅ |

### Module 3.7 - Export CSV
| Exigence CDC | État réel | Conforme |
|--------------|-----------|----------|
| Export CSV avec filtres | ✅ Endpoint `/api/marches/export` | ✅ |
| Colonnes principales (uid, objet, montant, date, etc.) | ✅ Implémenté | ✅ |

### Sécurité
| Exigence CDC | État réel | Conforme |
|--------------|-----------|----------|
| Headers de sécurité (CSP, X-Frame-Options, etc.) | ✅ Configurés dans `SecurityConfig` | ✅ |
| Rate limiting (10 actions/heure/utilisateur) | ⚠️ 100 req/min/IP (pas par utilisateur) | ⚠️ Partiel |
| Validation des entrées | ✅ Annotations `@Valid`, `@Size`, `@Pattern` | ✅ |

---

## ⚠️ Points PARTIELLEMENT conformes

### 1. Rate limiting
**Exigence CDC** : 10 actions par utilisateur et par heure  
**État réel** : 100 requêtes par minute par adresse IP  
**Écart** : 
- ✅ Le rate limiting existe
- ❌ Il est basé sur l'IP, pas sur l'utilisateur authentifié
- ❌ La limite est beaucoup plus permissive (100/min vs 10/heure)

**Impact** : Faible risque de spam, mais pas de protection contre un utilisateur créant plusieurs comptes

### 2. Affichage des commentaires en attente
**Exigence CDC** : L'auteur voit toujours son commentaire avec mention "en cours de modération"  
**État réel** : Les commentaires EN_ATTENTE ne sont pas retournés par l'API publique  
**Impact** : L'utilisateur ne voit pas son commentaire après soumission, ce qui peut créer de la confusion

**Code concerné** : `CommentaireService.getCommentairesByMarche()` filtre uniquement `APPROUVE`

### 3. Export CSV - Lien source
**Exigence CDC** : Inclure le lien vers la source data.gouv.fr  
**État réel** : Colonnes exportées sans la colonne `source_url`  
**Impact** : Mineur, mais non conforme au CDC

---

## ❌ Points NON conformes

### 1. Case de consentement RGPD à l'inscription
**Exigence CDC** : Case à cocher non pré-cochée pour le consentement RGPD  
**État réel** : Absente  
**Fichiers concernés** : 
- `RegisterRequest.java` : pas de champ `consentementRGPD`
- `register.component.html` : pas de checkbox

**Correction nécessaire** :
```java
// RegisterRequest.java
@AssertTrue(message = "Vous devez accepter la politique de confidentialité")
private Boolean consentementRGPD;
```

### 2. Suppression de compte avec anonymisation
**Exigence CDC** : L'utilisateur peut supprimer son compte, avec anonymisation des contributions  
**État réel** : Aucun endpoint de suppression de compte  
**Impact** : Non-conformité RGPD

**Correction nécessaire** :
- Endpoint `DELETE /api/auth/compte`
- Service d'anonymisation (remplacer email par "Utilisateur supprimé", garder commentaires/signalements)

### 3. Tests unitaires obligatoires
**Exigence CDC** : Tests unitaires sur `decp-import` et `alert-engine` (section 9)  
**État réel** : Aucun test unitaire  
**Impact** : Livrables manquants

**Fichiers manquants** :
- `src/test/java/.../service/ImportServiceTest.java`
- `src/test/java/.../service/AlerteServiceTest.java`

### 4. Index SQL
**Exigence CDC** : Index sur date_notification, montant, cpv_code, titulaire  
**État réel** : Aucun index explicite (seulement ceux générés automatiquement par Hibernate pour les FK)  
**Impact** : Performances sous-optimales sur de gros volumes

**Correction nécessaire** :
```java
// Dans MarchePublic.java
@Table(name = "marche_public", indexes = {
    @Index(name = "idx_date_notification", columnList = "date_notification"),
    @Index(name = "idx_montant", columnList = "montant"),
    @Index(name = "idx_cpv_code", columnList = "cpv_code")
})
```

### 5. Peuplement de CpvLibelle
**Exigence CDC** : Table de référence CPV avec libellés  
**État réel** : Table créée mais vide  
**Impact** : Affichage des codes CPV bruts sans libellé explicite

### 6. Modération non sécurisée
**Exigence CDC** : Endpoints de modération réservés aux rôles ADMIN et MODERATEUR  
**État réel** : `permitAll()` sur `/api/moderation/**`  
**Fichier concerné** : `SecurityConfig.java` ligne 45

**Impact** : ⚠️ **CRITIQUE** - N'importe qui peut modérer les commentaires en production

**Correction nécessaire** :
```java
.requestMatchers("/api/moderation/**").hasAnyRole("ADMIN", "MODERATEUR")
```
**ET** implémenter un intercepteur HTTP côté frontend pour envoyer le token JWT

---

## 🔒 Risques de sécurité identifiés

### CRITIQUE
1. **Secrets en clair dans le dépôt public**
   - Mot de passe BDD : `devpass` dans `application.properties`
   - Mot de passe admin par défaut : `admin123` documenté dans `ADMIN_CREDENTIALS.md`
   - Fichier `ADMIN_CREDENTIALS.md` committé

2. **Modération non protégée**
   - Les endpoints `/api/moderation/**` sont publics
   - Risque de manipulation malveillante des commentaires

### ÉLEVÉ
3. **JWT dans localStorage**
   - Vulnérable au XSS
   - CDC l'accepte si documenté comme limite connue (pas fait)

4. **Clé JWT par défaut**
   - Clé secrète JWT probablement générique (à vérifier dans `application.properties`)

### MOYEN
5. **CORS trop permissif en dev**
   - `http://localhost:4200` OK pour le dev
   - ⚠️ À restreindre au domaine de production

---

## 📊 Indicateurs techniques vérifiables

| Indicateur | Valeur | Méthode de vérification |
|------------|--------|-------------------------|
| Tables PostgreSQL | 11 | `psql -c "\dt"` |
| Marchés importés | 1070 | `SELECT COUNT(*) FROM marche_public` |
| Avenants importés | 1035 | `SELECT COUNT(*) FROM avenant` |
| Titulaires uniques | 476 | `SELECT COUNT(*) FROM titulaire` |
| Alertes générées | Variable | `SELECT COUNT(*) FROM alerte` |
| Endpoints API | À compter | Swagger UI `/swagger-ui/index.html` |
| Commits Git | 2 | `git log --oneline \| wc -l` |

---

## 📋 Livrables manquants (section 8 du CDC)

### Code source
- ✅ Backend Java/Spring Boot
- ✅ Frontend Angular
- ❌ Tests unitaires (ImportService, AlerteService)
- ⚠️ Historique Git insuffisant (2 commits seulement)

### Documentation
- ✅ Documentation API (Swagger)
- ⚠️ README minimal (à enrichir avec instructions complètes)
- ❌ Maquettes UI de la semaine 1
- ❌ Rapport avec partie "enjeux juridiques et éthiques"

### Démonstration
- ❌ Instance déployée accessible en ligne
- ⚠️ Démo locale possible mais non documentée

### Support de soutenance
- ❌ Diapositives de présentation

---

## 🎯 Plan d'action recommandé (par priorité)

### Priorité 1 - SÉCURITÉ (URGENT)
1. [ ] Passer le dépôt GitHub en privé **OU** retirer les secrets
2. [ ] Activer la protection par rôle sur `/api/moderation/**`
3. [ ] Créer un `.env` pour les secrets (BDD, JWT)
4. [ ] Supprimer `ADMIN_CREDENTIALS.md` du dépôt
5. [ ] Documenter JWT dans localStorage comme limite connue

### Priorité 2 - CONFORMITÉ RGPD
6. [ ] Ajouter case consentement RGPD à l'inscription
7. [ ] Implémenter suppression de compte avec anonymisation
8. [ ] Vérifier présence des mentions obligatoires dans les pages légales

### Priorité 3 - FONCTIONNALITÉS MANQUANTES
9. [ ] Permettre à l'auteur de voir ses commentaires en attente
10. [ ] Ajouter colonne `source_url` à l'export CSV
11. [ ] Peupler la table `CpvLibelle`
12. [ ] Ajouter les index SQL sur les colonnes de recherche

### Priorité 4 - TESTS ET QUALITÉ
13. [ ] Écrire tests unitaires pour ImportService
14. [ ] Écrire tests unitaires pour AlerteService
15. [ ] Enrichir l'historique Git avec commits atomiques

### Priorité 5 - DOCUMENTATION
16. [ ] Compléter le README avec instructions détaillées
17. [ ] Rédiger la partie juridique/éthique du rapport
18. [ ] Créer le support de soutenance
19. [ ] Documenter les limites techniques assumées

---

## ✅ Checklist de validation finale

### Avant de soumettre le projet :
- [ ] Dépôt GitHub sécurisé (privé ou secrets retirés)
- [ ] Endpoints de modération protégés par authentification
- [ ] Case consentement RGPD à l'inscription
- [ ] Tests unitaires présents et passants
- [ ] Index SQL créés
- [ ] Table CpvLibelle peuplée
- [ ] Export CSV inclut le lien source
- [ ] README complet avec instructions de démarrage
- [ ] Rapport de stage avec partie juridique/éthique
- [ ] Support de soutenance prêt

### Pour une vraie mise en production (hors scope du projet d'études) :
- [ ] Variables d'environnement externalisées
- [ ] HTTPS activé
- [ ] CORS restreint au domaine de production
- [ ] Mot de passe admin par défaut désactivé
- [ ] Rate limiting par utilisateur authentifié
- [ ] Monitoring et logs centralisés
- [ ] Backup automatique de la base de données

---

**Conclusion de l'audit** :

Le projet **CivicAudit** implémente correctement **la majorité des modules du CDC** (modules 3.1 à 3.5, 3.7 partiellement). Les **structures de données sont conformes** (catégories, statuts, alertes). Les **règles métier complexes** (alertes, votes, modération) sont correctement implémentées.

**Points critiques à corriger avant toute diffusion** :
1. Sécurité du dépôt (secrets exposés)
2. Protection réelle des endpoints de modération
3. Conformité RGPD (consentement, suppression de compte)
4. Tests unitaires obligatoires

**Estimation du travail restant** : 2-3 jours pour atteindre la conformité complète au CDC.
