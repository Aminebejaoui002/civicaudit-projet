export interface Commentaire {
  id: number;
  contenu: string;
  statut: 'EN_ATTENTE' | 'APPROUVE' | 'REJETE';
  auteurEmail: string;
  auteurId: number;
  dateCreation: string;
  estAuteur?: boolean;
}

export interface CommentaireRequest {
  contenu: string;
}
