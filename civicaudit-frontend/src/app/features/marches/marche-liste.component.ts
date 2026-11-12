import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MarcheService, MarcheFiltres } from '../../core/services/marche.service';
import { CategorieCpv, MarcheListe, PageResult } from '../../core/models/marche.model';

/**
 * Liste des marchés (CDC 3.2.1) : tableau paginé (20 lignes), tri par date décroissante par défaut,
 * filtres combinables (ET logique) en Reactive Forms, export CSV respectant les filtres actifs (CDC 3.7).
 */
@Component({
  selector: 'app-marche-liste',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './marche-liste.component.html',
  styleUrl: './marche-liste.component.scss'
})
export class MarcheListeComponent implements OnInit {
  static readonly LONGUEUR_MAX_OBJET = 80;

  resultat: PageResult<MarcheListe> | null = null;
  chargement = false;
  erreur = false;
  exportEnCours = false;
  erreurExport = '';
  showFilters = false;

  categoriesCpv: CategorieCpv[] = [];
  procedures: string[] = [];

  filtresForm: FormGroup;
  private pageCourante = 0;

  constructor(
    private fb: FormBuilder,
    private marcheService: MarcheService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.filtresForm = this.fb.group({
      texte: [''],
      dateDebut: [''],
      dateFin: [''],
      montantMin: [null as number | null],
      montantMax: [null as number | null],
      categorieCpv: [''],
      procedure: [''],
      tri: ['date']
    });
  }

  ngOnInit(): void {
    this.marcheService.getValeursFiltres().subscribe({
      next: (valeurs) => {
        this.categoriesCpv = valeurs.categoriesCpv;
        this.procedures = valeurs.procedures;
        this.cdr.detectChanges();
      },
      error: () => { /* les listes déroulantes restent vides, la recherche reste utilisable */ }
    });
    this.charger();
  }

  /** Filtres actifs = valeurs du formulaire + page courante. */
  private filtresActifs(): MarcheFiltres {
    const v = this.filtresForm.value;
    return {
      page: this.pageCourante,
      taille: 20,
      tri: v.tri,
      texte: v.texte,
      dateDebut: v.dateDebut,
      dateFin: v.dateFin,
      montantMin: v.montantMin,
      montantMax: v.montantMax,
      categorieCpv: v.categorieCpv,
      procedure: v.procedure
    };
  }

  rechercher(): void {
    this.pageCourante = 0; // on repart en page 1 à chaque nouvelle recherche
    this.charger();
  }

  changerPage(nouvellePage: number): void {
    if (nouvellePage < 0 || (this.resultat && nouvellePage >= this.resultat.totalPages)) return;
    this.pageCourante = nouvellePage;
    this.charger(true);
  }

  trierPar(colonne: 'date' | 'montant'): void {
    const actuel = this.filtresForm.value.tri as string;
    const nouveau = actuel === colonne ? `${colonne}_asc` : colonne;
    this.filtresForm.patchValue({ tri: nouveau });
    this.rechercher();
  }

  indicateurTri(colonne: 'date' | 'montant'): string {
    const tri = this.filtresForm.value.tri as string;
    if (tri === colonne) return '▼';
    if (tri === `${colonne}_asc`) return '▲';
    return '';
  }

  private charger(remonter = false): void {
    this.chargement = true;
    this.erreur = false;
    this.marcheService.lister(this.filtresActifs()).subscribe({
      next: (res) => {
        this.resultat = res;
        this.chargement = false;
        this.cdr.detectChanges();
        if (remonter) window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: () => { this.erreur = true; this.chargement = false; this.cdr.detectChanges(); }
    });
  }

  ouvrir(marche: MarcheListe): void {
    this.router.navigate(['/marches', marche.id]);
  }

  objetTronque(objet: string): string {
    const max = MarcheListeComponent.LONGUEUR_MAX_OBJET;
    return objet && objet.length > max ? objet.substring(0, max - 1) + '…' : objet;
  }

  formatMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 }).format(montant);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  get nombreFiltresActifs(): number {
    const v = this.filtresForm.value;
    return ['dateDebut', 'dateFin', 'montantMin', 'montantMax', 'categorieCpv', 'procedure']
      .filter(cle => v[cle] !== null && v[cle] !== '').length;
  }

  exporterCSV(): void {
    this.exportEnCours = true;
    this.erreurExport = '';
    this.marcheService.exporterCSV(this.filtresActifs()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `marches_publics_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        this.exportEnCours = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreurExport = 'L\'export CSV a échoué. Réessayez dans un instant.';
        this.exportEnCours = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  resetFilters(): void {
    this.filtresForm.reset({
      texte: '', dateDebut: '', dateFin: '', montantMin: null, montantMax: null,
      categorieCpv: '', procedure: '', tri: 'date'
    });
    this.rechercher();
  }
}
