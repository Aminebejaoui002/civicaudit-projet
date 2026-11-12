export interface Commentaire {
  id: number;
  contenu: string;
  statut: 'EN_ATTENTE' | 'APPROUVE' | 'REJETE';
  auteurNom: string; // pseudonyme public, ou « Utilisateur supprimé »
  auteurId: number;
  dateCreation: string;
  estAuteur?: boolean;
}

export interface CommentaireRequest {
  contenu: string;
}
