# Règles de nettoyage DECP et heuristiques d'alerte

Document de référence du cahier des charges (livrable section 8) : règles appliquées par le module
`decp-import` (CDC 3.1) et par le module `alert-engine` (CDC 3.5), avec la justification des seuils
et les limites assumées.

Code concerné :
- `ImportService` et `ImportMarcheWriter` (import) — tests : `ImportServiceTest`
- `AlerteService` (alertes) — tests : `AlerteServiceTest`

---

## 1. Pipeline d'import (CDC 3.1)

| Étape | Règle appliquée | Où |
|---|---|---|
| 1. Récupération | Fichier consolidé CSV téléchargé manuellement sur data.gouv.fr puis envoyé via `POST /api/admin/import` (rôle ADMIN, page « Import DECP »). Import au démarrage possible via `civicaudit.import.file`. | `AdminImportController`, `ImportRunner` |
| 2. Filtrage | Si `civicaudit.import.acheteur-id` est renseigné, seules les lignes dont `acheteur_id` vaut ce SIRET sont gardées ; les autres sont comptées « hors périmètre ». Vide = fichier déjà filtré sur la commune. | `ImportService.estDansLePerimetre` |
| 3a. Dédoublonnage titulaires | Toutes les lignes de même `id` (uid DECP) forment **un seul** `MarchePublic`. Chaque `titulaire_id` distinct est lié une seule fois via la table `marche_titulaire`. | `ImportMarcheWriter` |
| 3b. Avenants | Lignes regroupées par `modification_id`. La version la plus petite (ou sans `modification_id`) est le marché d'origine ; chaque version suivante devient un `Avenant` (montant et durée après modification, date = `datePublicationDonnees`). Le montant affiché au citoyen est celui du dernier avenant en date. | `ImportMarcheWriter`, `MarcheDetailService`, `MarcheConsultationService` |
| 4. Normalisation | Montants : `BigDecimal`, 2 décimales, virgule acceptée. Dates : partie `yyyy-MM-dd` uniquement (les heures et fuseaux sont ignorés). Textes : trim + espaces multiples réduits à un seul, **aucune correction sémantique** (casse et orthographe conservées). Valeurs `nan`, `none`, `null` et vides = absentes. BOM UTF-8 ignoré. | `ImportMarcheWriter.valeur`, `parseMontant`, `parseDate` |
| 4. CPV | Code brut conservé ; libellé de la grande catégorie (2 premiers chiffres) lu dans la table `cpv_libelle`, chargée au démarrage depuis `referentiel/cpv-divisions.csv` (divisions de la nomenclature CPV 2008). | `CpvLibelleService` |
| 5. Rejet contrôlé | Marché sans objet, montant, date de notification ou `acheteur_id`, ou avec une valeur illisible → **rejeté en entier** (transaction `REQUIRES_NEW` annulée), journalisé dans `import_error` (ligne brute + raison). Jamais d'insertion à moitié remplie. La durée (facultative) illisible est simplement ignorée. | `ImportService.journaliserRejet` |
| 6. Idempotence | Upsert sur `uid` (contrainte unique) : un marché existant est mis à jour, ses titulaires et avenants sont reconstruits depuis le fichier. Relancer le même fichier ne crée aucun doublon (testé). | `ImportMarcheWriter`, `ImportServiceTest.relancerLImportNeCreeAucunDoublon` |

Après chaque import, un rapport est renvoyé et affiché : lignes lues, hors périmètre, marchés créés,
mis à jour, rejetés. Les alertes sont ensuite recalculées.

**Limites assumées**
- Les avenants sans montant sont ignorés (rien d'exploitable à afficher au citoyen).
- Un marché en groupement compte son montant total pour chacun de ses titulaires dans les cumuls
  par fournisseur (la répartition entre cotraitants n'est pas publiée dans les DECP).
- La date d'un avenant est la date de publication des données modifiées, faute de date de signature fiable.

---

## 2. Heuristiques d'alerte (CDC 3.5)

Ce sont des **signaux à examiner, jamais des accusations**. Wording neutre et pédagogique, badge
orange, jamais rouge (`alerte.model.ts`, test de wording dans `app.spec.ts`). Recalcul complet après
chaque import, jamais à la consultation.

### Règle 1 — Concentration fournisseur (`CONCENTRATION_ELEVEE`)
- Calcul : part d'un titulaire dans le montant total des marchés notifiés sur les **3 dernières années**.
- Déclenchement : part **strictement supérieure à 40 %** → alerte sur chacun de ses marchés de **moins d'un an**.
- Valeur stockée : le pourcentage.
- Justification : 40 % signale une dépendance forte vis-à-vis d'un seul fournisseur ; la fenêtre de
  3 ans lisse les marchés pluriannuels ; l'alerte ne porte que sur les marchés récents pour rester actionnable.
- Limite : un fournisseur spécialisé et unique sur son secteur (eau, énergie...) peut légitimement dépasser ce seuil.

### Règle 2 — Montant atypique (`MONTANT_ATYPIQUE`)
- Calcul : par grande catégorie CPV (2 premiers chiffres), moyenne et écart-type **de population** des montants.
- Déclenchement : montant > moyenne + 2 écarts-types.
- **Garde-fou statistique** : aucune alerte pour une catégorie de moins de 5 marchés.
- Justification : au-delà de 2 écarts-types, un montant sort des valeurs habituelles de la catégorie
  pour la collectivité (environ 2,5 % des valeurs pour une distribution normale).
- Limite mathématique (à connaître pour la soutenance) : avec l'écart-type de population, un marché ne
  peut dépasser moyenne + 2σ que si la catégorie compte **au moins 6 marchés** (le z-score maximal possible
  pour n valeurs est (n−1)/√n, soit 1,79 pour n = 5 et 2,04 pour n = 6). Le seuil de 5 imposé par le CDC
  est donc respecté, et en pratique la règle ne se déclenche qu'à partir de 6 marchés.
- Autre limite : les montants de marchés publics sont très asymétriques (quelques gros travaux), la
  moyenne est tirée vers le haut ; une approche par médiane/écart interquartile serait plus robuste (perspective).

### Règle 3 — Procédure sans mise en concurrence proche du seuil (`SEUIL_SUSPECT`)
- Procédures concernées : libellé contenant « sans publicité » ou « négocié sans » (insensible à la casse et aux accents).
- Déclenchement : montant entre **35 000 € et 40 000 €** inclus.
- Justification : 40 000 € HT est le seuil sous lequel un marché peut être passé sans publicité ni mise en
  concurrence ; une concentration juste sous ce seuil peut indiquer un fractionnement, mais reste souvent légitime.
- Limite : les montants DECP ne précisent pas toujours HT/TTC ; le seuil réglementaire a pu évoluer selon les années.

### Tests unitaires (jeux de données synthétiques)
`AlerteServiceTest` couvre notamment : 60 % → alerte ; exactement 40 % → pas d'alerte ; marchés de plus
de 3 ans exclus ; garde-fou < 5 marchés ; catégories indépendantes ; bornes 35 000 € / 40 000 € incluses ;
34 999,99 € et 40 000,01 € exclus ; procédure avec mise en concurrence ignorée.
