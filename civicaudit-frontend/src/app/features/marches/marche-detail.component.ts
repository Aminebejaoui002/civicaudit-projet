import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MarcheService } from '../../core/services/marche.service';
import { MarcheDetail, Avenant } from '../../core/models/marche.model';

@Component({
  selector: 'app-marche-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './marche-detail.component.html',
  styleUrl: './marche-detail.component.scss'
})
export class MarcheDetailComponent implements OnInit {
  marche: MarcheDetail | null = null;
  avenants: Avenant[] = [];
  chargement = true;
  erreur = false;
  afficherAvenants = false;

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

  getProcedureExplication(procedure: string): string {
    const explications: { [key: string]: string } = {
      'Procédure adaptée': 'Procédure simplifiée pour les marchés de faible montant, permettant à l\'acheteur d\'adapter les modalités de mise en concurrence.',
      'Appel d\'offres ouvert': 'Procédure formelle où tout opérateur économique peut remettre une offre. La plus transparente et concurrentielle.',
      'Appel d\'offres restreint': 'Seuls les candidats présélectionnés peuvent remettre une offre.',
      'Marché négocié sans publicité': 'Marché attribué sans mise en concurrence préalable, autorisé dans des cas exceptionnels prévus par la loi.',
      'Dialogue compétitif': 'Utilisé pour les projets complexes, permet un dialogue avec les candidats avant de leur demander une offre finale.'
    };
    return explications[procedure] || 'Type de procédure utilisée pour attribuer ce marché public.';
  }

  getCpvLibelle(code: string): string {
    // Extraction des 2 premiers chiffres pour la catégorie
    const categorie = code.substring(0, 2);
    const categories: { [key: string]: string } = {
      '03': 'Produits de l\'agriculture, de la pêche, de la sylviculture',
      '09': 'Produits pétroliers, combustibles, électricité',
      '15': 'Produits alimentaires, boissons, tabac',
      '18': 'Vêtements, chaussures, bagages',
      '22': 'Imprimés et produits connexes',
      '24': 'Produits chimiques',
      '30': 'Machines de bureau et matériel informatique',
      '31': 'Appareils électriques',
      '32': 'Appareils de radio, télévision, communication',
      '33': 'Instruments médicaux, de précision, optiques',
      '34': 'Matériel de transport',
      '35': 'Équipements de sécurité, lutte incendie',
      '37': 'Instruments de musique, articles de sport',
      '38': 'Équipements de laboratoire',
      '39': 'Mobilier, équipements d\'ameublement',
      '41': 'Eaux captées et distribuées',
      '42': 'Machines industrielles',
      '43': 'Matériel d\'extraction minière',
      '44': 'Structures et matériaux de construction',
      '45': 'Travaux de construction',
      '48': 'Progiciels et systèmes d\'information',
      '50': 'Services de réparation et d\'entretien',
      '51': 'Services d\'installation',
      '55': 'Services d\'hôtellerie et de restauration',
      '60': 'Services de transport',
      '63': 'Services de soutien et auxiliaires des transports',
      '64': 'Services postaux et de télécommunication',
      '65': 'Services publics',
      '66': 'Services financiers et d\'assurance',
      '70': 'Services immobiliers',
      '71': 'Services d\'architecture, construction, ingénierie',
      '72': 'Services informatiques',
      '73': 'Services de recherche et développement',
      '75': 'Services d\'administration publique',
      '76': 'Services relatifs à l\'industrie pétrolière',
      '77': 'Services agricoles, forestiers, paysagers',
      '79': 'Services aux entreprises',
      '80': 'Services d\'éducation et de formation',
      '85': 'Services sanitaires et sociaux',
      '90': 'Services d\'assainissement et d\'enlèvement des ordures',
      '92': 'Services récréatifs, culturels et sportifs',
      '98': 'Autres services'
    };
    return categories[categorie] || `Catégorie CPV ${code}`;
  }
}
