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
        <h1>Mentions légales</h1>
        
        <section>
          <h2>1. Éditeur du site</h2>
          <p><strong>CivicAudit</strong></p>
          <p>Plateforme de transparence des marchés publics</p>
          <p>Commune de Six-Fours-les-Plages</p>
          <p>Hôtel de Ville - 83140 Six-Fours-les-Plages</p>
          <p>Téléphone : 04 XX XX XX XX</p>
          <p>Email : contact@civicaudit.fr</p>
        </section>

        <section>
          <h2>2. Directeur de la publication</h2>
          <p>Maire de Six-Fours-les-Plages</p>
        </section>

        <section>
          <h2>3. Hébergement</h2>
          <p><strong>Hébergeur</strong> : [Nom de l'hébergeur]</p>
          <p>Adresse : [Adresse de l'hébergeur]</p>
          <p>Téléphone : [Téléphone]</p>
        </section>

        <section>
          <h2>4. Propriété intellectuelle</h2>
          <p>
            L'ensemble du contenu de ce site (textes, images, vidéos) est protégé par le droit d'auteur.
            Toute reproduction, même partielle, est interdite sans autorisation préalable.
          </p>
          <p>
            Les données des marchés publics sont issues des Données Essentielles de la Commande Publique (DECP)
            et sont soumises à la Licence Ouverte / Open License Etalab 2.0.
          </p>
        </section>

        <section>
          <h2>5. Données personnelles</h2>
          <p>
            Conformément au Règlement Général sur la Protection des Données (RGPD), vous disposez d'un droit
            d'accès, de rectification et de suppression de vos données personnelles.
          </p>
          <p>
            Pour exercer ces droits, veuillez consulter notre 
            <a routerLink="/politique-confidentialite">politique de confidentialité</a> ou nous contacter à :
            <strong>dpo@civicaudit.fr</strong>
          </p>
        </section>

        <section>
          <h2>6. Cookies</h2>
          <p>
            Ce site utilise uniquement des cookies techniques nécessaires à son fonctionnement
            (authentification, préférences utilisateur). Aucun cookie de tracking n'est utilisé.
          </p>
        </section>

        <section>
          <h2>7. Responsabilité</h2>
          <p>
            Les informations fournies sur ce site le sont à titre informatif. La commune de Six-Fours-les-Plages
            s'efforce d'assurer l'exactitude des données mais ne saurait être tenue responsable des erreurs
            ou omissions.
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
