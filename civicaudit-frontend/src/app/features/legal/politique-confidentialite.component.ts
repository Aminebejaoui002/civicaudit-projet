import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-politique-confidentialite',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="legal-page">
      <div class="legal-container">
        <h1>Politique de confidentialité</h1>
        <p class="last-update">Dernière mise à jour : 26 août 2026</p>
        
        <section>
          <h2>1. Responsable du traitement</h2>
          <p>
            Le responsable du traitement des données est la <strong>Commune de Six-Fours-les-Plages</strong>,
            représentée par son Maire.
          </p>
          <p><strong>Délégué à la Protection des Données (DPO)</strong> : dpo@civicaudit.fr</p>
        </section>

        <section>
          <h2>2. Données collectées</h2>
          <h3>2.1 Lors de l'inscription</h3>
          <ul>
            <li>Adresse email</li>
            <li>Mot de passe (chiffré)</li>
            <li>Date de création du compte</li>
          </ul>

          <h3>2.2 Lors de l'utilisation</h3>
          <ul>
            <li>Commentaires et signalements publiés</li>
            <li>Votes sur les signalements</li>
            <li>Logs de connexion (adresse IP, date/heure)</li>
          </ul>

          <h3>2.3 Données non collectées</h3>
          <p>
            Nous <strong>ne collectons pas</strong> : nom, prénom, adresse postale, numéro de téléphone,
            données de géolocalisation précise, cookies de tracking publicitaire.
          </p>
        </section>

        <section>
          <h2>3. Finalités du traitement</h2>
          <ul>
            <li><strong>Gestion des comptes utilisateurs</strong> : authentification, sécurité</li>
            <li><strong>Participation citoyenne</strong> : publication de commentaires et signalements</li>
            <li><strong>Modération</strong> : prévention des abus, respect des CGU</li>
            <li><strong>Statistiques anonymisées</strong> : amélioration du service</li>
          </ul>
        </section>

        <section>
          <h2>4. Base légale</h2>
          <p>
            Le traitement de vos données repose sur :
          </p>
          <ul>
            <li><strong>Votre consentement</strong> lors de la création du compte</li>
            <li><strong>L'intérêt légitime</strong> de la commune à assurer la transparence des marchés publics</li>
            <li><strong>L'obligation légale</strong> de publier les données essentielles (DECP)</li>
          </ul>
        </section>

        <section>
          <h2>5. Durée de conservation</h2>
          <ul>
            <li><strong>Comptes utilisateurs</strong> : jusqu'à suppression par l'utilisateur ou 3 ans d'inactivité</li>
            <li><strong>Commentaires/signalements</strong> : durée de vie du marché + 5 ans (archives légales)</li>
            <li><strong>Logs de connexion</strong> : 12 mois (sécurité)</li>
          </ul>
        </section>

        <section>
          <h2>6. Destinataires des données</h2>
          <p>Vos données sont accessibles uniquement par :</p>
          <ul>
            <li>Les <strong>modérateurs</strong> de la plateforme (agents habilités de la commune)</li>
            <li>Les <strong>services techniques</strong> pour la maintenance</li>
            <li>L'<strong>hébergeur</strong> du site (sous contrat de confidentialité)</li>
          </ul>
          <p>
            <strong>Aucune donnée personnelle n'est vendue ou cédée à des tiers.</strong>
          </p>
        </section>

        <section>
          <h2>7. Vos droits (RGPD)</h2>
          <p>Conformément au RGPD, vous disposez des droits suivants :</p>
          
          <div class="rights-grid">
            <div class="right-card">
              <h4>✓ Droit d'accès</h4>
              <p>Obtenir une copie de vos données personnelles</p>
            </div>
            <div class="right-card">
              <h4>✓ Droit de rectification</h4>
              <p>Corriger des données inexactes ou incomplètes</p>
            </div>
            <div class="right-card">
              <h4>✓ Droit à l'effacement</h4>
              <p>Supprimer votre compte et vos données</p>
            </div>
            <div class="right-card">
              <h4>✓ Droit d'opposition</h4>
              <p>Vous opposer à un traitement de vos données</p>
            </div>
            <div class="right-card">
              <h4>✓ Droit à la portabilité</h4>
              <p>Récupérer vos données dans un format lisible</p>
            </div>
            <div class="right-card">
              <h4>✓ Droit de limitation</h4>
              <p>Limiter le traitement de vos données</p>
            </div>
          </div>

          <p class="exercise-rights">
            <strong>Pour exercer ces droits</strong>, contactez-nous à :
            <a href="mailto:dpo@civicaudit.fr">dpo@civicaudit.fr</a>
            <br>
            Réponse sous 1 mois maximum.
          </p>
        </section>

        <section>
          <h2>8. Sécurité des données</h2>
          <p>Nous mettons en œuvre les mesures suivantes :</p>
          <ul>
            <li><strong>Chiffrement</strong> : mots de passe hashés avec bcrypt, connexions HTTPS</li>
            <li><strong>Accès restreint</strong> : authentification obligatoire, rôles utilisateurs</li>
            <li><strong>Sauvegardes régulières</strong> : données sauvegardées quotidiennement</li>
            <li><strong>Surveillance</strong> : logs de sécurité, détection d'intrusion</li>
            <li><strong>Rate limiting</strong> : protection contre les attaques par force brute</li>
          </ul>
        </section>

        <section>
          <h2>9. Cookies</h2>
          <p>
            Ce site utilise uniquement un <strong>cookie de session</strong> pour maintenir votre connexion.
            Ce cookie est supprimé à la fermeture du navigateur.
          </p>
          <p>
            <strong>Aucun cookie publicitaire ou de tracking n'est utilisé.</strong>
          </p>
        </section>

        <section>
          <h2>10. Données publiques (DECP)</h2>
          <p>
            Les <strong>données des marchés publics</strong> affichées sur ce site proviennent des 
            Données Essentielles de la Commande Publique (DECP), publiées en open data.
          </p>
          <p>
            Ces données sont <strong>publiques par nature</strong> et soumises à la 
            <a href="https://www.etalab.gouv.fr/licence-ouverte-open-licence/" target="_blank" rel="noopener">
              Licence Ouverte Etalab 2.0
            </a>.
          </p>
        </section>

        <section>
          <h2>11. Réclamation</h2>
          <p>
            Si vous estimez que vos droits ne sont pas respectés, vous pouvez introduire une réclamation
            auprès de la <strong>CNIL</strong> :
          </p>
          <p>
            Commission Nationale de l'Informatique et des Libertés<br>
            3 Place de Fontenoy - TSA 80715<br>
            75334 PARIS CEDEX 07<br>
            Tél : 01 53 73 22 22<br>
            <a href="https://www.cnil.fr" target="_blank" rel="noopener">www.cnil.fr</a>
          </p>
        </section>

        <section>
          <h2>12. Modifications</h2>
          <p>
            Cette politique de confidentialité peut être mise à jour. Les modifications seront publiées
            sur cette page avec une nouvelle date de mise à jour.
          </p>
        </section>

        <div class="back-link">
          <a routerLink="/marches">← Retour à l'accueil</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .legal-page {
      min-height: 100vh;
      background: var(--bg-light);
      padding: 3rem 1.5rem;
    }

    .legal-container {
      max-width: 1000px;
      margin: 0 auto;
      background: white;
      border-radius: 16px;
      padding: 3rem;
      box-shadow: var(--shadow-md);
    }

    h1 {
      font-size: 2.5rem;
      color: var(--primary);
      margin: 0 0 0.5rem 0;
      font-weight: 700;
      letter-spacing: -0.02em;
    }

    .last-update {
      color: var(--text-muted);
      font-size: 0.875rem;
      margin-bottom: 2rem;
      font-style: italic;
    }

    section {
      margin-bottom: 2.5rem;
    }

    h2 {
      font-size: 1.5rem;
      color: var(--text);
      margin: 0 0 1rem 0;
      font-weight: 600;
    }

    h3 {
      font-size: 1.125rem;
      color: var(--text);
      margin: 1.5rem 0 0.75rem 0;
      font-weight: 600;
    }

    h4 {
      font-size: 1rem;
      margin: 0 0 0.5rem 0;
      font-weight: 600;
      color: var(--primary);
    }

    p {
      line-height: 1.7;
      color: var(--text-secondary);
      margin: 0 0 1rem 0;
    }

    ul {
      margin: 0.5rem 0 1rem 0;
      padding-left: 1.5rem;
      
      li {
        margin-bottom: 0.5rem;
        line-height: 1.6;
        color: var(--text-secondary);
      }
    }

    strong {
      color: var(--text);
      font-weight: 600;
    }

    a {
      color: var(--primary);
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }

    .rights-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1rem;
      margin: 1.5rem 0;
    }

    .right-card {
      background: var(--bg-light);
      border-radius: 12px;
      padding: 1.25rem;
      border: 1px solid var(--border);
      transition: all 0.2s;

      &:hover {
        box-shadow: var(--shadow-sm);
        border-color: var(--primary);
      }

      p {
        margin: 0;
        font-size: 0.875rem;
      }
    }

    .exercise-rights {
      background: #dbeafe;
      border-left: 4px solid var(--primary);
      padding: 1rem 1.5rem;
      border-radius: 8px;
      margin-top: 1.5rem;

      a {
        font-weight: 600;
      }
    }

    .back-link {
      margin-top: 3rem;
      padding-top: 2rem;
      border-top: 1px solid var(--border);
      
      a {
        display: inline-flex;
        align-items: center;
        padding: 0.75rem 1.5rem;
        background: var(--bg-light);
        border-radius: 8px;
        transition: all 0.2s;

        &:hover {
          background: var(--primary);
          color: white;
          text-decoration: none;
          transform: translateX(-4px);
        }
      }
    }

    @media (max-width: 768px) {
      .legal-container {
        padding: 2rem 1.5rem;
      }

      h1 {
        font-size: 2rem;
      }

      h2 {
        font-size: 1.25rem;
      }

      .rights-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class PolitiqueConfidentialiteComponent {}
