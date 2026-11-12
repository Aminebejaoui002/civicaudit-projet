import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { CommentaireService } from '../../core/services/commentaire.service';
import { SignalementService } from '../../core/services/signalement.service';
import { AuthService } from '../../core/services/auth.service';
import { Commentaire } from '../../core/models/commentaire.model';
import { Signalement, CATEGORIES_LABELS, STATUTS_LABELS, SignalementCategorie } from '../../core/models/signalement.model';

/** Commentaires, signalements et votes sur un marché (CDC 3.3), formulaires en Reactive Forms. */
@Component({
  selector: 'app-marche-interactions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './marche-interactions.component.html',
  styleUrl: './marche-interactions.component.scss'
})
export class MarcheInteractionsComponent implements OnInit {
  @Input() marcheId!: number;

  commentaires: Commentaire[] = [];
  signalements: Signalement[] = [];

  commentaireForm: FormGroup;
  signalementForm: FormGroup;

  afficherFormulaireCommentaire = false;
  afficherFormulaireSignalement = false;

  messageCommentaire = '';
  erreurCommentaire = '';
  messageSignalement = '';
  erreurSignalement = '';

  readonly categoriesLabels = CATEGORIES_LABELS;
  readonly statutsLabels = STATUTS_LABELS;
  readonly categories = Object.keys(CATEGORIES_LABELS) as SignalementCategorie[];

  constructor(
    private fb: FormBuilder,
    private commentaireService: CommentaireService,
    private signalementService: SignalementService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.commentaireForm = this.fb.group({
      contenu: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]]
    });
    // Catégorie obligatoire (liste fermée) + description obligatoire quelle que soit la catégorie (CDC 3.3.2)
    this.signalementForm = this.fb.group({
      categorie: ['' as SignalementCategorie | '', Validators.required],
      description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(1000)]]
    });
  }

  ngOnInit(): void {
    this.chargerCommentaires();
    this.chargerSignalements();
  }

  get contenuCommentaire(): string {
    return (this.commentaireForm.value.contenu ?? '') as string;
  }

  get descriptionSignalement(): string {
    return (this.signalementForm.value.description ?? '') as string;
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

  ouvrirCommentaire(): void {
    this.afficherFormulaireCommentaire = true;
    this.messageCommentaire = '';
    this.erreurCommentaire = '';
  }

  annulerCommentaire(): void {
    this.afficherFormulaireCommentaire = false;
    this.commentaireForm.reset({ contenu: '' });
  }

  ajouterCommentaire(): void {
    if (this.commentaireForm.invalid) {
      this.commentaireForm.markAllAsTouched();
      return;
    }
    this.erreurCommentaire = '';
    const contenu = this.contenuCommentaire.trim();

    this.commentaireService.creerCommentaire(this.marcheId, { contenu }).subscribe({
      next: () => {
        this.annulerCommentaire();
        this.messageCommentaire = 'Merci ! Votre commentaire sera visible publiquement après modération.';
        this.chargerCommentaires();
      },
      error: (err: HttpErrorResponse) => {
        this.erreurCommentaire = this.messageErreur(err, 'Erreur lors de l\'ajout du commentaire');
        this.cdr.detectChanges();
      }
    });
  }

  supprimerCommentaire(commentaireId: number): void {
    if (!confirm('Voulez-vous vraiment supprimer ce commentaire ?')) return;

    this.commentaireService.supprimerCommentaire(this.marcheId, commentaireId).subscribe({
      next: () => this.chargerCommentaires(),
      error: (err: HttpErrorResponse) => {
        this.erreurCommentaire = this.messageErreur(err, 'Erreur lors de la suppression');
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirSignalement(): void {
    this.afficherFormulaireSignalement = true;
    this.messageSignalement = '';
    this.erreurSignalement = '';
  }

  annulerSignalement(): void {
    this.afficherFormulaireSignalement = false;
    this.signalementForm.reset({ categorie: '', description: '' });
  }

  ajouterSignalement(): void {
    if (this.signalementForm.invalid) {
      this.signalementForm.markAllAsTouched();
      return;
    }
    this.erreurSignalement = '';

    this.signalementService.creerSignalement(this.marcheId, {
      categorie: this.signalementForm.value.categorie as SignalementCategorie,
      description: this.descriptionSignalement.trim()
    }).subscribe({
      next: () => {
        this.annulerSignalement();
        this.messageSignalement = 'Votre signalement a été enregistré.';
        this.chargerSignalements();
      },
      error: (err: HttpErrorResponse) => {
        this.erreurSignalement = this.messageErreur(err, 'Erreur lors de l\'ajout du signalement');
        this.cdr.detectChanges();
      }
    });
  }

  voterSignalement(signalementId: number): void {
    this.erreurSignalement = '';
    this.signalementService.voterSignalement(this.marcheId, signalementId).subscribe({
      next: () => this.chargerSignalements(),
      error: (err: HttpErrorResponse) => {
        this.erreurSignalement = this.messageErreur(err, 'Erreur lors du vote');
        this.cdr.detectChanges();
      }
    });
  }

  /** Message renvoyé par le backend (GlobalExceptionHandler), y compris erreurs de validation et limite anti-spam. */
  private messageErreur(err: HttpErrorResponse, defaut: string): string {
    if (err.status === 401) return 'Votre session a expiré, reconnectez-vous.';
    const corps = err.error;
    if (corps?.errors) return Object.values(corps.errors).join(' ');
    if (corps?.message) return corps.message;
    return defaut;
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
