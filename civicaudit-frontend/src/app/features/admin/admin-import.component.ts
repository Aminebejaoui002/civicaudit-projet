import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminService, ErreurImport, ResultatImport } from '../../core/services/admin.service';

/** Import manuel du fichier DECP (CDC 3.1) avec rapport d'import — rôle ADMIN. */
@Component({
  selector: 'app-admin-import',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <h1>Import des données DECP</h1>
      <p class="intro">
        Téléchargez le fichier consolidé (CSV) sur
        <a href="https://www.data.gouv.fr/datasets/donnees-essentielles-de-la-commande-publique-consolidees-format-tabulaire"
           target="_blank" rel="noopener noreferrer">data.gouv.fr</a>, puis importez-le ici.
        L'import est idempotent : relancer le même fichier met à jour les marchés sans créer de doublon.
        Les alertes automatiques sont recalculées à chaque import.
      </p>

      <section class="carte">
        <label for="fichier">Fichier CSV</label>
        <input id="fichier" type="file" accept=".csv,text/csv" (change)="choisir($event)" />
        <button type="button" class="btn-primary" (click)="importer()" [disabled]="!fichier || enCours">
          {{ enCours ? 'Import en cours...' : 'Lancer l\\'import' }}
        </button>
        <p *ngIf="erreur" class="erreur" role="alert">{{ erreur }}</p>
      </section>

      <section *ngIf="resultat" class="carte" aria-live="polite">
        <h2>Rapport d'import — {{ resultat.rapport.fichier }}</h2>
        <dl class="rapport">
          <dt>Lignes lues</dt><dd>{{ resultat.rapport.lignesLues }}</dd>
          <dt>Lignes hors périmètre</dt><dd>{{ resultat.rapport.lignesHorsPerimetre }}</dd>
          <dt>Marchés trouvés</dt><dd>{{ resultat.rapport.marchesTrouves }}</dd>
          <dt>Marchés créés</dt><dd>{{ resultat.rapport.marchesImportes }}</dd>
          <dt>Marchés mis à jour</dt><dd>{{ resultat.rapport.marchesMisAJour }}</dd>
          <dt>Marchés rejetés</dt><dd [class.alerte]="resultat.rapport.marchesRejetes > 0">{{ resultat.rapport.marchesRejetes }}</dd>
          <dt>Alertes recalculées</dt><dd>{{ resultat.alertesGenerees }}</dd>
        </dl>
      </section>

      <section class="carte">
        <h2>Dernières lignes rejetées</h2>
        <p *ngIf="erreurs.length === 0" class="vide">Aucune ligne rejetée.</p>
        <div class="table-wrapper" *ngIf="erreurs.length > 0">
          <table>
            <thead><tr><th scope="col">Date</th><th scope="col">Marché (uid)</th><th scope="col">Raison</th><th scope="col">Ligne brute</th></tr></thead>
            <tbody>
              <tr *ngFor="let e of erreurs">
                <td>{{ e.dateImport | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ e.uidMarche || '—' }}</td>
                <td>{{ e.raison }}</td>
                <td class="brute" [title]="e.ligneBrute">{{ e.ligneBrute }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .page { max-width: 1000px; margin: 0 auto; padding: 2.5rem 1.5rem 4rem; }
    h1 { font-size: 2rem; margin-bottom: 0.5rem; }
    .intro { color: #475569; line-height: 1.6; margin-bottom: 1.5rem; }
    .carte { background: #fff; border-radius: 12px; padding: 1.5rem; box-shadow: 0 2px 12px rgba(0,0,0,0.06);
      margin-bottom: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem; }
    h2 { font-size: 1.125rem; margin: 0; }
    label { font-weight: 600; }
    .btn-primary { align-self: flex-start; background: var(--primary, #1d4ed8); color: #fff; border: none;
      border-radius: 8px; padding: 0.75rem 1.25rem; font-weight: 600; cursor: pointer; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .erreur { color: #b91c1c; margin: 0; }
    .rapport { display: grid; grid-template-columns: max-content 1fr; gap: 0.5rem 1.5rem; margin: 0; }
    dt { color: #475569; }
    dd { margin: 0; font-weight: 700; }
    dd.alerte { color: #b45309; }
    .vide { color: #64748b; margin: 0; }
    .table-wrapper { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    th, td { text-align: left; padding: 0.5rem; border-bottom: 1px solid #e2e8f0; vertical-align: top; }
    .brute { max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: monospace; }
  `]
})
export class AdminImportComponent implements OnInit {
  fichier: File | null = null;
  enCours = false;
  erreur = '';
  resultat: ResultatImport | null = null;
  erreurs: ErreurImport[] = [];

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.chargerErreurs();
  }

  choisir(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fichier = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  importer(): void {
    if (!this.fichier) return;
    this.enCours = true;
    this.erreur = '';
    this.adminService.importer(this.fichier).subscribe({
      next: (resultat) => {
        this.resultat = resultat;
        this.enCours = false;
        this.chargerErreurs();
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.enCours = false;
        this.erreur = err.error?.message || 'L\'import a échoué.';
        this.cdr.detectChanges();
      }
    });
  }

  private chargerErreurs(): void {
    this.adminService.getErreurs().subscribe({
      next: (erreurs) => { this.erreurs = erreurs; this.cdr.detectChanges(); },
      error: () => { /* liste facultative */ }
    });
  }
}
