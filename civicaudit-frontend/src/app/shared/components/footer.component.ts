import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h3>CivicAudit</h3>
          <p>Plateforme citoyenne de transparence<br>des marchés publics</p>
        </div>

        <div class="footer-section">
          <h4>Navigation</h4>
          <ul>
            <li><a routerLink="/marches">Marchés publics</a></li>
            <li><a routerLink="/statistiques">Statistiques</a></li>
            <li><a routerLink="/moderation">Modération</a></li>
          </ul>
        </div>

        <div class="footer-section">
          <h4>Légal</h4>
          <ul>
            <li><a routerLink="/mentions-legales">Mentions légales</a></li>
            <li><a routerLink="/politique-confidentialite">Politique de confidentialité</a></li>
            <li><a routerLink="/cgus">Conditions d'utilisation</a></li>
          </ul>
        </div>

        <div class="footer-section">
          <h4>Contact</h4>
          <ul>
            <li><a href="mailto:contact@civicaudit.fr">contact@civicaudit.fr</a></li>
            <li><a href="mailto:dpo@civicaudit.fr">DPO : dpo@civicaudit.fr</a></li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <p>© 2026 Commune de Six-Fours-les-Plages - Tous droits réservés</p>
        <p class="data-source">
          Données issues des <a href="https://data.gouv.fr" target="_blank" rel="noopener">DECP (data.gouv.fr)</a>
          sous <a href="https://www.etalab.gouv.fr/licence-ouverte-open-licence/" target="_blank" rel="noopener">Licence Ouverte 2.0</a>
        </p>
      </div>
    </footer>
  `,
  styles: [`
    .footer {
      background: var(--color-bg-primary, #1e293b);
      color: white;
      padding: 3rem 1.5rem 1.5rem;
      margin-top: 5rem;
    }

    .footer-content {
      max-width: 1200px;
      margin: 0 auto;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 2.5rem;
      margin-bottom: 2.5rem;
    }

    .footer-section {
      h3 {
        font-size: 1.5rem;
        font-weight: 700;
        margin: 0 0 0.75rem 0;
        color: white;
      }

      h4 {
        font-size: 1rem;
        font-weight: 600;
        margin: 0 0 1rem 0;
        color: rgba(255, 255, 255, 0.9);
      }

      p {
        color: rgba(255, 255, 255, 0.7);
        line-height: 1.6;
        margin: 0;
        font-size: 0.9375rem;
      }

      ul {
        list-style: none;
        padding: 0;
        margin: 0;

        li {
          margin-bottom: 0.75rem;

          a {
            color: rgba(255, 255, 255, 0.7);
            text-decoration: none;
            font-size: 0.9375rem;
            transition: color 0.2s;

            &:hover {
              color: white;
              text-decoration: underline;
            }
          }
        }
      }
    }

    .footer-bottom {
      max-width: 1200px;
      margin: 0 auto;
      padding-top: 2rem;
      border-top: 1px solid rgba(255, 255, 255, 0.15);
      text-align: center;

      p {
        color: rgba(255, 255, 255, 0.6);
        font-size: 0.875rem;
        margin: 0.5rem 0;

        a {
          color: rgba(255, 255, 255, 0.7);
          text-decoration: none;

          &:hover {
            color: white;
            text-decoration: underline;
          }
        }
      }

      .data-source {
        font-size: 0.8125rem;
      }
    }

    @media (max-width: 768px) {
      .footer {
        padding: 2rem 1.5rem 1rem;
      }

      .footer-content {
        grid-template-columns: 1fr;
        gap: 2rem;
      }
    }
  `]
})
export class FooterComponent {}
