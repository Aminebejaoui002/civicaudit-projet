import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ModerationService } from '../../core/services/moderation.service';
import { CommentaireModeration, SignalementModeration } from '../../core/models/moderation.model';
import { CATEGORIES_LABELS, STATUTS_LABELS, SignalementStatut } from '../../core/models/signalement.model';

/** Interface de modération (CDC 3.4) — accessible aux rôles MODERATEUR et ADMIN uniquement. */
@Component({
  selector: 'app-moderation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './moderation.component.html',
  styleUrls: ['./moderation.component.scss']
})
export class ModerationComponent implements OnInit {
  commentaires: CommentaireModeration[] = [];
  signalements: SignalementModeration[] = [];
  chargement = true;
  erreur = '';
  ongletActif: 'commentaires' | 'signalements' = 'commentaires';

  readonly categoriesLabels = CATEGORIES_LABELS;
  readonly statutsLabels = STATUTS_LABELS;
  readonly statuts = Object.keys(STATUTS_LABELS) as SignalementStatut[];

  constructor(
    private moderationService: ModerationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.chargerDonnees();
  }

  chargerDonnees() {
    this.chargement = true;
    this.erreur = '';

    this.moderationService.getCommentairesEnAttente().subscribe({
      next: (data) => {
        this.commentaires = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => this.afficherErreur(err)
    });

    this.moderationService.getSignalements().subscribe({
      next: (data) => {
        this.signalements = data;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => this.afficherErreur(err)
    });
  }

  changerOnglet(onglet: 'commentaires' | 'signalements') {
    this.ongletActif = onglet;
  }

  approuverCommentaire(id: number) {
    this.moderationService.approuverCommentaire(id).subscribe({
      next: () => this.retirerCommentaire(id),
      error: (err: HttpErrorResponse) => this.afficherErreur(err)
    });
  }

  /** Un commentaire rejeté disparaît immédiatement de la vue publique mais reste en base (traçabilité). */
  rejeterCommentaire(id: number) {
    this.moderationService.rejeterCommentaire(id).subscribe({
      next: () => this.retirerCommentaire(id),
      error: (err: HttpErrorResponse) => this.afficherErreur(err)
    });
  }

  changerStatutSignalement(id: number, statut: string) {
    this.moderationService.changerStatutSignalement(id, statut as SignalementStatut).subscribe({
      next: () => {
        const sig = this.signalements.find(s => s.id === id);
        if (sig) {
          sig.statut = statut as SignalementStatut;
          this.cdr.detectChanges();
        }
      },
      error: (err: HttpErrorResponse) => this.afficherErreur(err)
    });
  }

  private retirerCommentaire(id: number) {
    this.commentaires = this.commentaires.filter(c => c.id !== id);
    this.cdr.detectChanges();
  }

  private afficherErreur(err: HttpErrorResponse) {
    this.chargement = false;
    this.erreur = err.status === 403
      ? 'Accès réservé aux modérateurs et administrateurs.'
      : (err.error?.message || 'Une erreur est survenue, réessayez.');
    this.cdr.detectChanges();
  }

  getStatutBadgeClass(statut: string): string {
    switch (statut) {
      case 'NOUVEAU': return 'badge-warning';
      case 'EN_DISCUSSION': return 'badge-info';
      case 'VERIFIE': return 'badge-success';
      case 'CLOS': return 'badge-neutral';
      default: return '';
    }
  }
}
