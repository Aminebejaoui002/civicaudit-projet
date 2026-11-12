export interface Signalement {
  id: number;
  categorie: SignalementCategorie;
  description: string;
  statut: SignalementStatut;
  auteurNom: string; // pseudonyme public, ou « Utilisateur supprimé »
  auteurId: number;
  dateCreation: string;
  nombreVotes: number;
  aVote?: boolean;
}

export type SignalementCategorie = 
  | 'MONTANT_ANORMAL'
  | 'CONCENTRATION_FOURNISSEUR'
  | 'PROCEDURE_INHABITUELLE'
  | 'DELAI_SUSPECT'
  | 'AUTRE';

export type SignalementStatut = 
  | 'NOUVEAU'
  | 'EN_DISCUSSION'
  | 'VERIFIE'
  | 'CLOS';

export interface SignalementRequest {
  categorie: SignalementCategorie;
  description: string;
}

export const CATEGORIES_LABELS: Record<SignalementCategorie, string> = {
  MONTANT_ANORMAL: 'Montant anormal',
  CONCENTRATION_FOURNISSEUR: 'Concentration fournisseur',
  PROCEDURE_INHABITUELLE: 'Procédure inhabituelle',
  DELAI_SUSPECT: 'Délai suspect',
  AUTRE: 'Autre'
};

export const STATUTS_LABELS: Record<SignalementStatut, string> = {
  NOUVEAU: 'Nouveau',
  EN_DISCUSSION: 'En discussion',
  VERIFIE: 'Vérifié',
  CLOS: 'Clos'
};
