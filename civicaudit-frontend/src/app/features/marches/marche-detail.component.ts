import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarcheService } from '../../core/services/marche.service';
import { MarcheDetail, Avenant, Alerte } from '../../core/models/marche.model';
import { ALERTE_AVERTISSEMENT, getAlerteMessage, getAlerteTypeLabel } from '../../core/models/alerte.model';
import { MarcheInteractionsComponent } from './marche-interactions.component';

@Component({
  selector: 'app-marche-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MarcheInteractionsComponent],
  templateUrl: './marche-detail.component.html',
  styleUrl: './marche-detail.component.scss'
})
export class MarcheDetailComponent implements OnInit {
  marche: MarcheDetail | null = null;
  avenants: Avenant[] = [];
  chargement = true;
  erreur = false;
  afficherAvenants = false;
  readonly avertissementAlertes = ALERTE_AVERTISSEMENT;

  constructor(
    private route: ActivatedRoute,
    private marcheService: MarcheService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.chargerMarche(id);
    } else {
      this.erreur = true;
      this.chargement = false;
    }
  }

  chargerMarche(id: number): void {
    this.chargement = true;
    this.marcheService.getDetail(id).subscribe({
      next: (marche) => {
        this.marche = marche;
        this.chargement = false;
        this.cdr.detectChanges();
        
        // Charger les avenants si nécessaire
        if (marche.aDesAvenants) {
          this.chargerAvenants(id);
        }
      },
      error: () => {
        this.erreur = true;
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerAvenants(id: number): void {
    this.marcheService.getAvenants(id).subscribe({
      next: (avenants) => {
        this.avenants = avenants;
        this.cdr.detectChanges();
      },
      error: () => {
        console.error('Erreur lors du chargement des avenants');
      }
    });
  }

  toggleAvenants(): void {
    this.afficherAvenants = !this.afficherAvenants;
  }

  formatMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', { 
      style: 'currency', 
      currency: 'EUR', 
      maximumFractionDigits: 0 
    }).format(montant);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', { 
      day: '2-digit', 
      month: 'long', 
      year: 'numeric' 
    });
  }

  /**
   * Explication en une phrase de chaque type de procédure pour un non-initié (CDC 3.2.2).
   * Correspondance par mots-clés car les libellés DECP varient selon les plateformes de publication.
   */
  getProcedureExplication(procedure: string): string {
    const p = (procedure || '').toLowerCase();
    if (!p || p === 'non renseigné') {
      return 'Le type de procédure n\'a pas été publié pour ce marché.';
    }
    if (p.includes('sans publicité') || p.includes('négocié sans') || p.includes('sans mise en concurrence')) {
      return 'Marché attribué sans publicité ni mise en concurrence préalable : c\'est autorisé sous certains seuils '
        + '(40 000 € HT) ou dans des cas prévus par la loi (urgence, prestataire unique...).';
    }
    if (p.includes('adaptée') || p.includes('adaptee') || p.includes('mapa')) {
      return 'Procédure simplifiée pour les marchés sous les seuils européens : l\'acheteur fixe lui-même '
        + 'les modalités de publicité et de mise en concurrence, adaptées au montant.';
    }
    if (p.includes('appel d\'offres ouvert')) {
      return 'Procédure formelle où toute entreprise peut remettre une offre ; c\'est la plus ouverte à la concurrence.';
    }
    if (p.includes('appel d\'offres restreint')) {
      return 'Procédure formelle où seules les entreprises sélectionnées après une première étape peuvent remettre une offre.';
    }
    if (p.includes('négociation') || p.includes('négocié') || p.includes('negocie')) {
      return 'Procédure où l\'acheteur négocie les conditions du marché avec une ou plusieurs entreprises, '
        + 'après une publicité, dans les cas prévus par le code de la commande publique.';
    }
    if (p.includes('dialogue compétitif')) {
      return 'Procédure pour les projets complexes : l\'acheteur dialogue avec les candidats pour définir la solution '
        + 'avant de leur demander une offre finale.';
    }
    if (p.includes('concours')) {
      return 'Procédure qui sélectionne un projet (souvent d\'architecture) sur avis d\'un jury, avant d\'attribuer le marché.';
    }
    return 'Mode de mise en concurrence utilisé par l\'acheteur pour attribuer ce marché public.';
  }

  getAlerteMessage(alerte: Alerte): string {
    return getAlerteMessage(alerte);
  }

  getAlerteTypeLabel(type: string): string {
    return getAlerteTypeLabel(type);
  }


}
