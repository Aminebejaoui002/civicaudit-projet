import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-cgus',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="legal-page">
      <div class="legal-container">
        <h1>Conditions Générales d'Utilisation (CGU)</h1>
        <p class="last-update">Dernière mise à jour : 26 août 2026</p>
        
        <section>
          <h2>1. Objet</h2>
          <p>
            Les présentes Conditions Générales d'Utilisation (CGU) ont pour objet de définir les modalités
            et conditions d'utilisation de la plateforme CivicAudit, ainsi que les droits et obligations
            des utilisateurs.
          </p>
        </section>

        <section>
          <h2>2. Acceptation des CGU</h2>
          <p>
            L'utilisation de la plateforme implique l'acceptation pleine et entière des présentes CGU.
            En créant un compte, vous déclarez avoir lu, compris et accepté les présentes conditions.
          </p>
        </section>

        <section>
          <h2>3. Inscription et compte utilisateur</h2>
          <h3>3.1 Conditions d'inscription</h3>
          <ul>
            <li>Vous devez avoir au moins 16 ans pour créer un compte</li>
            <li>Vous devez fournir une adresse email valide</li>
            <li>Vous êtes responsable de la confidentialité de votre mot de passe</li>
          </ul>

          <h3>3.2 Responsabilités</h3>
          <ul>
            <li>Vous êtes responsable de toutes les activités effectuées depuis votre compte</li>
            <li>Vous devez signaler immédiatement toute utilisation non autorisée de votre compte</li>
            <li>Vous ne pouvez pas créer plusieurs comptes pour une même personne</li>
          </ul>
        </section>

        <section>
          <h2>4. Utilisation de la plateforme</h2>
          <h3>4.1 Usage autorisé</h3>
          <p>CivicAudit vous permet de :</p>
          <ul>
            <li>Consulter les données des marchés publics</li>
            <li>Publier des commentaires constructifs</li>
            <li>Signaler des anomalies potentielles</li>
            <li>Voter sur les signalements</li>
          </ul>

          <h3>4.2 Usages interdits</h3>
          <p>Il est strictement interdit de :</p>
          <ul>
            <li><strong>Publier du contenu illégal</strong> : diffamation, injures, incitation à la haine</li>
            <li><strong>Harceler</strong> d'autres utilisateurs ou entités publiques</li>
            <li><strong>Spammer</strong> : publications répétitives ou non pertinentes</li>
            <li><strong>Manipuler</strong> les votes avec de faux comptes</li>
            <li><strong>Usurper</strong> l'identité d'une autre personne ou entité</li>
            <li><strong>Tenter de</strong> contourner les mesures de sécurité</li>
            <li><strong>Utiliser des bots</strong> ou scripts automatisés</li>
          </ul>
        </section>

        <section>
          <h2>5. Contenu utilisateur</h2>
          <h3>5.1 Propriété</h3>
          <p>
            Vous conservez la propriété intellectuelle du contenu que vous publiez (commentaires, signalements).
          </p>

          <h3>5.2 Licence</h3>
          <p>
            En publiant du contenu, vous accordez à CivicAudit une licence non exclusive, gratuite et mondiale
            pour afficher, reproduire et distribuer ce contenu dans le cadre de la plateforme.
          </p>

          <h3>5.3 Modération</h3>
          <p>
            Tout contenu publié est soumis à modération. CivicAudit se réserve le droit de :
          </p>
          <ul>
            <li>Refuser ou supprimer un contenu ne respectant pas les CGU</li>
            <li>Suspendre ou supprimer un compte en cas d'abus</li>
            <li>Modifier le statut des signalements (validation, rejet)</li>
          </ul>
        </section>

        <section>
          <h2>6. Données publiques (DECP)</h2>
          <p>
            Les données des marchés publics affichées proviennent des Données Essentielles de la Commande Publique
            (DECP), publiées en open data conformément à la réglementation.
          </p>
          <p>
            Ces données sont <strong>publiques</strong> et peuvent contenir des erreurs. CivicAudit s'efforce
            de présenter des informations exactes mais ne garantit pas leur exhaustivité.
          </p>
        </section>

        <section>
          <h2>7. Responsabilité</h2>
          <h3>7.1 Responsabilité de CivicAudit</h3>
          <p>CivicAudit s'engage à :</p>
          <ul>
            <li>Fournir un service de qualité et sécurisé</li>
            <li>Protéger vos données personnelles (voir Politique de confidentialité)</li>
            <li>Modérer le contenu pour maintenir un climat constructif</li>
          </ul>
          <p>Toutefois, CivicAudit <strong>ne peut garantir</strong> :</p>
          <ul>
            <li>La disponibilité continue du service (maintenance, pannes)</li>
            <li>L'exactitude absolue des données DECP</li>
            <li>L'absence totale de contenu inapproprié (malgré la modération)</li>
          </ul>

          <h3>7.2 Limitation de responsabilité</h3>
          <p>
            CivicAudit ne saurait être tenu responsable des dommages directs ou indirects résultant de :
          </p>
          <ul>
            <li>L'utilisation ou l'impossibilité d'utiliser la plateforme</li>
            <li>Erreurs dans les données affichées</li>
            <li>Contenu publié par d'autres utilisateurs</li>
          </ul>
        </section>

        <section>
          <h2>8. Sanctions</h2>
          <p>En cas de non-respect des CGU, CivicAudit se réserve le droit de :</p>
          <ul>
            <li><strong>Avertissement</strong> : pour une première infraction mineure</li>
            <li><strong>Suppression de contenu</strong> : commentaires ou signalements inappropriés</li>
            <li><strong>Suspension temporaire</strong> : 7 à 30 jours selon la gravité</li>
            <li><strong>Suppression définitive du compte</strong> : en cas d'abus répétés ou graves</li>
          </ul>
          <p>
            Les sanctions sont appliquées à la discrétion des modérateurs, sans obligation de préavis.
          </p>
        </section>

        <section>
          <h2>9. Droit applicable et litiges</h2>
          <p>
            Les présentes CGU sont soumises au <strong>droit français</strong>.
          </p>
          <p>
            En cas de litige, une résolution amiable sera privilégiée. À défaut, le litige sera porté
            devant les tribunaux compétents de Toulon.
          </p>
        </section>

        <section>
          <h2>10. Modifications des CGU</h2>
          <p>
            CivicAudit se réserve le droit de modifier les présentes CGU à tout moment.
            Les utilisateurs seront informés par email ou notification sur la plateforme.
          </p>
          <p>
            La poursuite de l'utilisation du service après modification vaut acceptation des nouvelles CGU.
          </p>
        </section>

        <section>
          <h2>11. Contact</h2>
          <p>
            Pour toute question concernant les CGU, contactez-nous à : <strong>contact@civicaudit.fr</strong>
          </p>
        </section>

        <div class="acceptance">
          <p>
            ✓ En créant un compte CivicAudit, vous acceptez les présentes Conditions Générales d'Utilisation
            ainsi que notre <a routerLink="/politique-confidentialite">Politique de confidentialité</a>.
          </p>
        </div>

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

    .acceptance {
      background: #d1fae5;
      border-left: 4px solid #10b981;
      padding: 1.25rem 1.5rem;
      border-radius: 8px;
      margin: 2rem 0;

      p {
        margin: 0;
        color: #065f46;
        font-weight: 500;
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
    }
  `]
})
export class CgusComponent {}
