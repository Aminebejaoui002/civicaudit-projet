import { SignalementCategorie, SignalementStatut } from './signalement.model';

export interface CommentaireModeration {
  id: number;
  contenu: string;
  auteurNom: string;
  auteurEmail: string;
  dateCreation: string;
  marcheId: number;
  marcheObjet: string;
}

export interface SignalementModeration {
  id: number;
  categorie: SignalementCategorie;
  description: string;
  auteurNom: string;
  auteurEmail: string;
  dateCreation: string;
  marcheId: number;
  marcheObjet: string;
  statut: SignalementStatut;
  nombreVotes: number;
}
