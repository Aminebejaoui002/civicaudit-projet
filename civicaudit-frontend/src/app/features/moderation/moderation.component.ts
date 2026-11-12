import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ModerationService } from '../../core/services/moderation.service';
import { CommentaireModeration, SignalementModeration } from '../../core/models/moderation.model';

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
  ongletActif: 'commentaires' | 'signalements' = 'commentaires';

  constructor(
    private moderationService: ModerationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.chargerDonnees();
  }

  chargerDonnees() {
    this.chargement = true;
    
    this.moderationService.getCommentairesEnAttente().subscribe({
      next: (data) => {
        this.commentaires = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement commentaires:', err);
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });

    this.moderationService.getSignalements().subscribe({
      next: (data) => {
        this.signalements = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement signalements:', err);
      }
    });
  }

  changerOnglet(onglet: 'commentaires' | 'signalements') {
    this.ongletActif = onglet;
  }

  approuverCommentaire(id: number) {
    this.moderationService.approuverCommentaire(id).subscribe({
      next: () => {
        this.commentaires = this.commentaires.filter(c => c.id !== id);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Erreur approbation:', err)
    });
  }

  rejeterCommentaire(id: number) {
    this.moderationService.rejeterCommentaire(id).subscribe({
      next: () => {
        this.commentaires = this.commentaires.filter(c => c.id !== id);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Erreur rejet:', err)
    });
  }

  changerStatutSignalement(id: number, statut: string) {
    this.moderationService.changerStatutSignalement(id, statut).subscribe({
      next: () => {
        const sig = this.signalements.find(s => s.id === id);
        if (sig) {
          sig.statut = statut as any;
          this.cdr.detectChanges();
        }
      },
      error: (err) => console.error('Erreur changement statut:', err)
    });
  }

  getStatutBadgeClass(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'badge-warning';
      case 'EN_DISCUSSION': return 'badge-info';
      case 'RESOLU': return 'badge-success';
      case 'REJETE': return 'badge-danger';
      default: return '';
    }
  }

  getStatutLabel(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'En attente';
      case 'EN_DISCUSSION': return 'En discussion';
      case 'RESOLU': return 'Résolu';
      case 'REJETE': return 'Rejeté';
      default: return statut;
    }
  }
}
