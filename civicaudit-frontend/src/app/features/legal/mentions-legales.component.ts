import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-mentions-legales',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="legal-page">
      <div class="legal-container">
        <h1>Mentions légales et charte d'usage</h1>

        <section class="encadre">
          <h2>L'essentiel</h2>
          <ol>
            <li>
              <strong>Les données de marchés sont publiques et officielles.</strong> Elles proviennent des Données
              Essentielles de la Commande Publique (DECP) que les acheteurs publics ont l'obligation de publier,
              diffusées sur data.gouv.fr sous Licence Ouverte 2.0. CivicAudit les présente sans les modifier
              (seul un nettoyage de format est appliqué) et indique pour chaque marché sa source.
            </li>
            <li>
              <strong>Les commentaires et signalements sont des contributions citoyennes modérées</strong>, et non
              des constats de fait établis par la plateforme. Les « points à examiner » calculés automatiquement
              sont des indicateurs statistiques, jamais des accusations.
            </li>
            <li>
              <strong>Tout contenu diffamatoire sera retiré.</strong> Sont notamment refusés : les accusations non
              sourcées présentées comme des faits (« cette entreprise est corrompue ») et toute mise en cause
              nominative d'un agent public ou d'un dirigeant d'entreprise à titre personnel.
            </li>
          </ol>
        </section>

        <section>
          <h2>1. Éditeur du site</h2>
          <p><strong>CivicAudit</strong> — plateforme indépendante de co-audit citoyen des marchés publics,
            réalisée dans le cadre d'un stage de fin d'études d'ingénieur.</p>
          <p>Éditeur : [Nom de l'éditeur ou de l'organisme d'accueil du stage]</p>
          <p>Contact : [adresse email de contact]</p>
          <p>
            CivicAudit n'est ni éditée ni validée par la commune de Six-Fours-les-Plages, dont elle se contente
            de réutiliser les données publiques.
          </p>
        </section>

        <section>
          <h2>2. Directeur de la publication</h2>
          <p>[Nom du directeur de la publication]</p>
        </section>

        <section>
          <h2>3. Hébergement</h2>
          <p><strong>Hébergeur</strong> : [Nom de l'hébergeur]</p>
          <p>Adresse : [Adresse de l'hébergeur]</p>
        </section>

        <section>
          <h2>4. Données et licence</h2>
          <p>
            Les données des marchés publics sont issues du jeu de données
            <a href="https://www.data.gouv.fr/datasets/donnees-essentielles-de-la-commande-publique-consolidees-format-tabulaire"
               target="_blank" rel="noopener noreferrer">« Données essentielles de la commande publique consolidées »</a>
            et réutilisées conformément à la Licence Ouverte / Open Licence Etalab 2.0, avec mention de la source.
          </p>
        </section>

        <section>
          <h2>5. Modération</h2>
          <p>
            Chaque commentaire est relu par un modérateur avant publication. Un commentaire rejeté n'est jamais
            affiché mais reste conservé pour traçabilité en cas de contestation. Pour demander le retrait d'un
            contenu, écrivez à l'adresse de contact ci-dessus. Voir aussi la <a routerLink="/cgus">charte d'usage</a>.
          </p>
        </section>

        <section>
          <h2>6. Données personnelles</h2>
          <p>
            Conformément au RGPD, vous disposez d'un droit d'accès, de rectification et d'effacement de vos données.
            Vous pouvez supprimer votre compte à tout moment depuis la page « Mon compte » ; vos contributions sont
            alors anonymisées. Détails dans la <a routerLink="/politique-confidentialite">politique de confidentialité</a>.
          </p>
        </section>

        <section>
          <h2>7. Stockage local et cookies</h2>
          <p>
            Ce site n'utilise aucun cookie de suivi. Une fois connecté, votre jeton de session est conservé dans le
            stockage local de votre navigateur pendant 24 heures au maximum ; il est effacé à la déconnexion.
          </p>
        </section>

        <section>
          <h2>8. Responsabilité</h2>
          <p>
            Les informations sont fournies à titre informatif, telles que publiées par les acheteurs publics.
            En cas d'écart, la source officielle (DECP) fait foi.
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
      max-width: 900px;
      margin: 0 auto;
      background: white;
      border-radius: 16px;
      padding: 3rem;
      box-shadow: var(--shadow-md);
    }

    h1 {
      font-size: 2.5rem;
      color: var(--primary);
      margin: 0 0 2rem 0;
      font-weight: 700;
      letter-spacing: -0.02em;
    }

    section {
      margin-bottom: 2.5rem;
    }

    .encadre {
      background: #f0f7ff;
      border-left: 4px solid var(--primary);
      border-radius: 8px;
      padding: 1.25rem 1.5rem;
    }

    .encadre ol {
      margin: 0;
      padding-left: 1.25rem;
      line-height: 1.6;
    }

    .encadre li {
      margin-bottom: 0.75rem;
    }

    h2 {
      font-size: 1.5rem;
      color: var(--text);
      margin: 0 0 1rem 0;
      font-weight: 600;
    }

    p {
      line-height: 1.7;
      color: var(--text-secondary);
      margin: 0 0 1rem 0;
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
export class MentionsLegalesComponent {}
