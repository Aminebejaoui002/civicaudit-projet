# État réel du projet vs cahier des charges

**Mise à jour** : 12 novembre 2026 (remplace l'audit du 27 août 2026)

Checklist de fin de stage (CDC section 9) :

| Critère CDC | État | Où le vérifier |
|---|---|---|
| Import des DECP réelles sans doublon, avenants gérés | ✅ Upsert sur uid, titulaires regroupés, avenants séparés, rejet contrôlé journalisé dans `import_error`, rapport d'import | Page « Import DECP » (ADMIN), `POST /api/admin/import`, `ImportServiceTest` |
| Recherche et filtres combinés | ✅ Dates, montants, catégorie CPV, procédure, texte libre ; ET logique ; Reactive Forms | Page Marchés |
| Fiche marché complète, données manquantes gérées | ✅ Montant actuel + historique des avenants, titulaires + SIRET, procédure expliquée, libellé CPV, source, compteurs, « Non renseigné » | Fiche marché |
| Vue fournisseur avec concentration | ✅ | Clic sur un titulaire |
| Page statistiques avec 4 graphiques | ✅ Les 4 graphiques affichent leurs données (3 étaient vides à cause de noms de champs différents front/back) | Page Statistiques |
| Commentaires avec modération | ✅ EN_ATTENTE → APPROUVE / REJETE, l'auteur voit son commentaire « en cours de modération », soft delete | Page Modération, `CommentaireServiceTest` |
| Signalements : catégories, vote, tri par votes | ✅ Les 5 catégories du CDC (le backend refusait 4 d'entre elles), 1 signalement par catégorie, vote unique, passage EN_DISCUSSION à 3 votes, changement de statut par le modérateur | `SignalementServiceTest` |
| 3 règles d'alerte testées + garde-fou 5 marchés | ✅ Calcul pur testé unitairement, wording neutre, badge orange | `AlerteServiceTest`, `docs/REGLES_NETTOYAGE_ET_ALERTES.md` |
| JWT avec 3 rôles | ✅ Modération réservée MODERATEUR/ADMIN (était publique), import réservé ADMIN, guards Angular par rôle | `SecurityConfig`, `@PreAuthorize` |
| Export CSV respectant les filtres | ✅ Mêmes filtres et tri que la liste, + code CPV brut + lien source | Bouton « Exporter CSV » |
| Mentions légales / RGPD | ✅ Les 3 points obligatoires du CDC, consentement enregistré à l'inscription, suppression de compte avec anonymisation | Mentions légales, « Mon compte » |
| Tests unitaires decp-import et alert-engine | ✅ + signalements, commentaires, anti-spam, RGPD, export | `civicaudit-backend/src/test` |
| Swagger à jour | ✅ Accessible sans authentification, bouton « Authorize » pour le JWT | `/swagger-ui.html` |

Exigences non fonctionnelles (CDC section 4) :

| Exigence | État |
|---|---|
| Traçabilité vers la source | ✅ `source_filename` + `source_url` sur chaque marché, affichés et exportés |
| Index SQL sur les colonnes filtrées | ✅ date de notification, montant, code CPV, titulaire, nom du titulaire |
| Anti-spam | ✅ 10 actions/utilisateur/heure (compteur en base) + 100 requêtes/minute/IP |
| Validation des entrées | ✅ Spring Validation, messages d'erreur renvoyés au frontend |
| Secrets hors du dépôt | ✅ Variables d'environnement ou `application-local.properties` (ignoré par Git). Attention : l'ancien mot de passe de base de données reste dans l'historique Git du premier commit. |
| Accessibilité de base | ✅ Libellés sur les champs, navigation clavier, contrastes corrigés |

Limites connues et perspectives : voir la section « Limites connues » du `README.md` et les limites
documentées dans `docs/REGLES_NETTOYAGE_ET_ALERTES.md`.
