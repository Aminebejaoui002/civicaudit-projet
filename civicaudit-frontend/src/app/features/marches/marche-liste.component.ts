import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MarcheService, MarcheFiltres } from '../../core/services/marche.service';
import { MarcheListe, PageResult } from '../../core/models/marche.model';

@Component({
  selector: 'app-marche-liste',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './marche-liste.component.html',
  styleUrl: './marche-liste.component.scss'
})
export class MarcheListeComponent implements OnInit {
  resultat: PageResult<MarcheListe> | null = null;
  chargement = false;
  erreur = false;
  exportEnCours = false;
  showFilters = false;

  filtres: MarcheFiltres = {
    page: 0,
    taille: 20,
    tri: 'date'
  };

  constructor(
    private marcheService: MarcheService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.rechercher();
  }

  rechercher(): void {
    this.chargement = true;
    this.erreur = false;
    this.filtres.page = 0; // on repart en page 1 à chaque nouvelle recherche
    this.marcheService.lister(this.filtres).subscribe({
      next: (res) => {
        console.log('DONNEES RECUES:', res);
        this.resultat = res;
        this.chargement = false;
        this.cdr.detectChanges(); // Force la détection de changement
      },
      error: () => { this.erreur = true; this.chargement = false; this.cdr.detectChanges(); }
    });
  }

  changerPage(nouvellePage: number): void {
    if (nouvellePage < 0 || (this.resultat && nouvellePage >= this.resultat.totalPages)) return;
    this.filtres.page = nouvellePage;
    this.chargement = true;
    this.marcheService.lister(this.filtres).subscribe({
      next: (res) => {
        console.log('DONNEES RECUES:', res);
        this.resultat = res;
        this.chargement = false;
        this.cdr.detectChanges(); // Force la détection de changement
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: () => { this.erreur = true; this.chargement = false; this.cdr.detectChanges(); }
    });
  }

  formatMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 }).format(montant);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  exporterCSV(): void {
    this.exportEnCours = true;
    this.marcheService.exporterCSV().subscribe({
      next: (data) => {
        // Créer un blob et télécharger le fichier
        const blob = new Blob([data], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', `marches_publics_${new Date().toISOString().split('T')[0]}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.exportEnCours = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur export CSV:', err);
        alert('Erreur lors de l\'export CSV');
        this.exportEnCours = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  resetFilters(): void {
    this.filtres = {
      page: 0,
      taille: 20,
      tri: 'date'
    };
    this.rechercher();
  }
}