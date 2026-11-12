import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommentaireService } from '../../core/services/commentaire.service';
import { SignalementService } from '../../core/services/signalement.service';
import { AuthService } from '../../core/services/auth.service';
import { Commentaire } from '../../core/models/commentaire.model';
import { Signalement, CATEGORIES_LABELS, STATUTS_LABELS, SignalementCategorie } from '../../core/models/signalement.model';

@Component({
  selector: 'app-marche-interactions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './marche-interactions.component.html',
  styleUrl: './marche-interactions.component.scss'
})
export class MarcheInteractionsComponent implements OnInit {
  @Input() marcheId!: number;

  commentaires: Commentaire[] = [];
  signalements: Signalement[] = [];
  
  nouveauCommentaire = '';
  nouveauSignalement = {
    categorie: 'AUTRE' as SignalementCategorie,
    description: ''
  };

  afficherFormulaireCommentaire = false;
  afficherFormulaireSignalement = false;

  readonly categoriesLabels = CATEGORIES_LABELS;
  readonly statutsLabels = STATUTS_LABELS;
  readonly categories = Object.keys(CATEGORIES_LABELS) as SignalementCategorie[];

  constructor(
    private commentaireService: CommentaireService,
    private signalementService: SignalementService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerCommentaires();
    this.chargerSignalements();
  }

  chargerCommentaires(): void {
    this.commentaireService.getCommentaires(this.marcheId).subscribe({
      next: (commentaires) => {
        this.commentaires = commentaires;
        this.cdr.detectChanges();
      },
      error: () => console.error('Erreur lors du chargement des commentaires')
    });
  }

  chargerSignalements(): void {
    this.signalementService.getSignalements(this.marcheId).subscribe({
      next: (signalements) => {
        this.signalements = signalements;
        this.cdr.detectChanges();
      },
      error: () => console.error('Erreur lors du chargement des signalements')
    });
  }

  ajouterCommentaire(): void {
    const contenu = this.nouveauCommentaire.trim();
    
    if (!contenu) {
      alert('Le commentaire ne peut pas être vide');
      return;
    }
    
    if (contenu.length < 10) {
      alert('Le commentaire doit contenir au moins 10 caractères');
      return;
    }
    
    if (contenu.length > 1000) {
      alert('Le commentaire ne peut pas dépasser 1000 caractères');
      return;
    }

    this.commentaireService.creerCommentaire(this.marcheId, { contenu }).subscribe({
      next: () => {
        this.nouveauCommentaire = '';
        this.afficherFormulaireCommentaire = false;
        this.chargerCommentaires();
      },
      error: (err) => {
        console.error('Erreur commentaire:', err);
        if (err.error && err.error.errors) {
          const messages = Object.values(err.error.errors).join('\n');
          alert(`Erreur de validation:\n${messages}`);
        } else {
          alert('Erreur lors de l\'ajout du commentaire');
        }
      }
    });
  }

  supprimerCommentaire(commentaireId: number): void {
    if (!confirm('Voulez-vous vraiment supprimer ce commentaire ?')) return;

    this.commentaireService.supprimerCommentaire(this.marcheId, commentaireId).subscribe({
      next: () => this.chargerCommentaires(),
      error: () => alert('Erreur lors de la suppression')
    });
  }

  ajouterSignalement(): void {
    const description = this.nouveauSignalement.description.trim();
    
    if (!description) {
      alert('La description ne peut pas être vide');
      return;
    }
    
    if (description.length < 20) {
      alert('La description doit contenir au moins 20 caractères');
      return;
    }
    
    if (description.length > 1000) {
      alert('La description ne peut pas dépasser 1000 caractères');
      return;
    }

    this.signalementService.creerSignalement(this.marcheId, {
      categorie: this.nouveauSignalement.categorie,
      description
    }).subscribe({
      next: () => {
        this.nouveauSignalement = { categorie: 'AUTRE' as SignalementCategorie, description: '' };
        this.afficherFormulaireSignalement = false;
        this.chargerSignalements();
      },
      error: (err) => {
        console.error('Erreur signalement:', err);
        if (err.status === 400) {
          if (err.error && err.error.errors) {
            const messages = Object.values(err.error.errors).join('\n');
            alert(`Erreur de validation:\n${messages}`);
          } else {
            alert('Vous avez déjà signalé ce marché dans cette catégorie');
          }
        } else {
          alert('Erreur lors de l\'ajout du signalement');
        }
      }
    });
  }

  voterSignalement(signalementId: number): void {
    this.signalementService.voterSignalement(this.marcheId, signalementId).subscribe({
      next: () => this.chargerSignalements(),
      error: (err) => {
        if (err.status === 400) {
          alert('Vous avez déjà voté pour ce signalement');
        } else {
          alert('Erreur lors du vote');
        }
      }
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatutClass(statut: string): string {
    const classes: Record<string, string> = {
      'EN_ATTENTE': 'statut-attente',
      'APPROUVE': 'statut-approuve',
      'REJETE': 'statut-rejete',
      'NOUVEAU': 'statut-nouveau',
      'EN_DISCUSSION': 'statut-discussion',
      'VERIFIE': 'statut-verifie',
      'CLOS': 'statut-clos'
    };
    return classes[statut] || '';
  }
}
