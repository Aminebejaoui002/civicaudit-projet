export interface MarcheListe {
  id: number;
  objet: string;
  montant: number;
  titulairePrincipal: string;
  dateNotification: string;
  procedure: string;
}

export interface Titulaire {
  siret: string;
  nom: string;
}

export interface MarcheDetail {
  id: number;
  objet: string;
  montant: number;
  acheteurNom: string;
  titulaires: Titulaire[];
  dateNotification: string;
  dureeMois: number | null;
  procedure: string;
  cpvCode: string;
  sourceFilename: string;
  aDesAvenants: boolean;
  alertes: Alerte[];
}

export interface Alerte {
  id: number;
  type: 'CONCENTRATION_ELEVEE' | 'MONTANT_ATYPIQUE' | 'SEUIL_SUSPECT';
  valeurCalculee: number;
  dateGeneration: string;
}

export interface Avenant {
  id: number;
  montantApres: number;
  dureeApresMois: number | null;
  dateAvenant: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}