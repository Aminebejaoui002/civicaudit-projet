export interface CommentaireModeration {
  id: number;
  contenu: string;
  auteurNom: string;
  dateCreation: string;
  marcheId: number;
  marcheObjet: string;
}

export interface SignalementModeration {
  id: number;
  categorie: string;
  description: string;
  auteurNom: string;
  dateCreation: string;
  marcheId: number;
  marcheObjet: string;
  statut: 'EN_ATTENTE' | 'EN_DISCUSSION' | 'RESOLU' | 'REJETE';
  nbVotes: number;
}
