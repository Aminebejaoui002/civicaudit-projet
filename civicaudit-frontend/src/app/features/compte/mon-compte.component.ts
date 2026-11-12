import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/** Espace « Mon compte » : droit à l'effacement RGPD (CDC 3.6). */
@Component({
  selector: 'app-mon-compte',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <h1>Mon compte</h1>
      <p class="info" *ngIf="authService.currentUser$ | async as user">
        Connecté en tant que <strong>{{ user.email }}</strong>
        <span class="role">({{ user.role }})</span>
      </p>

      <section class="carte danger" aria-labelledby="titre-suppression">
        <h2 id="titre-suppression">Supprimer mon compte</h2>
        <p>
          Conformément au RGPD (droit à l'effacement), vous pouvez supprimer votre compte à tout moment.
          Votre adresse email est alors effacée et vous ne pourrez plus vous connecter.
        </p>
        <p>
          Vos commentaires et signalements restent visibles pour préserver la cohérence des discussions,
          mais ils sont <strong>anonymisés</strong> : l'auteur affiché devient « Utilisateur supprimé ».
          Voir la <a routerLink="/politique-confidentialite">politique de confidentialité</a>.
        </p>

        <form [formGroup]="form" (ngSubmit)="supprimer()">
          <label for="confirmation">Pour confirmer, tapez <strong>SUPPRIMER</strong></label>
          <input id="confirmation" type="text" formControlName="confirmation" autocomplete="off" />
          <p *ngIf="erreur" class="erreur" role="alert">{{ erreur }}</p>
          <button type="submit" class="btn-danger" [disabled]="form.invalid || enCours">
            {{ enCours ? 'Suppression...' : 'Supprimer définitivement mon compte' }}
          </button>
        </form>
      </section>
    </div>
  `,
  styles: [`
    .page { max-width: 720px; margin: 0 auto; padding: 2.5rem 1.5rem 4rem; }
    h1 { font-size: 2rem; margin-bottom: 0.5rem; }
    .info { color: #475569; margin-bottom: 2rem; }
    .role { font-size: 0.875rem; color: #64748b; }
    .carte { background: #fff; border-radius: 12px; padding: 1.5rem; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
    .carte.danger { border-left: 4px solid #b91c1c; }
    h2 { font-size: 1.25rem; margin: 0 0 1rem; }
    p { line-height: 1.6; color: #334155; }
    form { display: flex; flex-direction: column; gap: 0.75rem; margin-top: 1.5rem; }
    input { padding: 0.625rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 1rem; max-width: 280px; }
    input:focus-visible { outline: 3px solid #93c5fd; outline-offset: 1px; }
    .btn-danger { align-self: flex-start; background: #b91c1c; color: #fff; border: none; border-radius: 8px;
      padding: 0.75rem 1.25rem; font-weight: 600; cursor: pointer; }
    .btn-danger:disabled { background: #fca5a5; cursor: not-allowed; }
    .erreur { color: #b91c1c; margin: 0; }
  `]
})
export class MonCompteComponent {
  form: FormGroup;
  enCours = false;
  erreur = '';

  constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      confirmation: ['', [Validators.required, Validators.pattern(/^SUPPRIMER$/)]]
    });
  }

  supprimer(): void {
    if (this.form.invalid) return;
    this.enCours = true;
    this.erreur = '';
    this.authService.supprimerCompte().subscribe({
      next: () => this.router.navigate(['/marches']),
      error: (err) => {
        this.enCours = false;
        this.erreur = err.error?.message || 'La suppression a échoué, réessayez.';
        this.cdr.detectChanges();
      }
    });
  }
}
