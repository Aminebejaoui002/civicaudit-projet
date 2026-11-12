# CivicAudit

Plateforme de co-audit citoyen des marchés publics locaux (Six-Fours-les-Plages), à partir des
Données Essentielles de la Commande Publique (DECP).

- `civicaudit-backend/` : Spring Boot (Java 17+), Spring Security + JWT, Spring Data JPA, PostgreSQL
- `civicaudit-frontend/` : Angular (standalone components, Reactive Forms, Chart.js)
- `docs/REGLES_NETTOYAGE_ET_ALERTES.md` : règles d'import et heuristiques d'alerte

## Lancer en local

### 1. Base de données
PostgreSQL avec une base `civicaudit`.

### 2. Backend
```bash
cd civicaudit-backend
# Copier le modèle de configuration locale (jamais versionnée) et renseigner les valeurs
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
./mvnw spring-boot:run        # Windows : mvnw.cmd spring-boot:run
```
Aucun secret n'est versionné : `application.properties` lit les variables d'environnement
(`DB_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD`, `IMPORT_FILE_PATH`, `ACHETEUR_ID`...) ou le fichier
`application-local.properties` (ignoré par Git).

- API : http://localhost:8080
- Swagger UI : http://localhost:8080/swagger-ui.html (bouton « Authorize » pour le JWT)

Un compte ADMIN est créé au premier démarrage si `civicaudit.admin.password` est renseigné.
Le rôle MODERATEUR s'attribue directement en base :
```sql
UPDATE users SET role = 'MODERATEUR' WHERE email = 'moderateur@exemple.fr';
```

### 3. Import des données
Se connecter avec le compte ADMIN → menu « Import DECP » → envoyer le CSV consolidé téléchargé sur
[data.gouv.fr](https://www.data.gouv.fr/datasets/donnees-essentielles-de-la-commande-publique-consolidees-format-tabulaire).
Équivalent API : `POST /api/admin/import` (multipart, champ `fichier`).

### 4. Frontend
```bash
cd civicaudit-frontend
npm install
npm start            # http://localhost:4200
```

## Tests
```bash
cd civicaudit-backend && ./mvnw test     # tests unitaires decp-import, alert-engine, signalements, RGPD...
cd civicaudit-frontend && npx ng test --watch=false
```
`CivicauditBackendApplicationTests` (chargement du contexte Spring) nécessite PostgreSQL démarré.

## Limites connues
- Le JWT est stocké dans le `localStorage` du navigateur (compromis accepté par le CDC 3.6, à remplacer
  par un cookie httpOnly en production) : il est exposé en cas de faille XSS. Atténuations : CSP stricte côté
  backend, aucun `innerHTML` côté Angular, expiration à 24 h, déconnexion automatique sur 401.
- Réinitialisation de mot de passe : hors périmètre V1.
- Anti-spam : 10 actions (commentaires + signalements + votes) par utilisateur et par heure, plus une
  limite globale de 100 requêtes/minute par IP.
