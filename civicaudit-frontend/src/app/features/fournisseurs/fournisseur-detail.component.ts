import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FournisseurService } from '../../core/services/fournisseur.service';
import { FournisseurDetail } from '../../core/models/fournisseur.model';
import { MarcheListe, PageResult } from '../../core/models/marche.model';

@Component({
  selector: 'app-fournisseur-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './fournisseur-detail.component.html',
  styleUrl: './fournisseur-detail.component.scss'
})
export class FournisseurDetailComponent implements OnInit {
  fournisseur: FournisseurDetail | null = null;
  marches: PageResult<MarcheListe> | null = null;
  chargement = true;
  erreur = false;
  siret = '';

  constructor(
    private route: ActivatedRoute,
    private fournisseurService: FournisseurService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.siret = this.route.snapshot.paramMap.get('siret') || '';
    if (this.siret) {
      this.chargerFournisseur();
    } else {
      this.erreur = true;
      this.chargement = false;
    }
  }

  chargerFournisseur(): void {
    this.chargement = true;
    this.fournisseurService.getDetail(this.siret).subscribe({
      next: (fournisseur) => {
        this.fournisseur = fournisseur;
        this.chargerMarches(0);
      },
      error: () => {
        this.erreur = true;
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerMarches(page: number): void {
    this.fournisseurService.getMarches(this.siret, page).subscribe({
      next: (marches) => {
        this.marches = marches;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreur = true;
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  changerPage(nouvellePage: number): void {
    if (nouvellePage < 0 || (this.marches && nouvellePage >= this.marches.totalPages)) return;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.chargerMarches(nouvellePage);
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
      month: 'short', 
      year: 'numeric' 
    });
  }

  getConcentrationClass(pourcentage: number): string {
    if (pourcentage >= 40) return 'concentration-haute';
    if (pourcentage >= 20) return 'concentration-moyenne';
    return 'concentration-faible';
  }

  getConcentrationTexte(pourcentage: number): string {
    if (pourcentage >= 40) {
      return 'Concentration élevée — Ce fournisseur représente une part importante des dépenses de la collectivité.';
    }
    if (pourcentage >= 20) {
      return 'Concentration modérée — Ce fournisseur est un partenaire régulier de la collectivité.';
    }
    return 'Concentration faible — Ce fournisseur représente une part modeste des dépenses totales.';
  }
}
