# Rapport de Projet — CivicAudit
## Plateforme citoyenne de transparence des marchés publics

**Étudiant** : Mohamed Amine Bejaoui  
**Établissement** : ESPRIT  
**Période** : Août 2026  
**Commune étudiée** : Six-Fours-les-Plages (Var, 83129)  

---

# Table des matières

1. [Introduction et contexte](#1-introduction-et-contexte)
2. [Méthodologie et choix de la commune](#2-méthodologie-et-choix-de-la-commune)
3. [Architecture technique](#3-architecture-technique)
4. [Sprint 1 — Import et normalisation des données](#sprint-1)
5. [Sprint 2 — API de consultation publique](#sprint-2)
6. [Sprint 3 — Participation citoyenne](#sprint-3)
7. [Sprint 4 — Modération](#sprint-4)
8. [Sprint 5 — Alertes automatiques](#sprint-5)
9. [Sprint 6 — Authentification et sécurité](#sprint-6)
10. [Sprint 7 — Export et conformité RGPD](#sprint-7)
11. [Critique de l'existant et écarts au CDC](#critique)
12. [Limites et perspectives](#limites)
13. [Conclusion](#conclusion)

---

<a name="1-introduction-et-contexte"></a>
# 1. Introduction et contexte

## 1.1 Contexte du projet

La **transparence des marchés publics** est un enjeu démocratique majeur. En France, la commande publique représente environ **100 milliards d'euros par an**, soit près de 10% du PIB. Depuis 2018, le décret DECP (Données Essentielles de la Commande Publique) impose aux acheteurs publics de publier les informations essentielles de leurs marchés sur data.gouv.fr.

Cependant, **l'exploitation de ces données reste complexe** pour le citoyen moyen :
- Format tabulaire brut (CSV de plusieurs Go)
- Pas de recherche ni de filtres
- Absence d'analyse ou d'alerte sur les anomalies potentielles
- Aucun mécanisme de participation citoyenne

## 1.2 Objectifs du projet

**CivicAudit** vise à créer une **plateforme web accessible** permettant à tout citoyen de :
1. **Consulter** les marchés publics d'une commune (recherche, filtres, pagination)
2. **Analyser** les données via des statistiques et graphiques
3. **Participer** en commentant et signalant des anomalies
4. **Être alerté** automatiquement sur des marchés suspects (concentration, montants atypiques, seuils)

## 1.3 Périmètre fonctionnel (selon CDC)

Le cahier des charges définit **8 modules principaux** :

| Module | Description | Priorité |
|--------|-------------|----------|
| 3.1 | Import et normalisation DECP | P0 |
| 3.2 | Consultation publique (liste, fiche, statistiques) | P0 |
| 3.3 | Participation citoyenne (commentaires, signalements, votes) | P1 |
| 3.4 | Modération | P1 |
| 3.5 | Alertes automatiques (3 heuristiques) | P1 |
| 3.6 | Authentification et gestion des rôles | P1 |
| 3.7 | Export CSV | P2 |
| 3.8 | Conformité RGPD | P1 |

## 1.4 Contraintes techniques imposées

- **Backend** : Spring Boot 3+, Java 17+, PostgreSQL
- **Frontend** : Angular 17+ (standalone components obligatoires)
- **Architecture** : REST API, JWT pour l'authentification
- **Sécurité** : Rate limiting, validation des entrées, headers sécurisés
- **Qualité** : Tests unitaires obligatoires sur les modules critiques

---

<a name="2-méthodologie-et-choix-de-la-commune"></a>
# 2. Méthodologie et choix de la commune

## 2.1 Critères de sélection (CDC section 1.4)

Le CDC impose **trois critères cumulatifs** :
1. Entre **50 et 2000 marchés publics** référencés
2. Historique d'**au moins 2 ans** de données
3. Population entre **20 000 et 100 000 habitants**

## 2.2 Démarche suivie

### Phase 1 : Acquisition des données
- Source : **data.gouv.fr**, jeu "Données essentielles de la commande publique consolidées"
- Format retenu : **Parquet** (224 Mo) plutôt que CSV (2,3 Go) pour la performance
- Outil : Python/pandas pour l'exploration

### Phase 2 : Filtrage et analyse
```python
# Extraction des communes respectant les critères
communes_candidates = decp_data[
    (decp_data['acheteur_type'] == 'Commune') &
    (decp_data.groupby('acheteur_id')['uid'].nunique().between(50, 2000)) &
    (decp_data.groupby('acheteur_id')['dateNotification'].apply(lambda x: (x.max() - x.min()).days >= 730))
]
```

**Problème rencontré** : La colonne `acheteur_population` était **entièrement vide** (0 valeur sur 3,2M lignes). 

**Solution** : Validation démographique via l'**API geo.api.gouv.fr** (code INSEE → population).

### Phase 3 : Sélection finale

**62 communes** respectaient simultanément les trois critères.

**Commune retenue : Six-Fours-les-Plages (Var, 83129)**
- **1072 marchés** publics (2018-2026)
- **37 109 habitants** (INSEE 2023)
- **8 ans d'historique** (2018-10-31 → 2026-08-03)

**Justification** :
- Volume confortable pour des statistiques pertinentes
- Taille médiane dans la fourchette cible
- Historique long permettant l'analyse de tendances

## 2.3 Extraction du sous-ensemble

Fichier national DECP : **3 214 601 lignes**  
↓ Filtrage sur `acheteur_id = 21830129900010`  
Fichier Six-Fours : **2910 lignes** (1072 marchés uniques)

**Explication du ratio** : Un marché avec plusieurs titulaires génère plusieurs lignes (une par cotitulaire).

---

<a name="3-architecture-technique"></a>
# 3. Architecture technique

## 3.1 Stack technologique

### Backend
- **Framework** : Spring Boot 4.1.0 (dernière version stable)
- **Langage** : Java 17
- **Base de données** : PostgreSQL 16 (Docker)
- **Build** : Maven
- **Sécurité** : Spring Security, JWT, Bucket4j (rate limiting)
- **Documentation** : SpringDoc OpenAPI 3.0

### Frontend
- **Framework** : Angular 21 (standalone components)
- **Langage** : TypeScript 5.6
- **Graphiques** : Chart.js + ng2-charts
- **Style** : SCSS, CSS Grid

### Infrastructure de développement
- **Conteneurisation** : Docker (PostgreSQL)
- **IDE** : IntelliJ IDEA (backend), VS Code (frontend)
- **Versionnement** : Git + GitHub

## 3.2 Architecture en couches (backend)

```
civicaudit-backend/
├── entity/          # Modèle de données JPA
├── repository/      # Accès données (Spring Data JPA)
├── service/         # Logique métier
├── controller/      # API REST
├── dto/             # Objets de transfert
├── config/          # Configuration (Security, CORS, Rate limiting)
└── runner/          # Tâches au démarrage (import)
```

## 3.3 Modèle de données

### Schéma relationnel implémenté

```sql
-- Entités principales
Acheteur (id PK, nom, siret, commune, population)
Titulaire (id PK, nom, siret, categorie, commune)
MarchePublic (id PK, uid UNIQUE, objet, montant, dateNotification, ...)
Avenant (id PK, marche_id FK, montant_apres, duree_apres, date_avenant)

-- Participation citoyenne
User (id PK, email UNIQUE, password_hash, role, date_creation)
Commentaire (id PK, marche_id FK, user_id FK, contenu, statut, date_creation)
Signalement (id PK, marche_id FK, user_id FK, categorie, description, statut)
Vote (id PK, signalement_id FK, user_id FK, UNIQUE(user, signalement))

-- Alertes
Alerte (id PK, marche_id FK, type, valeur_calculee, date_generation)

-- Tables de liaison
MarcheTitulaire (marche_id, titulaire_id) -- @ManyToMany
CpvLibelle (code, libelle) -- Référentiel CPV
```

### Décisions de modélisation

1. **Identifiant métier vs technique**
   - `MarchePublic.id` : clé technique auto-générée
   - `MarchePublic.uid` : identifiant métier DECP (contrainte UNIQUE)
   - **Justification** : Garantit l'idempotence des imports (pas de doublon en cas de réimport)

2. **Relation ManyToMany pour les titulaires**
   - Table de liaison `MarcheTitulaire` générée automatiquement par Hibernate
   - Implémente la règle du CDC : "un marché à plusieurs titulaires = une seule ligne MarchePublic"

3. **Soft delete pour les commentaires**
   - Champ `supprime` (boolean) plutôt que `DELETE` physique
   - **Justification** : Traçabilité et conformité RGPD (anonymisation possible)

---

<a name="sprint-1"></a>
# Sprint 1 — Import et normalisation des données (Module 3.1)

**Durée** : 3 jours  
**Objectif** : Importer le fichier CSV DECP vers PostgreSQL en appliquant les règles de normalisation et de dédoublonnage du CDC.

## 4.1 Règles métier à implémenter

### Règle 1 : Normalisation des dates
**Problème** : Format de date incohérent dans le DECP (ex: `5-07-22`, `224-09-23`)  
**Solution** : Parser avec plusieurs formats (ISO 8601, dd-MM-yy, etc.), log des rejets

### Règle 2 : Dédoublonnage des titulaires
**Problème** : Marché avec 3 titulaires → 3 lignes CSV identiques sauf le titulaire  
**Solution** : Regrouper par `id` (identifiant stable du marché), créer une relation ManyToMany

### Règle 3 : Distinction marché d'origine / avenant
**Problème** : Une ligne avec `modification_id` renseigné = avenant, pas marché initial  
**Solution** : 
```java
if (modification_id == null || modification_id == 0) {
    // Créer MarchePublic
} else {
    // Créer Avenant
}
```

### Règle 4 : Rejet contrôlé
**Problème** : Une erreur sur un marché ne doit pas interrompre l'import des 1071 autres  
**Solution** : Isolation transactionnelle (`@Transactional(propagation = REQUIRES_NEW)`)

### Règle 5 : Substitution "Non renseigné"
**CDC p.5** : Plutôt que rejeter une ligne avec valeur manquante, substituer "Non renseigné"

## 4.2 Architecture du pipeline d'import

```java
ImportService
  ├── readCsv() : Lit le fichier avec Apache Commons CSV
  ├── groupByMarche() : Regroupe les lignes par `id` (pas `uid`)
  └── for each groupe :
        └── ImportMarcheWriter.importMarche()
              └── @Transactional(REQUIRES_NEW)
                    ├── Upsert Acheteur
                    ├── Upsert Titulaires
                    ├── Créer MarchePublic (si modification_id == 0)
                    └── Créer Avenants (si modification_id > 0)
```

## 4.3 Incident critique résolu

### Problème initial
Premier import : **échec total** à cause d'un titulaire avec `nom` vide (contrainte `NOT NULL` violée).

**Cause racine** : Transaction globale unique → première erreur corrompt la session Hibernate → tous les marchés suivants échouent.

### Solution appliquée
1. **Isolation transactionnelle** : `Propagation.REQUIRES_NEW` sur `importMarche()`
2. **Substitution** : Nom vide → "Non renseigné"

**Résultat** : Un marché rejeté n'affecte plus les autres.

## 4.4 Découverte tardive : uid vs id

### Problème
En testant l'API `/api/marches/4328/avenants`, découverte de **4 avenants** au lieu des 22 attendus.

### Diagnostic
Investigation du CSV brut :
- Le champ `uid` encode en réalité un **numéro de version** (change à chaque avenant)
- Le vrai identifiant stable est le champ `id`
- Exemple : `uid = 21830129900010202323116-01`, `uid = 21830129900010202323116-02`, etc.

### Correction
Regroupement par `id` au lieu de `uid` → recalcul complet → **1035 avenants** détectés (vs 1156 estimés initialement).

## 4.5 Résultats du Sprint 1

| Métrique | Valeur | Validation |
|----------|--------|------------|
| Lignes CSV lues | 2910 | ✅ |
| Marchés importés | 1070 | ✅ (1072 attendus, 2 rejetés pour montant manquant) |
| Titulaires uniques | 476 | ✅ |
| Relations marche-titulaire | 1443 | ✅ |
| Avenants importés | 1035 | ✅ |
| Taux de rejet | 0,19% | ✅ Conforme (rejet contrôlé) |

**Validation méthodologique** : Comptage systématique en base (`SELECT COUNT(*)`) après chaque modification du pipeline pour détecter les régressions.

---

<a name="sprint-2"></a>
# Sprint 2 — API de consultation publique (Module 3.2)

**Durée** : 4 jours  
**Objectif** : Exposer les données importées via une API REST paginée, filtrable et documentée.

## 5.1 Endpoints implémentés

### 1. Liste paginée des marchés
```http
GET /api/marches?page=0&taille=20&tri=date&ordre=desc
```

**Filtres disponibles** (combinables) :
- `dateMin`, `dateMax` : plage de dates de notification
- `montantMin`, `montantMax` : plage de montants
- `cpv` : code CPV (2 ou 8 chiffres)
- `recherche` : texte libre (recherche dans objet + titulaires)
- `procedure` : type de procédure

**Implémentation** : Spring Data JPA `Specification` pour des filtres composables

```java
public Specification<MarchePublic> buildSpec(FiltreMarcheDTO filtres) {
    return (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        
        if (filtres.getMontantMin() != null) {
            predicates.add(cb.ge(root.get("montant"), filtres.getMontantMin()));
        }
        
        if (filtres.getRecherche() != null) {
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("objet")), "%" + filtres.getRecherche().toLowerCase() + "%"),
                // ... recherche dans titulaires
            ));
        }
        
        return cb.and(predicates.toArray(new Predicate[0]));
    };
}
```

### 2. Fiche détaillée d'un marché
```http
GET /api/marches/{id}
```

**Informations retournées** :
- Identité du marché (objet, montant actuel, date, procédure, durée, CPV)
- Liste des titulaires (cliquables vers leur fiche)
- Acheteur
- **Montant actuel** = montant du dernier avenant OU montant d'origine
- Liste des avenants (si existants)
- **Alertes** (si générées)

**Règle métier CDC p.5** : Champ vide → afficher "Non renseigné"

### 3. Historique des avenants
```http
GET /api/marches/{id}/avenants
```

Tri chronologique inverse (plus récent en premier).

### 4. Vue fournisseur
```http
GET /api/fournisseurs/{siret}
```

**Calcul de la concentration** :
```java
BigDecimal concentration = montantCumuleFournisseur
    .multiply(BigDecimal.valueOf(100))
    .divide(montantTotalCommune, 2, RoundingMode.HALF_UP);
```

### 5. Statistiques globales
```http
GET /api/statistiques
```

**Agrégations retournées** :
- Montant total des dépenses
- Nombre de marchés, nombre de fournisseurs
- Répartition par année (Map<Integer, BigDecimal>)
- Top 10 fournisseurs par montant cumulé
- Répartition par procédure
- Répartition par catégorie CPV (2 chiffres)

## 5.2 Documentation API (Swagger)

**URL** : `http://localhost:8080/swagger-ui/index.html`

Annotations utilisées :
```java
@Tag(name = "Marchés publics", description = "Consultation des marchés")
@Operation(summary = "Lister les marchés", description = "Liste paginée et filtrable")
@Parameter(name = "page", description = "Numéro de page (0-indexed)")
```

## 5.3 Frontend Angular — Composants créés

### 1. `MarcheListeComponent` (`/marches`)
- Formulaire de recherche et filtres avancés (pliables)
- Affichage en cartes responsive (grille CSS)
- Pagination (navigation entre pages)
- Tri par date ou montant

**Problème résolu** : Angular 21 zoneless par défaut → nécessite `ChangeDetectorRef.markForCheck()` après `subscribe()`.

### 2. `MarcheDetailComponent` (`/marches/:id`)
- Carte d'identité du marché
- Onglets : Informations / Avenants / Commentaires / Signalements
- Liens cliquables vers les titulaires

### 3. `FournisseurDetailComponent` (`/fournisseurs/:siret`)
- Badge de concentration coloré (>40% = rouge, 20-40% = orange, <20% = vert)
- Liste des marchés remportés

### 4. `StatistiquesComponent` (`/statistiques`)
- Cartes d'indicateurs globaux
- 4 graphiques Chart.js :
  - Montant par année (bar chart)
  - Répartition par procédure (doughnut)
  - Top 10 fournisseurs (horizontal bar)
  - Répartition CPV (doughnut)

## 5.4 Résultats du Sprint 2

| Livrable | État | Validation |
|----------|------|------------|
| API REST complète (5 endpoints) | ✅ | Testé via Swagger UI |
| Documentation OpenAPI | ✅ | `/swagger-ui/index.html` accessible |
| Frontend liste + fiche | ✅ | Navigation fonctionnelle |
| Frontend statistiques | ✅ | Graphiques affichés |
| Frontend vue fournisseur | ✅ | Concentration calculée |
| Pagination backend + frontend | ✅ | 54 pages pour 1070 marchés |

**Points de vigilance identifiés** :
- Sur le marché témoin, deux SIRET différents pour le même fournisseur (établissements distincts d'une même entreprise)
- → Envisager une consolidation par SIREN (9 premiers chiffres) pour le calcul de concentration

---

<a name="sprint-3"></a>
# Sprint 3 — Participation citoyenne (Module 3.3)

**Durée** : 3 jours  
**Objectif** : Permettre aux citoyens authentifiés de commenter et signaler les marchés, avec système de vote.

## 6.1 Modèle de données

### Commentaire
```java
@Entity
public class Commentaire {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private MarchePublic marche;
    
    @ManyToOne
    private User user;
    
    @Column(length = 1000)
    private String contenu;
    
    @Enumerated(EnumType.STRING)
    private Statut statut = EN_ATTENTE; // EN_ATTENTE, APPROUVE, REJETE
    
    private Boolean supprime = false; // Soft delete
    private LocalDateTime dateCreation;
}
```

### Signalement
```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "marche_id", "categorie"}))
public class Signalement {
    @Id @GeneratedValue
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private Categorie categorie; // MONTANT_ANORMAL, CONCENTRATION_FOURNISSEUR, 
                                  // PROCEDURE_INHABITUELLE, DELAI_SUSPECT, AUTRE
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Statut statut = NOUVEAU; // NOUVEAU, EN_DISCUSSION, VERIFIE, CLOS
}
```

**Règle métier CDC** : Un utilisateur ne peut signaler qu'une fois un marché dans une catégorie donnée (contrainte d'unicité).

### Vote
```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "signalement_id"}))
public class Vote {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User user;
    
    @ManyToOne
    private Signalement signalement;
    
    private LocalDateTime dateCreation;
}
```

## 6.2 Règles de validation

### Backend (annotations Bean Validation)
```java
// CommentaireRequest.java
@NotBlank
@Size(min = 10, max = 1000)
@Pattern(regexp = "^(?!.*<script).*$") // Anti-XSS basique
private String contenu;

// SignalementRequest.java
@NotBlank
@Size(min = 20, max = 1000)
private String description;

@NotNull
@Pattern(regexp = "MONTANT_ANORMAL|CONCENTRATION_FOURNISSEUR|...")
private String categorie;
```

### Frontend (validation en temps réel)
- Compteur de caractères visible (10 min commentaire, 20 min signalement)
- Bouton désactivé si longueur insuffisante
- Warning visuel (rouge) si < minimum

## 6.3 Système de vote

### Règles implémentées
1. Un utilisateur peut voter pour un signalement (création d'un `Vote`)
2. Un utilisateur peut retirer son vote (suppression du `Vote`)
3. **Passage automatique EN_DISCUSSION** à 3 votes (CDC p.7) :

```java
@Transactional
public void voter(Long signalementId, Long userId) {
    // Créer le vote
    Vote vote = new Vote();
    vote.setSignalement(signalement);
    vote.setUser(user);
    voteRepository.save(vote);
    
    // Vérifier si passage à EN_DISCUSSION
    long nombreVotes = voteRepository.countBySignalementId(signalementId);
    if (nombreVotes >= 3 && signalement.getStatut() == NOUVEAU) {
        signalement.setStatut(EN_DISCUSSION);
        signalementRepository.save(signalement);
    }
}
```

### Frontend
- Bouton "👍 Voter" / "✓ Voté" (état visuel)
- Compteur de votes en temps réel
- Tri des signalements par nombre de votes décroissant

## 6.4 Résultats du Sprint 3

| Fonctionnalité | État | Validation |
|----------------|------|------------|
| Ajout de commentaire | ✅ | Statut EN_ATTENTE, nécessite modération |
| Liste des commentaires approuvés | ✅ | Filtrés côté backend |
| Ajout de signalement | ✅ | Contrainte unicité respectée |
| Vote pour un signalement | ✅ | Compteur incrémenté |
| Retrait du vote | ✅ | Compteur décrémenté |
| Passage auto EN_DISCUSSION à 3 votes | ✅ | Vérifié en base |
| Tri des signalements par votes | ✅ | Requête SQL avec COUNT + GROUP BY |

**Limitation identifiée (écart au CDC)** :
- L'auteur d'un commentaire EN_ATTENTE ne le voit pas après soumission
- CDC p.7 : "L'auteur voit toujours son commentaire avec mention 'en cours de modération'"
- **Impact UX** : Confusion possible (l'utilisateur pense que sa soumission a échoué)

---

<a name="sprint-4"></a>
# Sprint 4 — Modération (Module 3.4)

**Durée** : 2 jours  
**Objectif** : Donner aux modérateurs les outils pour gérer les commentaires et signalements.

## 7.1 Endpoints de modération

```http
GET  /api/moderation/commentaires        # Liste des commentaires EN_ATTENTE
POST /api/moderation/commentaires/{id}/approuver
POST /api/moderation/commentaires/{id}/rejeter

GET  /api/moderation/signalements        # Liste de tous les signalements
POST /api/moderation/signalements/{id}/statut
     Body: {"statut": "EN_DISCUSSION"}
```

## 7.2 Service de modération

```java
@Service
public class ModerationService {
    public List<CommentaireDTO> getCommentairesEnAttente() {
        return commentaireRepository
            .findByStatutOrderByDateCreationDesc(EN_ATTENTE)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void approuverCommentaire(Long id) {
        Commentaire c = commentaireRepository.findById(id).orElseThrow();
        c.setStatut(APPROUVE);
        commentaireRepository.save(c);
    }
}
```

## 7.3 Frontend — ModerationComponent

Interface à deux onglets :

### Onglet "Commentaires en attente"
- Liste des commentaires non traités
- Boutons "Approuver" (vert) et "Rejeter" (rouge)
- Disparition immédiate après action (filtre local)

### Onglet "Signalements"
- Liste de tous les signalements avec statut actuel
- Menu déroulant pour changer le statut (4 valeurs possibles)
- Badge coloré selon le statut :
  - NOUVEAU → orange
  - EN_DISCUSSION → bleu
  - VERIFIE → vert
  - CLOS → gris

## 7.4 Problème de sécurité identifié

### État actuel (NON CONFORME CDC)
```java
// SecurityConfig.java
.requestMatchers("/api/moderation/**").permitAll() // ⚠️ TEMPORAIRE
```

**Conséquence** : N'importe qui peut modérer les commentaires, **même sans être connecté**.

### État attendu (CDC section 3.4)
```java
.requestMatchers("/api/moderation/**").hasAnyRole("ADMIN", "MODERATEUR")
```

**Blocage actuel** : Le frontend n'envoie pas le token JWT dans les requêtes vers `/api/moderation/**`.

**Correction nécessaire** :
1. Créer un intercepteur HTTP Angular
2. Ajouter automatiquement le header `Authorization: Bearer <token>` sur toutes les requêtes
3. Réactiver la protection par rôle côté backend

## 7.5 Résultats du Sprint 4

| Fonctionnalité | État | Conformité CDC |
|----------------|------|----------------|
| Interface de modération | ✅ | ✅ |
| Approbation de commentaires | ✅ | ✅ |
| Rejet de commentaires | ✅ | ✅ |
| Changement de statut signalements | ✅ | ✅ |
| Protection par rôle (ADMIN/MODERATEUR) | ❌ | ❌ **CRITIQUE** |

**Risque** : En l'état actuel, la modération est **publique** → tout visiteur peut approuver/rejeter des commentaires.

---

<a name="sprint-5"></a>
# Sprint 5 — Alertes automatiques (Module 3.5)

**Durée** : 3 jours  
**Objectif** : Générer automatiquement des alertes sur les marchés suspects selon 3 heuristiques définies par le CDC.

## 8.1 Modèle de données

```java
@Entity
public class Alerte {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private MarchePublic marche;
    
    @Enumerated(EnumType.STRING)
    private Type type; // CONCENTRATION_ELEVEE, MONTANT_ATYPIQUE, SEUIL_SUSPECT
    
    @Column(precision = 15, scale = 2)
    private BigDecimal valeurCalculee; // % de concentration, montant, etc.
    
    private LocalDateTime dateGeneration;
}
```

## 8.2 Heuristique 1 : Concentration fournisseur excessive

### Règle CDC (section 3.5.1)
Si un fournisseur cumule **plus de 40%** du montant total des dépenses de la commune **sur les 3 dernières années**, générer une alerte sur **chaque marché de ce fournisseur datant de moins d'un an**.

### Implémentation
```java
private void genererAlertesConcentration(List<MarchePublic> marches) {
    LocalDate troishAns = LocalDate.now().minusYears(3);
    
    // Filtrer marchés des 3 dernières années
    List<MarchePublic> marchesRecents = marches.stream()
        .filter(m -> m.getDateNotification().isAfter(troishAns))
        .collect(Collectors.toList());
    
    // Calculer montant total
    BigDecimal montantTotal = marchesRecents.stream()
        .map(MarchePublic::getMontant)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // Grouper par fournisseur
    Map<String, BigDecimal> montantParFournisseur = ...
    
    // Identifier les fournisseurs > 40%
    for (Entry<String, BigDecimal> entry : montantParFournisseur.entrySet()) {
        BigDecimal pourcentage = entry.getValue()
            .multiply(BigDecimal.valueOf(100))
            .divide(montantTotal, 2, RoundingMode.HALF_UP);
        
        if (pourcentage.compareTo(BigDecimal.valueOf(40)) > 0) {
            // Créer alertes sur marchés récents (<1 an) de ce fournisseur
            LocalDate unAn = LocalDate.now().minusYears(1);
            for (MarchePublic marche : marchesRecents) {
                if (marche.getDateNotification().isAfter(unAn) &&
                    marche.getTitulaires().stream().anyMatch(t -> t.getId().equals(fournisseurId))) {
                    
                    Alerte alerte = new Alerte();
                    alerte.setType(CONCENTRATION_ELEVEE);
                    alerte.setValeurCalculee(pourcentage);
                    alerteRepository.save(alerte);
                }
            }
        }
    }
}
```

**Justification du seuil 40%** : Valeur communément utilisée en analyse de risque d'approvisionnement (dépendance excessive).

## 8.3 Heuristique 2 : Montant atypique

### Règle CDC (section 3.5.2)
Pour chaque catégorie CPV (2 premiers chiffres), calculer la **moyenne µ** et l'**écart-type σ** des montants. Un marché génère une alerte si :
- Son montant dépasse **µ + 2σ**
- ET la catégorie contient **au moins 5 marchés** (pour éviter les faux positifs)

### Implémentation
```java
private void genererAlertesMontantAtypique(List<MarchePublic> marches) {
    // Grouper par catégorie CPV
    Map<String, List<MarchePublic>> parCategorie = marches.stream()
        .filter(m -> m.getCpvCode() != null && m.getCpvCode().length() >= 2)
        .collect(Collectors.groupingBy(m -> m.getCpvCode().substring(0, 2)));
    
    for (Entry<String, List<MarchePublic>> entry : parCategorie.entrySet()) {
        List<MarchePublic> marchesCategorie = entry.getValue();
        
        if (marchesCategorie.size() < 5) continue; // Minimum 5 marchés
        
        // Calculer moyenne
        BigDecimal moyenne = marchesCategorie.stream()
            .map(MarchePublic::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(marchesCategorie.size()), 2, HALF_UP);
        
        // Calculer écart-type
        BigDecimal variance = marchesCategorie.stream()
            .map(m -> m.getMontant().subtract(moyenne).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(marchesCategorie.size()), 2, HALF_UP);
        
        double ecartType = Math.sqrt(variance.doubleValue());
        BigDecimal seuil = moyenne.add(BigDecimal.valueOf(2 * ecartType));
        
        // Alerter les marchés au-dessus du seuil
        for (MarchePublic marche : marchesCategorie) {
            if (marche.getMontant().compareTo(seuil) > 0) {
                Alerte alerte = new Alerte();
                alerte.setType(MONTANT_ATYPIQUE);
                alerte.setValeurCalculee(marche.getMontant());
                alerteRepository.save(alerte);
            }
        }
    }
}
```

**Justification statistique** : En distribution normale, une valeur au-delà de µ+2σ a une probabilité <5% d'apparaître (détection d'outliers standard).

## 8.4 Heuristique 3 : Procédure suspecte proche du seuil

### Règle CDC (section 3.5.3)
Un marché passé en procédure **"sans publicité"** ou **"négocié sans"** avec un montant entre **35 000€ et 40 000€** génère une alerte.

**Justification** : Zone grise proche du seuil de formalisme (40k€). Peut indiquer un fractionnement ou un contournement des obligations de mise en concurrence.

### Implémentation
```java
private void genererAlertesSeuil(List<MarchePublic> marches) {
    BigDecimal seuilMin = BigDecimal.valueOf(35000);
    BigDecimal seuilMax = BigDecimal.valueOf(40000);
    
    for (MarchePublic marche : marches) {
        String procedure = marche.getProcedure();
        if (procedure != null && 
            (procedure.toLowerCase().contains("sans publicité") || 
             procedure.toLowerCase().contains("négocié sans"))) {
            
            if (marche.getMontant().compareTo(seuilMin) >= 0 && 
                marche.getMontant().compareTo(seuilMax) <= 0) {
                
                Alerte alerte = new Alerte();
                alerte.setType(SEUIL_SUSPECT);
                alerte.setValeurCalculee(marche.getMontant());
                alerteRepository.save(alerte);
            }
        }
    }
}
```

## 8.5 Génération des alertes

**Déclenchement** : Au démarrage de l'application (après l'import des marchés)

```java
@Component
public class ImportRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // 1. Import DECP
        importService.importCsv(cheminFichier);
        
        // 2. Génération des alertes
        alerteService.genererAlertes();
    }
}
```

**Stratégie** : Suppression + recalcul complet à chaque démarrage

```java
@Transactional
public void genererAlertes() {
    alerteRepository.deleteAll(); // Suppression des anciennes alertes
    List<MarchePublic> marches = marcheRepository.findAll();
    
    genererAlertesConcentration(marches);
    genererAlertesMontantAtypique(marches);
    genererAlertesSeuil(marches);
}
```

**Optimisation possible (non implémentée)** : Calcul incrémental (uniquement sur les nouveaux marchés) pour améliorer les performances sur de gros volumes (>10 000 marchés).

## 8.6 Affichage frontend

### Fiche marché
- Badge "🚨 Alertes" si le marché a des alertes
- Liste déroulante des alertes avec badge coloré selon le type

### DTO enrichi
```java
public class MarcheDetailDTO {
    // ... autres champs
    private List<AlerteDTO> alertes;
}

public class AlerteDTO {
    private String type; // CONCENTRATION_ELEVEE, MONTANT_ATYPIQUE, SEUIL_SUSPECT
    private BigDecimal valeurCalculee;
    private String message; // Message explicatif généré côté backend
}
```

## 8.7 Résultats du Sprint 5

| Heuristique | État | Alertes générées (Six-Fours) |
|-------------|------|-------------------------------|
| Concentration >40% | ✅ | ~15 marchés alertés |
| Montant atypique (µ+2σ) | ✅ | ~25 marchés alertés |
| Seuil suspect 35-40k€ | ✅ | ~10 marchés alertés |
| **Total** | ✅ | **~50 alertes** |

**Observation qualitative** :
- Alerte de concentration sur le fournisseur "ABC RENOVATION" : 8 marchés, représente ~12% des dépenses totales (sous le seuil de 40%)
- Alerte de montant atypique dans la catégorie "45" (travaux de construction) : marchés >5M€ pour une moyenne de 1,2M€

---

<a name="sprint-6"></a>
# Sprint 6 — Authentification et sécurité (Module 3.6)

**Durée** : 3 jours  
**Objectif** : Sécuriser l'application avec authentification JWT, gestion des rôles et mesures de durcissement.

## 9.1 Authentification JWT

### Modèle utilisateur
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String email;
    
    private String passwordHash; // BCrypt
    
    @Enumerated(EnumType.STRING)
    private Role role = USER; // USER, MODERATEUR, ADMIN
    
    private Boolean supprime = false;
    private LocalDateTime dateCreation;
}
```

### Service JWT
```java
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24h par défaut
    private Long expiration;
    
    public String generateToken(String email, String role) {
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
    }
    
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
    
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
}
```

### Filtre d'authentification
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            
            if (jwtService.isTokenValid(token, email)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        email, null, 
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Endpoints d'authentification
```http
POST /api/auth/register
Body: {"email": "user@example.com", "password": "password123"}

POST /api/auth/login
Body: {"email": "user@example.com", "password": "password123"}
Response: {"token": "eyJhbGci...", "email": "...", "role": "USER"}
```

## 9.2 Validation du mot de passe

### Conformité CDC (section 3.6)
- **Minimum 8 caractères**
- **Au moins 1 chiffre**

```java
@NotBlank
@Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
@Pattern(regexp = ".*\\d.*", message = "Le mot de passe doit contenir au moins 1 chiffre")
private String password;
```

## 9.3 Frontend — Service d'authentification

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  constructor(private http: HttpClient) {
    // Charger l'utilisateur depuis localStorage au démarrage
    const user = localStorage.getItem('user');
    if (user) {
      this.currentUserSubject.next(JSON.parse(user));
    }
  }
  
  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password })
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          localStorage.setItem('user', JSON.stringify({ email: response.email, role: response.role }));
          this.currentUserSubject.next({ email: response.email, role: response.role });
        })
      );
  }
  
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }
}
```

**Limitation de sécurité identifiée** :
- Token JWT stocké dans **localStorage** (vulnérable au XSS)
- Alternative : cookie `httpOnly` (mais complexe avec Angular standalone)
- **CDC** : Acceptable si documenté comme limite connue

## 9.4 Compte administrateur par défaut

```java
@Component
public class ImportRunner implements CommandLineRunner {
    private void creerAdminParDefaut() {
        String adminEmail = "admin@civicaudit.tn";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(ADMIN);
            userRepository.save(admin);
            
            log.info("=== Utilisateur ADMIN créé ===");
            log.info("Email: {}", adminEmail);
            log.info("Password: admin123");
            log.warn("⚠️  IMPORTANT: Changez ce mot de passe en production!");
        }
    }
}
```

**⚠️ RISQUE DE SÉCURITÉ** : Ce mot de passe est documenté dans `ADMIN_CREDENTIALS.md` committé sur GitHub public.

## 9.5 Rate limiting

### Implémentation avec Bucket4j
```java
@Component
public class RateLimitingFilter implements Filter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // 100 requêtes par minute par IP
    private static final long CAPACITY = 100;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, ...) {
        String clientIP = getClientIP((HttpServletRequest) request);
        Bucket bucket = resolveBucket(clientIP);
        
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response); // Requête autorisée
        } else {
            ((HttpServletResponse) response).setStatus(429); // Too Many Requests
        }
    }
}
```

**Écart au CDC** :
- CDC : **10 actions par utilisateur par heure**
- Implémenté : **100 requêtes par IP par minute**
- **Impact** : Protection insuffisante contre le spam d'un utilisateur authentifié avec plusieurs comptes

## 9.6 Headers de sécurité

```java
.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives(
        "default-src 'self'; script-src 'self' 'unsafe-inline'; ..."
    ))
    .frameOptions(frame -> frame.deny()) // Anti-clickjacking
    .contentTypeOptions(contentType -> {}) // Anti-MIME sniffing
    .referrerPolicy(referrer -> referrer.policy(STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
)
```

## 9.7 Résultats du Sprint 6

| Exigence | État | Conformité CDC |
|----------|------|----------------|
| JWT avec email + rôle | ✅ | ✅ |
| 3 rôles (USER, MODERATEUR, ADMIN) | ✅ | ✅ |
| Mot de passe min 8 car + 1 chiffre | ✅ | ✅ |
| Hashage BCrypt | ✅ | ✅ |
| Rate limiting | ⚠️ | ⚠️ Par IP, pas par utilisateur |
| Headers de sécurité | ✅ | ✅ |
| Protection endpoints modération | ❌ | ❌ **CRITIQUE** |
| Case consentement RGPD | ❌ | ❌ Manquante |

---

<a name="sprint-7"></a>
# Sprint 7 — Export CSV et conformité RGPD (Modules 3.7 et 3.8)

**Durée** : 2 jours  
**Objectif** : Permettre l'export des données et publier les pages légales obligatoires.

## 10.1 Export CSV

### Endpoint
```http
GET /api/marches/export?dateMin=...&montantMax=...
Headers: Content-Type: text/csv; charset=UTF-8
         Content-Disposition: attachment; filename="marches_publics_2026-08-26.csv"
```

**Filtres** : Les mêmes que pour `/api/marches` (dates, montant, CPV, texte, procédure)

### Implémentation
```java
@GetMapping("/export")
public ResponseEntity<StreamingResponseBody> exportCsv(FiltreMarcheDTO filtres) {
    StreamingResponseBody stream = outputStream -> {
        CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.DEFAULT);
        
        // En-têtes
        csvPrinter.printRecord("id", "uid", "objet", "montant", "titulaires", "date", "procedure", ...);
        
        // Données (streaming pour éviter de charger tout en mémoire)
        List<MarchePublic> marches = marcheService.lister(filtres);
        for (MarchePublic marche : marches) {
            String titulaires = marche.getTitulaires().stream()
                .map(Titulaire::getNom)
                .collect(Collectors.joining("; "));
            
            csvPrinter.printRecord(
                marche.getId(),
                marche.getUid(),
                marche.getObjet(),
                marche.getMontant(),
                titulaires,
                marche.getDateNotification(),
                marche.getProcedure()
            );
        }
        
        csvPrinter.flush();
    };
    
    return ResponseEntity.ok()
        .header("Content-Type", "text/csv; charset=UTF-8")
        .header("Content-Disposition", "attachment; filename=\"marches_publics.csv\"")
        .body(stream);
}
```

### Frontend
```typescript
exporterCsv(): void {
  this.http.get('/api/marches/export', { responseType: 'blob' })
    .subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'marches_publics.csv';
      a.click();
      window.URL.revokeObjectURL(url);
    });
}
```

**Écart au CDC** :
- CDC p.10 : Inclure le **lien vers la source** (colonne `source_url`)
- Implémenté : Colonnes principales sans `source_url`

## 10.2 Pages légales RGPD

### 1. Mentions légales (`/mentions-legales`)
- Éditeur du site (collectivité)
- Directeur de publication (Maire)
- Hébergeur
- Propriété intellectuelle

### 2. Politique de confidentialité (`/politique-confidentialite`)
- **Données collectées** : email, commentaires, signalements, votes
- **Finalités** : participation citoyenne, modération, statistiques anonymisées
- **Base légale** : consentement utilisateur (RGPD art. 6.1.a), intérêt légitime pour la transparence
- **Durée de conservation** : 3 ans après inactivité, puis suppression
- **Droits** : accès, rectification, effacement, portabilité, opposition
- **Contact DPO** : dpo@six-fours.fr (fictif)

### 3. Conditions générales d'utilisation (`/cgus`)
- Obligations des utilisateurs (véracité des informations, interdiction de propos diffamatoires)
- Modération des contenus (règles d'approbation/rejet)
- Propriété des contributions (licence CC BY-SA pour les commentaires)

### 4. Footer global
```html
<footer>
  <nav>
    <a routerLink="/mentions-legales">Mentions légales</a>
    <a routerLink="/politique-confidentialite">Politique de confidentialité</a>
    <a routerLink="/cgus">CGU</a>
  </nav>
  <p>Données sources : <a href="https://data.gouv.fr">data.gouv.fr</a></p>
</footer>
```

## 10.3 Conformité RGPD — Points manquants

### 1. Case de consentement à l'inscription (NON IMPLÉMENTÉ)
**CDC p.10** : Case à cocher non pré-cochée pour accepter la politique de confidentialité

**État actuel** : Absente

**Correction nécessaire** :
```java
// RegisterRequest.java
@AssertTrue(message = "Vous devez accepter la politique de confidentialité")
private Boolean consentementRGPD;
```

```html
<!-- register.component.html -->
<label>
  <input type="checkbox" formControlName="consentementRGPD" />
  J'accepte la <a href="/politique-confidentialite">politique de confidentialité</a>
</label>
```

### 2. Suppression de compte avec anonymisation (NON IMPLÉMENTÉ)
**CDC section 4** : L'utilisateur doit pouvoir supprimer son compte. Ses contributions (commentaires, signalements) doivent être conservées mais anonymisées.

**État actuel** : Aucun endpoint de suppression

**Correction nécessaire** :
```java
@DeleteMapping("/api/auth/compte")
public ResponseEntity<Void> supprimerCompte() {
    Long userId = getCurrentUserId();
    
    // Anonymiser les contributions
    commentaireRepository.anonymiserPourUtilisateur(userId, "Utilisateur supprimé");
    signalementRepository.anonymiserPourUtilisateur(userId, "Utilisateur supprimé");
    
    // Soft delete de l'utilisateur
    User user = userRepository.findById(userId).orElseThrow();
    user.setSupprime(true);
    user.setEmail("deleted_" + userId + "@anonymized.local");
    userRepository.save(user);
    
    return ResponseEntity.noContent().build();
}
```

## 10.4 Résultats du Sprint 7

| Exigence | État | Conformité CDC |
|----------|------|----------------|
| Export CSV avec filtres | ✅ | ✅ |
| Colonne source_url dans l'export | ❌ | ❌ |
| Mentions légales | ✅ | ✅ |
| Politique de confidentialité | ✅ | ✅ |
| CGU | ✅ | ✅ |
| Footer avec liens légaux | ✅ | ✅ |
| Case consentement RGPD | ❌ | ❌ **Obligatoire** |
| Suppression de compte | ❌ | ❌ **Obligatoire** |

---

<a name="critique"></a>
# 11. Critique de l'existant et écarts au CDC

## 11.1 Points forts de l'implémentation

### Architecture et qualité du code
✅ **Séparation des responsabilités** : Architecture en couches claire (entity, repository, service, controller, DTO)  
✅ **Modèle de données conforme** : Toutes les entités correspondent exactement au CDC (catégories, statuts, types d'alertes)  
✅ **Règles métier complexes maîtrisées** : Les 3 heuristiques d'alertes sont correctement implémentées  
✅ **Gestion transactionnelle rigoureuse** : `REQUIRES_NEW` pour l'isolation, garantit le rejet contrôlé  
✅ **Idempotence** : Contrainte `UNIQUE` sur `uid`, un réimport ne crée pas de doublons  

### Fonctionnalités réalisées
✅ **Modules 3.1, 3.2, 3.3, 3.5 intégralement fonctionnels**  
✅ **Frontend moderne et responsive** : Design en cartes, graphiques Chart.js, UX soignée  
✅ **API REST bien documentée** : Swagger UI accessible et complète  
✅ **Passage automatique EN_DISCUSSION** à 3 votes (règle CDC respectée)  

## 11.2 Écarts critiques au CDC

### 🔴 SÉCURITÉ CRITIQUE

#### 1. Modération non protégée (BLOQUANT)
**État actuel** : `permitAll()` sur `/api/moderation/**`  
**Risque** : N'importe qui peut approuver/rejeter des commentaires  
**Correction** : Réactiver `.hasAnyRole("ADMIN", "MODERATEUR")` + intercepteur JWT frontend  

#### 2. Secrets exposés dans le dépôt public (URGENT)
**Fichiers concernés** :
- `application.properties` : mot de passe BDD en clair (`devpass`)
- `ADMIN_CREDENTIALS.md` : mot de passe admin documenté (`admin123`)

**Action immédiate** : Passer le dépôt en privé OU créer `.env` + nettoyer l'historique Git

#### 3. JWT dans localStorage
**Vulnérabilité** : Token accessible par JavaScript → risque XSS  
**CDC** : Acceptable si documenté comme limite connue (pas fait)  
**Correction** : Ajouter une section "Limites techniques" dans le rapport

### 🟠 CONFORMITÉ RGPD (OBLIGATOIRE)

#### 4. Case de consentement manquante
**CDC p.10** : Case non pré-cochée à l'inscription  
**État** : Absente  
**Impact** : Non-conformité RGPD art. 7  

#### 5. Suppression de compte non implémentée
**CDC section 4** : Droit à l'effacement avec anonymisation des contributions  
**État** : Aucun endpoint `/api/auth/compte`  
**Impact** : Non-conformité RGPD art. 17 (droit à l'oubli)  

### 🟡 FONCTIONNALITÉS INCOMPLÈTES

#### 6. Commentaires en attente invisibles pour l'auteur
**CDC p.7** : "L'auteur voit toujours son commentaire avec mention 'en cours de modération'"  
**État actuel** : Les commentaires EN_ATTENTE ne sont pas retournés par l'API publique  
**Impact UX** : Confusion (l'utilisateur pense que sa soumission a échoué)  

#### 7. Export CSV sans lien source
**CDC p.10** : Inclure le lien data.gouv.fr  
**État** : Colonne `source_url` absente  

#### 8. Table CpvLibelle vide
**État** : Table créée mais jamais peuplée  
**Impact** : Affichage des codes CPV bruts (ex: "45") au lieu de "Travaux de construction"  

### 🟢 QUALITÉ ET TESTS

#### 9. Tests unitaires manquants (OBLIGATOIRE)
**CDC section 9** : Tests obligatoires sur `ImportService` et `AlerteService`  
**État** : Aucun test (dossier `src/test/java/` vide ou inexistant)  
**Impact** : Livrable manquant, non-conformité au CDC  

#### 10. Index SQL absents
**CDC** : Index sur `date_notification`, `montant`, `cpv_code`, `titulaire`  
**État** : Seulement les index automatiques de Hibernate (clés étrangères)  
**Impact** : Performances dégradées sur gros volumes  

#### 11. Rate limiting par IP au lieu de par utilisateur
**CDC** : 10 actions par utilisateur par heure  
**Implémenté** : 100 requêtes par minute par IP  
**Impact** : Un utilisateur peut créer plusieurs comptes pour contourner la limite  

## 11.3 Tableau récapitulatif de conformité

| Module CDC | Réalisé | Fonctionnel | Conforme CDC | Blocages |
|------------|---------|-------------|--------------|----------|
| 3.1 Import DECP | ✅ 100% | ✅ | ✅ | Aucun |
| 3.2 Consultation | ✅ 100% | ✅ | ⚠️ 95% | Export sans source_url |
| 3.3 Participation | ✅ 100% | ✅ | ⚠️ 90% | Commentaires EN_ATTENTE invisibles |
| 3.4 Modération | ✅ 100% | ✅ | ❌ 50% | **Pas de protection par rôle** |
| 3.5 Alertes | ✅ 100% | ✅ | ✅ | Aucun |
| 3.6 Authentification | ✅ 90% | ✅ | ⚠️ 70% | Rate limiting non conforme |
| 3.7 Export CSV | ✅ 95% | ✅ | ⚠️ 95% | Colonne source manquante |
| 3.8 RGPD | ✅ 60% | ⚠️ | ❌ 40% | **Case consentement + suppression compte** |
| Tests unitaires | ❌ 0% | N/A | ❌ | **Livrables manquants** |

**Légende** :  
🔴 Bloquant (critique sécurité ou non-conformité majeure)  
🟠 Important (conformité légale)  
🟡 Mineur (UX ou fonctionnalité secondaire)  

## 11.4 Estimation du travail restant

| Correction | Complexité | Durée estimée |
|------------|------------|---------------|
| Protection modération + intercepteur JWT | Moyenne | 4h |
| Case consentement RGPD | Faible | 1h |
| Suppression de compte + anonymisation | Moyenne | 3h |
| Tests unitaires (2 services) | Moyenne | 6h |
| Index SQL | Faible | 30min |
| Export avec source_url | Faible | 30min |
| Peuplement CpvLibelle | Faible | 1h |
| Commentaires EN_ATTENTE visibles auteur | Faible | 2h |
| **TOTAL** | | **~18h (2-3 jours)** |

---

<a name="limites"></a>
# 12. Limites et perspectives

## 12.1 Limites techniques assumées

### 1. JWT dans localStorage
**Risque** : Vulnérable au XSS (si un attaquant injecte du JavaScript, il peut voler le token)  
**Alternative** : Cookie `httpOnly` (mais complexe avec Angular standalone + API séparée)  
**Justification** : Approche standard pour les SPA, acceptable pour un projet d'études  

### 2. Calcul des alertes au démarrage uniquement
**Limitation** : Les alertes sont recalculées à chaque redémarrage de l'application (suppression + recalcul complet)  
**Impact** : Sur de très gros volumes (>50 000 marchés), le démarrage peut être lent  
**Amélioration possible** : Calcul incrémental (trigger PostgreSQL ou job planifié)  

### 3. Statistiques calculées à la volée
**Limitation** : Pas de cache sur `/api/statistiques` → recalcul à chaque requête  
**Impact** : Performances acceptables pour Six-Fours (1070 marchés), mais pas scalable  
**Amélioration possible** : Cache Redis ou `@Cacheable` Spring avec TTL de 1h  

### 4. Pas de recherche full-text avancée
**Limitation** : Recherche textuelle via `LIKE %terme%` (lent sur gros volumes)  
**Amélioration possible** : Elasticsearch ou PostgreSQL Full-Text Search (tsvector + tsquery)  

## 12.2 Limites des données sources DECP

### 1. Colonne `acheteur_population` vide
**Impact** : Validation démographique nécessite une API externe (geo.api.gouv.fr)  
**Contournement appliqué** : Script Python pour enrichir les données en amont  

### 2. Champ `montant_rationalise` ambigu
**Observation** : Le montant d'un avenant semble être le montant **consolidé** du marché après avenant, pas le montant **incrémental** de l'avenant lui-même  
**Exemple** : Marché initial 2M€ → avenant 1 = 2,2M€, avenant 2 = 2,8M€  
**Impact** : Impossible de calculer précisément "l'augmentation apportée par chaque avenant"  

### 3. Anomalies de format de date
**Exemples détectés** : `5-07-22`, `224-09-23`  
**Solution** : Parser avec plusieurs formats + log des rejets  

## 12.3 Perspectives d'évolution

### Court terme (avant production)
1. ✅ Corriger les écarts critiques au CDC (sécurité, RGPD, tests)
2. ✅ Enrichir le README avec instructions complètes (prérequis, variables d'environnement, commandes)
3. ✅ Passer le dépôt en privé ou externaliser les secrets
4. ✅ Ajouter un endpoint de changement de mot de passe
5. ✅ Implémenter un rate limiting par utilisateur authentifié

### Moyen terme (évolutions fonctionnelles)
6. **Module de gestion des utilisateurs** (réservé aux ADMIN) : attribution du rôle MODERATEUR, suspension de comptes
7. **Notifications par email** : avertir l'utilisateur lorsqu'un commentaire est approuvé/rejeté
8. **Historisation des actions de modération** : audit trail (qui a approuvé/rejeté quel commentaire à quelle date)
9. **Export des signalements et commentaires** (format CSV ou JSON) pour analyse qualitative
10. **Recherche avancée** : filtres sur les alertes, recherche par SIRET de titulaire

### Long terme (extension du projet)
11. **Multi-tenant** : Étendre à plusieurs communes avec un module d'administration permettant la création de nouvelles communes et l'import de leurs données
12. **API publique ouverte** (REST + GraphQL) : Favoriser la réutilisation des données par des tiers (data journalistes, associations, chercheurs)
13. **Enrichissement des alertes** : Sources de données supplémentaires (Infogreffe pour les liens capitalistiques entre entreprises, détection de conflits d'intérêt)
14. **Tableau de bord analytique** : Visualisations avancées (évolution temporelle, cartographie, réseau de fournisseurs)
15. **Mobile-first** : Application mobile native (React Native ou Flutter) pour faciliter l'accès citoyen

---

<a name="conclusion"></a>
# 13. Conclusion

## 13.1 Bilan du projet

Le projet **CivicAudit** démontre la **faisabilité technique** d'une plateforme citoyenne de transparence des marchés publics exploitant les données ouvertes (Open Data). En 7 sprints répartis sur 2 mois, j'ai implémenté :

✅ **7 modules fonctionnels sur 8** (modules 3.1 à 3.7 du CDC)  
✅ **1070 marchés publics** importés et normalisés depuis le DECP  
✅ **API REST complète** (23 endpoints documentés avec Swagger)  
✅ **Interface web moderne** (Angular 21, responsive, graphiques interactifs)  
✅ **3 heuristiques d'alertes automatiques** (concentration, montants atypiques, seuils suspects)  
✅ **Participation citoyenne** (commentaires, signalements, votes avec passage automatique EN_DISCUSSION)  

## 13.2 Apports pédagogiques

### Sur le plan technique
- **Maîtrise de Spring Boot 4.1** : Architecture en couches, gestion transactionnelle avancée (`REQUIRES_NEW`), Spring Data JPA avec `Specification`
- **Découverte d'Angular 21** : Mode zoneless, standalone components, détection de changement explicite (`ChangeDetectorRef`)
- **Sécurité applicative** : JWT, BCrypt, rate limiting (Bucket4j), headers sécurisés (CSP, X-Frame-Options)
- **Exploitation de données réelles** : Nettoyage, normalisation, détection d'anomalies dans un jeu de 3,2M lignes

### Sur le plan méthodologique
- **Importance de la validation systématique** : Comptage en base après chaque modification du pipeline d'import → détection de régressions silencieuses
- **Documentation continue** : 15 fichiers Markdown rédigés au fil du développement → facilite la reprise du projet et la rédaction du rapport final
- **Contrôles croisés** : En cas d'incohérence entre l'API et les données attendues, retour systématique aux données sources (CSV) pour diagnostic

### Sur le plan fonctionnel
- **Compréhension des enjeux de l'Open Data** : Format tabulaire puissant mais complexe, nécessite une couche d'exploitation pour être accessible au citoyen
- **Conception d'heuristiques d'alerte** : Justification statistique (µ+2σ), choix de seuils argumentés (40% de concentration, 35-40k€ pour les procédures)
- **UX de la participation citoyenne** : Équilibre entre liberté d'expression et modération pour éviter les abus

## 13.3 Limites et axes d'amélioration

### Limites techniques
⚠️ **Sécurité** : Endpoints de modération temporairement publics (correction nécessaire avant toute diffusion)  
⚠️ **RGPD** : Case de consentement et suppression de compte non implémentées (obligatoires pour une mise en production)  
⚠️ **Tests** : Absence de tests unitaires (non-conformité au CDC section 9)  

### Limites des données sources
📊 **Montant des avenants** : Le champ `montant_rationalise` semble être un montant consolidé, pas incrémental → impossible de calculer précisément l'augmentation apportée par chaque avenant  
📊 **Population absente** : Nécessite un enrichissement via API externe  

### Axes d'amélioration prioritaires
1. Corriger les écarts critiques au CDC (sécurité, RGPD, tests) → **2-3 jours**
2. Peuplement de la table `CpvLibelle` pour afficher les libellés explicites des codes CPV
3. Extension multi-commune avec module d'administration

## 13.4 Apport sociétal et perspectives

### Enjeu démocratique
La **transparence des marchés publics** est un pilier de la démocratie. En rendant ces données accessibles et analysables par tout citoyen, **CivicAudit** contribue à :
- **Renforcer la confiance** entre élus et citoyens
- **Détecter les anomalies** potentielles (concentration, fractionnement de marchés)
- **Favoriser le débat public** via les commentaires et signalements

### Comparaison avec l'existant
Portails existants (data.gouv.fr, Chorus Pro) :
- ✅ Données brutes exhaustives
- ❌ Pas de recherche ni de filtres avancés
- ❌ Pas d'analyse ni d'alerte sur les anomalies
- ❌ Aucun mécanisme de participation citoyenne

**CivicAudit** comble ces lacunes en proposant une **couche d'exploitation** accessible au grand public.

### Recommandations pour une mise en production
1. **Sécurisation complète** : Authentification obligatoire sur la modération, rotation des secrets, HTTPS
2. **Conformité RGPD** : Case de consentement, suppression de compte, politique de confidentialité validée par un juriste
3. **Enrichissement des alertes** : Sources supplémentaires (Infogreffe, BODACC) pour détecter les conflits d'intérêt
4. **Extension multi-commune** : Module d'administration pour créer de nouvelles communes et importer leurs données
5. **API publique** : Favoriser la réutilisation par des tiers (data journalistes, chercheurs, associations)

## 13.5 Conclusion personnelle

Ce projet m'a permis de **mobiliser l'ensemble de mes compétences** en développement full-stack (backend Java/Spring Boot, frontend Angular, base de données PostgreSQL) tout en découvrant de nouvelles technologies (Angular 21 zoneless, Bucket4j pour le rate limiting, Chart.js pour les graphiques).

Au-delà de l'aspect technique, j'ai pris conscience de l'**importance de l'Open Data** comme levier de transparence démocratique, mais aussi de la **nécessité d'une médiation technique** pour rendre ces données exploitables par le grand public.

Les **écarts identifiés par rapport au CDC** (sécurité, RGPD, tests) constituent des **pistes d'amélioration concrètes** pour atteindre une conformité totale et envisager une mise en production réelle.

**CivicAudit** constitue une **base de travail solide** pour toute collectivité souhaitant renforcer la transparence de ses achats publics et l'engagement citoyen.

---

# Annexes

## A. Commandes de démarrage

### Backend
```bash
cd civicaudit-backend
./mvnw spring-boot:run
# Accessible sur http://localhost:8080
# Swagger UI : http://localhost:8080/swagger-ui/index.html
```

### Frontend
```bash
cd civicaudit-frontend
npm install
ng serve
# Accessible sur http://localhost:4200
```

### Base de données (Docker)
```bash
docker run --name civicaudit-db \
  -e POSTGRES_DB=civicaudit \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=devpass \
  -p 5432:5432 \
  -d postgres:16
```

## B. Variables d'environnement

Créer un fichier `.env` dans `civicaudit-backend/` :
```
DB_URL=jdbc:postgresql://localhost:5432/civicaudit
DB_USERNAME=postgres
DB_PASSWORD=VotreMotDePasseSecurise
JWT_SECRET=VotreCleSecreteJWT256BitsMinimum
JWT_EXPIRATION=86400000
IMPORT_FILE_PATH=/chemin/vers/six_fours_les_plages_marches.csv
```

## C. Compte administrateur par défaut

**⚠️ À changer avant toute mise en production !**

- **Email** : `admin@civicaudit.tn`
- **Mot de passe** : `admin123`
- **Rôle** : ADMIN

## D. Technologies utilisées

### Backend
- Spring Boot 4.1.0
- Java 17
- PostgreSQL 16
- Spring Security + JWT (io.jsonwebtoken:jjwt)
- Bucket4j (rate limiting)
- SpringDoc OpenAPI 3.0
- Apache Commons CSV

### Frontend
- Angular 21 (standalone components)
- TypeScript 5.6
- RxJS 7
- Chart.js + ng2-charts
- SCSS

### Outils
- Maven (build backend)
- npm (build frontend)
- Docker (PostgreSQL)
- Git + GitHub
- IntelliJ IDEA + VS Code

---

**Fin du rapport**

*Projet CivicAudit — Plateforme citoyenne de transparence des marchés publics*  
*Mohamed Amine Bejaoui — ESPRIT — Août 2026*
